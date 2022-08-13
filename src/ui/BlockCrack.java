package ui;

import json.JSONObject;
import main.GameObject;
import resources.Sprite;
import resources.Spritesheet;
import world.World;

public class BlockCrack extends GameObject {
	
	private static Spritesheet crackSheet = new Spritesheet ("resources/sprites/crackstrip.png");
	private static Sprite crackSprite = new Sprite (crackSheet, 8, 8);
	
	private int damage = 0;
	private int displayFrame = 0;
	
	public BlockCrack () {
		setSprite (crackSprite);
	}
	
	public void breakTile (int amt, int x, int y, int layer) {
		
		//Do damage to the tile
		if ((int)getX () == x * 8 && (int)getY () == y * 8) {
			damage += amt;
		} else {
			setX (x * 8);
			setY (y * 8);
			damage = amt;
		}
		
		//Calculate the toughness and the display frame
		JSONObject tileProperties = World.getTileProperties (World.getTile (x, y, layer));
		int toughness = tileProperties.getInt ("toughness");
		displayFrame = (int)((((double)(damage)) / toughness) * 3);
		if (displayFrame >= 3) {
			displayFrame = 3;
		}
		
		//Break the tile if applicable
		if (damage > toughness) {
			World.getPlayer ().getTileInterface ().breakTile (x, y);
			setPosition (-64, -64);
		}
		
	}
	
	@Override
	public void draw () {
		getAnimationHandler ().setFrame (displayFrame);
		super.draw ();
	}
	
}
