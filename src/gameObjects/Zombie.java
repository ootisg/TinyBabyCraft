package gameObjects;

import java.util.HashMap;

import ai.ChaseAI;
import resources.Sprite;
import resources.Spritesheet;
import world.Entity;
import world.World;

public class Zombie extends EntityObject {

	Spritesheet zombieSheet = new Spritesheet ("resources/sprites/zombie.png");
	Sprite zombieSprites = new Sprite (zombieSheet, 8, 12);
	
	ChaseAI ai;
	
	int animState = 0;
	
	public Zombie (double x, double y) {
		super (x, y);
		setSprite (zombieSprites);
		getAnimationHandler ().setAnimationSpeed (0);
		ai = new ChaseAI (this);
		setAiTime (1000);
	}
	
	public Zombie (Entity entity) {
		super (entity);
		setSprite (zombieSprites);
		getAnimationHandler ().setAnimationSpeed (0);
		ai = new ChaseAI (this);
		setAiTime (1000);
	}
	
	@Override
	public void aiStep () {
		if (Math.abs (this.getX () - World.getPlayer ().getX ()) > 160) {
			if (Math.random () < .02) {
				//1 in 25 chance to despawn
				this.forget ();
			}
		}
		int prevX = (int)this.getX ();
		ai.aiStep ();
		if (getX () > prevX) {
			animState |= 0x1;
			animState |= 0x2;
		} else if (getX () < prevX) {
			animState &= ~(0x1);
			animState |= 0x2;
		} else {
			animState &= ~(0x2);
		}
	}
	
	@Override
	public void draw () {
		//Offset for drawing
		getAnimationHandler ().setFrame (animState);
		setY (this.getY () - 4);
		super.draw ();
		setY (this.getY () + 4);
	}
}
