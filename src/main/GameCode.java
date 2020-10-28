package main;

import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.HashMap;

import gameObjects.Player;
import gameObjects.Zombie;
import resources.FileUtil;
import ui.Inventory;
import ui.TileInterface;
import world.World;

public class GameCode extends GameAPI {
	private GameWindow gameWindow;
	public void initialize () {
		gameWindow = MainLoop.getWindow ();
		gameWindow.setResolution (192, 144);
		World.initWorld ();
		HashMap<String, String> mappers = new HashMap<String, String> ();
		mappers.put ("YOLO1", "YEETUS");
		mappers.put ("YOLO2", "YOITIS");
		mappers.put ("YOLO3", "YOITIS");
		System.out.println (mappers);
		double[] fromFile = FileUtil.populateFromFile ("resources/gamedata/furnace.txt");
		System.out.println(Arrays.toString (fromFile));
	}
	public void gameLoop () {
		World.worldFrame ();
		World.draw ();
		//inv.draw ();
	}
}