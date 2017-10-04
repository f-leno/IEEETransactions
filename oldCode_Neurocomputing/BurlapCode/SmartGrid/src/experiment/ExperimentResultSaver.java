/**
 * 
 */
package experiment;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

import Multiobjective.MOWorldObserver;
import burlap.oomdp.core.objects.ObjectInstance;
import burlap.oomdp.core.states.State;
import burlap.oomdp.stochasticgames.JointAction;
import burlap.oomdp.stochasticgames.agentactions.GroundedSGAgentAction;
import domain.SmartGridConstants;

/**
 * @author Felipe Leno da Silva
 * 
 * Class to store experiment Results in a .csv file
 *
 */
public class ExperimentResultSaver implements MOWorldObserver {

	protected String fileName;
	protected File outputFile;
	protected PrintWriter writer;
	protected String sep = ";";


	//Control Variables
	private int stepsPerDay = (60*24) / SmartGridConstants.TIME_STEP;
	protected int currentTrial;

	//------------------
	// Metrics
	//------------------
	protected int day;    //number of training days
	protected int step; //step ID
	protected int discEV;  //Number of discharged EVs in this time step
	protected int overload; //Number of transformer overloads
	protected int agentsCharging; //Number of agents charging
	protected int dayTraining; //Number of exploration days
	protected double meanBattery; //Mean battery value
	protected int atHome; //Number of cars at home	
	//Mean reward values
	protected double meanReward1;
	protected double meanReward2;
	protected double meanReward3;

	boolean recordState;


	public ExperimentResultSaver(String fileName){
		this.fileName = fileName; 
		//startTrial();
		this.currentTrial = 0;
		this.recordState = false;
		this.step = 0;
		this.day = 1;
	}
	/**
	 * Write the File Header
	 */
	private void writeHeader() {
		writer.write("DayTraining"        + this.sep);
		writer.write("Day"                + this.sep);
		writer.write("Step"               + this.sep);
		writer.write("discEV"             + this.sep);
		writer.write("Overload"           + this.sep);
		writer.write("agentsCharging"     + this.sep);
		writer.write("meanBatttery"       + this.sep);
		writer.write("atHome"             + this.sep);
		writer.write("reward1"            + this.sep);
		writer.write("reward2"            + this.sep);
		writer.write("reward3"            + this.sep);
		writer.write("\n");

	}

	/* (non-Javadoc)
	 * @see burlap.oomdp.stochasticgames.WorldObserver#gameStarting(burlap.oomdp.core.states.State)
	 */
	@Override
	public void gameStarting(State s) {
		if(this.recordState)
		{
			this.day++;
			this.step = 1;
		}
	}

	/* (non-Javadoc)
	 * @see burlap.oomdp.stochasticgames.WorldObserver#observe(burlap.oomdp.core.states.State, burlap.oomdp.stochasticgames.JointAction, java.util.Map, burlap.oomdp.core.states.State)
	 */
	@Override
	public void observe(State s, JointAction ja, List<Map<String, Double>> reward, State sp) {
		if(this.recordState){
			writeState(s,ja,reward,sp);
			this.step++;
		}


	}

	/**
	 * Write the current state on the file
	 * @param s state 
	 * @param ja joint action
	 * @param reward rewards for all objectives
	 * @param sp next state
	 */
	protected void writeState(State s, JointAction ja, List<Map<String, Double>> reward, State sp) {
		calculateMetrics(s,ja,reward,sp);

		writer.write(this.dayTraining 		+this.sep);
		writer.write(this.day       		+this.sep);
		writer.write(this.step      		+this.sep);
		writer.write(this.discEV    		+this.sep);
		writer.write(this.overload  		+this.sep);
		writer.write(this.agentsCharging	+this.sep);
		writer.write(this.meanBattery  		+this.sep);		
		writer.write(this.atHome     		+this.sep);	
		writer.write(this.meanReward1       +this.sep);
		writer.write(this.meanReward2       +this.sep);
		writer.write(this.meanReward3       +this.sep);
		writer.write("\n");

	}

	/**
	 * Extract all relevant metrics from the informed data
	 * @param s current state
	 * @param ja joint action
	 * @param reward rewards for all agents and objectives
	 * @param sp next state
	 */
	protected void calculateMetrics(State s, JointAction ja, List<Map<String, Double>> reward, State sp) {

		List<String> agentNames = ja.getAgentNames();

		//Number of discharged EVs
		this.discEV = 0;
		for(String agName: agentNames){
			ObjectInstance obj = sp.getObject(agName);
			if(obj.getStringValForAttribute(SmartGridConstants.ATT_AT_HOME).equals(SmartGridConstants.IS_AWAY)){
				ObjectInstance objBefore = s.getObject(agName);
				if(objBefore.getStringValForAttribute(SmartGridConstants.ATT_AT_HOME).equals(SmartGridConstants.IS_AT_HOME)){
					double battery = obj.getRealValForAttribute(SmartGridConstants.ATT_BATTERY);
					if(battery == 0){
						this.discEV++;
					}
				}
			}

		}



		//Number of overloads
		int load = sp.getFirstObjectOfClass(SmartGridConstants.CLS_TRANSFORMER).getIntValForAttribute(SmartGridConstants.ATT_TRANSFORMER_LOAD);
		if (load <= SmartGridConstants.TRANSFORMER_UNDERLOAD || load >= SmartGridConstants.TRANSFORMER_OVERLOAD){
			this.overload = 1;
		}
		else{
			this.overload = 0;
		}

		//Number of charging agents
		this.agentsCharging = 0;
		for(GroundedSGAgentAction a : ja.getActionList()){
			if(a.actionName().equals(SmartGridConstants.ACTION_CHARGE)){
				this.agentsCharging++;
			}
		}

		//Rewards Information
		this.meanReward1=0;
		this.meanReward2=0;
		this.meanReward3=0;
		this.meanBattery = 0;
		this.atHome = 0;
		for(String agName: agentNames){
			//Mean battery
			meanBattery += s.getObject(agName).getRealValForAttribute(SmartGridConstants.ATT_BATTERY);

			//At home
			if(s.getObject(agName).getStringValForAttribute(SmartGridConstants.ATT_AT_HOME).equals(SmartGridConstants.IS_AT_HOME)){
				this.atHome++;
				meanReward1 += reward.get(0).get(agName);
				meanReward2 += reward.get(1).get(agName);
			}

			meanReward3 += reward.get(2).get(agName);
		}
		if(this.atHome>0){
			meanReward2 /= this.atHome;
			meanReward3 /= this.atHome;
		}
		
		meanBattery /= agentNames.size();

	}
	/* (non-Javadoc)
	 * @see burlap.oomdp.stochasticgames.WorldObserver#gameEnding(burlap.oomdp.core.states.State)
	 */
	@Override
	public void gameEnding(State s) {
		// TODO Auto-generated method stub

	}

	/**
	 * Create a new output File
	 * @return a reference to the new file
	 */
	protected File CreateOutputFile() {
		File outputFile = new File(this.fileName+this.currentTrial+".csv");
		try{
			if(!outputFile.exists()){
				outputFile.getParentFile().mkdirs();
				outputFile.createNewFile();				
			}
			this.writer = new PrintWriter(outputFile);
			return outputFile;			
		}
		catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("Problems creating the output File in ExperimentResultSaver");
		}
	}

	/**
	 * Defines if the object should record info about the next states
	 * @param record should it record?
	 * @param dayTrainig number of training days
	 */
	public void setRecordingState(boolean record, int dayTraining){
		this.recordState = record;
		if(record){
			this.day = 0;
			this.dayTraining = dayTraining;
		}

	}
	/**
	 * End this trial
	 */
	public void endTrial(){
		this.writer.close();
	}
	/**
	 * Starts next trial
	 */
	public void startTrial(int trial){
		this.currentTrial = trial;
		this.outputFile = CreateOutputFile();
		writeHeader();
	}
}
