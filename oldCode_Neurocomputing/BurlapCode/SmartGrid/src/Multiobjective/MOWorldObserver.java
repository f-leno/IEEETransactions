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
 * WorldObserve for MultipleObjectives
 */
public interface MOWorldObserver {

	/**
	 * This method is called whenever an interaction in the world occurs.
	 * @param s the previous state of the world
	 * @param ja the joint action taken in the world
	 * @param reward the joint reward received by the agents
	 * @param sp the next state of the world
	 */
	public void observe(State s, JointAction ja, List<Map<String, Double>> reward, State sp);
	
	/**
	 * This method is called whenever a game in a world ends.
	 * @param s the final state of the world when it ends.
	 */
	public void gameEnding(State s);
	

	/**
	 * This method is called whenever a new game in a world is starting.
	 * @param s the state in which the world is starting.
	 */
	public void gameStarting(State s);	
	
	
}
