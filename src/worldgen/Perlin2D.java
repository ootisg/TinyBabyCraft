package worldgen;

import java.util.HashMap;
import java.util.Random;

public class Perlin2D extends Function2D {

	private long seed;
	private HashMap<String, Vector2D> vectors;
	
	public Perlin2D (long seed) {
		this.seed = seed;
		vectors = new HashMap<String, Vector2D> ();
	}
	
	public Vector2D getVector (int x, int y) {
		
		//Check if vector is cached
		String vId = Integer.toString (x) + "," + Integer.toString (y);
		Vector2D cached = vectors.get (vId);
		if (cached != null) {
			return cached;
		}
		
		//Set the rng parameters
		Random r = new Random (seed);
		long yOffset = r.nextLong () % 131707310437L; //An 11-digit prime number
		long xOffset = r.nextLong () % 191373251117L;
		long origin = r.nextLong ();
		long newSeed = origin + x * xOffset + y * yOffset;
		r.setSeed (newSeed);
		
		//Get a 'random' direction
		double direction = r.nextDouble () * Math.PI * 2;
		
		//Generate and save the vector
		double vx = Math.cos (direction);
		double vy = Math.sin (direction);
		Vector2D newVector = new Vector2D (vx, vy);
		vectors.put (vId, newVector);
		return newVector;
		
	}

	@Override
	public double evaluate (double[] inputs) {
		double x = inputs [0];
		double y = inputs [1];
		int xgrid = (int)x;
		int ygrid = (int)y;
		double xrel = x - xgrid;
		double yrel = y - ygrid;
		double tldot = dotGradient (xgrid, ygrid, x, y);
		double trdot = dotGradient (xgrid + 1, ygrid, x, y);
		double bldot = dotGradient (xgrid, ygrid + 1, x, y);
		double brdot = dotGradient (xgrid + 1, ygrid + 1, x, y);
		double tdot = interpolate (xrel, tldot, trdot);
		double bdot = interpolate (xrel, bldot, brdot);
		double dot = interpolate (yrel, tdot, bdot);
		if (dot > 0) {
			return 1;
		} else {
			return -1;
		}
		//return dot;
	}
	
	public double dotGradient (int gridX, int gridY, double x, double y) {
		
		//Get the gradient vector
		Vector2D gradientVec = getVector (gridX, gridY);
		
		//Make the offset vector
		double dx = x - gridX;
		double dy = y - gridY;
		Vector2D offs = new Vector2D (dx, dy);
		
		//Dot product time
		return Vector2D.dot (gradientVec, offs);
		
	}
	
	public double interpolate (double pos, double a, double b) {
		double w = pos * pos * pos * (pos * (pos * 6 - 15) + 10); //6x^5 - 15x^4 + 10x^3
		return (1.0 - w) * a + w * b;
	}
}
