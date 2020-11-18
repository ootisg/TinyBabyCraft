package ai;

import java.awt.Point;

import gameObjects.EntityObject;

public abstract class MobAI {

	private EntityObject pairedObject;
	
	private Point target;
	
	protected MobAI (EntityObject pairedObject) {
		this.pairedObject = pairedObject;
	}
	
	public void setTarget (Point target) {
		this.target = target;
	}
	
	public Point getTarget () {
		return target;
	}
	
	public EntityObject getPairedObject () {
		return pairedObject;
	}
	
	public abstract void selectTarget ();
	public abstract void aiStep ();
	
}
