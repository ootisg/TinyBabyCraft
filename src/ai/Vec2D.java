package ai;

import java.awt.Point;

public class Vec2D {

	private int x;
	private int y;
	
	public Vec2D (int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	public int getX () {
		return x;
	}
	
	public int getY () {
		return y;
	}
	
	public void setX (int x) {
		this.x = x;
	}
	
	public void setY (int y) {
		this.y = y;
	}
	
	public Point translate (Point in) {
		return new Point (in.x + x, in.y + y);
	}
	
}
