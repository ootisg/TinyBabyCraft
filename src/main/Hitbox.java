package main;

import java.awt.Color;

public class Hitbox {
	public int width;
	public int height;
	public int x;
	public int y;
	public Hitbox (int x, int y, int width, int height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}
	public boolean checkOverlap (Hitbox hitbox) {
		//Returns true if this hitbox is overlapping with another hitbox
		if (x > hitbox.x - width && x < hitbox.x + hitbox.width && y > hitbox.y - height && y < hitbox.y + hitbox.height) {
			return true;
		} else {
			return false;
		}
	}
	public double[] checkVectorCollision (Hitbox hitbox, double xTo, double yTo) {
		//Field collision detection using a hitbox and the coordinates it will be travelling to
		for (int i = 0; i < 4; i ++) {
			double x1 = this.x;
			double y1 = this.y;
			double x2 = xTo;
			double y2 = yTo;
			if (x >= 1 && x <= 3) {
				x1 += this.width;
				x2 += this.width;
			}
			if (y >= 2) {
				y1 += this.height;
				y2 += this.height;
			}
			double[] collidingCoords = new double[] {hitbox.x, hitbox.y, hitbox.x + hitbox.width, hitbox.y, hitbox.x + hitbox.width, hitbox.y + hitbox.height, hitbox.x, hitbox.y + hitbox.height, hitbox.x, hitbox.y};
			for (int j = 0; j <= 6; j += 2) {
				double[] isect = this.getIntersect (x1, y1, x2, y2, collidingCoords [j], collidingCoords [j + 1], collidingCoords [j + 2], collidingCoords [j + 3]);
				if (isect != null) {
					double xmod = 0;
					double ymod = 0;
					if (i >= 1 && i <= 3) {
						xmod = -1 * this.width;
					}
					if (i >= 2) {
						ymod = -1 * this.height;
					}
					return new double[] {isect [0] + xmod, isect [1] + ymod};
				}
			}
		}
		return null;
	}
	public double[] getIntersect (double x1, double y1, double x2, double y2, double x3, double y3, double x4, double y4) {
		//Returns the point at which two lines intersect in the format [x, y], and returns null if they do not intersect
		double[] intersectCoords = new double[2];
		if (y1 == y2 && y3 == y4) {
			return null;
		}
		if ((y1 - y2) / (x1 - x2) == (y3 - y4) / (x3 - x4)) {
			return null;
		}
		if (x1 == x2) {
			double temp = x1;
			x1 = x3;
			x3 = temp;
			temp = x2;
			x2 = x4;
			x4 = temp;
			temp = y1;
			y1 = y3;
			y3 = temp;
			temp = y2;
			y2 = y4;
			y4 = temp;
		}
		double m1 = (y1 - y2) / (x1 - x2);
		double b1 = y1 - m1 * x1;
		if (x3 == x4) {
			intersectCoords [0] = x3;
			intersectCoords [1] = m1 * x3 + b1;
		} else {
			double m2 = (y3 - y4) / (x3 - x4);
			double b2 = y3 - m2 * x3;
			if (m1 == m2) {
				return null;
			}
			//General line intersection, derived from substitution
			//y = m1 * x + b1
			//y = m2 * x + b2
			//m1 * x + b1 = m2 * x + b2
			//m1 * x = m2 * x + b2 - b1
			//m1 * x - m2 * x = b2 - b1
			//x * (m1 - m2) = b2 - b1
			//x = (b2 - b1) / (m1 - m2)
			intersectCoords [0] = (b2 - b1) / (m1 - m2);
			intersectCoords [1] = m1 * intersectCoords [0] + b1;
		}
		if (!isBetween (intersectCoords [0], x1, x2)) {
			return null;
		}
		if (x3 == x4) {
			if (!isBetween (intersectCoords [1], y3, y4)) {
				return null;
			}
		} else {
			if (!isBetween (intersectCoords [0], x3, x4)) {
				return null;
			}
		}
		return intersectCoords;
	}
	public boolean isBetween (double num, double bound1, double bound2) {
		//Returns true if num is between bound1 and bound2
		if (bound1 >= bound2) {
			double temp = bound2;
			bound2 = bound1;
			bound1 = temp;
		}
		return (num >= bound1 && num <= bound2);
	}
}