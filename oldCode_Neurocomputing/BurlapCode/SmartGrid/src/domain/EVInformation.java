/**
 * 
 */
package domain;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Random;

import burlap.oomdp.core.objects.ObjectInstance;

/**
 * @author Felipe Leno da Silva
 *
 *Records information about All EVs, such as average battery used in daily jorney, what time the car leaves the house
 *the journey duration.
 */
public class EVInformation {

	//Stores agent indexes
	Hashtable<String,Integer> indexes;
	//Random variables for each agent
	List<Random> agentRandom;
	//Next Step to start Journey
	List<Integer> nextJourneyStep;
	//Next Step to return from Journey
	List<Integer> nextJourneyReturn;
	//Battery consumption for the next journey
	List<Double> batteryConsumption;
	//Number of time steps in a day
	int numberOfTimeSteps;
	//Current Time step in a day
	int currentTimeStep;
	
	public EVInformation(){
		 indexes = new Hashtable<String,Integer>();
		 agentRandom = new ArrayList<Random>();
		 batteryConsumption = new ArrayList<Double>();
		 this.nextJourneyReturn = new ArrayList<Integer>();
		 this.nextJourneyStep = new ArrayList<Integer>();
		 currentTimeStep = 0;
		 numberOfTimeSteps = (24 * 60) / SmartGridConstants.TIME_STEP;
	}
	/**
	 * Links an agent to the EVInformation, instantiating his data structures
	 * @param agent
	 * @param nextInt
	 */
	public void linkAgent(ObjectInstance agent, int nextInt) {
		if(!indexes.contains(agent.getName())){
			int index = agentRandom.size();
			indexes.put(agent.getName(), index);
			agentRandom.add(new Random(nextInt));
			
			batteryConsumption.add(0d); //Updated in sortNextReturn
			int nextJourneyStep = sortNextJourney(index);
			this.nextJourneyStep.add(nextJourneyStep);
			int nextJourneyRet = sortNextReturn(index);			
			this.nextJourneyReturn.add(nextJourneyRet);
		}
	}
	
	/**
	 * Ends the current time step
	 */
	public void endTimeStep() {
		currentTimeStep++;
		if(currentTimeStep>=numberOfTimeSteps){
			currentTimeStep = 0;
		}
		
	}
	/**
	 * Verifies if the journey should start for a given agent
	 * @param name agent name
	 * @return should the journey start?
	 */
	public boolean isStartOfJourney(String name) {
		int index = indexes.get(name);
		
		return this.nextJourneyStep.get(index).intValue() == this.currentTimeStep;
	}
	
	public boolean isEndOfJourney(String name) {
		int index = this.indexes.get(name);
		int journeyReturn = this.nextJourneyReturn.get(index);
		boolean journeyArrived = this.currentTimeStep == journeyReturn;
		return journeyArrived;
	}
	
	/**
	 * Sorts the next time step to start a new journey
	 * @param index agent index
	 * @return number of time step
	 */
	protected int sortNextJourney(int index) {
		Random rnd = this.agentRandom.get(index);
		int nextJourney = -1;
		
		double mean = SmartGridConstants.DEFAULT_JOURNEY_START_MEAN/24 * numberOfTimeSteps;
		double variance = SmartGridConstants.DEFAULT_JOURNEY_START_VARIANCE/24 * numberOfTimeSteps;
		
		//Sort next journey from a normal distribution
		while(nextJourney<0){
			nextJourney = (int) Math.round(mean + rnd.nextGaussian()*variance);			
		}
		return nextJourney;
		
	}
	
	/**
	 * Sorts the next time step to return from journey
	 * @param index agent index
	 * @return number of time step
	 */
	protected int sortNextReturn(int index) {
		Random rnd = this.agentRandom.get(index);
		int nextReturn = Integer.MAX_VALUE;
		
		
					
		double mean = SmartGridConstants.DEFAULT_JOURNEY_DURATION_MEAN/24 * numberOfTimeSteps;
		double variance = SmartGridConstants.DEFAULT_JOURNEY_DURATION_VARIANCE/24 * numberOfTimeSteps;
		
		//Sort next journey from a normal distribution
		while(nextReturn>=numberOfTimeSteps || nextReturn <= this.nextJourneyStep.get(index)){
			int duration = (int) Math.round(mean + rnd.nextGaussian()*variance);
			updateBattery(duration,index);
			nextReturn = duration + this.nextJourneyStep.get(index);
		}
		return nextReturn;
		
	} 
	
	/**
	 * Calculates the battery consumption in the next journey
	 * @param duration duration in steps
	 */
	protected void updateBattery(int duration,int index) {
		double averagePercentage = (SmartGridConstants.DEFAULT_AVERAGE_DAILY_NEED/24 * numberOfTimeSteps) * SmartGridConstants.RECHARGES_AT_STEP;
		double meanJourney = SmartGridConstants.DEFAULT_JOURNEY_DURATION_MEAN/24 * numberOfTimeSteps;
		
		double consump = averagePercentage * duration / meanJourney;
		this.batteryConsumption.set(index, consump);
		
	}
	/**
	 * Sorts the journey hour for the next day
	 * @param name name of agent
	 */
	public void beginJourney(String name) {
		int index = indexes.get(name);
		int nextJourneyStep = sortNextJourney(index);
		
		this.nextJourneyStep.set(index,nextJourneyStep);
	}
	/**
	 * Sorts the return hour for the next day
	 * @param name name of agent
	 */
	public void endJourney(String name) {
		int index = indexes.get(name);
		int nextJourneyRet = sortNextReturn(index);
		this.nextJourneyReturn.set(index,nextJourneyRet);
		
	}
	/**
	 * Returns the battery consumption for the current journey
	 * @param name agent name
	 * @return percentage of consumption
	 */
	public double batteryConsumption(String name) {
		int index = indexes.get(name);
		return batteryConsumption.get(index);
	}
	/**
	 * Reinitiates
	 */
	public void reset() {
		indexes = new Hashtable<String,Integer>();
		 agentRandom = new ArrayList<Random>();
		 batteryConsumption = new ArrayList<Double>();
		 currentTimeStep = 0;
		 System.gc();
		
	}

	
}
