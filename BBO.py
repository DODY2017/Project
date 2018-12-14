import numpy as np
import numpy.matlib
from sklearn import metrics
def fitness(candidate, nn, X_train, y_train ):
    y_pred= []
    nn.set_weights(candidate)
    for p in X_train:
        ANNout=nn.update(p)
        y_pred.append(numpy.argmax(ANNout))
    s=metrics.accuracy_score(y_train, y_pred)
    #print len(y_pred)
    return (1.0-s)
def HillClimbing(Obj_func, x, ANN, X_train, y_train,dim ):
	newpop=x
	for k in range(len(x)):
		oldfitness=Obj_func(x[k,:], X_train, y_train, ANN )
		for j in range(100):
			for i in range(dim):
				newpop[k][i]=x[k][i]+ 0.01*(1-np.random.random())
			newfitness=Obj_func(newpop[k], X_train, y_train, ANN )
			
			if newfitness>oldfitness:
				x[k]=newpop[k]
	return x

def newbbo(Obj_func, lb, ub, PopulationSize, GenerationLimit, X_train, y_train, ANN, ProblemDimension ):
	# generation count limit
     # population size
     # number of variables in each solution (i.e., problem dimension)
    MutationProbability = 0.05 #; % mutation probability per solution per independent variable
    NumberOfElites = 2 #; % how many of the best solutions to keep from one generation to the next
 #   MinDomain = -4 #-2.048 #; % lower bound of each element of the function domain
 #   MaxDomain = 4 #+2.048 #; % upper bound of each element of the function domain
    #% Initialize the population
    x = numpy.random.uniform (0,1, (PopulationSize, ProblemDimension))* (ub-lb)+lb; #% allocate memory for the population
    #x=HillClimbing(Obj_func, x, ANN, X_train, y_train, ProblemDimension)
    #print np.shape(x)
    Cost=np.zeros(PopulationSize)
    for i in range(0,PopulationSize):
    	#Cost[i]= fitness(x[i,:], ANN, X_train, y_train )func_obj(newpos,trainInput,trainOutput,net)
		Cost[i]= Obj_func(x[i,:], X_train, y_train, ANN )
    #varible to hold returned values
    from solution import solution
    import time
    bestsolution=[]
    convergence=[]
    s=solution()
    s.optimizer="BBO"
    
    timerStart=time.time() 
    s.startTime=time.strftime("%Y-%m-%d-%H-%M-%S")
    
    #sort the population
    Lightn=numpy.sort(Cost)
    Index=numpy.argsort(Cost)
    x=x[Index,:]
    #print 'x=='+ str(np.shape(x))
    Cost=Lightn
    
    MinimumCost = numpy.zeros(GenerationLimit) #; % allocate memory
    
    MinimumCost[0] = Cost[0] #; % save the best cost at each generation in the MinimumCost array
    bestsolution=x[0,:]
    
    convergence.append(MinimumCost[0])
    
    print(['At iteration '+ str(0)+ ' the best fitness is '+ str(MinimumCost[0])])
    z=numpy.zeros([PopulationSize, ProblemDimension])
    
    # % Compute migration rates, assuming the population is sorted from most fit to least fit
    
    mu = np.linspace(1, 0, PopulationSize)# Emmigration Rates
    lam = 1 - mu #; % immigration rate
    
    EliteSolutions=numpy.zeros([NumberOfElites, ProblemDimension])
    EliteCosts =[]

    print("BBO is optimizing  \""+Obj_func.__name__+"\"")    
    
    for Generation in range(1, GenerationLimit):
        #% Save the best solutions and costs in the elite arrays
        EliteSolutions = x[0 : NumberOfElites, :]
        EliteCosts = Cost[0 : NumberOfElites]
        #% Use migration rates to decide how much information to share between solutions
        for k in range ( 0 , PopulationSize):
            #% Probabilistic migration to the k-th solution
            for j in range (0 , ProblemDimension):
                rand=numpy.random.rand()
                if rand < lam[k]:
                    #% Should we immigrate? #% Yes - Pick a solution from which to emigrate 
                    RandomNum = rand * np.sum(mu)
                    Select = mu[0]
                    SelectIndex = 0
                    while ((RandomNum > Select) and (SelectIndex < PopulationSize)):
                        SelectIndex = SelectIndex + 1
                        Select = Select + mu[SelectIndex]
                    z[k, j] = x[SelectIndex, j]                 # % this is the migration step
                else:
                    z[k, j] = x[k, j]              # % no migration for this independent variable
    
        for k in range( 0, PopulationSize):
            for ParameterIndex in range(0 , ProblemDimension):
                if (rand < MutationProbability):
                    z[k, ParameterIndex] = lb + (ub - lb) * rand
        x = z        # replace the solutions with their new migrated and mutated versions
        for i in range(0,PopulationSize):
            #Cost[i]=F2(x[i,:])
            #Cost[i]= fitness(x[i,:], ANN, X_train, y_train )
            Cost[i]= Obj_func(x[i,:], X_train, y_train, ANN )
        #sort the population
        Lightn=numpy.sort(Cost)
        Index=numpy.argsort(Cost)
        x=x[Index,:]
        Cost=Lightn
        for k in range (0 , NumberOfElites): # % replace the worst individuals with the previous generation's elites
            x[PopulationSize-k-1, :] = EliteSolutions[k, :]
            Cost[PopulationSize-k-1] = EliteCosts[k]

        #sort the population
        Lightn=numpy.sort(Cost)
        Index=numpy.argsort(Cost)
        x=x[Index,:]
        Cost=Lightn
        MinimumCost[Generation] = Cost[0]
        bestsolution=x[0, :]
        convergence.append(MinimumCost[Generation])
        print([s.optimizer + ' At iteration '+ str(Generation)+ ' best fitness  '+  str(MinimumCost[Generation])])
##        if MinimumCost[Generation]<0.1:
##        	print 'break at generation' + str(Generation)
##        	break
    
    #print ('best solution='+ str(bestsolution))
    timerEnd=time.time()
    s.endTime=time.strftime("%Y-%m-%d-%H-%M-%S")
    s.executionTime=timerEnd-timerStart
    s.convergence=convergence
    s.optimizer="BBO"
    #s.objfname='BBO'
    s.objfname=Obj_func.__name__

    s.bestIndividual = bestsolution
    return s
