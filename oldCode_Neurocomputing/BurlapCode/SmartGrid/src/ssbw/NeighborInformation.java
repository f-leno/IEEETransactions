/**
 * 
 */
package ssbw;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.management.RuntimeErrorException;

import burlap.behavior.valuefunction.QValue;
import burlap.behavior.valuefunction.ValueFunctionInitialization;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.states.State;
import burlap.oomdp.statehashing.HashableState;
import burlap.oomdp.stochasticgames.agentactions.GroundedSGAgentAction;
import burlap.oomdp.stochasticgames.agentactions.SGAgentAction;

/**
 * @author Felipe Leno da Silva
 * Neighbor informations for a given step
 */
public class NeighborInformation {


	protected Domain domain;
	
	//Current and next states for all objectives
	protected List<HashableState> currentStates;
	protected List<HashableState> nextStates;
	
	protected ValueFunctionInitialization	qInit;
	
	protected List<Map<HashableState, QLearningStateNode>>	qTable;
	protected List<Map<HashableState,Double>> wTable;
	String parentAgentName;

	public NeighborInformation(Domain domain,String parentAgentName, ValueFunctionInitialization qInit, int numberObjectives){
		this.currentStates = null;
		this.nextStates = null;
		this.domain = domain;
		this.qInit = qInit;
		this.parentAgentName = parentAgentName;
		this.qTable = new ArrayList<Map<HashableState, QLearningStateNode>>();
		this.wTable = new ArrayList<Map<HashableState,Double>>();
		
		for(int i=0;i<numberObjectives;i++){
			qTable.add(new Hashtable<HashableState, QLearningStateNode>());
			wTable.add(new Hashtable<HashableState, Double>());
		}
	}


	/**
	 * @return the currentState
	 */
	public List<HashableState> getCurrentState() {
		return currentStates;
	}

	/**
	 * @param currentState the currentState to set
	 */
	public void setCurrentState(List<HashableState> currentState) {
		this.currentStates = currentState;
	}

	/**
	 * @return the nextState
	 */
	public List<HashableState> getNextState() {
		return nextStates;
	}

	/**
	 * @param nextState the nextState to set
	 */
	public void setNextState(List<HashableState> nextState) {
		this.nextStates = nextState;
	}


	/**
	 * Returns the current w-value for this agent
	 * @param objective the objective
	 * @return the W-value
	 */
	public double getW(int objective) {
		if(!this.wTable.get(objective).containsKey(currentStates.get(objective))){
			wTable.get(objective).put(currentStates.get(objective), 0d);
		}
		return this.wTable.get(objective).get(currentStates.get(objective));
	}


	public QLearningStateNode getStateNode(State s, int objective) {
		QLearningStateNode node = this.qTable.get(objective).get(this.currentStates.get(objective));

		if(node == null){
			node = new QLearningStateNode(this.currentStates.get(objective));
			List<GroundedSGAgentAction> gas;
			gas = SGAgentAction.getAllApplicableGroundedActionsFromActionList(s, this.parentAgentName, domain.getAgentActions());

			if(gas.size() == 0){
				throw new RuntimeErrorException(new Error("No possible actions in this state, cannot continue Q-learning"));
			}
			for(GroundedSGAgentAction ga : gas){
				node.addQValue(ga, qInit.qValue(s, ga));
			}

			qTable.get(objective).put(this.currentStates.get(objective), node);
		}
		return node;
	}

	/**
	 * Return a Q-value according to the informed parameters
	 * @param parentState current state of the calling agent (for applicable actions definition)
	 * @param hs state
	 * @param a action
	 * @param objective objective
	 * @return the Qvalue
	 */
	public QValue getQ(State parentState, HashableState hs, GroundedSGAgentAction a, int objective) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Set a new W-value in the table
	 * @param objective objective 
	 * @param newW new value
	 */
	public void setW(int objective, double newW) {
		this.wTable.get(objective).put(this.currentStates.get(objective), newW);		
	}





}

