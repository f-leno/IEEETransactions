/**
 * 
 */
package domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Multiobjective.MOJointReward;
import burlap.oomdp.core.objects.ObjectInstance;
import burlap.oomdp.core.states.State;
import burlap.oomdp.stochasticgames.JointAction;
import burlap.oomdp.stochasticgames.agentactions.GroundedSGAgentAction;

/**
 * @author Felipe Leno da Silva
 *
 * Reward Functions for Smart Grid domain
 */
public class SmartGridMORewardFunction implements MOJointReward {

	protected int underLoadThreshold;
	protected int overLoadThreshold;
	/**
	 * Constructor that need to specify thresholds for the third reward function
	 * @param underLoadThreshold lower bound for transformer desired load (in number of EVs)
	 * @param overLoadThreshold upṕer bound for transformer desired load (in number of EVs)
	 */
	public SmartGridMORewardFunction(int underLoadThreshold,int overLoadThreshold){
		this.underLoadThreshold = underLoadThreshold;
		this.overLoadThreshold = overLoadThreshold;
	}
	/* (non-Javadoc)
	 * @see Multiobjective.MOJointReward#reward(burlap.oomdp.core.states.State, burlap.oomdp.stochasticgames.JointAction, burlap.oomdp.core.states.State)
	 */
	@Override
	public List<Map<String, Double>> reward(State s, JointAction ja, State sp) {
		List<Map<String, Double>> rewards = new ArrayList<Map<String, Double>>();

		rewards.add(batteryJourneyFunction(s,ja,sp));
		rewards.add(batteryLevelFunction(s,ja,sp));
		rewards.add(transformerFunction(s,ja,sp));

		return rewards;
	}

	/**
	 * Process the reward for Reward Function 3
	 * @param s state
	 * @param ja joint action
	 * @param sp Resulting state
	 * @return the reward for the third function
	 */
	protected Map<String, Double> transformerFunction(State s, JointAction ja, State sp) {
		int chargingEvs = 0;
		//Count number of charging agents
		List<GroundedSGAgentAction> actions = ja.getActionList();
		for(GroundedSGAgentAction action: actions){
			if(action.actionName().equals(SmartGridConstants.ACTION_CHARGE))
				chargingEvs++;
		}

		double reward = 0;
		//Check Thresholds
		if(chargingEvs >= this.overLoadThreshold || chargingEvs <= this.underLoadThreshold)
			reward = SmartGridConstants.DEFAULT_OVERLOAD_REWARD;
		else
			reward = SmartGridConstants.DEFAULT_LOAD_OK_REWARD;

		Map<String, Double> rewards = new HashMap<String, Double>();

		for(String ag : ja.getAgentNames()){
			boolean isAtHome = s.getObject(ag).getStringValForAttribute(SmartGridConstants.ATT_AT_HOME).equals(SmartGridConstants.IS_AT_HOME);
			if(isAtHome)
				rewards.put(ag, reward);
			else
				rewards.put(ag, 0d);

		}
		return rewards;
	}

	/**
	 * Process the reward for Reward Function 2
	 * @param s state
	 * @param ja joint action
	 * @param sp Resulting state
	 * @return the reward for the second function
	 */
	protected Map<String, Double> batteryLevelFunction(State s, JointAction ja, State sp) {
		double mediumThreshold = SmartGridConstants.MEDIUM_BATTERY_THRESHOLD;
		double highThreshold = SmartGridConstants.HIGH_BATTERY_THRESHOLD;
		Map<String, Double> agentRewards = new HashMap<String, Double>();

		List<ObjectInstance> evs = s.getObjectsOfClass(SmartGridConstants.CLS_EV);

		for(ObjectInstance ev: evs){
			double reward = 0;
			boolean recharging = ja.action(ev.getName()).actionName().equals(SmartGridConstants.ACTION_CHARGE);
			double battery = ev.getRealValForAttribute(SmartGridConstants.ATT_BATTERY);

			boolean isAway = ev.getStringValForAttribute(SmartGridConstants.ATT_AT_HOME).equals(SmartGridConstants.IS_AWAY);

			if(!isAway){
				//Low Level
				if(battery < mediumThreshold){
					//If the agent is recharging?
					if(recharging){
						reward = SmartGridConstants.DEFAULT_LOW_RECHARGING_REWARD;
					}
					else{
						reward = SmartGridConstants.DEFAULT_LOW_NOT_RECHARGING_REWARD;
					}
				}
				else{
					//Medium Level?
					if(battery < highThreshold){
						//If the agent is recharging?
						if(recharging){
							reward = SmartGridConstants.DEFAULT_MEDIUM_RECHARGING_REWARD;
						}
						else{
							reward = SmartGridConstants.DEFAULT_MEDIUM_NOT_RECHARGING_REWARD;
						}
					}
					else{
						//If the agent is recharging?
						if(recharging){
							reward = SmartGridConstants.DEFAULT_HIGH_RECHARGING_REWARD;
						}
						else{
							reward = SmartGridConstants.DEFAULT_HIGH_NOT_RECHARGING_REWARD;
						}
					}
				}
			}
			agentRewards.put(ev.getName(), reward);

		}

		return agentRewards;
	}

	/**
	 * Process the reward for Reward Function 1
	 * @param s state
	 * @param ja joint action
	 * @param sp resulting state
	 * @return return the reward for the first function.
	 */
	protected Map<String, Double> batteryJourneyFunction(State s, JointAction ja, State sp) {
		double rewardJourneyOk = SmartGridConstants.DEFAULT_JOURNEY_OK_REWARD;
		double rewardJourneyNo = SmartGridConstants.DEFAULT_JOURNEY_NO_REWARD;

		List<ObjectInstance> evsAfter = sp.getObjectsOfClass(SmartGridConstants.CLS_EV);
		Map<String, Double> agentRewards = new HashMap<String, Double>();


		for(ObjectInstance ev: evsAfter){
			double reward = 0;
			//Checks if an EV has started his journey in this time step
			if(ev.getStringValForAttribute(SmartGridConstants.ATT_AT_HOME).equals(SmartGridConstants.IS_AWAY)){
				//Same ev on the previous time step
				ObjectInstance evBefore = s.getObject(ev.getName());
				//Journey started on this time step?
				if(evBefore.getStringValForAttribute(SmartGridConstants.ATT_AT_HOME).equals(SmartGridConstants.IS_AT_HOME)){
					//Check if the battery was enough
					if(ev.getRealValForAttribute(SmartGridConstants.ATT_BATTERY) > 0){
						//OK
						reward = rewardJourneyOk;
					}
					else{
						//Insufficient Reward
						reward = rewardJourneyNo;
					}
				}

			}

			agentRewards.put(ev.getName(), reward);
		}
		return agentRewards;
	}

}
