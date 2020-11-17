package worldgen;

public class Vector2D {

	double x;
	double y;
	
	public Vector2D (double x, double y) {
		this.x = x;
		this.y = y;
	}
	
	public static double dot (Vector2D a, Vector2D b) {
		return a.x * b.x + a.y * b.y;
	}
	
}
