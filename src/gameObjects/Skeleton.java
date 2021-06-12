package gameObjects;

import ai.ChaseAI;
import resources.Sprite;
import resources.Spritesheet;
import world.Entity;
import world.World;

public class Skeleton extends EntityObject {

	public static Spritesheet skeletonSheet = new Spritesheet ("resources/sprites/skeleton.png");
	public static Sprite skeletonSprites = new Sprite (skeletonSheet, 8, 12);
	
	ChaseAI ai;
	
	int animState = 0;
	
	public Skeleton (double x, double y) {
		super (x, y);
		setSprite (skeletonSprites);
		getAnimationHandler ().setAnimationSpeed (0);
		ai = new ChaseAI (this);
		setAiTime (1000);
	}
	
	public Skeleton (Entity entity) {
		super (entity);
		setSprite (skeletonSprites);
		getAnimationHandler ().setAnimationSpeed (0);
		ai = new ChaseAI (this);
		setAiTime (1000);
	}
	
	@Override
	public void aiStep () {
		//Do despawning
		if (Math.abs (this.getX () - World.getPlayer ().getX ()) > 160) {
			if (Math.random () < .02) {
				//1 in 25 chance to despawn
				this.forget ();
			}
		}
		
		//Fall
		int tileX = (int)(getX () / 8);
		int wy = (int)(getY () / 8) + 1;
		while (!World.isSolid (World.getTile (tileX, wy)) && wy < 256) {
			wy++;
		}
		wy--;
		setPosition (getX (), wy * 8);
		
		if (getY () != World.getPlayer ().getY ()) {
			//Chase after the player
			int prevX = (int)this.getX ();
			ai.aiStep ();
			if (getX () > prevX) {
				animState |= 0x1;
				animState |= 0x2;
			} else if (getX () < prevX) {
				animState &= ~(0x1);
				animState |= 0x2;
			} else {
				if (Math.abs (getX () - World.getPlayer ().getX ()) != 8) {
					//Idle
					animState &= ~(0x2);
				}
			}
		} else {
			//Shoot
			Arrow arrow = new Arrow (getX (), getY () - 4);
			if (World.getPlayer ().getX () < getX ()) {
				arrow.setDirection (0);
			} else {
				arrow.setDirection (1);
			}
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
