package ui;

import java.awt.Rectangle;

import main.GameObject;
import resources.Sprite;
import world.World;

public class DeathScreen extends GameObject {

	public static Sprite deathScreenOverlay = new Sprite ("resources/sprites/death_screen.png");
	
	public static Sprite respawnButtonDown = new Sprite ("resources/sprites/respawn_button_pressed.png");
	public static Sprite exitButtonDown = new Sprite ("resources/sprites/exit_game_button_pressed.png");
	
	public static final Rectangle respawnButtonBounds = new Rectangle (50, 77, 95, 18);
	public static final Rectangle exitButtonBounds = new Rectangle (50, 107, 95, 18);
	
	private int hoveredButton = 0; //0 is none, 1 is respawn, 2 is exit
	
	private boolean isHidden;
	
	public DeathScreen () {
		isHidden = true;
	}
	
	public void show () {
		isHidden = false;
	}
	
	public void hide () {
		isHidden = true;
	}
	
	@Override
	public void frameEvent () {
		if (!isHidden) {
			int x = getCursorX ();
			int y = getCursorY ();
			if (respawnButtonBounds.contains (x, y)) {
				hoveredButton = 1;
				if (mouseButtonClicked (0)) {
					World.getPlayer ().setHealth (100);
					hide ();
				}
			} else if (exitButtonBounds.contains (x, y)) {
				hoveredButton = 2;
				if (mouseButtonClicked (0)) {
					//Save and exit
					World.savePlayer ();
					World.saveReigons ();
					System.exit (0);
				}
			} else {
				hoveredButton = 0;
			}
		}
	}
	
	@Override
	public void draw () {
		if (!isHidden) {
			deathScreenOverlay.draw (0, 0);
			if (hoveredButton == 1) {
				respawnButtonDown.draw (respawnButtonBounds.x, respawnButtonBounds.y);
			}
			if (hoveredButton == 2) {
				exitButtonDown.draw (exitButtonBounds.x, exitButtonBounds.y);
			}
		}
	}
	
}
