/**
 * 
 */
package domain;

import java.util.Random;

import burlap.oomdp.auxiliary.DomainGenerator;
import burlap.oomdp.core.Attribute;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectClass;
import burlap.oomdp.core.objects.MutableObjectInstance;
import burlap.oomdp.core.objects.ObjectInstance;
import burlap.oomdp.core.states.MutableState;
import burlap.oomdp.core.states.State;
import burlap.oomdp.stochasticgames.SGAgentType;
import burlap.oomdp.stochasticgames.SGDomain;
import burlap.oomdp.stochasticgames.agentactions.SimpleSGAgentAction;

/**
 * @author Felipe Leno da Silva
 * Domain generator for the SmartGrid load balancing domain.
 *
 */
public class SmartGridLoadBalancing implements DomainGenerator {
	
	protected int numberOfEVs; //Number of Evs in the transformer
	
	protected EVInformation evInformation; //Saves informations about EVs
	
	protected SGAgentType agentType; //All agents are homogeneous in this domain.
	/**
	 * Constructor to specify the number of Electric Vehicles connected to a transformer
	 * @param numberOfEVs number of cars
	 */
	public SmartGridLoadBalancing(int numberOfEVs){
		this.numberOfEVs = numberOfEVs;
	}
	
	/**
	 * Loads the default parameters
	 */
	public SmartGridLoadBalancing(){
		this.numberOfEVs = SmartGridConstants.NUMBER_OF_EVS;
	}
	
	/**
	 * Generates a Multiagent SmartGrid Domain
	 */
	public Domain generateDomain() {
		SGDomain domain = new SGDomain();
		
		//--------------------
		//Attribute Definition
		//--------------------
		Attribute batteryAtt = new Attribute(domain, SmartGridConstants.ATT_BATTERY, Attribute.AttributeType.REAL);
		batteryAtt.setLims(0, 100);
		
		Attribute transformerAtt = new Attribute(domain, SmartGridConstants.ATT_TRANSFORMER_LOAD, Attribute.AttributeType.INT);
		transformerAtt.setLims(0, this.numberOfEVs);
		
		Attribute atHomeAtt = new Attribute(domain, SmartGridConstants.ATT_AT_HOME, Attribute.AttributeType.DISC);
		atHomeAtt.setDiscValues(new String[]{SmartGridConstants.IS_AT_HOME,SmartGridConstants.IS_AWAY});
		
		//-----------------------
		// Class Definition
		//----------------------
		ObjectClass agentClass = new ObjectClass(domain, SmartGridConstants.CLS_EV);
		agentClass.addAttribute(batteryAtt);
		agentClass.addAttribute(atHomeAtt);
		
		ObjectClass transformer = new ObjectClass(domain, SmartGridConstants.CLS_TRANSFORMER);
		transformer.addAttribute(transformerAtt);
		
		//-----------------
		//Action Definition
		//-----------------
			
		//Charge or Don't charge actions
		new SimpleSGAgentAction(domain, SmartGridConstants.ACTION_CHARGE);
		new SimpleSGAgentAction(domain, SmartGridConstants.ACTION_NOT_CHARGE);
		
		evInformation = new EVInformation();
		
		//Set transition dynamics
		domain.setJointActionModel(new SmartGridStandardMechanics(evInformation));
		
		this.agentType = new SGAgentType(SmartGridConstants.CLS_EV, domain.getObjectClass(SmartGridConstants.CLS_EV), domain.getAgentActions());
		
		return domain;
		
		
	}
	
	
	/**
	 * Creates a n initial state according to the currently specified parameter
	 * @param domain domain
	 * @param seed Random object  for generating agent distributions
	 * @return initial state
	 */
	public State getInitialState(Domain domain, Random seed){
		State s = new MutableState();
		evInformation.reset();
		//Create all agents
		for(int i=0;i<this.numberOfEVs;i++){
			ObjectInstance agent = new MutableObjectInstance(domain.getObjectClass(SmartGridConstants.CLS_EV), "ev"+(i));
			agent.setValue(SmartGridConstants.ATT_BATTERY, SmartGridConstants.INITIAL_BATTERY);
			agent.setValue(SmartGridConstants.ATT_AT_HOME, SmartGridConstants.IS_AT_HOME);
			evInformation.linkAgent(agent,seed.nextInt());
			s.addObject(agent);
		}
		//create transformer
		ObjectInstance trans = new MutableObjectInstance(domain.getObjectClass(SmartGridConstants.CLS_TRANSFORMER), "trans");
		trans.setValue(SmartGridConstants.ATT_TRANSFORMER_LOAD, 0);
		s.addObject(trans);
		return s;
		
	}

	public SGAgentType getAgentType() {
		return this.agentType;
	}
}
