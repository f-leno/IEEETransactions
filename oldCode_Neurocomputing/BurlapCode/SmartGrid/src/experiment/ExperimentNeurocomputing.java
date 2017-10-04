/**
 * 
 */
package experiment;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import Multiobjective.MOJointReward;
import Multiobjective.MOSGAgent;
import Multiobjective.MOWorld;
import burlap.debugtools.DPrint;
import burlap.oomdp.auxiliary.DomainGenerator;
import burlap.oomdp.auxiliary.common.NullTermination;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.core.states.State;
import burlap.oomdp.statehashing.HashableStateFactory;
import burlap.oomdp.stochasticgames.JointActionModel;
import burlap.oomdp.stochasticgames.SGDomain;
import domain.SmartGridBatteryHashableStateFactory;
import domain.SmartGridConstants;
import domain.SmartGridLoadBalancing;
import domain.SmartGridMORewardFunction;
import domain.SmartGridTransformerHashableStateFactory;
import dwl.DWLAgent;
import otherAlgorithms.AlwaysChargingAlgorithm;
import otherAlgorithms.RandomAlgorithmPolicy;
import ssbw.SSBWAgent;

/**
 * @author Felipe Leno da Silva
 *	Executes the experiment described on the Paper.
 *	All results can be reproduced by this class.
 */
public class ExperimentNeurocomputing {

	
	

	ExperimentResultSaver resultSaverSSBW ;
	ExperimentResultSaver resultSaverDWL;
	ExperimentResultSaver resultSaverACP;
	ExperimentResultSaver resultSaverRP;
	/**
	 * Executes the experiment.
	 */
	public void runExperiment(){

		final boolean testSSBW = true;
		final boolean testRandom = false;
		final boolean testACP = false;
		final boolean testDWL = true;

		final int transformerOverload = SmartGridConstants.TRANSFORMER_OVERLOAD;
		final int transformerUnderload = SmartGridConstants.TRANSFORMER_UNDERLOAD;
		final int stepsPerDay = (60*24) / SmartGridConstants.TIME_STEP;

		final double alpha = 0.2;
		final double gamma = 0.9;
		final double epsilon = 0.1;
		final double cooperationRate = 1;
		final double qInitialValue = 0;

		final int numberAgents = SmartGridConstants.NUMBER_OF_EVS;


		//Experiment Variables
		final int numberOfDays = 400;
		final int intervalExploitation =5;
		final int daysExploitation = 5;
		final int numberOfTrials = 50;


		/**
		 * Parameters for DWL experiments
		 */
		//double[] dwlCParameters = {0,0.2,0.4,0.6,0.8,1};
		//double[] dwlCParameters = {0,0.5,1};
		double[] dwlCParameters = {0.2,0.7};
		
		
		/**
		 * Parameters for SSBW experiments
		 */
		List<List<Double>> ssbwWeightParameters;
		ssbwWeightParameters = new ArrayList<List<Double>>();

		List<Double> weightVector = new ArrayList<Double>();
		/*weightVector.add(0d); //Journey
		weightVector.add(1d); //Battery
		weightVector.add(0d); //Transformer
		ssbwWeightParameters.add(weightVector);
		
		
		weightVector = new ArrayList<Double>();
		weightVector.add(0d); //Journey
		weightVector.add(0d); //Battery
		weightVector.add(1d); //Transformer
		ssbwWeightParameters.add(weightVector);
		
		weightVector = new ArrayList<Double>();
		weightVector.add(1d); //Journey
		weightVector.add(1d); //Battery
		weightVector.add(1d); //Transformer
		ssbwWeightParameters.add(weightVector);*/
		weightVector.add(1d); //Journey
		weightVector.add(0d); //Battery
		weightVector.add(0d); //Transformer
		ssbwWeightParameters.add(weightVector);
		
		weightVector.add(1d); //Journey
		weightVector.add(0d); //Battery
		weightVector.add(1d); //Transformer
		ssbwWeightParameters.add(weightVector);
		



		


		resultSaverACP = new ExperimentResultSaver("experiments/ACP/");
		resultSaverRP = new ExperimentResultSaver("experiments/RP/");

		for(int trial=0; trial< numberOfTrials; trial++){
			//State initialState = gen.getInitialState(domain, new Random(trial));



			//----------
			// SSBW experiments
			//--------------
			if(testSSBW){

				
				Thread[] ssbwThreads = new Thread[ssbwWeightParameters.size()];
				for(int i = 0; i<dwlCParameters.length;i++){
					//Bulding the domain
					SmartGridLoadBalancing gen = new SmartGridLoadBalancing();
					SGDomain domain = (SGDomain)gen.generateDomain();

					SmartGridMORewardFunction rewardFunction = new SmartGridMORewardFunction(transformerUnderload,transformerOverload); 
					TerminalFunction tf = new NullTermination(); //THis domains does not have terminal states
					
					State threadState = gen.getInitialState(domain, new Random(trial));
					resultSaverSSBW = new ExperimentResultSaver("experiments/SSBW"+ssbwWeightParameters.get(i).toString()+"/");
					Runnable experiment = new SSBWParameterizedExperiment(gen,domain,rewardFunction,tf,trial,threadState,alpha,gamma,epsilon,cooperationRate,
							ssbwWeightParameters.get(i),qInitialValue,numberAgents,numberOfDays,stepsPerDay,intervalExploitation,
							daysExploitation,resultSaverSSBW);
					ssbwThreads[i] = new Thread(experiment);
					ssbwThreads[i].start();
				}
				for(int i = 0; i<dwlCParameters.length;i++){
					try {
						ssbwThreads[i].join();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				ssbwThreads = null;
				resultSaverSSBW = null;
				System.gc();

			}

			//-----
			// DWL Experiments
			if(testDWL){
				Thread[] dwlThreads = new Thread[dwlCParameters.length];
				
				for(int i = 0; i<dwlCParameters.length;i++){
					//Bulding the domain
					SmartGridLoadBalancing gen = new SmartGridLoadBalancing();
					SGDomain domain = (SGDomain)gen.generateDomain();

					SmartGridMORewardFunction rewardFunction = new SmartGridMORewardFunction(transformerUnderload,transformerOverload); 
					TerminalFunction tf = new NullTermination(); //THis domains does not have terminal states
					
					State threadState = gen.getInitialState(domain, new Random(trial));
					resultSaverDWL = new ExperimentResultSaver("experiments/DWL"+dwlCParameters[i]+"/");
					Runnable experiment = new DWLParameterizedExperiment(gen,domain,rewardFunction,tf,trial, threadState, alpha, gamma, epsilon, dwlCParameters[i], 
							qInitialValue, numberAgents, numberOfDays, stepsPerDay, intervalExploitation, daysExploitation, resultSaverDWL);
					dwlThreads[i] = new Thread(experiment);
					dwlThreads[i].start();
				}
				for(int i = 0; i<dwlCParameters.length;i++){
					try {
						dwlThreads[i].join();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				dwlThreads = null;
				resultSaverDWL = null;
				System.gc();

			}

			if(testACP){
				//Bulding the domain
				SmartGridLoadBalancing gen = new SmartGridLoadBalancing();
				SGDomain domain = (SGDomain)gen.generateDomain();

				SmartGridMORewardFunction rewardFunction = new SmartGridMORewardFunction(transformerUnderload,transformerOverload); 
				TerminalFunction tf = new NullTermination(); //THis domains does not have terminal states
				
				State threadState = gen.getInitialState(domain, new Random(trial));
				Random randomSeed = new Random(trial);
				MOWorld w = new MOWorld(domain,rewardFunction,tf,threadState,3);
				//Disable Debug Messages.
				DPrint.toggleCode(w.getDebugId(),false);

				w.addWorldObserver(resultSaverACP);
				resultSaverACP.startTrial(trial+1);

				for(int i=0; i<numberAgents;i++){
					MOSGAgent rdAgent = new AlwaysChargingAlgorithm(domain,new Random(randomSeed.nextLong())); 
					rdAgent.joinWorld(w, gen.getAgentType());
				}
				//---------------------------
				// Starting Exploration
				//-----------------------------
				int curDay=0;
				while(curDay<numberOfDays+1){

					curDay++;
					//Run one day
					w.runMOGame(stepsPerDay);
					//Begins exploitation
					if(curDay % intervalExploitation == 0){
						int curDayExploitation = 0;
						resultSaverACP.setRecordingState(true, curDay);

						System.out.println("----Exploration day: "+curDay+ "---- alg: ACP - Trial: "+trial);
						while(curDayExploitation < daysExploitation){
							curDayExploitation++;
							w.runMOGame(stepsPerDay);
						}
						resultSaverACP.setRecordingState(false, curDay);
					}

				}
				resultSaverACP.endTrial();

			}

			if(testRandom){
				//Bulding the domain
				SmartGridLoadBalancing gen = new SmartGridLoadBalancing();
				SGDomain domain = (SGDomain)gen.generateDomain();

				SmartGridMORewardFunction rewardFunction = new SmartGridMORewardFunction(transformerUnderload,transformerOverload); 
				TerminalFunction tf = new NullTermination(); //THis domains does not have terminal states
				
				State threadState = gen.getInitialState(domain, new Random(trial));
				Random randomSeed = new Random(trial);
				MOWorld w = new MOWorld(domain,rewardFunction,tf,threadState,3);
				//Disable Debug Messages.
				DPrint.toggleCode(w.getDebugId(),false);

				w.addWorldObserver(resultSaverRP);
				resultSaverRP.startTrial(trial+1);


				for(int i=0; i<numberAgents;i++){
					MOSGAgent rdAgent = new RandomAlgorithmPolicy(domain,new Random(randomSeed.nextLong())); 
					rdAgent.joinWorld(w, gen.getAgentType());
				}
				//---------------------------
				// Starting Exploration
				//-----------------------------
				int curDay=0;
				while(curDay<numberOfDays+1){

					curDay++;
					//Run one day
					w.runMOGame(stepsPerDay);
					//Begins exploitation
					if(curDay % intervalExploitation == 0){
						int curDayExploitation = 0;
						resultSaverRP.setRecordingState(true, curDay);

						System.out.println("----Exploration day: "+curDay+ "---- alg: RP - Trial: "+trial);
						while(curDayExploitation < daysExploitation){
							curDayExploitation++;
							w.runMOGame(stepsPerDay);
						}
						resultSaverRP.setRecordingState(false, curDay);
					}

				}
				resultSaverRP.endTrial();
			}



		}
		System.out.println("OK");
	}


	/**
	 * Class to execuyte SBW experiments
	 * @author Felipe Leno da Silva
	 *
	 */
	private class SSBWParameterizedExperiment implements Runnable{
	
		private int trial;
		private State initialState;
		private int numberAgents;
		private double alpha;
		private double gamma;
		private double epsilon;
		private double cooperationRate;
		private double qInitialValue;
		private int numberOfDays;
		private int stepsPerDay;
		private int intervalExploitation;
		private int daysExploitation;
		private ExperimentResultSaver resultSaverSSBW;
		private List<Double> weightVector;
		private SmartGridLoadBalancing gen;
		private SGDomain domain;
		private MOJointReward rewardFunction;
		private TerminalFunction tf;
		
		/**
		 * Default constructor specifying experiment variables
		 * @param tf 
		 * @param rewardFunction 
		 * @param domain 
		 * @param gen 
		 * @param trial number of the current trial
		 * @param initialState initial state
		 * @param alpha alpha
		 * @param gamma gamma
		 * @param epsilon epsilon for exploration
		 * @param cooperationRate cooperation rate
		 * @param weightVector weight parameter for objectives
		 * @param qInitialValue initial Q value
		 * @param numberAgents number of agents in the simulation
		 * @param numberOfDays number of simulation days
		 * @param stepsPerDay number of decision steps per day
		 * @param intervalExploitation number of days until next evaluation
		 * @param daysExploitation days of exploitation
		 * @param resultSaverSSBW object to record results
		 */
		public SSBWParameterizedExperiment(SmartGridLoadBalancing gen, SGDomain domain, SmartGridMORewardFunction rewardFunction, TerminalFunction tf, int trial,State initialState, double alpha, double gamma,
				double epsilon, double cooperationRate, List<Double> weightVector, double qInitialValue, int numberAgents, int numberOfDays,
				int stepsPerDay, int intervalExploitation, int daysExploitation, ExperimentResultSaver resultSaverSSBW){
			super();
			this.initialState = initialState;
			this.alpha = alpha;
			this.gamma = gamma;
			this.epsilon = epsilon;
			this.cooperationRate = cooperationRate;
			this.qInitialValue = qInitialValue;
			this.numberAgents = numberAgents;
			this.numberOfDays = numberOfDays;
			this.stepsPerDay = stepsPerDay;
			this.intervalExploitation = intervalExploitation;
			this.daysExploitation = daysExploitation;
			this.resultSaverSSBW = resultSaverSSBW;
			this.trial = trial;
			this.weightVector = weightVector;
			this.gen = gen;
			this.domain = domain;
			this.rewardFunction = rewardFunction;
			this.tf = tf;
		}
		
		public void run(){
			Random randomSeed = new Random(trial);
			MOWorld w = new MOWorld(domain,rewardFunction,tf,initialState,3);
			//Disable Debug Messages.
			DPrint.toggleCode(w.getDebugId(),false);	

			w.addWorldObserver(resultSaverSSBW);

			resultSaverSSBW.startTrial(trial+1);
			List<SSBWAgent> allAgents = new ArrayList<SSBWAgent>();



			for(int i=0; i<numberAgents;i++){
				SSBWAgent agent = new SSBWAgent(domain,3,alpha,gamma,epsilon,weightVector,cooperationRate,qInitialValue,new Random(randomSeed.nextLong()));
				allAgents.add(agent);
				agent.joinWorld(w, gen.getAgentType());
				List<HashableStateFactory> factories = new ArrayList<HashableStateFactory>();
				factories.add(new SmartGridBatteryHashableStateFactory(agent.getAgentName(), (int)SmartGridConstants.NUM_BATTERY_LEVELS));
				factories.add(new SmartGridBatteryHashableStateFactory(agent.getAgentName(), (int)SmartGridConstants.NUM_BATTERY_LEVELS));
				factories.add(new SmartGridTransformerHashableStateFactory(agent.getAgentName()));
				agent.setHashableStateFactories(factories);
			}
			ExperimentNeurocomputing.setAgentNeighbors(allAgents);
			for(int i=0; i<numberAgents;i++){
				allAgents.get(i).initiateState(initialState);
			}

			//---------------------------
			// Starting Exploration
			//-----------------------------
			int curDay=0;
			while(curDay<numberOfDays+1){

				curDay++;
				//Run one day
				w.runMOGame(stepsPerDay);
				//Begins exploitation
				if(curDay % intervalExploitation == 0){
					//Sets exploration OFF
					for(int i=0; i<numberAgents;i++){
						allAgents.get(i).setExploring(false);
					}
					int curDayExploitation = 0;
					resultSaverSSBW.setRecordingState(true, curDay);

					System.out.println("----Exploration day: "+curDay+ "---- alg: SSBW - Trial: "+trial);
					while(curDayExploitation < daysExploitation){
						curDayExploitation++;
						w.runMOGame(stepsPerDay);
					}
					resultSaverSSBW.setRecordingState(false, curDay);
					//Set Exploration ON
					for(int i=0; i<numberAgents;i++){
						allAgents.get(i).setExploring(true);
					}
				}

			}
			resultSaverSSBW.endTrial();
		}


	}
	/**
	 * Class to execute one Experiment for DWL.. All experiment parameters must be specified
	 * @author Felipe Leno da Silva
	 *
	 */
	private class DWLParameterizedExperiment implements Runnable{

		private State initialState;
		private double alpha;
		private double gamma;
		private double epsilon;
		private double cooperationRate;
		private double qInitialValue;
		private int numberAgents;
		private int numberOfDays;
		private int stepsPerDay;
		private int intervalExploitation;
		private int daysExploitation;
		private ExperimentResultSaver resultSaverDWL;
		private int trial;
		private SGDomain domain;
		private MOJointReward rewardFunction;
		private TerminalFunction tf;
		private SmartGridLoadBalancing gen;




		/**
		 * Default constructor
		 * @param tf 
		 * @param rewardFunction 
		 * @param domain 
		 * @param trial current Trial
		 * @param initialState initial State
		 * @param alpha alpha parameter
		 * @param gamma gamma parameter
		 * @param epsilon epsilon for exploration
		 * @param cooperationRate cooperation rate C
		 * @param qInitialValue initial value for Q-table
		 * @param numberAgents number of agents in the simulation
		 * @param numberOfDays number of days to be simulated
		 * @param stepsPerDay number of decision steps per day
		 * @param intervalExploitation how much time until exploitation tests
		 * @param daysExploitation number of exploitation days
		 * @param resultSaverDWL Class to store results.
		 */
		public DWLParameterizedExperiment(SmartGridLoadBalancing gen,SGDomain domain, SmartGridMORewardFunction rewardFunction, TerminalFunction tf, int trial,State initialState, double alpha, double gamma,
				double epsilon, double cooperationRate, double qInitialValue, int numberAgents, int numberOfDays,
				int stepsPerDay, int intervalExploitation, int daysExploitation, ExperimentResultSaver resultSaverDWL) {
			super();
			this.initialState = initialState;
			this.alpha = alpha;
			this.gamma = gamma;
			this.epsilon = epsilon;
			this.cooperationRate = cooperationRate;
			this.qInitialValue = qInitialValue;
			this.numberAgents = numberAgents;
			this.numberOfDays = numberOfDays;
			this.stepsPerDay = stepsPerDay;
			this.intervalExploitation = intervalExploitation;
			this.daysExploitation = daysExploitation;
			this.resultSaverDWL = resultSaverDWL;
			this.trial = trial;
			this.domain = domain;
			this.rewardFunction = rewardFunction;
			this.tf = tf;
			this.gen = gen;
		}




		public void run(){

			Random randomSeed = new Random(trial);
			MOWorld w = new MOWorld(domain,rewardFunction,tf,initialState,3);
			//Disable Debug Messages.
			DPrint.toggleCode(w.getDebugId(),false);	

			w.addWorldObserver(resultSaverDWL);

			resultSaverDWL.startTrial(this.trial+1);
			List<DWLAgent> allAgents = new ArrayList<DWLAgent>();



			for(int i=0; i<numberAgents;i++){
				DWLAgent agent = new DWLAgent(domain,3,alpha,gamma,epsilon,cooperationRate,qInitialValue,new Random(randomSeed.nextLong()));
				allAgents.add(agent);
				agent.joinWorld(w, gen.getAgentType());
				List<HashableStateFactory> factories = new ArrayList<HashableStateFactory>();
				factories.add(new SmartGridBatteryHashableStateFactory(agent.getAgentName(), (int)SmartGridConstants.NUM_BATTERY_LEVELS));
				factories.add(new SmartGridBatteryHashableStateFactory(agent.getAgentName(), (int)SmartGridConstants.NUM_BATTERY_LEVELS));
				factories.add(new SmartGridTransformerHashableStateFactory(agent.getAgentName()));
				agent.setHashableStateFactories(factories);
			}
			ExperimentNeurocomputing.setDWLAgentNeighbors(allAgents);
			for(int i=0; i<numberAgents;i++){
				allAgents.get(i).initiateState(initialState);
			}

			//---------------------------
			// Starting Exploration
			//-----------------------------
			int curDay=0;
			while(curDay<numberOfDays+1){
				curDay++;
				//Run one day
				w.runMOGame(stepsPerDay);
				//Begins exploitation
				if(curDay % intervalExploitation == 0){
					//Sets exploration OFF
					for(int i=0; i<numberAgents;i++){
						allAgents.get(i).setExploring(false);
					}
					int curDayExploitation = 0;
					resultSaverDWL.setRecordingState(true, curDay);

					System.out.println("----Exploration day: "+curDay + "---- alg: DWL - Trial: "+trial);
					while(curDayExploitation < daysExploitation){
						curDayExploitation++;
						w.runMOGame(stepsPerDay);
					}
					resultSaverDWL.setRecordingState(false, curDay);
					//Set Exploration ON
					for(int i=0; i<numberAgents;i++){
						allAgents.get(i).setExploring(true);
					}
				}

			}
			resultSaverDWL.endTrial();
		}

	}
	/*
		if(testSSBW){
			Runnable r = new Runnable() {
				public void run() {
					for(int trial=0; trial< numberOfTrials; trial++){
						System.gc();
						State initialState = gen.getInitialState(domain, new Random(trial));
						Random randomSeed = new Random(trial);
						MOWorld w = new MOWorld(domain,rewardFunction,tf,initialState,3);
						//Disable Debug Messages.
						DPrint.toggleCode(w.getDebugId(),false);	

						w.addWorldObserver(resultSaverSSBW);

						resultSaverSSBW.startTrial();
						List<SSBWAgent> allAgents = new ArrayList<SSBWAgent>();



						for(int i=0; i<numberAgents;i++){
							SSBWAgent agent = new SSBWAgent(domain,3,alpha,gamma,epsilon,weightVector,cooperationRate,qInitialValue,new Random(randomSeed.nextLong()));
							allAgents.add(agent);
							agent.joinWorld(w, gen.getAgentType());
							List<HashableStateFactory> factories = new ArrayList<HashableStateFactory>();
							factories.add(new SmartGridBatteryHashableStateFactory(agent.getAgentName(), (int)SmartGridConstants.NUMBER_BATTERY_DESCRETIZATIONS));
							factories.add(new SmartGridBatteryHashableStateFactory(agent.getAgentName(), (int)SmartGridConstants.NUMBER_BATTERY_DESCRETIZATIONS));
							factories.add(new SmartGridTransformerHashableStateFactory(agent.getAgentName()));
							agent.setHashableStateFactories(factories);
						}
						ExperimentNeurocomputing.setAgentNeighbors(allAgents);
						for(int i=0; i<numberAgents;i++){
							allAgents.get(i).initiateState(initialState);
						}

						//---------------------------
						// Starting Exploration
						//-----------------------------
						int curDay=0;
						while(curDay<numberOfDays+1){

							curDay++;
							//Run one day
							w.runMOGame(stepsPerDay);
							//Begins exploitation
							if(curDay % intervalExploitation == 0){
								//Sets exploration OFF
								for(int i=0; i<numberAgents;i++){
									allAgents.get(i).setExploring(false);
								}
								int curDayExploitation = 0;
								resultSaverSSBW.setRecordingState(true, curDay);

								System.out.println("----Exploration day: "+curDay+ "---- alg: SSBW - Trial: "+trial);
								while(curDayExploitation < daysExploitation){
									curDayExploitation++;
									w.runMOGame(stepsPerDay);
								}
								resultSaverSSBW.setRecordingState(false, curDay);
								//Set Exploration ON
								for(int i=0; i<numberAgents;i++){
									allAgents.get(i).setExploring(true);
								}
							}

						}
						resultSaverSSBW.endTrial();

					}
					System.out.println("OK - SSBW");
				}

			};

			Thread t = new Thread(r);
			t.start();
		}
		for(int trial=0; trial< numberOfTrials; trial++){
			if(testDWL){

				System.gc();
				State initialState = gen.getInitialState(domain, new Random(trial));

				Random randomSeed = new Random(trial);
				MOWorld w = new MOWorld(domain,rewardFunction,tf,initialState,3);
				//Disable Debug Messages.
				DPrint.toggleCode(w.getDebugId(),false);	

				w.addWorldObserver(resultSaverDWL);

				resultSaverDWL.startTrial();
				List<DWLAgent> allAgents = new ArrayList<DWLAgent>();



				for(int i=0; i<numberAgents;i++){
					DWLAgent agent = new DWLAgent(domain,3,alpha,gamma,epsilon,cooperationRate,qInitialValue,new Random(randomSeed.nextLong()));
					allAgents.add(agent);
					agent.joinWorld(w, gen.getAgentType());
					List<HashableStateFactory> factories = new ArrayList<HashableStateFactory>();
					factories.add(new SmartGridBatteryHashableStateFactory(agent.getAgentName(), (int)SmartGridConstants.NUMBER_BATTERY_DESCRETIZATIONS));
					factories.add(new SmartGridBatteryHashableStateFactory(agent.getAgentName(), (int)SmartGridConstants.NUMBER_BATTERY_DESCRETIZATIONS));
					factories.add(new SmartGridTransformerHashableStateFactory(agent.getAgentName()));
					agent.setHashableStateFactories(factories);
				}
				ExperimentNeurocomputing.setDWLAgentNeighbors(allAgents);
				for(int i=0; i<numberAgents;i++){
					allAgents.get(i).initiateState(initialState);
				}

				//---------------------------
				// Starting Exploration
				//-----------------------------
				int curDay=0;
				while(curDay<numberOfDays+1){
					curDay++;
					//Run one day
					w.runMOGame(stepsPerDay);
					//Begins exploitation
					if(curDay % intervalExploitation == 0){
						//Sets exploration OFF
						for(int i=0; i<numberAgents;i++){
							allAgents.get(i).setExploring(false);
						}
						int curDayExploitation = 0;
						resultSaverDWL.setRecordingState(true, curDay);

						System.out.println("----Exploration day: "+curDay + "---- alg: DWL - Trial: "+trial);
						while(curDayExploitation < daysExploitation){
							curDayExploitation++;
							w.runMOGame(stepsPerDay);
						}
						resultSaverDWL.setRecordingState(false, curDay);
						//Set Exploration ON
						for(int i=0; i<numberAgents;i++){
							allAgents.get(i).setExploring(true);
						}
					}

				}
				resultSaverDWL.endTrial();



				System.out.println("OK - SSBW");
			}





			if(testACP){


				State initialState = gen.getInitialState(domain, new Random(trial));
				Random randomSeed = new Random(trial);
				MOWorld w = new MOWorld(domain,rewardFunction,tf,initialState,3);
				//Disable Debug Messages.
				DPrint.toggleCode(w.getDebugId(),false);

				w.addWorldObserver(resultSaverACP);
				resultSaverACP.startTrial();

				for(int i=0; i<numberAgents;i++){
					MOSGAgent rdAgent = new AlwaysChargingAlgorithm(domain,new Random(randomSeed.nextLong())); 
					rdAgent.joinWorld(w, gen.getAgentType());
				}
				//---------------------------
				// Starting Exploration
				//-----------------------------
				int curDay=0;
				while(curDay<numberOfDays+1){

					curDay++;
					//Run one day
					w.runMOGame(stepsPerDay);
					//Begins exploitation
					if(curDay % intervalExploitation == 0){
						int curDayExploitation = 0;
						resultSaverACP.setRecordingState(true, curDay);

						System.out.println("----Exploration day: "+curDay+ "---- alg: ACP - Trial: "+trial);
						while(curDayExploitation < daysExploitation){
							curDayExploitation++;
							w.runMOGame(stepsPerDay);
						}
						resultSaverACP.setRecordingState(false, curDay);
					}

				}
				resultSaverACP.endTrial();

			}

			if(testRandom){


				State initialState = gen.getInitialState(domain, new Random(trial));
				Random randomSeed = new Random(trial);
				MOWorld w = new MOWorld(domain,rewardFunction,tf,initialState,3);
				//Disable Debug Messages.
				DPrint.toggleCode(w.getDebugId(),false);

				w.addWorldObserver(resultSaverRP);
				resultSaverRP.startTrial();


				for(int i=0; i<numberAgents;i++){
					MOSGAgent rdAgent = new RandomAlgorithmPolicy(domain,new Random(randomSeed.nextLong())); 
					rdAgent.joinWorld(w, gen.getAgentType());
				}
				//---------------------------
				// Starting Exploration
				//-----------------------------
				int curDay=0;
				while(curDay<numberOfDays+1){

					curDay++;
					//Run one day
					w.runMOGame(stepsPerDay);
					//Begins exploitation
					if(curDay % intervalExploitation == 0){
						int curDayExploitation = 0;
						resultSaverRP.setRecordingState(true, curDay);

						System.out.println("----Exploration day: "+curDay+ "---- alg: RP - Trial: "+trial);
						while(curDayExploitation < daysExploitation){
							curDayExploitation++;
							w.runMOGame(stepsPerDay);
						}
						resultSaverRP.setRecordingState(false, curDay);
					}

				}
				resultSaverRP.endTrial();

			}
		}
		System.out.println("OK --  ALL");

	}


	 */



	/*
	 * 
	 * 
		for(int trial=0; trial< numberOfTrials; trial++){
			State initialState = gen.getInitialState(domain, new Random(trial));



			if(testSSBW){

				Random randomSeed = new Random(trial);
				MOWorld w = new MOWorld(domain,rewardFunction,tf,initialState,3);
				//Disable Debug Messages.
				DPrint.toggleCode(w.getDebugId(),false);	

				w.addWorldObserver(resultSaverSSBW);

				resultSaverSSBW.startTrial();
				List<SSBWAgent> allAgents = new ArrayList<SSBWAgent>();



				for(int i=0; i<numberAgents;i++){
					SSBWAgent agent = new SSBWAgent(domain,3,alpha,gamma,epsilon,weightVector,cooperationRate,qInitialValue,new Random(randomSeed.nextLong()));
					allAgents.add(agent);
					agent.joinWorld(w, gen.getAgentType());
					List<HashableStateFactory> factories = new ArrayList<HashableStateFactory>();
					factories.add(new SmartGridBatteryHashableStateFactory(agent.getAgentName(), (int)SmartGridConstants.NUMBER_BATTERY_DESCRETIZATIONS));
					factories.add(new SmartGridBatteryHashableStateFactory(agent.getAgentName(), (int)SmartGridConstants.NUMBER_BATTERY_DESCRETIZATIONS));
					factories.add(new SmartGridTransformerHashableStateFactory(agent.getAgentName()));
					agent.setHashableStateFactories(factories);
				}
				this.setAgentNeighbors(allAgents);
				for(int i=0; i<numberAgents;i++){
					allAgents.get(i).initiateState(initialState);
				}

				//---------------------------
				// Starting Exploration
				//-----------------------------
				int curDay=0;
				while(curDay<numberOfDays+1){

					curDay++;
					//Run one day
					w.runMOGame(stepsPerDay);
					//Begins exploitation
					if(curDay % intervalExploitation == 0){
						//Sets exploration OFF
						for(int i=0; i<numberAgents;i++){
							allAgents.get(i).setExploring(false);
						}
						int curDayExploitation = 0;
						resultSaverSSBW.setRecordingState(true, curDay);

						System.out.println("----Exploration day: "+curDay+ "---- alg: SSBW - Trial: "+trial);
						while(curDayExploitation < daysExploitation){
							curDayExploitation++;
							w.runMOGame(stepsPerDay);
						}
						resultSaverSSBW.setRecordingState(false, curDay);
						//Set Exploration ON
						for(int i=0; i<numberAgents;i++){
							allAgents.get(i).setExploring(true);
						}
					}

				}
				resultSaverSSBW.endTrial();

			}

			if(testDWL){
				Random randomSeed = new Random(trial);
				MOWorld w = new MOWorld(domain,rewardFunction,tf,initialState,3);
				//Disable Debug Messages.
				DPrint.toggleCode(w.getDebugId(),false);	

				w.addWorldObserver(resultSaverDWL);

				resultSaverDWL.startTrial();
				List<DWLAgent> allAgents = new ArrayList<DWLAgent>();



				for(int i=0; i<numberAgents;i++){
					DWLAgent agent = new DWLAgent(domain,3,alpha,gamma,epsilon,cooperationRate,qInitialValue,new Random(randomSeed.nextLong()));
					allAgents.add(agent);
					agent.joinWorld(w, gen.getAgentType());
					List<HashableStateFactory> factories = new ArrayList<HashableStateFactory>();
					factories.add(new SmartGridBatteryHashableStateFactory(agent.getAgentName(), (int)SmartGridConstants.NUMBER_BATTERY_DESCRETIZATIONS));
					factories.add(new SmartGridBatteryHashableStateFactory(agent.getAgentName(), (int)SmartGridConstants.NUMBER_BATTERY_DESCRETIZATIONS));
					factories.add(new SmartGridTransformerHashableStateFactory(agent.getAgentName()));
					agent.setHashableStateFactories(factories);
				}
				this.setDWLAgentNeighbors(allAgents);
				for(int i=0; i<numberAgents;i++){
					allAgents.get(i).initiateState(initialState);
				}

				//---------------------------
				// Starting Exploration
				//-----------------------------
				int curDay=0;
				while(curDay<numberOfDays+1){
					curDay++;
					//Run one day
					w.runMOGame(stepsPerDay);
					//Begins exploitation
					if(curDay % intervalExploitation == 0){
						//Sets exploration OFF
						for(int i=0; i<numberAgents;i++){
							allAgents.get(i).setExploring(false);
						}
						int curDayExploitation = 0;
						resultSaverDWL.setRecordingState(true, curDay);

						System.out.println("----Exploration day: "+curDay + "---- alg: DWL - Trial: "+trial);
						while(curDayExploitation < daysExploitation){
							curDayExploitation++;
							w.runMOGame(stepsPerDay);
						}
						resultSaverDWL.setRecordingState(false, curDay);
						//Set Exploration ON
						for(int i=0; i<numberAgents;i++){
							allAgents.get(i).setExploring(true);
						}
					}

				}
				resultSaverDWL.endTrial();

			}

			if(testACP){
				Random randomSeed = new Random(trial);
				MOWorld w = new MOWorld(domain,rewardFunction,tf,initialState,3);
				//Disable Debug Messages.
				DPrint.toggleCode(w.getDebugId(),false);

				w.addWorldObserver(resultSaverACP);
				resultSaverACP.startTrial();

				for(int i=0; i<numberAgents;i++){
					MOSGAgent rdAgent = new AlwaysChargingAlgorithm(domain,new Random(randomSeed.nextLong())); 
					rdAgent.joinWorld(w, gen.getAgentType());
				}
				//---------------------------
				// Starting Exploration
				//-----------------------------
				int curDay=0;
				while(curDay<numberOfDays+1){

					curDay++;
					//Run one day
					w.runMOGame(stepsPerDay);
					//Begins exploitation
					if(curDay % intervalExploitation == 0){
						int curDayExploitation = 0;
						resultSaverACP.setRecordingState(true, curDay);

						System.out.println("----Exploration day: "+curDay+ "---- alg: ACP - Trial: "+trial);
						while(curDayExploitation < daysExploitation){
							curDayExploitation++;
							w.runMOGame(stepsPerDay);
						}
						resultSaverACP.setRecordingState(false, curDay);
					}

				}
				resultSaverACP.endTrial();

			}

			if(testRandom){
				Random randomSeed = new Random(trial);
				MOWorld w = new MOWorld(domain,rewardFunction,tf,initialState,3);
				//Disable Debug Messages.
				DPrint.toggleCode(w.getDebugId(),false);

				w.addWorldObserver(resultSaverRP);
				resultSaverRP.startTrial();


				for(int i=0; i<numberAgents;i++){
					MOSGAgent rdAgent = new RandomAlgorithmPolicy(domain,new Random(randomSeed.nextLong())); 
					rdAgent.joinWorld(w, gen.getAgentType());
				}
				//---------------------------
				// Starting Exploration
				//-----------------------------
				int curDay=0;
				while(curDay<numberOfDays+1){

					curDay++;
					//Run one day
					w.runMOGame(stepsPerDay);
					//Begins exploitation
					if(curDay % intervalExploitation == 0){
						int curDayExploitation = 0;
						resultSaverRP.setRecordingState(true, curDay);

						System.out.println("----Exploration day: "+curDay+ "---- alg: RP - Trial: "+trial);
						while(curDayExploitation < daysExploitation){
							curDayExploitation++;
							w.runMOGame(stepsPerDay);
						}
						resultSaverRP.setRecordingState(false, curDay);
					}

				}
				resultSaverRP.endTrial();
			}



		}
		System.out.println("OK");
	 */


	/**
	 * Select and set the proper neighbors for all agents
	 * @param allAgents all agents in the system
	 */
	public static void setAgentNeighbors(List<SSBWAgent> allAgents) {
		for(int i=0; i<allAgents.size(); i++){
			List<SSBWAgent> neigh = new ArrayList<SSBWAgent>();

			/*neigh.add(allAgents.get((i+1)%allAgents.size()));
			int index = (i-1)==-1?allAgents.size()-1:(i-1);
			neigh.add(allAgents.get(index));*/
			for(int y=0; y<allAgents.size();y++){
				if(i!=y){
					neigh.add(allAgents.get(y));
				}
			}
			allAgents.get(i).setNeighbors(neigh);
		}

	}

	/**
	 * Select and set the proper neighbors for all agents
	 * @param allAgents all agents in the system
	 */
	public static void setDWLAgentNeighbors(List<DWLAgent> allAgents) {
		for(int i=0; i<allAgents.size(); i++){
			List<DWLAgent> neigh = new ArrayList<DWLAgent>();

			for(int y=0; y<allAgents.size();y++){
				if(i!=y){
					neigh.add(allAgents.get(y));
				}
			}
			/*neigh.add(allAgents.get((i+1)%allAgents.size()));
			int index = (i-1)==-1?allAgents.size()-1:(i-1);
			neigh.add(allAgents.get(index));*/
			allAgents.get(i).setNeighbors(neigh);
		}

	}

	/**
	 * executes new ExperimentNeurocomputing().runExperiment();
	 * @param args
	 */
	public static void main(String[] args) {
		new ExperimentNeurocomputing().runExperiment();
	}
}




