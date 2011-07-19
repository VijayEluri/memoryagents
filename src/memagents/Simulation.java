package memagents;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

import sun.misc.Regexp;

import memagents.agents.Agent;
import memagents.environment.Environment;
import memagents.food.FoodGenerator;
import memagents.memory.IMemory;
import memagents.memory.Memory;
import memagents.utils.Log;

/**
 * Simulation leads the general processes and calculates results of agents' interaction.
 * 
 * @author Vojtech Kopal
 *
 */
public class Simulation 
{
	/**
	 *	Size of simulation stands for width and height of 2D matrix. 
	 *
	 */
	public static final int SIZE = 128;
	
	/**
	 *	The number of agents in the simulation. 
	 *
	 */
	public static final int NUM_AGENTS = 12;
	
	/**
	 *	Number of food generators in the simulation. 
	 *
	 */
	public static final int FOOD_NUM = 6;
	
	/**
	 * 	Speed of simulation (sleep in ms)
	 * 
	 */
	public static final int SPEED = 1; 
	
	/**
	 *	Speed of food growing (number of ticks).	
	 *
	 */
	public static final int FOOD_SPEED = 50; 
	
	/**
	 * 
	 * 
	 */
	public static final int FOOD_MAXRANGE = 12;
	
	/**
	 * 
	 * 
	 */
	public static final int FOOD_MINRANGE = 8;
	
	public static final int FOOD_ADDITION_PER_UNIT = 1;
	
	/**
	 * 
	 */
	public static final int AGENT_LEARNING_SPEED = 1;
	
	public static final int AGENT_SIGHT = 40; //20;
	
	public static final int AGENT_AUDITION = 30; //15;
	
	public static final int ANSWER_SAMPLE = 5;
	
	protected Environment environment;
	protected ArrayList<Agent> agents;
	protected ArrayList<FoodGenerator> generators;
	protected SimulationSettings settings = new SimulationSettings();
	protected Random random = null;
	public Random getRandom() { return random; }
	
	public Simulation() 
	{
		System.out.println("Simulation starts.");
		init();
	}
	
	/**
	 * Initialization of environment, agents and their random positions.
	 */
	private void init()
	{
		random = new Random(); random.setSeed(123456789);
		environment = new Environment(SIZE, SIZE);
		agents = new ArrayList<Agent>();
		generators = new ArrayList<FoodGenerator>();
		
		for (int i = 0; i < FOOD_NUM; i++) {
			int range = FOOD_MINRANGE;
			if (FOOD_MAXRANGE > FOOD_MINRANGE) {
				range += random.nextInt(FOOD_MAXRANGE - FOOD_MINRANGE);
			}
			int x = random.nextInt(SIZE - range*2) + range;
			int y = random.nextInt(SIZE - range*2) + range;
			FoodGenerator generator = new FoodGenerator(x, y, this);
			generator.setRange(range);
			generators.add(generator);
		}
		
		System.out.println("Simulation initialized.");
	}
	
	/**
	 * 
	 */
	public Agent addAgent(final Agent agent) {
		
		agent.setAudition(AGENT_AUDITION);
		agent.setSight(AGENT_SIGHT);
		
		//int agentX = random.nextInt(SIZE);
		//int agentY = random.nextInt(SIZE);
		
		agents.add(agent);	
		
		/// Position him into environment
		/// env.add(agentX, agentY, agent);
		
		//Log.println("init " + (agents.size() - 1) + " " + agentX + " " + agentY);
		
		//agent.setId(agents.size()-1);
	
		return agent;
	}
	
	/**
	 * 
	 */
	public Agent addAgent(Agent agent, int x, int y) {
		addAgent(agent);
		
		Point agentPosition = agent.getPosition();
		agentPosition.x = x;
		agentPosition.y = y;
		
		return agent;
	}
	
	public ArrayList<Agent> getAgents() {
		return agents;
	}
	
	/**
	 * Shouldn't be here. Makes protected variable accessible.
	 * @return BasicEnvironment
	 */
	public Environment getEnvironment()
	{
		return environment;
	}
	
	public int getSize() {
		return SIZE;
	}
	
	public FoodGenerator getGenerator(int foodKind) {
		for (FoodGenerator foodGenerator : this.generators) {
			if (foodGenerator.getKind() == foodKind) {
				return foodGenerator;
			}
		}
		
		return null;
	}
	
	public SimulationSettings getSettings() {
		return settings;
	}
	
	
	/**
	 * Runs the entire simulation. 
	 */
	/**
	 * 
	 */
	public void run(int maxDays) 
	{
		int simulationTime = 0;
		while (true)
		{
			long startTime = new Date().getTime();
			//Log.println("nextday");
			environment.initNextDay();
			
			for (Agent agent : agents) {
				// agent live
				agent.live();
				
				// agent hunger
				// adds 0.01 to all needs
				for (int foodKind = 0; foodKind < FoodGenerator.getSize(); foodKind++) {
					float amount = agent.getNeed(foodKind);
					amount += 0.0005; //0.0005;
					agent.setNeed(foodKind, amount);
				}
				
				agent.processMonitors();
				
				IMemory memory = agent.getMemory();
				if (memory != null) {
					memory.run();
				}
			}
			
			if ((simulationTime % FOOD_SPEED) == 0) {
				// foooood, hungryyy!!!
				for (FoodGenerator generator : generators) {
					generator.seed(environment);
				}
			}
			
			simulationTime++;
			if (simulationTime > maxDays) {
				break;
			}
			
			long endTime = new Date().getTime();
			
			System.out.println("Cycle time: " + (endTime - startTime) + " ms");
			
			try {
				Thread.sleep(SPEED);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		System.out.println("Simulation stopped after "+(simulationTime - 1)+" days");
	}
	
	public String toString() {
		String ret = null;
		StringBuilder sb = new StringBuilder();
		
		sb.append("Simulation\n================\n");
		sb.append("SIZE="); 				sb.append(Simulation.SIZE);sb.append("\n");
		sb.append("SPEED="); 				sb.append(Simulation.SPEED);sb.append("\n");
		sb.append("NUM_AGENTS="); 			sb.append(Simulation.NUM_AGENTS);sb.append("\n");
		sb.append("AGENT_AUDITION"); 		sb.append(Simulation.AGENT_AUDITION);sb.append("\n");
		sb.append("AGENT_LEARNING_SPEED="); sb.append(Simulation.AGENT_LEARNING_SPEED);sb.append("\n");
		sb.append("AGENT_SIGHT="); 			sb.append(Simulation.AGENT_SIGHT);sb.append("\n");
		sb.append("FOOD_ADDITION_PER_UNIT="); sb.append(Simulation.FOOD_ADDITION_PER_UNIT);sb.append("\n");
		sb.append("FOOD_MAXRANGE="); 		sb.append(Simulation.FOOD_MAXRANGE);sb.append("\n");
		sb.append("FOOD_MINRANGE="); 		sb.append(Simulation.FOOD_MINRANGE);sb.append("\n");
		sb.append("FOOD_NUM=");	 			sb.append(Simulation.FOOD_NUM);sb.append("\n");
		sb.append("FOOD_SPEED="); 			sb.append(Simulation.FOOD_SPEED);sb.append("\n");
		sb.append("ANSWER_SAMPLE="); 		sb.append(Simulation.ANSWER_SAMPLE);sb.append("\n");
		sb.append("\n");
		
		ret = sb.toString();
		return ret;
	}

}
