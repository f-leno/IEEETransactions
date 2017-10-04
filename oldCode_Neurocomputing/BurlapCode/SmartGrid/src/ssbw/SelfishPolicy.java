/**
 * 
 */
package ssbw;

import java.util.List;

import burlap.behavior.policy.Policy;
import burlap.oomdp.core.AbstractGroundedAction;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.states.State;
import burlap.oomdp.statehashing.HashableState;
import burlap.oomdp.stochasticgames.agentactions.GroundedSGAgentAction;
import burlap.oomdp.stochasticgames.agentactions.SGAgentAction;

/**
 * @author Felipe Leno
 * Implements the selfish policy for SSBW
 */
public class SelfishPolicy extends Policy {

	protected Domain domain;
	protected SSBWAgent agent;


	protected GroundedSGAgentAction lastSelectedAction;
	protected int lastSelectedObjective;
	protected double lastSelectedW;
	/**
	 * Constructor that receives the domain description and the associated agent
	 * @param domain domain
	 * @param agent associated agent
	 */
	public SelfishPolicy(Domain domain, SSBWAgent agent){
		this.domain = domain;
		this.agent = agent;
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
		for(int objective=0;objective<agent.getNumberOfObjectives();objective++){
			for(GroundedSGAgentAction ga : actions){
				HashableState hs = this.agent.hashFactories.get(objective).hashState(s);
				double value = agent.getQHatValue(hs,ga,objective) * agent.getWeightValue(objective);
				//double value = agent.getQValue(hs, ga, objective) * agent.getWeightValue(objective);
				if(value>greater){
					greater = value;
					greaterAction = ga;
					greaterObjective = objective;
				}
			}
		}
		this.lastSelectedAction = greaterAction;
		this.lastSelectedObjective = greaterObjective;
		this.lastSelectedW = agent.getW(s,greaterObjective);
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
	 * Returns the objective of the last selected action
	 * @return the objective index
	 */
	public int getSelectedObjective() {
		return this.lastSelectedObjective;
	}

	/**
	 * Returns the W Value for the last selected objective
	 * @return the W-value
	 */
	public double getSelectedW() {
		return this.lastSelectedW;
	}

}
