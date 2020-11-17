package worldgen;

public abstract class Function1D implements Function {

	public double evaluate (double x) {
		return evaluate (new double[] {x});
	}
	
}
