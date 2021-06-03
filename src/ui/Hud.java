package ui;

import main.GameObject;
import resources.Sprite;
import resources.Spritesheet;
import world.World;

public class Hud extends GameObject {

	public static final int HEARTS_START_X = 5;
	public static final int HEARTS_START_Y = 18;
	public static final int HEARTS_SPACING = 6;
	
	public static Spritesheet heartSheet = new Spritesheet ("resources/sprites/heart.png");
	public static Sprite heartSprite = new Sprite (heartSheet, 5, 5);
	
	@Override
	public void draw () {
		int health = (int)Math.ceil (World.getPlayer ().getHealth ());
		for (int i = 0; i < 10; i++) {
			if (health <= 5) {
				heartSprite.setFrame (1);
			} else if (health > 0) {
				heartSprite.setFrame (0);
			}
			if (health <= 0) {
				heartSprite.setFrame (2);
			}
			heartSprite.draw (HEARTS_START_X + HEARTS_SPACING * i, HEARTS_START_Y);
			health -= 10;
		}
	}
	
}
