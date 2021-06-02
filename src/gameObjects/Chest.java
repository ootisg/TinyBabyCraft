package gameObjects;

import java.util.HashMap;

import world.Entity;

public class Chest extends Container {

	public Chest (Entity e) {
		super (e);
		setCapacity (15);
		setAiTime (1);
	}
	
	public static void generateLoot (Entity e) {
		HashMap<String, String> data = e.getProperties ();
		data.put ("s0", "2x4");
		data.put ("loot", "null");
	}
	
	@Override
	public void initPairedEntity (Entity e) {
		HashMap<String, String> chestMap = e.getProperties ();
		chestMap.put ("type", "Chest");
		chestMap.put ("loot", "null");
		chestMap.put ("seed", "0");
		for (int i = 0; i < 15; i++) {
			chestMap.put ("s" + i, "0x0");
		}
	}

}
