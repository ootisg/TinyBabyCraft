package worldgen;

public abstract class Function2D implements Function {

	public double evaluate (double x, double y) {
		return evaluate (new double[] {x, y});
	}
	
}
