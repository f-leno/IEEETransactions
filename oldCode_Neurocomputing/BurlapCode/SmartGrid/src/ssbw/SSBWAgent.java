/**
 * 
 */
package ssbw;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.management.RuntimeErrorException;

import Multiobjective.MOSGAgent;
import burlap.behavior.valuefunction.QValue;
import burlap.behavior.valuefunction.ValueFunctionInitialization;
import burlap.behavior.valuefunction.ValueFunctionInitialization.ConstantValueFunctionInitialization;
import burlap.oomdp.core.states.State;
import burlap.oomdp.statehashing.HashableState;
import burlap.oomdp.statehashing.HashableStateFactory;
import burlap.oomdp.stochasticgames.JointAction;
import burlap.oomdp.stochasticgames.SGDomain;
import burlap.oomdp.stochasticgames.agentactions.GroundedSGAgentAction;
import burlap.oomdp.stochasticgames.agentactions.SGAgentAction;
import domain.SmartGridConstants;

/**
 * @author Felipe Leno da Silva
 *
 *	Implementation of the Scalarized Selfish-Benevolent W-Learning (SS-BW) algorithm
 */
public class SSBWAgent extends MOSGAgent {


	//Q-tables (one for each objective)
	protected List<Map<HashableState, QLearningStateNode>>	qTable;
	//W-tables
	protected List<Map<HashableState,Double>>   wTable;

	//State visit count
	protected Map<HashableState,Integer> stateCount;

	//List of neighbors
	protected List<SSBWAgent> neighbors;

	//Weight Vector for each objective
	protected List<Double> weightVector;

	//Cooperation Rate
	protected double cooperationRate;

	//Discount Rate
	protected double gamma;

	//Learning rate
	protected double alpha;

	//Epsilon for "Should explore?"
	protected double epsilon;

	//Selfish and Benevolent Policies
	protected SelfishPolicy selfishPolicy;
	protected BenevolentPolicy benevolentPolicy;

	//State factory pra gerar hash
	protected List<HashableStateFactory> hashFactories;


	//Defines if the agent is exploring or no
	protected boolean exploring;
	//Information for W-value and Q-value updates
	protected int chosenAgentPolicy; //Agent related to the chosen policy
	protected int chosenObjectivePolicy; //Objective related to chosen policy
	protected GroundedSGAgentAction chosenAction; //Last chosen action


	protected ValueFunctionInitialization	qInit;

	protected State currentState;


	//Information to be updated
	protected List<NeighborInformation> neighborValues;

	//Random class for experiment reproducibility
	Random rnd;

	//Map AgentName X agentIndex
	protected Map<String,Integer> neighborNames;


	public SSBWAgent(SGDomain domain,int numberOfObjectives,double alpha,double gamma,
			double epsilon, List<Double> weightVector,double cooperationRate,double qInitialValue,Random rnd){
		this.domain = domain;
		qTable = new ArrayList<Map<HashableState,QLearningStateNode>>();
		wTable = new ArrayList<Map<HashableState,Double>>();

		for(int i=0; i<numberOfObjectives; i++){
			qTable.add(new HashMap<HashableState,QLearningStateNode>());
			wTable.add(new HashMap<HashableState,Double>());

		}
		this.qInit = new ConstantValueFunctionInitialization(qInitialValue);
		this.alpha = alpha;
		this.gamma = gamma;
		this.epsilon = epsilon;
		this.weightVector = weightVector;
		this.cooperationRate = cooperationRate;
		this.hashFactories = null;
		this.selfishPolicy = new SelfishPolicy(domain, this);
		this.rnd = rnd;
		this.exploring = true;
		this.neighborValues = new ArrayList<NeighborInformation>();
		this.neighborNames = new Hashtable<String,Integer>();
		//Benevolent Policy is built when the neighbours are received


	}

	/**
	 * Sets the hashFactories for all objectives. This method must be used before the world begins the execution
	 * @param hashFactories
	 */
	public void setHashableStateFactories(List<HashableStateFactory> hashFactories){
		this.hashFactories = hashFactories;
	}

	/* (non-Javadoc)
	 * @see Multiobjective.MOSGAgent#observeOutcome(burlap.oomdp.core.states.State, burlap.oomdp.stochasticgames.JointAction, java.util.List, burlap.oomdp.core.states.State, boolean)
	 */
	@Override
	public void observeOutcome(State s, JointAction jointAction, List<Map<String, Double>> jointReward, State sprime,
			boolean isTerminal) {
		//visitedS(s);
		List<HashableState> hss = new ArrayList<HashableState>(); 
		List<HashableState> hssprime = new ArrayList<HashableState>();
		for(int i=0;i<jointReward.size();i++){
			hss.add(this.hashFactories.get(i).hashState(s));
			hssprime.add(this.hashFactories.get(i).hashState(sprime));
		}

		GroundedSGAgentAction a = jointAction.action(this.worldAgentName);
		notifyNeighbors(hss,hssprime,jointReward);

		//Only update Q and W-values when exploring
		if(this.isExploring()){
			//Q-value and W-value updates
			for(int objective = 0; objective < this.getNumberOfObjectives(); objective++){
				QValue q = this.getQ(hss.get(objective), a, objective);

				double r = jointReward.get(objective).get(this.worldAgentName);
				double maxQ = getMaxQ(hssprime.get(objective),objective);
				//Q-Value update
				q.q = (1-this.alpha) * q.q + this.alpha * (r + this.gamma * maxQ);
				//Check if the W-value should be updated
				if(this.chosenAgentPolicy >=0 || objective != this.chosenObjectivePolicy){
					//double qNormalized = this.getQHatValue(hss.get(objective), a, objective);
					//double maxQNormalized = this.getMaxQHat(hssprime.get(objective),objective);
					double lastW = this.getW(hss.get(objective), objective);
					//double newW = (1-this.alpha) * lastW + this.alpha * (qNormalized - (r + this.gamma *maxQNormalized));
					double newW = (1-this.alpha) * lastW + this.alpha * (q.q - (r + this.gamma *maxQ));
					this.wTable.get(objective).put(hss.get(objective), newW);
				}
			}		
		}
		//All W and Q values from neighbor policies are updated on the "observeNotification" method, in which communications are received
		//The current step has just ended. Now the next state is updated on the information about neighbor agents.
		updateNeighborInformation();

	}

	/**
	 * Processes the state transition, the current state for all agents has change
	 */
	private void updateNeighborInformation() {
		for (NeighborInformation info : this.neighborValues){
			info.setCurrentState(info.getNextState());
			info.setNextState(null);

		}

	}

	/**
	 * Notify all friendly neighbors about the current state, next state and reward. the observeNeighbor() method is executed
	 * for all friendly agents
	 * @param hs the current state
	 * @param hsprime the next state
	 * @param jointReward information about the reward
	 */
	private void notifyNeighbors(List<HashableState> hs, List<HashableState> hsprime, List<Map<String, Double>> jointReward) {
		for(SSBWAgent neigh : this.neighbors){
			neigh.observeNeighbor(this.worldAgentName,hs,hsprime,jointReward);
		}

	}

	/**
	 * Receives information about a friendly agent to updates Q and W tables. 
	 * @param agentName agent notifying the data
	 * @param hss agent current state
	 * @param hssprime agent next state
	 * @param jointReward reward information
	 */
	public void observeNeighbor(String agentName, List<HashableState> hss, List<HashableState> hssprime,
			List<Map<String, Double>> jointReward) {


		int agentIndex = getNeighborIndex(agentName);
		NeighborInformation info = this.neighborValues.get(agentIndex);

		GroundedSGAgentAction a = this.chosenAction;

		//Information update
		info.setCurrentState(hss);
		info.setNextState(hssprime);



		for(int objective = 0; objective< jointReward.size(); objective++){
			QValue q = this.getQForNeigh(agentIndex, a, objective);

			double r = jointReward.get(objective).get(agentName);

			double maxQ = getMaxQForNeigh(agentIndex,hssprime.get(objective),objective);
			//Q-Value update
			q.q = (1-this.alpha) * q.q + this.alpha * (r + this.gamma * maxQ);
			//Check if the W-value should be updated
			if(this.chosenAgentPolicy !=agentIndex || objective != this.chosenObjectivePolicy){		
				//double qNormalized = this.getQHatValueForNeigh(hss.get(objective),agentIndex, a, objective);
				//double maxQNormalized = this.getMaxQHatForNeigh(hssprime.get(objective),agentIndex,objective);
				double lastW = this.getWForNeigh(agentIndex, objective);
				double newW = (1-this.alpha) * lastW + this.alpha * (q.q - (r + this.gamma *maxQ));
				//double newW = (1-this.alpha) * lastW + this.alpha * (qNormalized - (r + this.gamma *maxQNormalized));
				info.setW(objective,newW);
			}
		}		
	}


	/**
	 * Returns the index of a given neighbor
	 * @param agentName Neighbor Name
	 * @return the index
	 */
	private int getNeighborIndex(String agentName) {
		return this.neighborNames.get(agentName);
	}








	/**
	 * Returns the maximum Q-value in the hashed stated.
	 * @param s the state for which to get he maximum Q-value;
	 * @return the maximum Q-value in the hashed stated.
	 */
	protected double getMaxQ(HashableState s,int objective){
		List <QValue> qs = this.getQs(s,objective);
		double max = Double.NEGATIVE_INFINITY;
		for(QValue q : qs){
			if(q.q > max){
				max = q.q;
			}
		}
		return max;
	}
	
	/**
	 * Returns the maximum Q-value in the hashed stated.
	 * @param s the state for which to get he maximum Q-value;
	 * @return the maximum Q-value in the hashed stated.
	 */
	protected double getMaxQHat(HashableState s,int objective){
		List <QValue> qs = this.getQsHat(s,objective);
		double max = Double.NEGATIVE_INFINITY;
		for(QValue q : qs){
			if(q.q > max){
				max = q.q;
			}
		}
		return max;
	}

	/**
	 * Returns the possible Q-values for a given hashed stated.
	 * @param s the hashed state for which to get the Q-values.
	 * @return the possible Q-values for a given hashed stated.
	 */
	protected List<QValue> getQs(HashableState s,int objective) {
		QLearningStateNode node = this.getStateNode(s,objective);
		return node.qEntry;
	}

	/**
	 * Returns the possible Q-values for a given hashed stated.
	 * @param s the hashed state for which to get the Q-values.
	 * @return the possible Q-values for a given hashed stated.
	 */
	protected List<QValue> getQsForNeigh(int neighbour,HashableState s,int objective) {
		QLearningStateNode node = this.neighborValues.get(neighbour).getStateNode(s,objective);
		return node.qEntry;
	}

	/**
	 * Sum one visit on the current state
	 * @param s the state
	 */
	/*private void visitedS(State s) {
		HashableState hs = this.hashFactory.hashState(s);
		this.stateCount.put(hs, stateCount.get(hs) + 1);
	}*/


	/* (non-Javadoc)
	 * @see burlap.oomdp.stochasticgames.SGAgent#getAction(burlap.oomdp.core.states.State)
	 */
	@Override
	public GroundedSGAgentAction getAction(State s) {
		this.currentState = s;
		
		boolean atHome = s.getObject(worldAgentName).getStringValForAttribute(SmartGridConstants.ATT_AT_HOME).equals(SmartGridConstants.IS_AT_HOME);
		double battery = s.getObject(worldAgentName).getRealValForAttribute(SmartGridConstants.ATT_BATTERY);
		
		//Check if can use other action than noCharge
		if(!atHome){
			List<SGAgentAction> noChargeAct = new ArrayList<SGAgentAction>(); noChargeAct.add(domain.getSingleAction(SmartGridConstants.ACTION_NOT_CHARGE));
			GroundedSGAgentAction noCharge = SGAgentAction.getAllApplicableGroundedActionsFromActionList
					(s, this.worldAgentName, noChargeAct).get(0);
			
			return noCharge;
		}
		
		//CHeck if a random action should be chosen
		if(this.exploring){
			if(checkAleatory()){
				this.chosenAction =  getBoltzmannAction(s);
				this.chosenAgentPolicy = -1;
				this.chosenObjectivePolicy = -1;
				return this.chosenAction;
			}
		}


		//If not, chose selfish and benevolent actions.

		GroundedSGAgentAction selfishAction = (GroundedSGAgentAction) selfishPolicy.getAction(s);
		GroundedSGAgentAction benevolentAction = (GroundedSGAgentAction) benevolentPolicy.getAction(s);

		double wSelfish = selfishPolicy.getSelectedW();
		double wBenevolent = benevolentPolicy.getSelectedW();

		//Cooperation Criteria
		if(wSelfish > wBenevolent * this.cooperationRate){
			this.chosenAgentPolicy = -1; //Local agent
			this.chosenObjectivePolicy = selfishPolicy.getSelectedObjective();
			this.chosenAction = selfishAction;
			return selfishAction;
		}

		this.chosenAgentPolicy = benevolentPolicy.getSelectedAgent();
		this.chosenObjectivePolicy = benevolentPolicy.getSelectedObjective();
		this.chosenAction = benevolentAction;
		return benevolentAction;

	}


	/**
	 * Calculates the action to be chosen randomly as described on the paper
	 * @return random action
	 */
	private GroundedSGAgentAction getBoltzmannAction(State s) {
		List<SGAgentAction> allActions = domain.getAgentActions();
		List<GroundedSGAgentAction> groundActions = SGAgentAction.getAllApplicableGroundedActionsFromActionList(s, this.worldAgentName, allActions);
		GroundedSGAgentAction returnedAction = null;

		return groundActions.get(this.rnd.nextInt(groundActions.size()));

		/*List<Double> probs = new ArrayList<Double>();
		for(GroundedSGAgentAction gact : groundActions){
			//Calculates the probability of action gact being selected
			//probs.add(calculateActionProb(s,gact)); boltzmann ist not yet implemented
			probs.add( 1d/groundActions.size());
		}
		//Avoiding problems when rnd.nextDouble()==0
		double sorted = rnd.nextDouble() + 0.000001;

		double sum = 0;int i = -1;
		while(sum < sorted){
			sum += probs.get(i+1);
			i++;
		}
		returnedAction = groundActions.get(i);


		return returnedAction;	*/
	}


	/**
	 * Calculates the probability of a given action being chosen in the Boltzmann exploration
	 * @param s current state
	 * @param gact action for which the probability must be calculateed
	 * @return the probability
	 */
	/*private Double calculateActionProb(State s, GroundedSGAgentAction gact) {
		double sig = 0.000001; //Variable to avoid division by 0

		double localQhat = calcQhat(s,gact);

		//List<Double> qHats = getAllQHat(s,gact);


		//----------------------------------
		// Temperature calculation
		//----------------------------------
		double higher = Double.NEGATIVE_INFINITY, smaller = Double.POSITIVE_INFINITY;

		for(double qHat : qHats){
			if(higher < qHat)
				higher = qHat;
			if(smaller > qHat)
				smaller = qHat;
		}
		double temperature = Math.log(this.getVisits(s))/(higher-smaller);
		//--------------------------------------

		double upPart = Math.pow(Math.E,localQhat/temperature);


		double sumHats =0;


		for(double qHat : qHats){
			sumHats += Math.pow(Math.E, qHat/temperature);
		}

		//Sig is used to avoid divisions by 0
		return (upPart+sig)/(sumHats+sig);



	}
	 */
	/**
	 * Calculate the Qhat value for a given state and action (see article)
	 * @param s current local state
	 * @param gact action
	 * @return the value
	 */
	/*private double calcQhat(State s, GroundedSGAgentAction gact) {

		//List<Double> getAllQValuesFromAllQTables(s);

		HashableState hs = this.hashFactory.hashState(s);

	}*/

	/**
	 * Return the number of visits of the current state
	 * @param s the state
	 * @return the number of visits
	 */
	/*private double getVisits(State s) {
		HashableState hs = hashFactory.hashState(s);
		return this.stateCount.get(hs);
	}*/

	/**
	 * Returns if a random action should be chosen
	 * @return
	 */
	private boolean checkAleatory() {
		double random = rnd.nextDouble();

		return (random < this.epsilon);		
	}


	/* (non-Javadoc)
	 * @see burlap.oomdp.stochasticgames.SGAgent#gameTerminated()
	 */
	@Override
	public void gameTerminated() {
		//No procedure to perform
	}

	/* (non-Javadoc)
	 * @see burlap.oomdp.stochasticgames.SGAgent#gameStarting()
	 */
	@Override
	public void gameStarting() {
		//No procedure to perform

	}

	/**
	 * Set the agents with whom this agent wants to cooperate
	 * @param neighbors the set of friendly agents
	 */
	public void setNeighbors(List<SSBWAgent> neighbors){
		this.neighbors = neighbors;
		this.benevolentPolicy = new BenevolentPolicy(domain, this, neighbors.size());
		for(int n=0;n<neighbors.size();n++){
			this.neighborNames.put(neighbors.get(n).worldAgentName, n);
			neighborValues.add(new NeighborInformation(domain, this.worldAgentName, qInit, qTable.size()));
		}

	}





	/**
	 * Defines the value for "Exploring"
	 * @param exploring value
	 */
	public void setExploring(boolean exploring){
		this.exploring = exploring;
	}

	/**
	 * Returns if the agent is exploring
	 * @return
	 */
	public boolean isExploring(){
		return this.exploring;
	}

	/**
	 * Calculates the Q hat value for a given (s,action) tuple
	 * @param s current state
	 * @param ga action
	 * @return Q hat value
	 */
	public double getQHatValue(HashableState hs, GroundedSGAgentAction ga, int objective) {

		List<SGAgentAction> allActions = domain.getAgentActions();
		List<GroundedSGAgentAction> actions = SGAgentAction.getAllApplicableGroundedActionsFromActionList
				(hs.s, this.getAgentName(), allActions);

		double sig = 0.000001;
		double sum = 0;

		for(GroundedSGAgentAction a : actions){
			sum += this.getQValue(hs, a, objective);
		}
		double q = this.getQValue(hs, ga, objective);

		return (q+sig)/(sum+sig);


	}
	
	/**
	 * Returns the possible Q-values for a given hashed stated.
	 * @param s the hashed state for which to get the Q-values.
	 * @return the possible Q-values for a given hashed stated.
	 */
	protected List<QValue> getQsHat(HashableState hs,int objective) {
		List<SGAgentAction> allActions = domain.getAgentActions();
		List<GroundedSGAgentAction> actions = SGAgentAction.getAllApplicableGroundedActionsFromActionList
				(hs.s, this.getAgentName(), allActions);

		double sig = 0.000001;
		double sum = 0;

		for(GroundedSGAgentAction a : actions){
			sum += this.getQValue(hs, a, objective);
		}
		List<QValue> list = new ArrayList<QValue>();
		
		QLearningStateNode node = this.getStateNode(hs,objective);
		
		for(QValue q : node.qEntry)
		{
			QValue aNode = new QValue(q);
			aNode.q = (aNode.q+sig)/(sum+sig);
			list.add(aNode);
		}

		return list;

	}
	/**
	 * Calculates the Qhat value for a given (neighbour,action) tuple
	 * @param neighbor neighbor index
	 * @param state current local state
	 * @param ga action
	 * @param objective objective
	 * @return Qhat value
	 */
	public double getQHatValueForNeigh(State s, int neighbor, GroundedSGAgentAction ga, int objective) {
		List<SGAgentAction> allActions = domain.getAgentActions();
		List<GroundedSGAgentAction> actions = SGAgentAction.getAllApplicableGroundedActionsFromActionList
				(s, this.getAgentName(), allActions);

		double sig = 0.000001;
		double sum = 0;

		for(GroundedSGAgentAction a : actions){
			sum += this.getQValueForNeigh(neighbor, a, objective);
		}
		double q = this.getQValueForNeigh(neighbor, ga, objective);

		return (q+sig)/(sum+sig);
	}
	/**
	 * Calculates the max Qhat value for a given (neighbour,action) tuple
	 * @param neighbor neighbor index
	 * @param state current local state
	 * @param ga action
	 * @param objective objective
	 * @return Qhat value
	 */
	protected double getMaxQHatForNeigh(HashableState s,int neighbor,int objective){
		List <QValue> qs = this.getQsHatForNeighbor(s,neighbor,objective);
		double max = Double.NEGATIVE_INFINITY;
		for(QValue q : qs){
			if(q.q > max){
				max = q.q;
			}
		}
		return max;
	}
	
	
	/**
	 * Returns the possible Q-values for a given hashed stated.
	 * @param s the hashed state for which to get the Q-values.
	 * @return the possible Q-values for a given hashed stated.
	 */
	protected List<QValue> getQsHatForNeighbor(HashableState hs,int neighbor,int objective) {
		List<SGAgentAction> allActions = domain.getAgentActions();
		List<GroundedSGAgentAction> actions = SGAgentAction.getAllApplicableGroundedActionsFromActionList
				(hs.s, this.getAgentName(), allActions);

		double sig = 0.000001;
		double sum = 0;

		List<QValue> list = new ArrayList<QValue>();
		for(GroundedSGAgentAction a : actions){
			QValue q = this.getQForNeigh(neighbor, a, objective);
			list.add(new QValue(q));
			sum += q.q;
		}
		
		for(QValue q : list)
		{
			q.q = (q.q+sig)/(sum+sig);
		}

		return list;

	}

	/**
	 * Returns the Q value for a given state and action
	 * @param hs state
	 * @param a action
	 * @return Q value
	 */
	public double getQValue(HashableState hs, GroundedSGAgentAction a, int objective) {
		QValue q = getQ(hs,a,objective);
		return q.q;

	}
	/**
	 * Returns a Q node to a specified (state,action,objective)
	 * @param hs state
	 * @param a action
	 * @param objective objective
	 * @return QValue
	 */
	private QValue getQ(HashableState hs, GroundedSGAgentAction a, int objective) {
		QLearningStateNode node = this.getStateNode(hs,objective);

		for(QValue qv : node.qEntry){
			if(qv.a.equals(a)){
				return qv;
			}
		}

		return null; //no action for this state indexed
	}

	/**
	 * Returns the Q value for a given state and action
	 * @param hs state
	 * @param a action
	 * @return Q value
	 */
	public double getQValueForNeigh(int neighbor, GroundedSGAgentAction a, int objective) {
		QValue q = getQForNeigh(neighbor,a,objective);
		return q.q;
	}

	/**
	 * Returns a Q node to a specified (neighbor,action,objective)
	 * @param neighbor neighbor
	 * @param a action
	 * @param objective objective
	 * @return QValue
	 */
	public QValue getQForNeigh(int neighbor, GroundedSGAgentAction a, int objective) {
		QLearningStateNode node = this.neighborValues.get(neighbor).getStateNode(this.currentState,objective);

		for(QValue qv : node.qEntry){
			if(qv.a.equals(a)){
				return qv;
			}
		}

		return null; //no action for this state indexed
	}

	/**
	 * Returns the maximum Q-value in the hashed stated.
	 * @param s the state for which to get he maximum Q-value;
	 * @return the maximum Q-value in the hashed stated.
	 */
	protected double getMaxQForNeigh(int neighbor, HashableState hs,int objective){
		List <QValue> qs = this.getQsForNeigh(neighbor,hs,objective);
		double max = Double.NEGATIVE_INFINITY;
		for(QValue q : qs){
			if(q.q > max){
				max = q.q;
			}
		}
		return max;
	}

	/**
	 * Returns the {@link QLearningStateNode} object stored for the given hashed state. If no {@link QLearningStateNode} object.
	 * is stored, then it is created and has its Q-value initialize using this objects {@link burlap.behavior.valuefunction.ValueFunctionInitialization} data member.
	 * @param s the hashed state for which to get the {@link QLearningStateNode} object
	 * @return the {@link QLearningStateNode} object stored for the given hashed state. If no {@link QLearningStateNode} object.
	 */
	protected QLearningStateNode getStateNode(HashableState s,int objective){

		QLearningStateNode node = this.qTable.get(objective).get(s);

		if(node == null){
			node = new QLearningStateNode(s);
			List<GroundedSGAgentAction> gas;
			gas = SGAgentAction.getAllApplicableGroundedActionsFromActionList(s.s, this.worldAgentName, domain.getAgentActions());

			if(gas.size() == 0){
				throw new RuntimeErrorException(new Error("No possible actions in this state, cannot continue Q-learning"));
			}
			for(GroundedSGAgentAction ga : gas){
				node.addQValue(ga, qInit.qValue(s.s, ga));
			}

			qTable.get(objective).put(s, node);
		}
		return node;
	}


	public int getNumberOfObjectives() {
		return this.qTable.size();
	}

	/**
	 * Returns the weight value for a given objective
	 * @param objective number of objective
	 * @return weight value
	 */
	public double getWeightValue(int objective) {
		return this.weightVector.get(objective);
	}

	/**
	 * Returns W-Value
	 * @param s state
	 * @param objective objective
	 * @return the W-value
	 */
	public double getW(State s, int objective) {
		HashableState hs = this.hashFactories.get(objective).hashState(s);
		if(!wTable.get(objective).containsKey(hs)){
			wTable.get(objective).put(hs, 0d);
		}
		return this.wTable.get(objective).get(hs);
	}
	/**
	 * Reeturns the W-value for a given neighbor and objective
	 * @param neighbor neighbor index
	 * @param objective objective index
	 * @return the W-value
	 */
	public double getWForNeigh(int neighbor, int objective) {
		NeighborInformation information = this.neighborValues.get(neighbor);

		return information.getW(objective);
	}

	/**
	 * Agents communicate to neighbors their first state
	 */
	public void initiateState(State currentState) {
		List<HashableState> hs = new ArrayList<HashableState>();

		for(int i=0;i<hashFactories.size();i++){
			hs.add(this.hashFactories.get(i).hashState(currentState));
		}
		notifyNeighborsFirstState(hs);

	}

	/**
	 * Notify Neighbors about first State
	 * @param hs states for all objectives
	 */
	private void notifyNeighborsFirstState(List<HashableState> hs) {
		for(SSBWAgent neigh : this.neighbors){
			neigh.observeNeighbor(this.worldAgentName,hs);
		}

	}
	/**
	 * Receive information about an agent current state on the initial stae
	 * @param agentName agent communicating
	 * @param hss current states for all objectives
	 */
	public void observeNeighbor(String agentName, List<HashableState> hss) {
		int agentIndex = getNeighborIndex(agentName);

		for(int objective = 0; objective< hss.size(); objective++){
			this.neighborValues.get(agentIndex).setCurrentState(hss);	

		}		
	}



}



