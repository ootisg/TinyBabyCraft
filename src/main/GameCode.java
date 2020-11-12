package main;

import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.HashMap;

import gameObjects.Player;
import gameObjects.StructSpawner;
import gameObjects.Zombie;
import resources.FileUtil;
import ui.Inventory;
import ui.TileInterface;
import world.Entity;
import world.World;

public class GameCode extends GameAPI {
	private GameWindow gameWindow;
	public void initialize () {
		gameWindow = MainLoop.getWindow ();
		gameWindow.setResolution (192, 144);
		World.initWorld ();
	}
	public void gameLoop () {
		World.worldFrame ();
		World.draw ();
		//inv.draw ();
	}
}