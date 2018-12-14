"""
GLOWWORM-MFO SWARM OPTIMIZATION (GSO-MFO)
GSO Basic documentation:
	agents_number:	(int) number of individuals
	dim:			(int) dimensions of the problem
	func_obj:		(function) function that returns a fitness for a given individual
	epochs:			(int) maximos number of epochs
	step_size:		(double) size of the step of each individual (fixed when 'random_step' == False, and set as the max value when 'random_step' == False)
	dims_lim:		(list of int, with 2 positions) a list of two values, determining boundaries of the dimensions (considering the same for all dimensions in this version, should be updated for each one in a near future)
	random_step:	True or False - Determine if the step is fixed or exists a random between a minium value and the step size

	triggers:
		- individual_updated	= runs for each time an individual is updated
		- program_ends 			= runs when the program ends

"""
import numpy as np
from solution import solution
import time
import random
import math
def gso_mfo(func_obj, lb, ub, dim, agents_number, epochs):
#def gso(agents_number, dim, func_obj, epochs, step_size, r0, rs, b, k_neigh, dims_lim = [-4,4], random_step=False, virtual_individual = False, individual_updated=None, program_ends = None):

#initilization for GSO
	step_size=0.03
	r0=1
	rs=8
	b=0.08
	k_neigh=5
	random_step =False
	virtual_individual = True
#	individual_updated=None
	program_ends = None
	dims_lim =[lb, ub]
	#dims_lim = [-500,500]
	
	assert len(dims_lim) == 2
	assert type(agents_number) == int
	#assert type(dim) == int

#initialization for MFO
	N=agents_number
	Max_iteration=epochs
	objf=func_obj
	
	Moth_pos=np.random.uniform(0,1,(N,dim)) *(ub-lb)+lb
	Moth_fitness=np.full(N,float("inf"))
	Flame_pos=np.random.uniform(0,1,(N,dim)) *(ub-lb)+lb
	Flame_fitness=np.full(N,float("inf"))
#	Convergence_curve=[]
	sorted_population=np.copy(Flame_pos)
	fitness_sorted=np.zeros(N)
	#####################
	best_flames=np.copy(Flame_pos)
	best_flame_fitness=np.zeros(N)
	####################
	double_population=np.zeros((2*N,dim))
	double_fitness=np.zeros(2*N)
	
	double_sorted_population=np.zeros((2*N,dim))
	double_fitness_sorted=np.zeros(2*N)
	#########################
	previous_population=np.copy(Moth_pos)
	previous_fitness=Moth_fitness
	
	#start of GSO
	s=solution()
	s.optimizer="GSO-MFO hybrid"
	convergence=[]
	
	timerStart=time.time()
	s.startTime=time.strftime("%Y-%m-%d-%H-%M-%S")
	"""
	PARAMETTERS
	"""
	#RANGE of sight
	range_init =r0
	# range_min = 0.1
	range_boundary = rs

	#LUCIFERIN
	luciferin_init = 0.05
	luciferin_decay = 0.4
	luciferin_enhancement = 0.6

	#Neighbors
	beta = b

	"""
	AGENTES INITIALIZATION
	"""

	#random initiation
	glowworms = np.random.uniform(lb,ub,[agents_number,dim]) * (ub-lb)+lb
	#print ('glowworms  ',glowworms)

	
	#distances
	distances = np.zeros([agents_number,agents_number])

	#luciferin
	luciferins = np.zeros(agents_number)
	luciferins += luciferin_init
	
	#range of sigth
	ranges = np.zeros(agents_number)
	ranges += range_init

	"""
	LUCIFERIN UPDATE PHASE
	"""
	def luciferin_update(last_luciferin,fitness):
		l = ((1-luciferin_decay)*last_luciferin) + (luciferin_enhancement*fitness)

		return l

	"""
	POSITION UPDATE PHASE
	"""
	def find_neighbors(glowworm_index):

		i = glowworm_index
		neighbors_index = []

		#look for all the neighbors in a triangle distance matrix
		for k in range(agents_number):

			dist = get_distance(i,k)
			#if it is in it's range of sigth and it's brightness is higher
			if dist != 0 and dist <= ranges[i] and luciferins[i] < luciferins[k] :
			#if dist != 0 and dist <= ranges[i] and fitness[i] < fitness[k] :
				neighbors_index.append(k)

		return neighbors_index


	def follow(glowworm_index,neighbors_index):

		#current luciferin - li
		li = luciferins[glowworm_index]

		#luciferin of all neighbors (lj or lk)
		sum_lk = sum([luciferins[k] for k in neighbors_index])

		#calc probabilties for each neighbor been followed
		probs = np.array([luciferins[j]-li for j in neighbors_index])
		probs /= sum_lk - (len(probs)*li)

		#calc prob range
		acc = 0
		wheel = []
		for prob in probs:
			acc += prob
			wheel.append(acc)

		wheel[-1] = 1

		#randomly choice a value for wheel selection method
		rand_val = np.random.random()
		following = None
		for i, value in enumerate(wheel):
			if rand_val <= value:
				following = i

		return neighbors_index[following]

	def position_update(i, j, best_flame_so_far):
		glowworm = glowworms[i]

		toward = None
		if type(j) == int:
			toward = glowworms[j]
		elif type(j) == type(np.array([])):
			toward = j

		norm = np.linalg.norm(toward-glowworm)

		if norm == 0 or np.isnan(norm):
			norm = step_size

		if random_step:
			step = step_size*np.random.random()
			if step < 0.01:
				step = 0.01
		else:
			step = step_size

		#new_position = glowworm + step_size*(toward-glowworm)/norm
		new_position = best_flame_so_far + step_size*(toward-glowworm)/norm
		#print ('norm ',norm)
		#print ('position update phase ',new_position)

		#new_position = glowworm + step_size*(best_flame_so_far-glowworm)/norm
		
		#update distmatrix for all associated cells (not all matrix)
		for k in range(agents_number):

			if max(k,i) == k:
				distances[i][k] = np.linalg.norm(new_position-glowworms[k])
			elif max(k,i) == i:
				distances[k][i] = np.linalg.norm(new_position-glowworms[k])

		return new_position

	def range_update(glowworm_index, neighbors):
		return min(range_boundary,max(0,ranges[glowworm_index] + (beta*(k_neigh-len(neighbors)))))

	def virtual_glowworm(glowworm_index):
		glowworm = glowworms[glowworm_index]
		virtual = np.random.uniform(lb,ub,[1,dim])
		virtual = glowworm + ranges[glowworm_index]*(virtual-glowworm)/np.linalg.norm(virtual-glowworm)
		return virtual
		

	def get_distance(i,j):
		if max(j,i) == j:
			return distances[i][j]
		elif max(j,i) == i:
			return distances[j][i]
		else:
			return 0.0
	
	def HillClimbing():
		newpop=glowworms
		for k in range(agents_number):
			oldfitness=1-func_obj(glowworms[k],trainInput,trainOutput,net)
			#oldfitness=func_obj(glowworms[k],trainInput,trainOutput,net)
			
			for j in range(100):
				for i in range(dim):
					newpop[k][i]=glowworms[k][i]+ step_size*(1-np.random.random())
				newfitness=1-func_obj(newpop[k],trainInput,trainOutput,net)
				#newfitness=func_obj(newpop[k],trainInput,trainOutput,net)
				
				if newfitness>oldfitness:
					glowworms[k]=newpop[k]
		return


	"""
	EXECUTION
	"""
	fitness = np.zeros(agents_number)

	best_fitness_history = []
	best_luciferins_history=[]
	#HillClimbing()
	#initialize distance matrix (over main diagonal, only)
	for i in range(agents_number):
		for j in range(agents_number):
			if max(i,j) == j:
				distances[i][j] = np.linalg.norm(glowworms[i]-glowworms[j])
			elif max(i,j) == i:
				distances[j][i] = np.linalg.norm(glowworms[i]-glowworms[j])
	
	#epoch_luciferins_history = []
	for epoch in range(epochs):
	    #update population using MFO explotation
	    Flame_no=round(N-epoch*((N-1)/Max_iteration));

	    for i in range(0,N):
		# Check if moths go out of the search spaceand bring it back
		Moth_pos[i,:]=np.clip(Moth_pos[i,:], lb, ub)
		Flame_pos[i,:]=np.clip(Flame_pos[i,:], lb, ub)

        	# evaluate moths
        	Moth_fitness[i]=objf(Moth_pos[i,:])
        	Flame_fitness[i]=objf(Flame_pos[i,:])  

            if epoch==0:
    		# Sort the first population of moths
    		fitness_sorted=np.sort(Flame_fitness)
    		I=np.argsort(Flame_fitness)
    		sorted_population=Flame_pos[I,:]
    		#Update the flames
    		best_flames=sorted_population;
    		best_flame_fitness=fitness_sorted;
    	    else:
    		# Sort the moths
    		double_population=np.concatenate((previous_population,best_flames),axis=0)
    		double_fitness=np.concatenate((previous_fitness, best_flame_fitness),axis=0);
    		double_fitness_sorted =np.sort(double_fitness);
    		I2 =np.argsort(double_fitness);
        	for newindex in range(0,2*N):
        	    double_sorted_population[newindex,:]=np.array(double_population[I2[newindex],:])
        	    fitness_sorted=double_fitness_sorted[0:N]
        	    sorted_population=double_sorted_population[0:N,:]
        		# Update the flames
        	best_flames=sorted_population;
        	best_flame_fitness=fitness_sorted;
            # Update the position best flame obtained so far
            previous_population=Moth_pos;
	    previous_fitness=Moth_fitness;
            # a linearly dicreases from -1 to -2 to calculate t in Eq. (3.12)
       	    a=-1+epoch*((-1)/Max_iteration);
    	    # Loop counter
    	    for i in range(0,N):
    	        for j in range(0,dim):
    	            #Update the position of the moth with respect to its corresponsing flame	D in Eq. (3.13)
    	            if (i<=Flame_no):
    	                distance_to_flame=abs(sorted_population[i,j]-Moth_pos[i,j])
            		b=1
            		t=(a-1)*random.random()+1; # % Eq. (3.12)
            		Moth_pos[i,j]=distance_to_flame*math.exp(b*t)*math.cos(t*2*math.pi)+sorted_population[i,j]
          	    if i>Flame_no: # Upaate the position of the moth with respct to one flame
          	        #	% Eq. (3.13)
          	        distance_to_flame=abs(sorted_population[i,j]-Moth_pos[i,j]);
          	        b=1;
          	        t=(a-1)*random.random()+1;
          	        #% Eq. (3.12)
          	        Moth_pos[i,j]=distance_to_flame*math.exp(b*t)*math.cos(t*2*math.pi)+sorted_population[Flame_no,j]
          	        
	    #GSO to start from here
	    
	    #epoch_fitness_history = []
	    epoch_luciferins_history=[]
	    #update all glowworms luciferin
	    for i in range(agents_number):
	        li = luciferins[i]
	        if epoch == 0:
	            fitness[i]=1-func_obj(glowworms[i])

	            #fitness[i]=func_obj(glowworms[i],trainInput,trainOutput,net)
	        luciferins[i] = luciferin_update(li,fitness[i])
	        #print ('luciferins in gso  ',luciferins[i])

	        #epoch_fitness_history.append(fitness[i])
	        epoch_luciferins_history.append(luciferins[i])
	            #best_fitness_history.append(max(epoch_fitness_history))
		#get the best for current generation
            best_luciferins_history.append(max(epoch_luciferins_history))
            best_index=np.argmax(epoch_luciferins_history)
            best_fitness_history.append(fitness[best_index])
            best_individual=glowworms[best_index]

            #if epoch==0:
                #	best_individual=glowworms[best_index]
                #best_fitness=best_luciferins_history[-1]
                #	best_fitness=fitness[best_index]
                #	best_epoch=epoch
	    #elif best_luciferins_history[-1]>best_fitness:
		#	best_individual=glowworms[best_index]
			#best_fitness=best_luciferins_history[-1]
		#	best_fitness=fitness[best_index]
		#	best_epoch=epoch
			
		#movement phase
	    for i in range(agents_number):
	        #find best neighbors
	        neighbors = find_neighbors(i)
	        change = False
	        if len(neighbors) > 0:
	            toward_index = follow(i,neighbors)

	            newpos=position_update(i,toward_index, best_flames[0,:])

	            #fitnew=1-func_obj(newpos,trainInput,trainOutput,net)
	            #lnew=luciferin_update(luciferin_init,fitnew)
	            #if func_obj(newpos) > func_obj(glowworms[i]):
	                #if (func_obj(newpos,trainInput,trainOutput,net)>func_obj(glowworms[i],trainInput,trainOutput,net)):
	            glowworms[i] = newpos
	            #print ('glowworm i  ',glowworms[i], i)
	            change = True
		elif virtual_individual:
		    virtual = virtual_glowworm(i)
		    glowworms[i] = position_update(i,virtual,best_flames[0,:])
		    change = True
		else:
		    print('Done everything')
		    #Updated fitness, but not luciferin
		if change:
		    fitness[i] = 1-func_obj(glowworms[i])
   
		    #fitness[i] = func_obj(glowworms[i],trainInput,trainOutput,net)
		ranges[i] = range_update(i,neighbors)

            convergence.append(1-best_fitness_history[-1])
            print(s.optimizer, epoch,'> best fitness:', 1-best_fitness_history[-1])
            #if 1-best_fitness_history[-1]<0.211:
            #if best_fitness_history[-1]>0.99:
            #	print 'break at generation' + str(epoch)
            #	break
	timerEnd=time.time()
	s.endTime=time.strftime("%Y-%m-%d-%H-%M-%S")
	s.executionTime=timerEnd-timerStart
	s.convergence=convergence
	s.optimizer="GSO-MFO no"
	s.bestIndividual = best_individual
	s.objfname=func_obj.__name__
	#s.best = best_fitness
	#print 'Best soultion at epoch = '+str(best_epoch+1)+' with fitness = '+ str(best_fitness)
	return s

	if program_ends is not None:
		program_ends()
