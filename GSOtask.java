/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cloudbus.cloudsim;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.GSOConfiguration;

import static org.cloudbus.cloudsim.examples.CloudSim_GSO.config;

public class GSOtask {
    public List<Vm> vmcreate;
     RewardFunction rf;
    

      public Vm GSO(Cloudlet c)
   {       
       Vm allocatedHost = null;
                               Vm p=vmcreate.get(0);
     //  Random r= new Random();
     //  Vm p= vmcreate.get(r.nextInt(6));

                                   int myIndex=0;
                                   
                                   for(int i=0;i<vmcreate.size();i++)
                                       {
                                	   if(vmcreate.get(i)==p)
                                            {
                                    	   myIndex=i;
                                            Log.printLine("vm index "+ myIndex);

                                            }

                                       }

                                    
                                    double power_after=0,power_after1=0,allocatedhost_power=0,diff=0;
                                    int allocatedhost_id=0;
                                     List<Vm> selected=new ArrayList<>();
                                     int selected1=0,selected2=0;
                                     int i=0;
                                   do
                                   {
                                       updateLuciferin(c);

 
                                    if(i==0)
                                    {   p.a.planMove(vmcreate, myIndex, config.stepSize,c,selected);
                                        selected.add(p.a.approachable);
                                        selected1=p.a.approachable.getId();

                                        if(p.a.approachable!=null)
                                        {
                                        	 double cost1 = p.a.approachable.cost;
                                          	double rfval1= (c.getCloudletLength()/(p.a.approachable.getMips() * p.a.approachable.getNumberOfPes()) +p.a.approachable.start_time)/3600;

                                        power_after=cost1 * rfval1;

                                        Log.printLine("vm selected:1 "+p.a.approachable.getId()+"execution cost - "+power_after);
                                        }
                                   }
                                   
                                   p.a.planMove(vmcreate, myIndex, config.stepSize,c,selected);

                                   selected.add(p.a.approachable);
                                   selected2=p.a.approachable.getId();
                                        if(p.a.approachable!=null)
                                        	
                                        {
                                        	double cost1 = p.a.approachable.cost;
                                          	double rfval1= (c.getCloudletLength()/(p.a.approachable.getMips() * p.a.approachable.getNumberOfPes()) +p.a.approachable.start_time)/3600;

                                        	power_after1= cost1 * rfval1;


                                        Log.printLine("vm selected:2 "+p.a.approachable.getId()+"execution cost - "+power_after1);
                                        }
                                   
                                        if(i==0)
                                        {  if(power_after>power_after1)
                                             {allocatedhost_id=selected2;
                                              allocatedhost_power=power_after1;
                                              diff=power_after-power_after1;
                                             }
                                            else
                                             {
                                                allocatedhost_id=selected1;
                                                 allocatedhost_power=power_after;
      
                                              //  allocatedhost_power=power_after1;
                                             // diff=power_after-power_after1;
                                                  diff=power_after1-power_after;
      
                                               } 
                                              
                                         i++;   
                                        }
                                        else
                                        {  if(allocatedhost_power>power_after1)
                                             {allocatedhost_id=selected2;
                                               
                                              diff=allocatedhost_power-power_after1;
                                              allocatedhost_power=power_after1;
                                             }
                                           else
                                             {
                                                
                                              diff=power_after1-allocatedhost_power;
                                        //  diff=allocatedhost_power-power_after1;
      
                                               
                                             }
                                        }
                                     
                                       System.out.println(i+" diff is  "+diff);
                                       updateSensorRanges();

                                   }while(diff>0.1);


                                   if(p.a.approachable!=null)
                                   {
                                    for(int j=0;j< vmcreate.size();j++)   
                                    { if(vmcreate.get(j).getId()==allocatedhost_id)
                                        {allocatedHost= vmcreate.get(j);
                                        }
                                    }
                                   Log.printLine("vm final selected "+allocatedHost.getId());
                                   }
                                   
                                   
                               
          allocatedHost.start_time=(c.getCloudletLength()/allocatedHost.mips)+allocatedHost.start_time;
         return allocatedHost;
   }
   
   
   public void updateSensorRanges(){
		 for(Vm h: vmcreate){
			h.a.updateSensorRange(config.getMaxSensorRange(),
					config.getDesiredNumberOfNeighbors(),
					config.getBeta());
		}
	}
        
        public void updateLuciferin(Cloudlet c){
		 rf = config.getRf();
                 
                       for(Vm h :vmcreate)
                       { // For every Agent 'a' in agents
                         //
                      	 h.a.luciferin= ((((c.getCloudletLength()/ ((h.getMips()* h.getNumberOfPes()))) +h.start_time))/3600) * h.getCost();

                           System.out.println("vm id is "+ h.getId()+" start time is "+ h.start_time + " luc is "+ h.a.luciferin);
                           h.a.updateLuciferin(rf.computeRewardFunctionValue(h,c));

		      }
	}
   
}
