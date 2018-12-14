/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEntity;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.examples.CloudSim_GSO;
import org.cloudbus.cloudsim.lists.CloudletList;
import org.cloudbus.cloudsim.lists.VmList;

/**
 * DatacentreBroker represents a broker acting on behalf of a user. It hides VM management, as vm
 * creation, sumbission of cloudlets to this VMs and destruction of VMs.
 * 
 * @author Rodrigo N. Calheiros
 * @author Anton Beloglazov
 * @since CloudSim Toolkit 1.0
 */
public class DatacenterBroker extends SimEntity {

/** The vm list. */
public static List<? extends Vm> vmList;
 
        protected List<Host> HostCreatedList=new ArrayList<>(); 
        public static List<Host> hostcreate;
/** The vms created list. */
protected List<? extends Vm> vmsCreatedList;
protected List<? extends Host> hostsCreatedList;

/** The cloudlet list. */
protected List<? extends Cloudlet> cloudletList;

/** The cloudlet submitted list. */
protected List<? extends Cloudlet> cloudletSubmittedList;

/** The cloudlet received list. */
protected List<? extends Cloudlet> cloudletReceivedList;

/** The cloudlets submitted. */
protected int cloudletsSubmitted;

/** The vms requested. */
protected int vmsRequested;

/** The vms acks. */
protected int vmsAcks;

/** The vms destroyed. */
protected int vmsDestroyed;

/** The datacenter ids list. */
protected List<Integer> datacenterIdsList;

/** The datacenter requested ids list. */
protected List<Integer> datacenterRequestedIdsList;

/** The vms to datacenters map. */
protected Map<Integer, Integer> vmsToDatacentersMap;

/** The datacenter characteristics list. */
protected Map<Integer, DatacenterCharacteristics> datacenterCharacteristicsList;

/**
* Created a new DatacenterBroker object.
* 
* @param name name to be associated with this entity (as required by Sim_entity class from
*            simjava package)
* @throws Exception the exception
* @pre name != null
* @post $none
*/
public DatacenterBroker(String name) throws Exception {
super(name);

setCloudletList(new ArrayList<Cloudlet>());
setCloudletSubmittedList(new ArrayList<Cloudlet>());
setVmList(new ArrayList<Vm>());
setVmsCreatedList(new ArrayList<Vm>());
setCloudletReceivedList(new ArrayList<Cloudlet>());

cloudletsSubmitted = 0;
setVmsRequested(0);
setVmsAcks(0);
setVmsDestroyed(0);

setDatacenterIdsList(new LinkedList<Integer>());
setDatacenterRequestedIdsList(new ArrayList<Integer>());
setVmsToDatacentersMap(new HashMap<Integer, Integer>());
setDatacenterCharacteristicsList(new HashMap<Integer, DatacenterCharacteristics>());
}

/**
* This method is used to send to the broker the list with virtual machines that must be
* created.
* 
* @param list the list
* @pre list !=null
* @post $none
*/
public void submitVmList(List<? extends Vm> list) {
getVmList().addAll(list);
}

/**
* This method is used to send to the broker the list of cloudlets.
* 
* @param list the list
* @pre list !=null
* @post $none
*/
public void submitCloudletList(List<? extends Cloudlet> list) {
getCloudletList().addAll(list);
}

/**
* Specifies that a given cloudlet must run in a specific virtual machine.
* 
* @param cloudletId ID of the cloudlet being bount to a vm
* @param vmId the vm id
* @pre cloudletId > 0
* @pre id > 0
* @post $none
*/
public void bindCloudletToVm(int cloudletId, int vmId) {
CloudletList.getById(getCloudletList(), cloudletId).setVmId(vmId);
}

/**
* Processes events available for this Broker.
* 
* @param ev a SimEvent object
* @pre ev != null
* @post $none
*/
@Override
public void processEvent(SimEvent ev) {
switch (ev.getTag()) {
// Resource characteristics request
case CloudSimTags.RESOURCE_CHARACTERISTICS_REQUEST:
processResourceCharacteristicsRequest(ev);
break;
// Resource characteristics answer
case CloudSimTags.RESOURCE_CHARACTERISTICS:
processResourceCharacteristics(ev);
break;
// VM Creation answer
case CloudSimTags.VM_CREATE_ACK:
processVmCreate(ev);
break;
// A finished cloudlet returned
case CloudSimTags.CLOUDLET_RETURN:
processCloudletReturn(ev);
break;
// if the simulation finishes
case CloudSimTags.END_OF_SIMULATION:
shutdownEntity();
break;
// other unknown tags are processed by this method
default:
processOtherEvent(ev);
break;
}
}

/**
* Process the return of a request for the characteristics of a PowerDatacenter.
* 
* @param ev a SimEvent object
* @pre ev != $null
* @post $none
*/
protected void processResourceCharacteristics(SimEvent ev) {
DatacenterCharacteristics characteristics = (DatacenterCharacteristics) ev.getData();
getDatacenterCharacteristicsList().put(characteristics.getId(), characteristics);

if (getDatacenterCharacteristicsList().size() == getDatacenterIdsList().size()) {
setDatacenterRequestedIdsList(new ArrayList<Integer>());
createVmsInDatacenter(getDatacenterIdsList().get(0));
}
}

/**
* Process a request for the characteristics of a PowerDatacenter.
* 
* @param ev a SimEvent object
* @pre ev != $null
* @post $none
*/
protected void processResourceCharacteristicsRequest(SimEvent ev) {
setDatacenterIdsList(CloudSim.getCloudResourceList());
setDatacenterCharacteristicsList(new HashMap<Integer, DatacenterCharacteristics>());

Log.printLine(CloudSim.clock() + ": " + getName() + ": Cloud Resource List received with "
+ getDatacenterIdsList().size() + " resource(s)");

for (Integer datacenterId : getDatacenterIdsList()) {
sendNow(datacenterId, CloudSimTags.RESOURCE_CHARACTERISTICS, getId());
}
}

/**
* Process the ack received due to a request for VM creation.
* 
* @param ev a SimEvent object
* @pre ev != null
* @post $none
*/

GSOtask g=new GSOtask();

protected void processVmCreate(SimEvent ev) {
	int[] data = (int[]) ev.getData();
	int datacenterId = data[0];
	int vmId = data[1];
	int result = data[2];

	if (result == CloudSimTags.TRUE) {
		getVmsToDatacentersMap().put(vmId, datacenterId);
		getVmsCreatedList().add(VmList.getById(getVmList(), vmId));
		Log.printLine(CloudSim.clock() + ": " + getName() + ": VM #" + vmId
				+ " has been created in Datacenter #" + datacenterId + ", Host #"
							//	+ VmList.getById(getVmsCreatedList(), vmId).getHost().getId() + "uti - " + (VmList.getById(getVmsCreatedList(), vmId).getHost().getTotalMips() - VmList.getById(getVmsCreatedList(), vmId).getHost().getAvailableMips())/VmList.getById(getVmsCreatedList(), vmId).getHost().getTotalMips());

				+ VmList.getById(getVmsCreatedList(), vmId).getHost().getId() + "uti - " + (VmList.getById(getVmsCreatedList(), vmId).getHost().getAvailableMips()));
	} else {
		Log.printLine(CloudSim.clock() + ": " + getName() + ": Creation of VM #" + vmId
				+ " failed in Datacenter #" + datacenterId);
	}

	incrementVmsAcks();

	// all the requested VMs have been created
	if (getVmsCreatedList().size() == getVmList().size() - getVmsDestroyed()) {
		submitCloudlets();
	} else {
		// all the acks received, but some VMs were not created
		if (getVmsRequested() == getVmsAcks()) {
			// find id of the next datacenter that has not been tried
			for (int nextDatacenterId : getDatacenterIdsList()) {
				if (!getDatacenterRequestedIdsList().contains(nextDatacenterId)) {
					createVmsInDatacenter(nextDatacenterId);
					return;
				}
			}

			// all datacenters already queried
			if (getVmsCreatedList().size() > 0) { // if some vm were created
				submitCloudlets();
			} else { // no vms created. abort
				Log.printLine(CloudSim.clock() + ": " + getName()
						+ ": none of the required VMs could be created. Aborting");
				finishExecution();
			}
		}
	}
}

/**
* Process a cloudlet return event.
* 
* @param ev a SimEvent object
* @pre ev != $null
* @post $none
*/
protected void processCloudletReturn(SimEvent ev) {
	Cloudlet cloudlet = (Cloudlet) ev.getData();
	getCloudletReceivedList().add(cloudlet);
	Log.printLine(CloudSim.clock() + ": " + getName() + ": Cloudlet " + cloudlet.getCloudletId()
			+ " received");
	cloudletsSubmitted--;
	if (getCloudletList().size() == 0 && cloudletsSubmitted == 0) { // all cloudlets executed
		Log.printLine(CloudSim.clock() + ": " + getName() + ": All Cloudlets executed. Finishing...");
		clearDatacenters();
		finishExecution();
	} else { // some cloudlets haven't finished yet
		if (getCloudletList().size() > 0 && cloudletsSubmitted == 0) {
			// all the cloudlets sent finished. It means that some bount
			// cloudlet is waiting its VM be created
			clearDatacenters();
			createVmsInDatacenter(0);
		}

	}
}

/**
* Overrides this method when making a new and different type of Broker. This method is called
* by {@link #body()} for incoming unknown tags.
* 
* @param ev a SimEvent object
* @pre ev != null
* @post $none
*/
protected void processOtherEvent(SimEvent ev) {
if (ev == null) {
Log.printLine(getName() + ".processOtherEvent(): " + "Error - an event is null.");
return;
}

Log.printLine(getName() + ".processOtherEvent(): "
+ "Error - event unknown by this DatacenterBroker.");
}

/**
* Create the virtual machines in a datacenter.
* 
* @param datacenterId Id of the chosen PowerDatacenter
* @pre $none
* @post $none
*/
protected void createVmsInDatacenter(int datacenterId) {
// send as much vms as possible for this datacenter before trying the next one
int requestedVms = 0;
String datacenterName = CloudSim.getEntityName(datacenterId);
/*
LinkedList<Vm> list = new LinkedList<Vm>();
populatin p= new populatin();
        for(int c=0;c<p.vm.size();c++)
        { 
            if(!list.contains(p.vm.get(c)))
            {
                list.add(p.vm.get(c));
            }
            
        }


            vmList=list;
            
       	 System.out.println("vm list size" + vmList.size());

*/  


for (Vm vm : getVmList()) {
if (!getVmsToDatacentersMap().containsKey(vm.getId())) 
{
Log.printLine(CloudSim.clock() + ": " + getName() + ": Trying toccccccc Create VM #" + vm.getId()
+ " in " + datacenterName+" id:"+datacenterId);
sendNow(datacenterId, CloudSimTags.VM_CREATE_ACK, vm);
requestedVms++;
}
}

getDatacenterRequestedIdsList().add(datacenterId);

setVmsRequested(requestedVms);
setVmsAcks(0);
}

/**
* Submit cloudlets to the created VMs.
* 
* @pre $none
* @post $none
*/
       
protected void submitCloudlets()
        {
	double selectedVm1=0;
	double selectedVm2=0;
	double selectedHost1= 0;
	double selectedHost2=0;
	    double allocatedHost_id = 0;
	    double allocatedVm_id = 0;
	    double result1=0;
	    double result2=0;
	    double allocated = 0;
	 double diff;
	int k= 0;
	Vm allocatedVm = null;
	Vm allocatedVm2 = null;
	int datacenterId = 0;
	Host allocatedHost = null;
	double t1 = 0;
	double t2 = 0;
	double t11 = 0;
	double t22 = 0;
	
            for (Cloudlet cloudlet : getCloudletList()) 
                {
// if user didn't bind this cloudlet and it has not been executed yet
 
	populatin p= new populatin();


	g.vmcreate=getVmList();

	//if (result == CloudSimTags.TRUE) {
	System.out.println("--------------------------------------------------------------");
	System.out.println("cloudlet no "+ cloudlet.getCloudletId());
	/*
	    p.task.add(getCloudletList().get(c));
	    p.task.add(c);
	  p.vm.add(g.GSO(getCloudletList().get(c)));
	 c.setVmId(g.GSO(c).getId());
	 */
	int i=0;

	do
	 {
	//g.updateLuciferin(getCloudletList().get(c));

	if( i==0)
	{
	 
	//getCloudletList().get(c).setVmId(g.GSO(getCloudletList().get(c)).getId());
	allocatedVm= g.GSO(cloudlet);
	selectedVm1= allocatedVm.getId();
	System.out.println("selected vm 1 " + selectedVm1);



	    
	    
	     t1= (cloudlet.getCloudletLength() / (allocatedVm.getMips()* allocatedVm.getNumberOfPes())+ allocatedVm.start_time/3600) * allocatedVm.getCost();

	   System.out.println("cloudlet length is " + cloudlet.getCloudletLength()+ "vm mips " + allocatedVm.getMips()+ "execution time is " + t1);

	//if (result == CloudSimTags.TRUE) {
	             //   getVmsToDatacentersMap().put(allocatedVm.getId(), datacenterId);
	//getVmsCreatedList().add(VmList.getById(getVmList(), allocatedVm.getId()));

	Log.printLine(CloudSim.clock() + ": " + getName() + ": VM # " +allocatedVm.getId()
	//   + vm.getId()
	+ " has been created in Datacenter #" + datacenterId + ", Host #"+
	          VmList.getById(getVmsCreatedList(), allocatedVm.getId()).getHost().getId() );

	selectedHost1 = VmList.getById(getVmsCreatedList(), allocatedVm.getId()).getHost().getId();
	System.out.println("selected host 1 " + selectedHost1);

	// t2 = VmList.getById(getVmsCreatedList(), allocatedVm.getId()).getHost().start_time+(VmList.getById(getVmsCreatedList(), allocatedVm.getId()).getHost().getTotalMips()- VmList.getById(getVmsCreatedList(), allocatedVm.getId()).getHost().getAvailableMips())/ VmList.getById(getVmsCreatedList(), allocatedVm.getId()).getHost().getTotalMips(); 
	 t2 = VmList.getById(getVmsCreatedList(), allocatedVm.getId()).getHost().start_time+ VmList.getById(getVmsCreatedList(), allocatedVm.getId()).getHost().getAvailableMips(); 

	System.out.println("total mips " + VmList.getById(getVmsCreatedList(), allocatedVm.getId()).getHost().getTotalMips()+ " availble mips "+ VmList.getById(getVmsCreatedList(), allocatedVm.getId()).getHost().getAvailableMips()); 
	System.out.println("available mips  t2 " + t2);

	   result1 = (t1 * 0.5) + (t2 * 0.5);
	System.out.println("integration result before iteration " + result1);
	}
	   System.out.println("ITERATION "+ i);

	// System.out.println("allocated host " + allocatedHost_id);

	//  for(int o=0;o<config.getmax_iteration();o++)
	// {
	  // p.vm.add(g.GSO(getCloudletList().get(c)));
	allocatedVm= g.GSO(cloudlet);

	  // p.vm.add(g.GSO(c));
	     // getCloudletList().get(c).setVmId(g.GSO(getCloudletList().get(c)).getId());
	                 //  p.host.add(v.GSO(getVmList().get(0)));  
	   selectedVm2= allocatedVm.getId();

	     t11= (cloudlet.getCloudletLength() / (allocatedVm.getMips()* allocatedVm.getNumberOfPes())+ allocatedVm.start_time/3600) * allocatedVm.getCost();
	      System.out.println("selected vm 2 " + selectedVm2);

	System.out.println("cloudlet length is " + cloudlet.getCloudletLength()+ "vm mips " + getVmList().get(allocatedVm.getId()).getMips()+ "execution time is " + t11);  
	     // System.out.println("cloudlet length is " + c.getCloudletLength()+ "vm mips " + getVmList().get(vmId).getMips()+ "execution time is " + t11);
	   //getVmsToDatacentersMap().put(allocatedVm.getId(), datacenterId);
	   
		 getVmsCreatedList().add(VmList.getById(getVmList(), allocatedVm.getId()));

		 Log.printLine(CloudSim.clock() + ": " + getName() + ": VM # " +allocatedVm.getId()
		 + " has been created in iteration in Datacenter #" + datacenterId + ", Host #"+
	                 VmList.getById(getVmsCreatedList(), allocatedVm.getId()).getHost().getId() ); 
		selectedHost2 = VmList.getById(getVmsCreatedList(), allocatedVm.getId()).getHost().getId();
	System.out.println("selected host 2 " + selectedHost2);

//	  	VmList.getById(getVmsCreatedList(), vmId).getHost().start_time = VmList.getById(getVmsCreatedList(), vmId).getHost().start_time+(VmList.getById(getVmsCreatedList(), vmId).getHost().getTotalMips()- VmList.getById(getVmsCreatedList(), vmId).getHost().getAvailableMips())/ VmList.getById(getVmsCreatedList(), vmId).getHost().getTotalMips();
	// VmList.getById(getVmsCreatedList(), vmId).getHost().start_time+ 
	    // t22 =VmList.getById(getVmsCreatedList(), allocatedVm.getId()).getHost().start_time+(VmList.getById(getVmsCreatedList(), allocatedVm.getId()).getHost().getTotalMips()- VmList.getById(getVmsCreatedList(), allocatedVm.getId()).getHost().getAvailableMips())/ VmList.getById(getVmsCreatedList(), allocatedVm.getId()).getHost().getTotalMips(); 
	     t22 =VmList.getById(getVmsCreatedList(), allocatedVm.getId()).getHost().start_time+ VmList.getById(getVmsCreatedList(), allocatedVm.getId()).getHost().getAvailableMips(); 

	     System.out.println("total mips " + VmList.getById(getVmsCreatedList(), allocatedVm.getId()).getHost().getTotalMips()+ " availble mips "+ VmList.getById(getVmsCreatedList(), allocatedVm.getId()).getHost().getAvailableMips()); 

	    System.out.println("available mips t22 " + t22);

	     result2 = (t11*0.5)+ (t22*0.5);
	      System.out.println("integration result in iteration " + result2);

	if(i==0)
	{
	if (result1> result2)
	{
	allocatedVm_id=selectedVm2;
	allocatedHost_id=selectedHost2;

	allocated=result2;
	diff=result1-result2;

	}
	else
	{ 
	allocatedVm_id=selectedVm1;
	allocatedHost_id=selectedHost1;

	allocated=result1;
	diff=result2-result1;


	}

	i++;   
	}
	
	else
	{  if(allocated>result2)
	{
	allocatedVm_id=selectedVm2;
	allocatedHost_id=selectedHost2;

	diff=allocated-result2;
	allocated=result2;

	}


	else
	{
	diff=allocated-result2;
	//diff=result2- allocated;

	}
	}
	k++;

	   // double diff = result2 - result1;
	     System.out.println("diff between two integrations " + diff);
	//g.updateSensorRanges();
	    // && k<=config.maxIterations
	 } while(diff>0.5 );       
	// } // end of iterations (outer loop)

           
	     

	      // }
	//}// end if result

	/*
	else {
	Log.printLine(CloudSim.clock() + ": " + getName() + ": Creation of VM #" + vmId
	+ " failed in Datacenter #" + datacenterId);
	}
	*/
	 // try
	//vm = getVmsCreatedList().get(vmIndex);
                        
                        Log.printLine("subit cloudlet"); 
                                

                        if(allocatedVm!=null)
                        {
Log.printLine(CloudSim.clock() + ": " + getName() + ": Sending cloudlet "
+ cloudlet.getCloudletId() + " to VM #" + allocatedVm.getId()+"-"+allocatedVm.getState());

cloudlet.setVmId(allocatedVm.getId());
Log.printLine("CHECK HERE");

sendNow(getVmsToDatacentersMap().get(allocatedVm.getId()), CloudSimTags.CLOUDLET_SUBMIT, cloudlet);
Log.printLine("OR HERE");

cloudletsSubmitted++;
//vmIndex = (vmIndex + 1) % getVmsCreatedList().size();
getCloudletSubmittedList().add(cloudlet);
Log.printLine("DONE");
                        }
                }
// remove submitted cloudlets from waiting list
for (Cloudlet cloudlet : getCloudletSubmittedList()) 
                {
                  
                        getCloudletList().remove(cloudlet);
                        Log.printLine("REMOVE CLOUDLET");

                        
               }
}

/**
* Destroy the virtual machines running in datacenters.
* 
* @pre $none
* @post $none
*/
//  getVmsList();
protected void clearDatacenters() {
Log.printLine("vm created list " + getVmList().size());

for (Vm vm : getVmList()) {
Log.printLine(CloudSim.clock() + ": " + getName() + ": Destroying VM #" + vm.getId());
//sendNow(getVmsToDatacentersMap().get(vm.getId()), CloudSimTags.VM_DESTROY, vm);
sendNow(vm.getId(), CloudSimTags.VM_DESTROY, vm);
Log.printLine("alrady destroyed");
}

getVmList().clear();
Log.printLine("clear created VMs");

}






/**
* Send an internal event communicating the end of the simulation.
* 
* @pre $none
* @post $none
*/
protected void finishExecution() {
sendNow(getId(), CloudSimTags.END_OF_SIMULATION);
}

/*
* (non-Javadoc)
* @see cloudsim.core.SimEntity#shutdownEntity()
*/
@Override
public void shutdownEntity() {
Log.printLine(getName() + " is shutting down...");
}

/*
* (non-Javadoc)
* @see cloudsim.core.SimEntity#startEntity()
*/
@Override
public void startEntity() {
Log.printLine(getName() + " is starting...");
schedule(getId(), 0, CloudSimTags.RESOURCE_CHARACTERISTICS_REQUEST);
}

/**
* Gets the vm list.
* 
* @param <T> the generic type
* @return the vm list
*/
@SuppressWarnings("unchecked")
public <T extends Vm> List<T> getVmList() {
return (List<T>) vmList;
}

/**
* Sets the vm list.
* 
* @param <T> the generic type
* @param vmList the new vm list
*/
protected <T extends Vm> void setVmList(List<T> vmList) {
this.vmList = vmList;
}

/**
* Gets the cloudlet list.
* 
* @param <T> the generic type
* @return the cloudlet list
*/
//@SuppressWarnings("unchecked")
public <T extends Cloudlet> List<T> getCloudletList() {
return (List<T>) cloudletList;
}

/**
* Sets the cloudlet list.
* 
* @param <T> the generic type
* @param cloudletList the new cloudlet list
*/
protected <T extends Cloudlet> void setCloudletList(List<T> cloudletList) {
this.cloudletList = cloudletList;
}

/**
* Gets the cloudlet submitted list.
* 
* @param <T> the generic type
* @return the cloudlet submitted list
*/
@SuppressWarnings("unchecked")
public <T extends Cloudlet> List<T> getCloudletSubmittedList() {
return (List<T>) cloudletSubmittedList;
}

/**
* Sets the cloudlet submitted list.
* 
* @param <T> the generic type
* @param cloudletSubmittedList the new cloudlet submitted list
*/
protected <T extends Cloudlet> void setCloudletSubmittedList(List<T> cloudletSubmittedList) {
this.cloudletSubmittedList = cloudletSubmittedList;
}

/**
* Gets the cloudlet received list.
* 
* @param <T> the generic type
* @return the cloudlet received list
*/
@SuppressWarnings("unchecked")
public <T extends Cloudlet> List<T> getCloudletReceivedList() {
return (List<T>) cloudletReceivedList;
}

/**
* Sets the cloudlet received list.
* 
* @param <T> the generic type
* @param cloudletReceivedList the new cloudlet received list
*/
protected <T extends Cloudlet> void setCloudletReceivedList(List<T> cloudletReceivedList) {
this.cloudletReceivedList = cloudletReceivedList;
}

/**
* Gets the vm list.
* 
* @param <T> the generic type
* @return the vm list
*/
@SuppressWarnings("unchecked")
public <T extends Vm> List<T> getVmsCreatedList() {
return (List<T>) vmsCreatedList;
}

/**
* Sets the vm list.
* 
* @param <T> the generic type
* @param vmsCreatedList the vms created list
*/
protected <T extends Vm> void setVmsCreatedList(List<T> vmsCreatedList) {
this.vmsCreatedList = vmsCreatedList;
}

/**
* Gets the vms requested.
* 
* @return the vms requested
*/
protected int getVmsRequested() {
return vmsRequested;
}

/**
* Sets the vms requested.
* 
* @param vmsRequested the new vms requested
*/
protected void setVmsRequested(int vmsRequested) {
this.vmsRequested = vmsRequested;
}

/**
* Gets the vms acks.
* 
* @return the vms acks
*/
protected int getVmsAcks() {
return vmsAcks;
}

/**
* Sets the vms acks.
* 
* @param vmsAcks the new vms acks
*/
protected void setVmsAcks(int vmsAcks) {
this.vmsAcks = vmsAcks;
}

/**
* Increment vms acks.
*/
protected void incrementVmsAcks() {
vmsAcks++;
}

/**
* Gets the vms destroyed.
* 
* @return the vms destroyed
*/
protected int getVmsDestroyed() {
return vmsDestroyed;
}

/**
* Sets the vms destroyed.
* 
* @param vmsDestroyed the new vms destroyed
*/
protected void setVmsDestroyed(int vmsDestroyed) {
this.vmsDestroyed = vmsDestroyed;
}

/**
* Gets the datacenter ids list.
* 
* @return the datacenter ids list
*/
protected List<Integer> getDatacenterIdsList() {
return datacenterIdsList;
}

/**
* Sets the datacenter ids list.
* 
* @param datacenterIdsList the new datacenter ids list
*/
protected void setDatacenterIdsList(List<Integer> datacenterIdsList) {
this.datacenterIdsList = datacenterIdsList;
}

/**
* Gets the vms to datacenters map.
* 
* @return the vms to datacenters map
*/
protected Map<Integer, Integer> getVmsToDatacentersMap() {
return vmsToDatacentersMap;
}

/**
* Sets the vms to datacenters map.
* 
* @param vmsToDatacentersMap the vms to datacenters map
*/
protected void setVmsToDatacentersMap(Map<Integer, Integer> vmsToDatacentersMap) {
this.vmsToDatacentersMap = vmsToDatacentersMap;
}

/**
* Gets the datacenter characteristics list.
* 
* @return the datacenter characteristics list
*/
protected Map<Integer, DatacenterCharacteristics> getDatacenterCharacteristicsList() {
return datacenterCharacteristicsList;
}

/**
* Sets the datacenter characteristics list.
* 
* @param datacenterCharacteristicsList the datacenter characteristics list
*/
protected void setDatacenterCharacteristicsList(
Map<Integer, DatacenterCharacteristics> datacenterCharacteristicsList) {
this.datacenterCharacteristicsList = datacenterCharacteristicsList;
}

/**
* Gets the datacenter requested ids list.
* 
* @return the datacenter requested ids list
*/
protected List<Integer> getDatacenterRequestedIdsList() {
return datacenterRequestedIdsList;
}

/**
* Sets the datacenter requested ids list.
* 
* @param datacenterRequestedIdsList the new datacenter requested ids list
*/
protected void setDatacenterRequestedIdsList(List<Integer> datacenterRequestedIdsList) {
this.datacenterRequestedIdsList = datacenterRequestedIdsList;
}

@SuppressWarnings("unchecked")
public <T extends Host> List<T> getHostsCreatedList() {
return (List<T>) hostsCreatedList;
}



}
