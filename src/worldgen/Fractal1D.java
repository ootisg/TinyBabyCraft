package worldgen;

public class Fractal1D extends Perlin1D {

	public Fractal1D (long seed) {
		super (seed);
	}
	
	@Override
	public double evaluate (double[] inputs) {
		double x = inputs [0];
		return (super.evaluate (new double[] {x}) + super.evaluate (new double[] {x * 2 + 291056}) + super.evaluate (new double[] {x * 3.2 + 21906801})) / 3;
	}

}
