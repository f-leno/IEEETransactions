/**
 * 
 */
package domain;

import java.util.List;

import burlap.oomdp.core.TransitionProbability;
import burlap.oomdp.core.objects.ObjectInstance;
import burlap.oomdp.core.states.State;
import burlap.oomdp.stochasticgames.JointAction;
import burlap.oomdp.stochasticgames.JointActionModel;
import burlap.oomdp.stochasticgames.agentactions.GroundedSGAgentAction;

/**
 * @author Felipe Leno da Silva
 *
 *Implements the state transition dynamics of the Smart Grid domain
 */
public class SmartGridStandardMechanics extends JointActionModel {

	
	protected EVInformation evInformation;
	
	/**
	 * Default contructor
	 * @param evInformation provide information about agents such as distributions for journey time, journey energy costs, etc.
	 */
	public SmartGridStandardMechanics(EVInformation evInformation){
		super();
		this.evInformation = evInformation;
	}
	/**
	 * Not implemented for this domain
	 */
	public List<TransitionProbability> transitionProbsFor(State s, JointAction ja) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Executes the joint action in the Smart Grid domain
	 */
	protected State actionHelper(State s, JointAction ja) {
		List <GroundedSGAgentAction> gsas = ja.getActionList();
		
		
		int numberCharging = 0; //Counts the number of EVs changing in this time step
		//Process each action individually 
		for(GroundedSGAgentAction gsa: gsas){
			//If the EV is charging
			if(gsa.actionName().equals(SmartGridConstants.ACTION_CHARGE)){
				//Process Action effects
				processCharging(s,gsa);
				numberCharging++;				
			}
		}
		
		//Verify all agents to see if they start their journey now.
		processJourney(s);
		
		//Set the number of charging agents
		ObjectInstance transformer = s.getFirstObjectOfClass(SmartGridConstants.CLS_TRANSFORMER);
		transformer.setValue(SmartGridConstants.ATT_TRANSFORMER_LOAD, numberCharging);
		
		evInformation.endTimeStep();
		
		return s;	
		
	}
	
	
	/**
	 * Firstly validates if the agent can be charging (if it is at home). If not a RuntimeException is thrown. If everything is ok, the agent battery is charged 
	 * @param s current state
	 * @param ga "charge" ground action
	 */
	protected void processCharging(State s, GroundedSGAgentAction ga){
		ObjectInstance agent = s.getObject(ga.actingAgent);		
		//Check if at home
		if(agent.getValueForAttribute(SmartGridConstants.ATT_AT_HOME).equals(SmartGridConstants.IS_AWAY)){
			throw new RuntimeException("No EV can be charging during its daily jorney");
		}
		
		//Update battery
		double batLevel = agent.getRealValForAttribute(SmartGridConstants.ATT_BATTERY);
		batLevel += SmartGridConstants.RECHARGES_AT_STEP;
		if(batLevel>100) batLevel = 100;
		
		agent.setValue(SmartGridConstants.ATT_BATTERY, batLevel);
	}
	
	/**
	 * Verify if agents started their journey now and modifies the state according to it
	 * @param s state
	 */
	protected void processJourney(State s) {
		List<ObjectInstance> allAgents = s.getObjectsOfClass(SmartGridConstants.CLS_EV);
		
		for(ObjectInstance agent:allAgents){
			
			//verifies if the agent should start or end his journey
			if(agent.getStringValForAttribute(SmartGridConstants.ATT_AT_HOME).equals(SmartGridConstants.IS_AT_HOME)){
				//Should the agent start his journey?
				if(evInformation.isStartOfJourney(agent.getName())){
					agent.setValue(SmartGridConstants.ATT_AT_HOME, SmartGridConstants.IS_AWAY);
					double battery = agent.getRealValForAttribute(SmartGridConstants.ATT_BATTERY) - evInformation.batteryConsumption(agent.getName());
					if(battery<0) battery = 0;
					agent.setValue(SmartGridConstants.ATT_BATTERY, battery);
	                evInformation.beginJourney(agent.getName()); 
				}			
			}
			else{
				//Should the agent finish his journey?
				if(evInformation.isEndOfJourney(agent.getName())){
					agent.setValue(SmartGridConstants.ATT_AT_HOME, SmartGridConstants.IS_AT_HOME);
					evInformation.endJourney(agent.getName());
				}
			}
		}
		
	}

}
