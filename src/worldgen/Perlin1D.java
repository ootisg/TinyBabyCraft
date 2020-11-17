package worldgen;

import java.util.HashMap;
import java.util.Random;

public class Perlin1D extends Function1D {

	private long seed;
	private HashMap<Integer, Vector2D> vectors;
	
	public Perlin1D (long seed) {
		this.seed = seed;
		vectors = new HashMap<Integer, Vector2D> ();
	}
	
	public Vector2D getVector (int x) {
		
		//Check if vector is cached
		Integer vId = x;
		Vector2D cached = vectors.get (vId);
		if (cached != null) {
			return cached;
		}
		
		//Set the rng parameters
		Random r = new Random (seed);
		long xOffset = r.nextLong () % 191373251117L;
		long origin = r.nextLong ();
		long newSeed = origin + x * xOffset;
		r.setSeed (newSeed);
		
		//Get a 'random' amplitude
		double amplitude = r.nextDouble () * 2 - 1;
		
		//Generate and save the vector
		Vector2D newVector = new Vector2D (0, amplitude);
		vectors.put (vId, newVector);
		return newVector;
		
	}

	@Override
	public double evaluate (double[] inputs) {
		double x = inputs [0];
		int xgrid = (int)x;
		double xrel = x - xgrid;
		double tdot = dotGradient (xgrid, x);
		double bdot = dotGradient (xgrid + 1, x);
		double dot = interpolate (xrel, tdot, bdot);
		return dot;
	}
	
	public double dotGradient (int gridX, double x) {
		
		//Get the gradient vector
		Vector2D gradientVec = getVector (gridX);
		
		//Make the offset vector
		double dx = x - gridX;
		Vector2D offs = new Vector2D (0, dx);
		
		//Dot product time
		return Vector2D.dot (gradientVec, offs);
		
	}
	
	public double interpolate (double pos, double a, double b) {
		double w = pos * pos * pos * (pos * (pos * 6 - 15) + 10); //6x^5 - 15x^4 + 10x^3
		return (1.0 - w) * a + w * b;
	}

}
