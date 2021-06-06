package gameObjects;

import java.util.HashMap;
import java.util.Random;

import json.JSONArray;
import json.JSONException;
import json.JSONObject;
import json.JSONUtil;
import world.Entity;

public class Chest extends Container {

	public static JSONObject chestLoot;
	
	public Chest (Entity e) {
		super (e);
		setCapacity (15);
		setAiTime (1);
	}
	
	public static void generateLoot (Entity e) {
		if (chestLoot == null) {
			loadLootFile ();
		}
		String lootId = e.getProperties ().get ("loot");
		JSONArray workingLoot = chestLoot.getJSONObject (lootId).getJSONArray ("items");
		int minStacks = chestLoot.getJSONObject (lootId).getInt ("minSpawns");
		String rawSeed = e.getProperties ().get ("seed");
		Random r;
		if (rawSeed != null) {
			int seed = Integer.parseInt (rawSeed);
			r = new Random (seed);
		} else {
			r = new Random ();
		}
		HashMap<String, String> data = e.getProperties ();
		if (workingLoot != null) {
			int spawns = 0;
			while (spawns < minStacks) {
				for (int i = 0; i < workingLoot.getContents ().size (); i++) {
					JSONObject curr = (JSONObject)workingLoot.get (i);
					double odds = curr.getDouble ("odds");
					double attempts = curr.getInt ("attempts");
					for (int j = 0; j < attempts; j++) {
						if (r.nextDouble () < odds) {
							//Spawn item in
							int slot = r.nextInt (15);
							if (data.get ("s" + slot).equals ("0x0")) {
								int id = curr.getInt ("id");
								int minAmt = curr.getInt ("minAmt");
								int maxAmt = curr.getInt ("maxAmt");
								int amt;
								if (minAmt == maxAmt) {
									amt = minAmt;
								} else {
									amt = r.nextInt (maxAmt - minAmt) + minAmt;
								}
								data.put ("s" + slot, id + "x" + amt);
								spawns++;
							}
						}
					}
				}
			}
		}
		data.put ("loot", "null");
	}
	
	public static void loadLootFile () {
		try {
			chestLoot = JSONUtil.loadJSONFile ("resources/gamedata/loot.json");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public void initPairedEntity (Entity e) {
		HashMap<String, String> chestMap = e.getProperties ();
		chestMap.put ("type", "Chest");
		chestMap.put ("loot", "null");
		for (int i = 0; i < 15; i++) {
			chestMap.put ("s" + i, "0x0");
		}
	}

}
