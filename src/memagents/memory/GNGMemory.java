package memagents.memory;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import memagents.Simulation;
import memagents.agents.Agent;
import memagents.food.FoodGenerator;
import memagents.memory.gng.EdgeGNG;
import memagents.memory.gng.MAComputeGNG;
import memagents.memory.gng.NodeGNG;
import memagents.memory.gng.NodePair;

public class GNGMemory extends Memory {
	
	protected HashMap<Integer, MAComputeGNG> gngEngines;
	protected Thread thread;
	
	public GNGMemory(int width, int height, Simulation simulation, Agent agent) {
		super(width, height, simulation, agent);
		
		this.gngEngines = new HashMap<Integer, MAComputeGNG>();
		
		for (int foodKind = 0; foodKind < FoodGenerator.getSize(); foodKind++) {
			MAComputeGNG gngEngine = new MAComputeGNG(this);
			// default: 0.8 1.0E-5 0.0050 6.0E-4 600 8
		    //gngEngine.setParams(0.8f, 1.0E-5f, 0.005f, 6.0E-4f, 600, 88, 5);
			gngEngine.setParams(0.8f, 1.0E-5f, 0.0005f, 6.0E-4f, 600, 2000, 5);
			
			// 0.5 1.0 0.0 1.0E-4 8
			// gngEngine.setParams(0.5f, 1.0f, 0.0f, 1.0E-4f, 600, 88, 8);
			// gngEngine.setParams(0.5f, 1.0f, 0.0f, 1.0E-4f, 600, 88, 8);
			gngEngines.put(foodKind, gngEngine);
		}

	}
	
	public void learn(int foodKind, ArrayList<Point> food) {
		MAComputeGNG gngEngine = gngEngines.get(foodKind);
		gngEngine.setDescreteSignals(food);
	}
	
	public Point[] getSample(int foodKind) {
		MAComputeGNG gngEngine = gngEngines.get(foodKind);
		gngEngine.computeExpectedDistribution();
		Point[] sample = new Point[numSamples];
		Random random = simulation.getRandom(); 
		int randomX = 0;
		int randomY = 0;
		
		for (int i = 0; i < numSamples; i++) {
			randomX = (int)(gngEngine.getExpectedValueX() + random.nextGaussian() * gngEngine.getExpectedVariance());
			randomY = (int)(gngEngine.getExpectedValueY() + random.nextGaussian() * gngEngine.getExpectedVariance());
			
			sample[i] = new Point(randomX, randomY);
		}
		
		return sample;
	}
	
	public Random getRandom() {
		return simulation.getRandom();
	}
	
	public HashMap<Integer, NodeGNG[]> getNodes() {
		HashMap<Integer, NodeGNG[]> nodes = new HashMap<Integer, NodeGNG[]>();
		
		for (int foodKind = 0; foodKind < FoodGenerator.getSize(); foodKind++) {
			MAComputeGNG gngEngine = gngEngines.get(foodKind);
			NodeGNG[] n = gngEngine.getNodes();
			nodes.put(foodKind, n);
		} 
		
		return nodes;
	}
	
	public ArrayList<NodePair> getEdges() {
		ArrayList<NodePair> lines = new ArrayList<NodePair>();
		
		for (int foodKind = 0; foodKind < FoodGenerator.getSize(); foodKind++) {
			MAComputeGNG gngEngine = gngEngines.get(foodKind);
			EdgeGNG[] n = gngEngine.getEdges();
			for (int i = 0; i < gngEngine.getNumEdges(); i++) {
				NodeGNG n1 = gngEngine.getNodeAt(n[i].getFrom());
				NodeGNG n2 = gngEngine.getNodeAt(n[i].getTo());
				
				lines.add(new NodePair(n1, n2));
			}
		} 
		
		return lines;
	}
	
	public Point getExpectedCenter(int foodKind) {
		gngEngines.get(foodKind).computeExpectedDistribution();
		return new Point((int)gngEngines.get(foodKind).getExpectedValueX(), (int)gngEngines.get(foodKind).getExpectedValueY());
	}
	
	public void run() {
		for (int foodKind = 0; foodKind < FoodGenerator.getSize(); foodKind++) {
			gngEngines.get(foodKind).learn();
		}
	}

	public HashMap<Integer, ExpectedGauss> getExpectedGausses() {
		HashMap<Integer, ExpectedGauss> gausses = new HashMap<Integer, ExpectedGauss>();
		for (int foodKind = 0; foodKind < FoodGenerator.getSize(); foodKind++) {
			ExpectedGauss gauss = new ExpectedGauss();
			gauss.x = (int)gngEngines.get(foodKind).getExpectedValueX();
			gauss.y = (int)gngEngines.get(foodKind).getExpectedValueY();
			gauss.var = gngEngines.get(foodKind).getExpectedVariance();
			gausses.put(foodKind, gauss);
		}
		return gausses;		
	}
}
