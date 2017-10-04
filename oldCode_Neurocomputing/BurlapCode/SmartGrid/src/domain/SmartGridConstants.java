/**
 * 
 */
package domain;

/**
 * @author Felipe Leno da Silva
 *	Repository of constants to the SmartGrid Domain.
 *
 */
public class SmartGridConstants {

	//Number of Electric Vehicles in the transformer
	public static final int NUMBER_OF_EVS = 20;
	//Number of possible values of the battery attribute after discretization
	public static final int NUM_BATTERY_LEVELS = 5;
	
	// Name of attributes
	public static final String ATT_BATTERY = "bat";
	public static final String ATT_TRANSFORMER_LOAD = "load";
	public static final String ATT_AT_HOME = "at_home";
	
	
	//Possible values for at_home attribute
	public static final String IS_AT_HOME = "1";
	public static final String IS_AWAY    = "0";
	
	
	//Classes
	public static final String CLS_TRANSFORMER = "transformer";
	public static final String CLS_EV = "ev";
	
	//Actions
	public static final String ACTION_CHARGE = "charge";
	public static final String ACTION_NOT_CHARGE = "noCharge";
	
	//Thresholds to the battery be considered in a medium or high level
	public static final double MEDIUM_BATTERY_THRESHOLD = 40;
	public static final double HIGH_BATTERY_THRESHOLD = 80;
	
	//Transformer underload and overload thresholds
	public static final int TRANSFORMER_OVERLOAD = 7;
	public static final int TRANSFORMER_UNDERLOAD = 1;
	
	//The percentage of battery that is recharged per time step
	public static double RECHARGES_AT_STEP;
	
	//Initial value for the battery
	public static double INITIAL_BATTERY = 60;
	//Time Step per decision (in Minutes)
	public static final int TIME_STEP = 15;
	
	// Hours to recharge when the journey duration is DEFAULT_JOURNEY_DURATION_MEAN
	public static final double DEFAULT_AVERAGE_DAILY_NEED = 9;
	
	//Time to recharge 100% of the battery in hours
	public static final double DEFAULT_FULL_BATTERY_CHARGE = 11;
	
	//--------------------------------------------------------------
	// Wrongly specified values in these variables may cause infinite loops
	//----------------------------------------------------------------------
	//Mean and variance of the hour to begin the journey (in hours)
	public static final double DEFAULT_JOURNEY_START_MEAN = 9;
	public static final double DEFAULT_JOURNEY_START_VARIANCE = 2;
	
	
	//Mean and variance of the Journey Duration
	public static final double DEFAULT_JOURNEY_DURATION_MEAN = 10;
	public static final double DEFAULT_JOURNEY_DURATION_VARIANCE = 1;
	
	//------------------------------------------------------------------------

	//------------------------
	//Default reward values
	//------------------------
	//Successful journey
	public static final double DEFAULT_JOURNEY_OK_REWARD = +400;
	//Unsuccessful jorney
	public static final double DEFAULT_JOURNEY_NO_REWARD = 0;
	
	//Rewards For Objective 2
	public static final double DEFAULT_LOW_RECHARGING_REWARD = +400;
	public static final double DEFAULT_LOW_NOT_RECHARGING_REWARD = 0;
	public static final double DEFAULT_MEDIUM_RECHARGING_REWARD = +200;
	public static final double DEFAULT_MEDIUM_NOT_RECHARGING_REWARD = 0;
	public static final double DEFAULT_HIGH_RECHARGING_REWARD = +100;
	public static final double DEFAULT_HIGH_NOT_RECHARGING_REWARD = +50;
	
	//Rewards for Transformer objective (3)
	public static final double DEFAULT_OVERLOAD_REWARD = 0;
	public static final double DEFAULT_LOAD_OK_REWARD = +400;
	

	
	/**
	 * Attributes that are calculated
	 */
	static{
		RECHARGES_AT_STEP =  (double)SmartGridConstants.TIME_STEP / 60 * 100 / DEFAULT_FULL_BATTERY_CHARGE ;
	}
	
	
	


}
