package gameObjects;

import resources.Sprite;
import resources.Spritesheet;
import world.World;

public class Arrow extends EntityObject {

	public static Spritesheet arrowSheet = new Spritesheet ("resources/sprites/arrow.png");
	public static Sprite arrowsSprite = new Sprite (arrowSheet, 8, 8);
	
	private int direction = 0; //0 for left, 1 for right
	private boolean stuck = false;
	
	public Arrow (double x, double y) {
		super (x, y);
		setSprite (arrowsSprite);
		getAnimationHandler ().setAnimationSpeed (0);
		setAiTime (1);
	}
	
	@Override
	public void aiStep () {
		System.out.println (World.getPlayer ().getY () + ", " + getY ());
		if (!stuck) {
			setX (getX () + (direction == 0 ? -4 : 4));
			if (World.isSolid (World.getTile ((int)(getX () / 8), (int)(getY () / 8), 0))) {
				forget ();
			}
		}
		if (getX () == World.getPlayer ().getX ()) {
			if (getY () < World.getPlayer ().getY () + 2 && getY () > World.getPlayer ().getY () - 8) {
				World.getPlayer ().damage (10);
				forget ();
			}
		}
	}
	
	public int getDirection () {
		return direction;
	}
	
	public void setDirection (int direction) {
		this.direction = direction;
		getAnimationHandler ().setFrame (direction);
	}
	
}
