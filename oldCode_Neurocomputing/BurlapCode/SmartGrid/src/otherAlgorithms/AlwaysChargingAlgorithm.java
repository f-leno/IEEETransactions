/**
 * 
 */
package otherAlgorithms;

import java.util.List;
import java.util.Map;
import java.util.Random;

import Multiobjective.MOSGAgent;
import burlap.oomdp.core.states.State;
import burlap.oomdp.stochasticgames.JointAction;
import burlap.oomdp.stochasticgames.SGDomain;
import burlap.oomdp.stochasticgames.agentactions.GroundedSGAgentAction;
import burlap.oomdp.stochasticgames.agentactions.SGAgentAction;
import domain.SmartGridConstants;

/**
 * @author Felipe Leno
 * This agents always charge when at home
 */
public class AlwaysChargingAlgorithm extends MOSGAgent {

protected Random rnd;
	
	/**
	 * Receives a Random object to experiments reproducibility 
	 * @param rnd random object
	 */
	public AlwaysChargingAlgorithm(SGDomain domain,Random rnd){
		this.rnd = rnd;
		this.domain = domain;
	}
	@Override
	public void observeOutcome(State s, JointAction jointAction, List<Map<String, Double>> jointReward, State sprime,
			boolean isTerminal) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void initiateState(State currentState) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void gameStarting() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public GroundedSGAgentAction getAction(State s) {
		//Returns random action if is at home and "nocharge" if is away 
		List<GroundedSGAgentAction> actions = SGAgentAction.getAllApplicableGroundedActionsFromActionList(s, this.worldAgentName, domain.getAgentActions());
		boolean isHome = s.getObject(this.worldAgentName).getStringValForAttribute(SmartGridConstants.ATT_AT_HOME).equals(SmartGridConstants.IS_AT_HOME);
		GroundedSGAgentAction charge = null;
		GroundedSGAgentAction noCharge = null;
		
		double battery = s.getObject(this.worldAgentName).getRealValForAttribute(SmartGridConstants.ATT_BATTERY);
		
		for(GroundedSGAgentAction act : actions){
			if(act.actionName().equals(SmartGridConstants.ACTION_CHARGE)){
				charge = act;
			}
			else{
				noCharge = act;
			}
		}
		
		if(isHome && battery != 100)
			return charge;
		
		return noCharge;
	}

	@Override
	public void gameTerminated() {
		// TODO Auto-generated method stub
		
	}

}
