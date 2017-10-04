/**
 * 
 */
package Multiobjective;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import burlap.behavior.stochasticgames.GameAnalysis;
import burlap.oomdp.core.states.State;
import burlap.oomdp.stochasticgames.JointAction;

/**
 * @author Felipe Leno da Silva
 *	Multiobjective implementation of GameAnalysis
 */
public class MOGameAnalysis extends GameAnalysis {
	
	
	
	
	
	
	public MOGameAnalysis() {
		super();
	}

	public MOGameAnalysis(State initialState) {
		super(initialState);
	}

	/* (non-Javadoc)
	 * @see burlap.behavior.stochasticgames.GameAnalysis#initializeDatastructures()
	 */
	@Override
	protected void initializeDatastructures() {
		this.states = new ArrayList<State>();
		this.jointActions = new ArrayList<JointAction>();
		this.jointRewards = new ArrayList<List<Map<String,Double>>>();
		this.agentsInvolvedInGame = new HashSet<String>();
	}

	public List<List<Map<String, Double>>>			jointRewards;
	
	/**
	 * Returns the joint reward received in time step t. Note that rewards are always returned in the time step after the joint action that generated them; therefore,
	 * this method is undefined for t = 0. Instead, the first time step with a reward is t=1 which refers to the joint reward received after the first joint
	 * action is taken in the initial state.
	 * @param t the time step
	 * @return the joint reward received at time step t
	 */
	public List<Map<String, Double>> getMOJointReward(int t){
		if(t >= this.states.size()){
			throw new RuntimeException("This game only has " + this.jointRewards.size() + " joint rewards recoreded; cannot return joint reward at time step " + t);
		}
		return this.jointRewards.get(t-1);
	}
	
	/**
	 * Records a transition from the last recorded state in this object using the specififed joint action to the specified next state and with the specified joint reward
	 * being recieved as a result.
	 * @param jointAction the joint action taken in the last recorded state in this object
	 * @param nextState the next state to which the agents transition
	 * @param jointReward the joint reward received for the transiton
	 */
	public void recordTransitionTo(JointAction jointAction, State nextState,  List<Map<String, Double>> jointReward){
		this.states.add(nextState);
		this.jointActions.add(jointAction);
		this.jointRewards.add(jointReward);
		for(String agent : jointAction.getAgentNames()){
			this.agentsInvolvedInGame.add(agent);
		}
	}
	
	public Map<String, Double> getJointReward(int t){
		throw new RuntimeException("In MO domains, Multiple Objectives should be specified");
	}
	
	/**
	 * Returns the joint reward sequence list object
	 * @return the joint reward sequence list object
	 */
	public List<List<Map<String, Double>>> getMOJointRewards() {
		return jointRewards;
	}
	
	public List<Map<String, Double>> getJointRewards(){
		throw new RuntimeException("In MO domains, Multiple Objectives should be specified");
	}
}
