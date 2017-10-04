/**
 * 
 */
package domain;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import burlap.oomdp.core.objects.ObjectInstance;
import burlap.oomdp.core.states.State;
import burlap.oomdp.statehashing.HashableState;
import burlap.oomdp.statehashing.HashableStateFactory;

/**
 * @author Felipe Leno da Silva
 *	Generates state hashes to identify states.
 *  This class generates information about the battery objectives (only battery level information)
 */
public class SmartGridBatteryHashableStateFactory implements HashableStateFactory {

	String agent;
	int discretizationLevels;
	/**
	 * For state representation, only his own battery level matters
	 * @param agentName agent that is generating the hashes
	 * @param discretizationLevels number of possible "battery Levels"
	 */
	public SmartGridBatteryHashableStateFactory(String agentName, int discretizationLevels){
		this.agent = agentName;
	}
	/* (non-Javadoc)
	 * @see burlap.oomdp.statehashing.HashableStateFactory#hashState(burlap.oomdp.core.states.State)
	 */
	@Override
	public HashableState hashState(State s) {
		return new SmartGridBatteryHashableState(s,this.agent,this.discretizationLevels);
	}

	/* (non-Javadoc)
	 * @see burlap.oomdp.statehashing.HashableStateFactory#objectIdentifierIndependent()
	 */
	@Override
	public boolean objectIdentifierIndependent() {
		// TODO Auto-generated method stub
		return false;
	}
	
	public class SmartGridBatteryHashableState extends HashableState.CachedHashableState{

		String agent;
		int discretizationLevels;
		
		/**
		 * Default constructor
		 * @param s state to be hashed
		 * @param agentIdentifier associated agent object ID.
		 */
		public SmartGridBatteryHashableState(State s, String agent,int discretizationLevels) {
			super(s);
			this.agent = agent;
			this.discretizationLevels = discretizationLevels;
			
		}

				
		@Override
		public State copy() {
			return new SmartGridBatteryHashableState(s.copy(),this.agent,this.discretizationLevels);
		}


		/**
		 * This method returns true only when the comparing object is a GoldMineHashableState with the same hash
		 */
		public boolean equals(Object obj) {
			//Checking the hashCode of all objecs]ts
			if(obj instanceof SmartGridBatteryHashableState){
				SmartGridBatteryHashableState cs = (SmartGridBatteryHashableState) obj;
				
				ObjectInstance objectThis = this.getObject(this.agent);
				ObjectInstance objectOther = cs.getObject(this.agent);
				
				boolean isHouse1 = objectThis.getStringValForAttribute(SmartGridConstants.ATT_AT_HOME).equals(SmartGridConstants.IS_AT_HOME);
				boolean isHouse2 = objectOther.getStringValForAttribute(SmartGridConstants.ATT_AT_HOME).equals(SmartGridConstants.IS_AT_HOME);
				
				double discValue = 100d / SmartGridConstants.NUM_BATTERY_LEVELS;
				
				if(isHouse1 && isHouse2){
					double currentLevel1 = objectThis.getRealValForAttribute(SmartGridConstants.ATT_BATTERY);
					double currentLevel2 = objectOther.getRealValForAttribute(SmartGridConstants.ATT_BATTERY);
					int battery1 = (int)Math.floor((currentLevel1-0.01d)/discValue);
					int battery2 = (int)Math.floor((currentLevel2-0.01d)/discValue);
					
					return battery1 == battery2;
				}
				else
					return isHouse1==isHouse2;
			}
			return false;
		}
		
		/**
		 * This method implementation was based on SimpleHashableStateFactory implementation, where the object invariance
		 * is assured by sorting objects by its hashCode. 
		 */
		public int computeHashCode() {
			//-----------------------------------------
			// Regular State
			//-----------------------------------------
			//Only battery values for the current agent matters
			ObjectInstance object = s.getObject(this.agent);
						
			
			boolean isHouse = object.getStringValForAttribute(SmartGridConstants.ATT_AT_HOME).equals(SmartGridConstants.IS_AT_HOME);
			
			
			int battery;
			double discValue = 100d / SmartGridConstants.NUM_BATTERY_LEVELS;
			//The battery level only matters if the agent is at home
			if(!isHouse){				
				double currentLevel = object.getRealValForAttribute(SmartGridConstants.ATT_BATTERY);
				battery = (int)Math.floor((currentLevel-0.01d)/discValue);
			}
			else{
				battery = (int) Math.floor((100d-0.01d)/discValue);
			}
			
			HashCodeBuilder hashCodeBuilder = new HashCodeBuilder(17, 31);
			hashCodeBuilder.append(battery);
			hashCodeBuilder.append(isHouse);
			
			int code = hashCodeBuilder.toHashCode();

			return code;
		}
		

		
		
	}

}
