package worldgen;

public class TerrainHeightGen {

	Perlin1D noise;
	
	public TerrainHeightGen (long seed) {
		noise = new Fractal1D (seed);
	}
	
	public int getTerrainHeight (int x) {
		x = x + Integer.MAX_VALUE / 2;
		double amplitude = noise.evaluate (new double[] {(double)x / 50}) * 16;
		//System.out.println(amplitude);
		//System.out.println (amplitude);
		return (int)(63 + amplitude);
	}
	
}
