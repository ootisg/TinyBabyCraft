package gameObjects;

import resources.Sprite;
import resources.Spritesheet;
import world.Entity;

public class BlockCrack extends EntityObject {

	public Spritesheet crackSheet = new Spritesheet ("resources/sprites/crackstrip.png");
	public Sprite crackSprite = new Sprite (crackSheet, 8, 8);
	
	public BlockCrack (Entity e) {
		super (e);
		setSprite (crackSprite);
		getAnimationHandler ().setAnimationSpeed (0);
		getAnimationHandler ().setFrame (2);
	}
	
}
