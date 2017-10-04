/**
 * 
 */
package Multiobjective;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import burlap.behavior.stochasticgames.GameAnalysis;
import burlap.datastructures.HashedAggregator;
import burlap.debugtools.DPrint;
import burlap.oomdp.auxiliary.StateAbstraction;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.core.states.State;
import burlap.oomdp.stochasticgames.JointAction;
import burlap.oomdp.stochasticgames.JointActionModel;
import burlap.oomdp.stochasticgames.SGAgent;
import burlap.oomdp.stochasticgames.SGDomain;
import burlap.oomdp.stochasticgames.SGStateGenerator;
import burlap.oomdp.stochasticgames.World;
import burlap.oomdp.stochasticgames.WorldObserver;
import burlap.oomdp.stochasticgames.common.ConstantSGStateGenerator;

/**
 * @author Felipe Leno da Silva
 * Class that extends a default world to a Multiobjective one.
 *
 */
public class MOWorld extends World {
	protected List<HashedAggregator<String>>			agentCumulativeReward;
	protected MOJointReward jointRewardModel;
	//protected List <MOSGAgent>						agents;
	protected List<MOWorldObserver>				worldObservers;
	protected MOGameAnalysis						currentGameRecord;
	int numberObjectives;

	public MOWorld(SGDomain domain, MOJointReward jr, TerminalFunction tf, SGStateGenerator sg,
			StateAbstraction abstractionForAgents,int numberObjectives) {
		super(domain, null, tf, sg, abstractionForAgents);
		this.jointRewardModel = jr;
		this.numberObjectives = numberObjectives;
		init(domain,  domain.getJointActionModel(), jr, tf, sg, abstractionForAgents, numberObjectives);
	}

	public MOWorld(SGDomain domain, MOJointReward jr, TerminalFunction tf, SGStateGenerator sg,int numberObjectives) {
		super(domain,null,tf,sg);
		this.jointRewardModel = jr;
		this.numberObjectives = numberObjectives;
		init(domain,  domain.getJointActionModel(), jr, tf, sg, abstractionForAgents, numberObjectives);
	}

	public MOWorld(SGDomain domain, MOJointReward jr, TerminalFunction tf, State initialState,int numberObjectives) {
		super(domain,null, tf, initialState);
		this.jointRewardModel = jr;
		this.numberObjectives = numberObjectives;

		init(domain,  domain.getJointActionModel(), jr, tf, new ConstantSGStateGenerator(initialState), abstractionForAgents, numberObjectives);
	}

	protected void init(SGDomain domain, JointActionModel jam, MOJointReward jr, TerminalFunction tf, SGStateGenerator sg, StateAbstraction abstractionForAgents,int numberObjectives){
		//Initiates the world
		super.init(domain, jam, null, tf, sg, abstractionForAgents);
		//Sets the multiobjective reward function
		this.jointRewardModel = jr;
		this.agentCumulativeReward = new ArrayList<HashedAggregator<String>>();

		for(int i = 0;i<this.numberObjectives;i++){
			this.agentCumulativeReward.add(new HashedAggregator<String>());
		}
		this.numberObjectives = numberObjectives;
		this.worldObservers = new ArrayList<MOWorldObserver>();
	}


	/* 
	 * Adapted runStage to run with multiple objectives
	 */
	@Override
	public void runStage() {
		if(tf.isTerminal(currentState)){
			return ; //cannot continue this game
		}



		JointAction ja = new JointAction();
		State abstractedCurrent = abstractionForAgents.abstraction(currentState);
		for(SGAgent a : agents){
			ja.addAction(a.getAction(abstractedCurrent));
		}
		this.lastJointAction = ja;


		DPrint.cl(debugId, ja.toString());


		//now that we have the joint action, perform it
		State sp = worldModel.performJointAction(currentState, ja);
		State abstractedPrime = this.abstractionForAgents.abstraction(sp);
		List<Map<String, Double>> jointReward = jointRewardModel.reward(currentState, ja, sp);

		DPrint.cl(debugId, jointReward.toString());

		//index reward
		for(String aname : jointReward.get(0).keySet()){

			for(int objective = 0;objective<jointReward.size();objective++){
				double r = jointReward.get(objective).get(aname);
				agentCumulativeReward.get(objective).add(aname, r);

			}


		}

		//tell all the agents about it
		for(SGAgent sa : agents){
			MOSGAgent a = (MOSGAgent) sa;
			a.observeOutcome(abstractedCurrent, ja, jointReward, abstractedPrime, tf.isTerminal(sp));
		}

		if(this.worldObservers!=null){
			//tell observers
			for(MOWorldObserver o : this.worldObservers){
				o.observe(currentState, ja, jointReward, sp);
			}
		}

		//update the state
		currentState = sp;

		//record events
		
		if(this.isRecordingGame){
			this.currentGameRecord.recordTransitionTo(this.lastJointAction, this.currentState, jointReward);
		}
	}

	/**
	 * Run a game without stage limit
	 */
	public MOGameAnalysis runMOGame(){
		return this.runMOGame(Integer.MAX_VALUE);
	}
	
	/**
	 * runs a game obeying the max number of stages
	 * @param maxStages max number of stages
	 */
	public MOGameAnalysis runMOGame(int maxStages){
		for(SGAgent a : agents){
			a.gameStarting();
		}
		
		currentState = initialStateGenerator.generateState(agents);
		this.currentGameRecord = new MOGameAnalysis(currentState);
		this.isRecordingGame = true;
		int t = 0;

		for(MOWorldObserver wob : this.worldObservers){
			wob.gameStarting(this.currentState);
		}
		
		while(!tf.isTerminal(currentState) && t < maxStages){
			this.runStage();
			t++;
		}
		
		for(SGAgent a : agents){
			a.gameTerminated();
		}

		for(MOWorldObserver wob : this.worldObservers){
			wob.gameEnding(this.currentState);
		}
		
		DPrint.cl(debugId, currentState.getCompleteStateDescription());
		
		this.isRecordingGame = false;
		
		return this.currentGameRecord;
	}
	
	


	@Override
	public void addWorldObserver(WorldObserver ob) {
		throw new RuntimeException("Use addWorldObserver(MOWorldObserver");
	}

	public void addWorldObserver(MOWorldObserver ob) {
		this.worldObservers.add(ob);
	}
	@Override
	public GameAnalysis runGame() {
		throw new RuntimeException("Use the runMOGame method which returns a MOGameAnalysis");
	}

	@Override
	public GameAnalysis runGame(int maxStages) {
		throw new RuntimeException("Use the runMOGame method which returns a MOGameAnalysis");
	}

	/**
	 * Causes the world to set the current state to a state generated by the provided {@link SGStateGenerator} object.
	 */
	public void generateNewCurrentState(){
		currentState = initialStateGenerator.generateState(agents);
		//Agents are notified about their first state
		for(SGAgent sa : agents){
			MOSGAgent a = (MOSGAgent) sa;
			a.initiateState(currentState);
		}

			
	}



	/**
	 * @return the jointRewardModel
	 */
	public MOJointReward getJointRewardModel() {
		return jointRewardModel;
	}





}
