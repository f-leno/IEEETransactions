package ssbw;

import java.util.ArrayList;
import java.util.List;

import burlap.behavior.valuefunction.QValue;
import burlap.oomdp.statehashing.HashableState;
import burlap.oomdp.stochasticgames.agentactions.GroundedSGAgentAction;

/**
 * 
 * 
 * This class is used to store the associated {@link burlap.behavior.valuefunction.QValue} objects for a given hashed sated.
 * This class was changed to store GroundedSGAgentAction instead of GroundedActions
 * @author James Macglasham
 *
 */
public class QLearningStateNode {

	/**
	 * A hashed state entry for which Q-value will be stored.
	 */
	public HashableState s;

	/**
	 * The Q-values for this object's state.
	 */
	public List<QValue>				qEntry;


	/**
	 * Creates a new object for the given hashed state. The list of {@link burlap.behavior.valuefunction.QValue} objects is initialized to be empty.
	 * @param s the hashed state for which to associate Q-values
	 */
	public QLearningStateNode(HashableState s) {
		this.s = s;
		qEntry = new ArrayList<QValue>();
	}


	/**
	 * Adds a Q-value to this state with the given numeric Q-value.
	 * @param a the action this Q-value is fore
	 * @param q the numeric Q-value
	 */
	public void addQValue(GroundedSGAgentAction a, double q){
		QValue qv = new QValue(s.s, a, q);
		qEntry.add(qv);
	}


}