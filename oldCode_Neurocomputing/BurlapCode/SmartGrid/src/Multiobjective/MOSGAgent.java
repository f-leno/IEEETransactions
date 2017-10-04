/**
 * 
 */
package Multiobjective;

import java.util.List;
import java.util.Map;

import burlap.oomdp.core.states.State;
import burlap.oomdp.stochasticgames.JointAction;
import burlap.oomdp.stochasticgames.SGAgent;

/**
 * @author Felipe Leno da Silva
 *	Extension of SGAgent thath allows multiple objectives
 */
public abstract class MOSGAgent extends SGAgent {


/**
 * This method should not be used in Multiobjective Worlds
 */
	public void observeOutcome(State s, JointAction jointAction, Map<String, Double> jointReward, State sprime,
			boolean isTerminal) {
		throw new RuntimeException("ObserveOutcome with single reward function should not be used in Multiobjective Worlds");
		
	}

	protected MOJointReward			internalRewardFunction;
	
	/**
	 * This method is called by the world when every agent in the world has taken their action. It conveys the result of
	 * the joint action. Here, multiple rewards are received.
	 * @param s the state in which the last action of each agent was taken
	 * @param jointAction the joint action of all agents in the world
	 * @param jointReward the joint reward of all agents in the world
	 * @param sprime the next state to which the agent transitioned
	 * @param isTerminal whether the new state is a terminal state
	 */
	public abstract void observeOutcome(State s, JointAction jointAction, List<Map<String, Double>> jointReward, State sprime,
			boolean isTerminal);

	/**
	 * Agents receive their first state, and can initiate data structures if they need to
	 * @param currentState state
	 */
	public abstract void initiateState(State currentState);
	
	



}
