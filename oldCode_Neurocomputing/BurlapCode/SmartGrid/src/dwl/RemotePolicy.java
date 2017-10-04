/**
 * 
 */
package dwl;

import java.util.List;

import burlap.behavior.policy.Policy;
import burlap.oomdp.core.AbstractGroundedAction;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.states.State;
import burlap.oomdp.stochasticgames.agentactions.GroundedSGAgentAction;
import burlap.oomdp.stochasticgames.agentactions.SGAgentAction;

/**
 * @author Felipe Leno
 * Implementation of a Remote Policy
 */
public class RemotePolicy extends Policy {
	protected int numberNeighbors;
	protected Domain domain;
	protected DWLAgent agent;


	protected GroundedSGAgentAction lastSelectedAction;
	protected int lastSelectedObjective;
	protected double lastSelectedW;
	private int lastSelectedAgent;
	
	/**
	 * Constructor for the Remote policy
	 * @param domain domain description
	 * @param agent related agent
	 * @param numberNeighbors number of Neighbors
	 * 	 */
	public RemotePolicy(Domain domain, DWLAgent agent, int numberNeighbors){
		this.domain = domain;
		this.agent =agent;
		this.numberNeighbors = numberNeighbors;
	}
	/* (non-Javadoc)
	 * @see burlap.behavior.policy.Policy#getAction(burlap.oomdp.core.states.State)
	 */
	@Override
	public AbstractGroundedAction getAction(State s) {
		List<SGAgentAction> allActions = domain.getAgentActions();
		List<GroundedSGAgentAction> actions = SGAgentAction.getAllApplicableGroundedActionsFromActionList
				(s, agent.getAgentName(), allActions);

		double greater = Double.NEGATIVE_INFINITY;



		GroundedSGAgentAction greaterAction = null;
		int greaterObjective=0;
		int greaterNeigh = 0;
		for(int neig=0;neig<numberNeighbors;neig++){
			for(int objective=0;objective<agent.getNumberOfObjectives();objective++){
				for(GroundedSGAgentAction ga : actions){
					double value = agent.getWForNeigh(neig, objective);
					if(value>greater){
						greater = value;
						greaterAction = ga;
						greaterObjective = objective;
						greaterNeigh = neig;
					}
				}
			}
		}
		this.lastSelectedAction = greaterAction;
		this.lastSelectedObjective = greaterObjective;
		this.lastSelectedAgent = greaterNeigh;
		this.lastSelectedW = greater;
		return greaterAction;
	}

	/* (non-Javadoc)
	 * @see burlap.behavior.policy.Policy#getActionDistributionForState(burlap.oomdp.core.states.State)
	 */
	@Override
	public List<ActionProb> getActionDistributionForState(State s) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see burlap.behavior.policy.Policy#isStochastic()
	 */
	@Override
	public boolean isStochastic() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see burlap.behavior.policy.Policy#isDefinedFor(burlap.oomdp.core.states.State)
	 */
	@Override
	public boolean isDefinedFor(State s) {
		// TODO Auto-generated method stub
		return true;
	}
	
	/**
	 * Return W-value for the last selected action
	 * @return W-value
	 */
	public double getSelectedW() {
		return this.lastSelectedW;
	}

	/**
	 * Return the neighbor for the last selected agent
	 * @return the agent
	 */
	public int getSelectedAgent() {
		return this.lastSelectedAgent;
	}

	/**
	 * Return the last selected Objective
	 * @return the objective index
	 */
	public int getSelectedObjective() {
		return this.lastSelectedObjective;
	}



}
