/**
 * 
 */
package Multiobjective;

import java.util.List;
import java.util.Map;

import burlap.oomdp.core.states.State;
import burlap.oomdp.stochasticgames.JointAction;

/**
 * @author Felipe Leno da Silva
 * This interface defines the method needed to return multiple reward received by each agent.
 */
public interface MOJointReward {

	/**
	 * Returns a list of rewards received by each agent specified in the joint action. The returned
	 * result is a Map from agent names to the reward that they received. Each position of the list corresponds to a objective.
	 * @param s that state in which the joint action was taken.
	 * @param ja the joint action taken.
	 * @param sp the resulting state from taking the joint action
	 * @return a Map from agent names to the reward that they received.
	 */
	public List<Map<String, Double>> reward(State s, JointAction ja, State sp);
}
