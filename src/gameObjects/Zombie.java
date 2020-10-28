package gameObjects;

import java.util.HashMap;

import resources.Sprite;
import resources.Spritesheet;
import world.Entity;

public class Zombie extends EntityObject {

	Spritesheet zombieSheet = new Spritesheet ("resources/sprites/zombie.png");
	Sprite zombieSprites = new Sprite (zombieSheet, 8, 12);
	
	public Zombie (double x, double y) {
		super (x, y);
		setSprite (zombieSprites);
		getAnimationHandler ().setAnimationSpeed (0);
		setAiTime (1000);
	}
	
	public Zombie (Entity entity) {
		super (entity);
		setSprite (zombieSprites);
		getAnimationHandler ().setAnimationSpeed (0);
		setAiTime (1000);
	}
	
	@Override
	public void aiStep () {
		//System.out.println("HIA");
		//setX (getX () + 8);
	}
}
