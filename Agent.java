
package org.cloudbus.cloudsim;
 
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
 
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicy;
 
public class Agent implements Comparable<Agent> {
    private Location currentLocation;
    private Location newLocation;
    private double sensorRange;
    private double decayConstant;
    private double enhancementConstant;
    public double luciferin;
    private ArrayList<Vm> neighbors;
        public Vm approachable;
        public double updateSensorRange;
     
     
    public Agent(){}
     
    public Agent(
            double sensorRange,
            double decayConstant,
            double enhancementConstant,
            Location location,
            double luciferin
            ){
        currentLocation = location;
        newLocation = null;
        this.sensorRange = sensorRange;
        this.decayConstant = decayConstant;
        this.enhancementConstant = enhancementConstant;
        this.luciferin = luciferin;
        this.neighbors = new ArrayList<>();
    }
 
    public Location getCurrentLocation() {
        return currentLocation;
    }
     
    public void setNewLocation(Location newLocation) {
        this.newLocation = newLocation;
    }
 
    public double updateLuciferin(double rewardFunctionValue){
        luciferin = (1-decayConstant) * luciferin + enhancementConstant * rewardFunctionValue;
        return luciferin;
        }
 
    public double getLuciferin() {
        return luciferin;
    }
 
    public double getSensorRange() {
        return sensorRange;
    }
     
     
     
    public void planMove(List<Vm> hostlist, int myIndex, double stepSize,Cloudlet vm,List<Vm> selected){
        Vm[] agents =new Vm[hostlist.size()];
                
                for(int i=0;i<hostlist.size();i++)
                       { // For every Agent 'a' in agents
                           //System.out.println(i+"-"+hostlist.size());
                            agents[i]=hostlist.get(i);
                            
                       }
             
            findNeighbors(agents, myIndex,vm,selected);
        if(neighbors.size() > 0){
            Vm neighborToApproach = selectNeighborToApproach();
                        approachable=neighborToApproach;
            updateNewLocation(approachable, stepSize);
        }
    }
     
    private void findNeighbors(Vm[] agents, int myIndex,Cloudlet vm,List<Vm> selected){
        double distance = 0;
        neighbors.clear();
        //int current = vm.getVmId();
 
        for(int higher = 0; higher < agents.length-1; higher++)
        //for(int higher = agents.length-1; higher> myIndex; higher--)
                {
            distance = currentLocation.computeDistanceTo(agents[higher].a.getCurrentLocation());
     
                        //Log.printLine("-"+agents[higher].getId()+"-dist:"+distance);
                        if( !selected.contains(agents[higher]))
                            //if(agents[higher].a.luciferin < agents[myIndex].a.luciferin )
                                if(distance < sensorRange )
                                {  
                                    Vm h=agents[higher];
                                   Log.printLine("I am one of neighbors "+agents[higher].getId()+" and the dist is: "+distance);
                                        neighbors.add(h);
                               
                            }
         
     
        }
    }
     
     
    private Vm selectNeighborToApproach(){
        double[] probabilities = computeProbabilities();
        double probability = 0;
        //double probability = Math.random();
                Vm selected=null;
                for(int i = 0; i < probabilities.length; i++)
                {
            if(probability < probabilities[i])
                        {  
                            Log.printLine("neighbors NO.  "+neighbors.get(i).getId()+"prob  "+probabilities[i]+ "luciferein   "+ neighbors.get(i).a.getLuciferin());
                selected=neighbors.get(i);
 
                                probability=probabilities[i];
            }
        }
                 
                if(selected!= null )
                {
                    return selected;
                }
        else
                {
                return neighbors.get(neighbors.size() - 1);
                 
    }}
     
     
        private double[] computeProbabilities(){
        double[] probabilities = new double[neighbors.size()];
        double sumOfDifferences = 0;
        for(Vm neighbor: neighbors){
            sumOfDifferences += neighbor.a.getLuciferin()-luciferin;
        }
         
        probabilities[0] = (neighbors.get(0).a.getLuciferin() - luciferin)/sumOfDifferences;
        for(int i = 1; i < neighbors.size(); i++){
            probabilities[i] = probabilities[i-1] + (neighbors.get(i).a.getLuciferin() - luciferin)/sumOfDifferences;
            // Log.printLine("neighbors NO.  "+neighbors.get(i).getId()+"prob  "+probabilities[i]);
        }
        return probabilities;
    }
         
         
        public Vm selectNeighborToApproach1(List<Vm> sec){
        double[] probabilities = computeProbabilities1(sec);
        //double probability = Math.random();
 
            double probability = 0;
                Vm selected=null;
                for(int i = 0; i < probabilities.length; i++)
                {
            if(probability < probabilities[i])
                        {  
                            Log.printLine("-"+sec.get(i).getId()+"-"+probabilities[i]);
                selected=sec.get(i);
                 probability=probabilities[i];
                 
                        }
         
                    }
                             
                            if(selected!= null )
                            {
                                return selected;
                            }
                    else
                            {
                            return neighbors.get(neighbors.size() - 1);
                             
                }}
         
         
        private double[] computeProbabilities1(List<Vm> sec){
        double[] probabilities = new double[sec.size()];
        double sumOfDifferences = 0;
        for(Vm neighbor: sec){
            sumOfDifferences += neighbor.a.getLuciferin()-luciferin;
        }
        probabilities[0] = (sec.get(0).a.getLuciferin() - luciferin)/sumOfDifferences;
        for(int i = 1; i < sec.size(); i++){
            probabilities[i] = probabilities[i-1] + (sec.get(i).a.getLuciferin() - luciferin)/sumOfDifferences;
        }
        return probabilities;
    }
     
    private void updateNewLocation(Vm neighborToApproach, double stepSize){
        newLocation = currentLocation.computeNewLocation(neighborToApproach.a.getCurrentLocation(), stepSize);
    }
     
     
    public void move(){
        if (null != newLocation)
            currentLocation = newLocation;
    }
     
 
    public double updateSensorRange(
            double maxSensorRange,
            int desiredNumberOfNeighbors,
            double beta)
    {
        sensorRange = Math.min(maxSensorRange, Math.max(0, sensorRange + 
                beta * (desiredNumberOfNeighbors - neighbors.size())));
        Log.printLine("sensore range is "+ sensorRange);
 
        return sensorRange;
    }
 
    public String toString(){
        return currentLocation.toString() + "," + luciferin + "\n";
         
    }
 
    @Override
    public int compareTo(Agent otherAgent) {
        double otherAgentsLuciferin = ((Agent)otherAgent).getLuciferin();
        if (luciferin < otherAgentsLuciferin)
            return -1;
        else if (luciferin > otherAgentsLuciferin)
            return 1;
        else
            return 0;
    }
 
 
}