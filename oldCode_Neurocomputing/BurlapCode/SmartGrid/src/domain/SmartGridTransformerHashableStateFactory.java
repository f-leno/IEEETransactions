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
 *
 *Hashable state factory for the transformer objective. Only the transformer attribute matters
 */
public class SmartGridTransformerHashableStateFactory implements HashableStateFactory {

	String agent;
	
	/**
	 * For state representation, only his own battery level matters
	 * @param agentName agent that is generating the hashes
	 * @param discretizationLevels number of possible "battery Levels"
	 */
	public SmartGridTransformerHashableStateFactory(String agentName){
		this.agent = agentName;
	}
	/* (non-Javadoc)
	 * @see burlap.oomdp.statehashing.HashableStateFactory#hashState(burlap.oomdp.core.states.State)
	 */
	@Override
	public HashableState hashState(State s) {
		return new SmartGridTransformerHashableState(s,this.agent);
	}

	/* (non-Javadoc)
	 * @see burlap.oomdp.statehashing.HashableStateFactory#objectIdentifierIndependent()
	 */
	@Override
	public boolean objectIdentifierIndependent() {
		// TODO Auto-generated method stub
		return false;
	}
	
	public class SmartGridTransformerHashableState extends HashableState.CachedHashableState{

		String agent;
		
		
		/**
		 * Default constructor
		 * @param s state to be hashed
		 * @param agentIdentifier associated agent object ID.
		 */
		public SmartGridTransformerHashableState(State s, String agent) {
			super(s);
			this.agent = agent;
			
			
		}

				
		@Override
		public State copy() {
			return new SmartGridTransformerHashableState(s.copy(),this.agent);
		}


		/**
		 * This method returns true only when the comparing object is a GoldMineHashableState with the same hash
		 */
		public boolean equals(Object obj) {
			//Checking the hashCode of all objecs]ts
			if(obj instanceof SmartGridTransformerHashableState){
				SmartGridTransformerHashableState cs = (SmartGridTransformerHashableState) obj;
				
				ObjectInstance objectThis = this.getObject(this.agent);
				ObjectInstance objectOther = cs.getObject(this.agent);
				
				boolean isHouse1 = objectThis.getStringValForAttribute(SmartGridConstants.ATT_AT_HOME).equals(SmartGridConstants.IS_AT_HOME);
				boolean isHouse2 = objectOther.getStringValForAttribute(SmartGridConstants.ATT_AT_HOME).equals(SmartGridConstants.IS_AT_HOME);
				
				
				
				if(isHouse1 == isHouse2){
					int load1 = this.getFirstObjectOfClass(SmartGridConstants.CLS_TRANSFORMER).getIntValForAttribute(SmartGridConstants.ATT_TRANSFORMER_LOAD);
					int load2 = cs.getFirstObjectOfClass(SmartGridConstants.CLS_TRANSFORMER).getIntValForAttribute(SmartGridConstants.ATT_TRANSFORMER_LOAD);
					
					return load1==load2;
				}
				else
					return false;
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
			
			int load = s.getFirstObjectOfClass(SmartGridConstants.CLS_TRANSFORMER).getIntValForAttribute(SmartGridConstants.ATT_TRANSFORMER_LOAD);
			
			HashCodeBuilder hashCodeBuilder = new HashCodeBuilder(17, 31);
			hashCodeBuilder.append(load);
			hashCodeBuilder.append(isHouse);
			
			int code = hashCodeBuilder.toHashCode();

			return code;
		}
		

		
		
	}

}
