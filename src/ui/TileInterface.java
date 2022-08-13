package ui;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.HashMap;

import gameObjects.Chest;
import gameObjects.Container;
import gameObjects.Furnace;
import json.JSONArray;
import json.JSONObject;
import main.GameObject;
import main.MainLoop;
import world.Entity;
import world.World;

public class TileInterface extends GameObject {
	
	public static Color cursorColor = new Color (0xFFFFFF);
	
	private boolean enabled = true;
	
	private HashMap<String, PlaceScript> placeScripts;
	private HashMap<String, UseScript> useScripts;
	
	private BlockCrack crackAnim;
	
	private HashMap<String, String> toolMap;
	
	public TileInterface () {
		crackAnim = new BlockCrack ();
		crackAnim.declare (-64, -64);
		loadToolMap ();
	}
	
	@Override
	public void frameEvent () {
		if (enabled) {
			int workingId = World.getPlayer ().getSelectedItem ();
			int currentTile = World.getTile (getHoveredTileX (), getHoveredTileY (), 0); //TODO bg layer stuff
			JSONObject properties = World.getTileProperties (currentTile);
			if (reachableFromPlayer (getHoveredTileX (), getHoveredTileY ())) {
				if (mouseButtonClicked (0)) {
					if (currentTile != 24 && currentTile != 0 && !Boolean.TRUE.equals (properties.get ("fluid"))) {
						World.doPlacementLightCalculation (0, getHoveredTileX (), getHoveredTileY ());
						String tileType = World.getTileProperties (currentTile).getString ("type");
						String toolType = Inventory.itemProperties.getJSONObject (Integer.toString(workingId)).getString ("tool");
						Integer hitStrength = 2;
						if (tileType != null && toolType != null && toolMap.containsKey (tileType) && toolType.equals (toolMap.get (tileType))) {
							hitStrength = Inventory.itemProperties.getJSONObject (Integer.toString(workingId)).getInt ("power");
							if (hitStrength == null) {
								hitStrength = 2;
							}
						}
						crackAnim.breakTile (hitStrength, getHoveredTileX (), getHoveredTileY (), 0); //TODO bg layer stuff
					}
				}
				if (mouseButtonClicked (2)) {
					if (workingId < 256) {
						String useScriptId = World.getTileProperties (currentTile).getString ("useScript");
						if (useScriptId == null) {
							if (currentTile == 25) {
								World.getPlayer ().open3x3CraftingGrid ();
							} else if (currentTile == 26 || currentTile == 27) {
								Entity e = World.getTileEntity (getHoveredTileX (), getHoveredTileY ());
								GameObject eObj = e.getObject ();
								if (eObj instanceof Furnace) {
									Inventory.setContainer ((Container)eObj);
									World.getPlayer ().openFurnace ();
								}
							} else if (currentTile == 42) {
								Entity e = World.getTileEntity (getHoveredTileX (), getHoveredTileY ());
								if (!e.getProperties ().get ("loot").equals ("null")) {
									Chest.generateLoot (e);
								}
								GameObject eObj = e.getObject ();
								if (eObj instanceof Chest) {
									Inventory.setContainer ((Container)eObj);
									World.getPlayer ().openChest ();
								}
							} else if (currentTile == 0 || Boolean.TRUE.equals (properties.get ("fluid"))) {
								String placeScriptName = Inventory.itemProperties.getJSONObject (Integer.toString(workingId)).getString ("placeScript");
								if (placeScriptName == null) {
									if (World.getPlayer ().useSelectedItem () == 1) {
										World.placeTile (workingId, getHoveredTileX (), getHoveredTileY (), 0); //TODO bg layer stuff
										if (workingId == 26) {
											//Furnace
											Entity furnaceEntity = new Entity ();
											furnaceEntity.setPosition (getHoveredTileX () * 8, getHoveredTileY () * 8);
											Furnace furnace = new Furnace (furnaceEntity);
											furnace.initPairedEntity (furnaceEntity);
											World.addEntity (furnaceEntity);
										} else if (workingId == 42) {
											Entity chestEntity = new Entity ();
											chestEntity.setPosition (getHoveredTileX () * 8, getHoveredTileY () * 8);
											Chest chest = new Chest (chestEntity);
											chest.initPairedEntity (chestEntity);
											World.addEntity (chestEntity);
										}
									}
								} else {
									if (placeScripts == null) {
										loadPlaceScripts ();
									}
									if (placeScripts.get (placeScriptName).doPlace (workingId, getHoveredTileX (), getHoveredTileY (), 0)) { //TODO bg layer stuff
										World.getPlayer ().useSelectedItem ();
									}
								}
							}
						} else {
							if (useScripts == null) {
								loadUseScripts ();
							}
							useScripts.get (useScriptId).doUse (currentTile, getHoveredTileX (), getHoveredTileY ());
						}
					} else {
						World.getPlayer ().useSelectedItem ();
					}
				}
			}
		}
	}
	
	@Override
	public void draw () {
		if (enabled) {
			int tileX = getCursorX () / 8;
			int tileY = getCursorY () / 8;
			if (reachableFromPlayer (getHoveredTileX (), getHoveredTileY ())) {
				Graphics g = MainLoop.getWindow ().getBufferGraphics ();
				g.setColor (cursorColor);
				g.drawRect (tileX * 8, tileY * 8, 7, 7);
			}
		}
	}
	
	public static boolean reachableFromPlayer (int tileX, int tileY) {
		int centerX = tileX * 8 + 4;
		int centerY = tileY * 8 + 4;
		int centerPlayerX = (int)World.getPlayer ().getX () + 4;
		int centerPlayerY = ((int)World.getPlayer ().getY () - 4);
		int diffX = centerPlayerX - centerX;
		int diffY = centerPlayerY - centerY;
		if (Math.sqrt (diffX * diffX + diffY * diffY) <= 32 || World.getPlayer ().noclipEnabled ()) { //4 tile reach
			return true;
		} else {
			return false;
		}
	}
	
	public static int getHoveredTileX () {
		int mouseTile = getCursorX () / 8;
		return mouseTile + World.getViewX ();
	}
	
	public static int getHoveredTileY () {
		int mouseTile = getCursorY () / 8;
		return mouseTile + World.getViewY ();
	}
	
	public void enable () {
		enabled = true;
	}
	
	public void disable () {
		enabled = false;
	}
	
	public void loadPlaceScripts () {
		
		//Make the script map
		placeScripts = new HashMap<String, PlaceScript> ();
		
		//Init all the scripts
		placeScripts.put ("Stair", new PlaceScript.Stair ());
		placeScripts.put ("Slab", new PlaceScript.Slab ());
		placeScripts.put ("Door", new PlaceScript.Door ());
		placeScripts.put ("Sapling", new PlaceScript.Sapling ());
		
	}
	
	public void loadUseScripts () {
		
		//Make the script map
		useScripts = new HashMap<String, UseScript> ();
		
		//Init all the scripts
		useScripts.put ("DoorInteract", new UseScript.DoorInteract ());
		
	}
	
	public void loadToolMap () {
		
		toolMap = new HashMap<String, String> ();
		toolMap.put ("stone", "pickaxe");
		toolMap.put ("metal", "pickaxe");
		toolMap.put ("soil", "shovel");
		toolMap.put ("sand", "shovel");
		toolMap.put ("wood", "axe");
		toolMap.put ("leaves", "hoe");
		
	}
	
	public void breakTile (int x, int y) {
		
		//Find the drop of the tile
		int currentTile = World.getTile (x, y, 0); //TODO bg layer stuff
		String dropName = World.getTileProperties (currentTile).getString ("drop");
		if (dropName == null) {
			dropName = World.tileProperties.getJSONObject ("default").getString ("drop");
		}
		
		//Apply drops
		if (dropName.equals ("self")) {
			//Drop itself
			World.getPlayer ().addToInventory (currentTile, 1);
		} else {
			//Drop the appropriate drop table
			JSONArray dropTable = World.getDropTable (dropName);
			System.out.println(dropTable);
			ArrayList<Object> drops = dropTable.getContents ();
			for (int i = 0; i < drops.size (); i++) {
				JSONObject workingDrop = (JSONObject)drops.get (i);
				if (workingDrop.get ("special") != null) {
					System.out.println ("SPECIAL STUFF HERE");
				} else if (workingDrop.get ("min") != null && workingDrop.get ("max") != null) {
					//Amount is in a range from min to max
					int minAmt = workingDrop.getInt ("min");
					int maxAmt = workingDrop.getInt ("max");
					int amt = minAmt + (int)(Math.random () * (maxAmt - minAmt));
					int itemId = workingDrop.getInt ("id");
					//Give the player the items
					World.getPlayer ().addToInventory (itemId, amt);
				} else {
					//Amount is fixed
					Object objAmt = workingDrop.get ("count");
					int intAmt = 0;
					if (objAmt == null) {
						intAmt = (Integer)World.dropList.getJSONObject ("default").get ("count");
					} else {
						intAmt = (Integer)objAmt;
					}
					int itemId = workingDrop.getInt ("id");
					World.getPlayer ().addToInventory (itemId, intAmt);
				}
			}
		}
		
		World.breakTile (x, y, 0); //Replace with air TODO bg layer stuff
		
	}
	
}
