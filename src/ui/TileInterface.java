package ui;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.HashMap;

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
	
	@Override
	public void frameEvent () {
		if (enabled) {
			if (mouseButtonClicked (0)) {
				int workingId = World.getPlayer ().getSelectedItem ();
				int currentTile = World.getTile (getHoveredTileX (), getHoveredTileY ());
				if (workingId == 0) {
					
					//Find the drop of the tile
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
								System.out.println (amt);
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
					World.breakTile (getHoveredTileX (), getHoveredTileY ());
				} else if (currentTile != 24) {
					if (World.getPlayer ().useSelectedItem () == 1) {
						World.setTile (workingId, getHoveredTileX (), getHoveredTileY ());
						if (workingId == 26 || workingId == 27) {
							//Furnace
							HashMap<String, String> furnaceMap = Entity.getEntityMap ();
							furnaceMap.put ("type", "Furnace");
							furnaceMap.put ("s0", "0x0");
							furnaceMap.put ("s1", "0x0");
							furnaceMap.put ("s2", "0x0");
							furnaceMap.put ("x", String.valueOf (getHoveredTileX () * 8));
							furnaceMap.put ("y", String.valueOf (getHoveredTileY () * 8));
							furnaceMap.put ("fuel", "0");
							furnaceMap.put ("time", "0");
							Entity furnaceEntity = new Entity (furnaceMap);
							Furnace furnace = new Furnace (furnaceEntity);
							World.addEntity (furnaceEntity);
						}
					}
				}
			}
			if (mouseButtonClicked (2)) {
				int currentTile = World.getTile (getHoveredTileX (), getHoveredTileY ());
				if (currentTile == 25) {
					World.getPlayer ().open3x3CraftingGrid ();
				} else if (currentTile == 26) {
					Entity e = World.getTileEntity (getHoveredTileX (), getHoveredTileY ());
					GameObject eObj = e.getObject ();
					if (eObj instanceof Furnace) {
						Inventory.setContainer ((Container)eObj);
						World.getPlayer ().openFurnace ();
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
			Graphics g = MainLoop.getWindow ().getBufferGraphics ();
			g.setColor (cursorColor);
			g.drawRect (tileX * 8, tileY * 8, 7, 7);
		}
	}
	
	public int getHoveredTileX () {
		int mouseTile = getCursorX () / 8;
		return mouseTile + World.getViewX ();
	}
	
	public int getHoveredTileY () {
		int mouseTile = getCursorY () / 8;
		return mouseTile + World.getViewY ();
	}
	
	public void enable () {
		enabled = true;
	}
	
	public void disable () {
		enabled = false;
	}
}
