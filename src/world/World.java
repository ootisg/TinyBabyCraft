package world;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;
import java.util.UUID;

import gameObjects.EntityObject;
import gameObjects.Player;
import gameObjects.StructSpawner;
import gameObjects.Zombie;
import json.JSONArray;
import json.JSONException;
import json.JSONObject;
import json.JSONUtil;
import main.GameAPI;
import main.GameObject;
import main.MainLoop;
import resources.Sprite;
import resources.Spritesheet;
import scripts.StructureScript;
import worldgen.TerrainHeightGen;

public class World {
	
	public static final int WORLD_HEIGHT = 128;
	public static final int LOAD_SIZE = 256;
	public static final int SECONDARY_LOAD_RADIUS = 64;
	public static final int SECONDARY_UNLOAD_RADIUS = SECONDARY_LOAD_RADIUS + 8;
	public static final int SCREEN_SIZE_H = 24; //Number of tiles that fit on the screen horizontally
	public static final int SCREEN_SIZE_V = 18; //Number of tiles that fit on the screen vertically
	
	public static final Spritesheet TILE_SHEET = new Spritesheet ("resources/sprites/tiles.png");
	public static final Spritesheet ITEM_SHEET = new Spritesheet ("resources/sprites/items.png");
	public static final Spritesheet LIGHT_SHEET = new Spritesheet ("resources/sprites/lighting.png");
	public static final Sprite PARSED_TILES = new Sprite (TILE_SHEET, 8, 8);
	public static final Sprite PARSED_ITEMS = new Sprite (ITEM_SHEET, 8, 8);
	public static final Sprite PARSED_LIGHTING = new Sprite (LIGHT_SHEET, 8, 8);
	
	private static int viewX;
	private static int viewY;
	
	private static int loadLeft;
	private static int loadRight;
	
	private static HashMap<Integer, WorldReigon> reigonsMap;
	private static LinkedList<Entity> entities;
	private static HashMap<Point, Entity> tileEntities;
	
	private static Player player;
	
	private static long seed = 69;
	
	private static String worldName = "default";
	
	private static HashMap<String, Structure> structures;
	
	private static long worldTime;
	private static int globalTickCount;
	private static int loadedDimension = 0;
	private static HashMap<Integer, ArrayList<Point>> schedTicks;
	
	public static JSONObject tileProperties;
	public static JSONObject dropList;
	
	private static int[] tileLightTable = new int[256];
	private static int[] tileSolidTable = new int[256];
	private static int[] tileTpTable = new int[256];
	
	private static int skylight = 15;
	
	static TerrainHeightGen heightGen;
	
	public static void initWorld () {
		
		try {
			tileProperties = JSONUtil.loadJSONFile ("resources/gamedata/tiles.json");
			dropList = JSONUtil.loadJSONFile ("resources/gamedata/drops.json");
		} catch (JSONException e) {
			e.printStackTrace ();
			System.exit (1);
		}
		
		//Initialze the global tick count/sched ticks
		globalTickCount = 0;
		schedTicks = new HashMap<Integer, ArrayList<Point>> ();
		
		viewX = 0;
		viewY = 0; //Initialize view
		loadLeft = -128;
		loadRight = 128; //Initialize loading bounds
		
		//Load the world resources
		loadStructures ();
		
		//Load the various tile properties
		populateTileProperties ();
		
		//Load the worldgen resources
		makeWorldGenResources ();
		
		File f = new File ("saves/" + worldName);
		if (!f.exists ()) {
			f.mkdir (); //TODO check if directory was not created
		} //Makes the directory for the world if it wasn't already created
		
		reigonsMap = new HashMap<Integer, WorldReigon> ();
		
		//Init entity properties
		Entity.initTypeProperties ();
		
		//Setup entity maps
		entities = new LinkedList<Entity> (); //Make the world entities list
		tileEntities = new HashMap<Point, Entity> ();
		
		initLighting ();
		updateReigons ();
		updateWorld (); //Fill the world
		//Make the player
		spawnPlayer ();
		
		//Init fluid ids
		Fluids.initFluidIDs ();
		
	}
	
	public static void makeWorldGenResources () {
		heightGen = new TerrainHeightGen (seed);
	}
	
	public static void initLighting () {
		//TODO
	}
	
	public static void populateTileProperties () {
		populateTilePropertyArray ("light", tileLightTable);
		populateTilePropertyArray ("transparent", tileTpTable);
		populateTilePropertyArray ("solid", tileSolidTable);
	}
	
	public static void populateTilePropertyArray (String propertyName, int[] propertyArr) {
		for (int i = 0; i < 256; i++) {
			JSONObject workingProperties = getTileProperties (i);
			if (workingProperties != null) {
				if (workingProperties.get (propertyName) != null) {
					Object val = workingProperties.get (propertyName);
					putTilePropertyElem (val, propertyArr, i);
				} else {
					Object val = tileProperties.getJSONObject ("default").get (propertyName);
					putTilePropertyElem (val, propertyArr, i);
				}
			}
		}
	}
	
	private static void putTilePropertyElem (Object value, int[] arr, int pos) {
		if (value instanceof Integer) {
			arr [pos] = (int)value;
		} else if (value instanceof Boolean) {
			if ((boolean)value) {
				arr [pos] = 1;
			}
		}
	}
	
	public static void worldFrame () {
		worldTime = System.currentTimeMillis ();
		if (GameAPI.keyPressed('Q')) {
			savePlayer ();
			unloadAll ();
			loadedDimension = loadedDimension == 0 ? 1 : 0;
			World.updateReigons ();
		}
	}
	
	public static void loadStructures () {
		structures = new HashMap<String, Structure> ();
		
		File dir = new File ("resources/gamedata/structures");
		File[] structPaths = dir.listFiles ();
		for (int i = 0; i < structPaths.length; i++) {
			
			//Check if the file is a JSON file
			String filename = structPaths [i].getName ();
			String[] fileSplit = filename.split ("\\.");
			if (fileSplit.length == 2 && fileSplit [1].equals ("json")) {
				
				//Read the file and make a structure object
				Structure struct = new Structure ();
				try {
					struct.setProperties (JSONUtil.loadJSONFile (structPaths [i].getPath ()));
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				//Put the structure in the list of structures
				structures.put (struct.getName (), struct);
			}
		}
		
		
		/*int count = 0;
		int iter = 0;
		boolean found = false;
		while (count < structPaths.length && iter < 1000) {
			Structure structure = new Structure ();
			File metaFile = structures.get (iter + "m.txt");
			File tileFile = structures.get (iter + "t.txt");
			File entityFile = structures.get (iter + "e.txt");
			if (metaFile != null) {
				ArrayList<String> attributes = new ArrayList<String> ();
				Scanner s;
				try {
					s = new Scanner (metaFile);
					while (s.hasNextLine ()) {
						attributes.add (s.nextLine ());
					}
					structure.setProperties (attributes);
					s.close ();
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				found = true;
				count++;
			}
			if (tileFile != null) {
				ArrayList<String> rows = new ArrayList<String> ();
				Scanner s;
				try {
					s = new Scanner (tileFile);
					while (s.hasNextLine ()) {
						rows.add (s.nextLine ());
					}
					structure.setTiles (rows);
					s.close ();
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				found = true;
				count++;
			}
			if (entityFile != null) {
				found = true;
				count++;
			}
			if (found) {
				World.structures.put (structure.getName (), structure);
			}
			iter++;
		}*/
	}
	
	public static void spawnPlayer () {
		String filepath = "saves/" + worldName + "/playerdat.txt";
		File f = new File (filepath); //Get the file to save to
		boolean newWorld = false;
		if (!f.exists ()) {
			try {
				f.createNewFile ();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			newWorld = true;
		} //Make new playerdat file to save to if it doesn't already exist
		
		if (newWorld) {
			player = new Player ();
			player.declare (0, 496);
			FileWriter fw;
			try {
				fw = new FileWriter (f);
				fw.append ("x:0\ny:496\ndimension:0");
				fw.close ();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			Scanner s = null;
			try {
				s = new Scanner (f);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			ArrayList<String> properties = new ArrayList<String> ();
			while (s.hasNextLine ()) {
				properties.add (s.nextLine ());
			}
			s.close (); //Get player attributes from file
			
			HashMap<String, String> playerAttributes = new HashMap<String, String> ();
			for (int i = 0; i < properties.size (); i++) {
				String working = properties.get (i);
				String[] split = working.split (":");
				playerAttributes.put (split [0], split [1]);
			} //Put attributes into a HashMap
			
			//Set the dimension to the dimension the player is currently in
			loadedDimension = Integer.parseInt (playerAttributes.get ("dimension"));
			
			player = new Player (playerAttributes); //Make a player with the given attributes
		}
	}
	
	public static void draw () {
		
		//Draw the background (sky)
		Graphics g = MainLoop.getWindow ().getBufferGraphics ();
		switch (loadedDimension) {
			case 0:
				g.setColor (new Color (0x97ECEF));
				break;
			case 1:
				g.setColor (new Color (0x600000));
				break;
			case 2:
				break;
			default:
				break;
		}
		g.fillRect (0, 0, SCREEN_SIZE_H * 8, SCREEN_SIZE_V * 8);
		
		//Draw the loaded reigons
		Iterator<Entry<Integer, WorldReigon>> iter = reigonsMap.entrySet ().iterator ();
		while (iter.hasNext ()) {
			iter.next ().getValue ().draw ();
		}

	}
	
	public static int getLoadedDimension () {
		return loadedDimension;
	}
	
	public static int getViewX () {
		return viewX;
	}
	
	public static int getViewY () {
		return viewY;
	}
	
	public static String getWorldName () {
		return worldName;
	}
	
	//0 for fg layer, 1 for bg layer
	public static int getTile (int x, int y, int layer) {
		if (y < 0 || y >= WORLD_HEIGHT) {
			return 24;
		}
		int rg = getReigonId (x);
		int rgX = x - rg * WorldReigon.REIGON_SIZE;
		return getReigon (rg).getTile (rgX, y, layer);
	}
	
	public static boolean isSolid (int id) {
		return tileSolidTable [id] == 1;
	}
	
	public static Player getPlayer () {
		return player;
	}
	
	public static void setViewX (int view) {
		viewX = view;
	}
	
	public static void setViewY (int view) {
		viewY = view;
	}
	
	public static void setLoadBounds (int left, int right) {
		loadLeft = left;
		loadRight = right;
	}
	
	public static int getCeilingHeight (int x) {
		int rx = Math.floorMod (x, LOAD_SIZE);
		return getReigon (x).getCeilingHeight (rx);
	}
	
	//Places AND ticks
	public static void placeTile (int id, int x, int y, int layer) {
		
		doPlacementLightCalculation (id, x, y);
		setTile (id, x, y, layer);
		tickNearby (x, y);
		
	}
	
	public static void setTile (int id, int x, int y, int layer) {
		try {
			int rg = getReigonId (x);
			int rgX = Math.floorMod (x, WorldReigon.REIGON_SIZE);
			getReigon (rg).setTile (id, rgX, y, layer);
		} catch (IndexOutOfBoundsException e) {
			//Do nothing
		}
	}
	
	public static void markTile (int x, int y, int val) {
		int drawX = x * 8 - viewX * 8;
		int drawY = y * 8 - viewY * 8;
		System.out.println (viewY);
		System.out.println (drawX + ", " + drawY);
		Font f = new Font ("courier", 1, 8);
		Graphics2D g = (Graphics2D)MainLoop.getWindow ().getBufferGraphics ();
		g.setFont (f);
		g.setColor (new Color (0xFF0000));
		g.drawString (String.valueOf (val), drawX, drawY);
	}
	
	public static void doPlacementLightCalculation (int id, int x, int y) {
		if (tileLightTable [getTile (x, y, 0)] != 0) {
			removeLightSource (tileLightTable [getTile (x, y, 0)], x, y);
		}
		if (tileLightTable [id] != 0) {
			putLightSource (tileLightTable [id], x, y);
		}
	}
	
	public static void putLightSource (int strength, int x, int y) {
		//TODO
	}
	
	public static void removeLightSource (int strength, int x, int y) {
		//TODO
	}
	
	public static void lightColumn (int x) {
		//TODO
	}
	
	public static void unlightColumn (int x) {
		//TODO
	}
	
	public static int getLightStrength (int x, int y) {
		//TODO
		return 0;
	}
	
	public static int computeLight (int x, int y) {
		return 15;
		//TODO add skylight computation
		/*int realX = Math.floorMod (x, LOAD_SIZE);
		ArrayList<Point> lightList = lights.get (realX).get (y);
		int totalLight;
		int ceilingY = getCeilingHeight (x);
		if (ceilingY >= y) {
			totalLight = skylight;
		} else if (y - ceilingY >= 4) {
			totalLight = 0;
		} else {
			totalLight = (int)((double)(skylight / 4) * (4 - (y - ceilingY)));
		}
		for (int i = 0; i < lightList.size (); i++) {
			
			//Get our wonderful point
			Point ls = lightList.get (i);
			
			//Get the light strength
			int tId = getTile (ls.x, ls.y, 0);
			JSONObject properties = getTileProperties (tId);
			int strength = tileLightTable [tId];
			
			//Compute lighting based on the given strength value
			int xDif = ls.x - x;
			int yDif = ls.y - y;
			double dist = Math.sqrt (xDif * xDif + yDif * yDif);
			if (dist < strength) {
				int shineStrength = (int)(((strength - dist) / strength) * 15);
				totalLight += shineStrength;
				if (totalLight > 15) {
					return 15;
				}
			}
		}
		return totalLight;*/
	}
	
	public static Entity getTileEntity (int x, int y) {
		Point p = new Point (x * 8, y * 8);
		return tileEntities.get (p);
	}
	
	public static void removeTileEntity (int x, int y) {
		Point p = new Point (x * 8, y * 8);
		tileEntities.remove (p);
	}
	
	public static void breakTile (int x, int y, int layer) {
		
		//Clear out the tile
		setTile (0, x, y, layer);
		
		//Remove tile entities, if applicable
		Entity e = getTileEntity (x, y);
		if (e != null) {
			removeEntity (e);
		}
		
		//Tick the surrounding area
		tickNearby (x, y);
		
	}
	
	public static void refreshLoadAround (int x) {
		//TODO this needs to be entirely re-done
		for (int colX = x - SECONDARY_UNLOAD_RADIUS; colX < x + SECONDARY_UNLOAD_RADIUS; colX++) {
			if (colX > x - SECONDARY_LOAD_RADIUS && colX < x + SECONDARY_LOAD_RADIUS) {
				if (!columnLoaded (colX)) {
					loadColumn (colX);
				}
			} else {
				if (columnLoaded (colX)) {
					unloadColumn (colX);
				}
			}
		}
	}
	
	public static void loadColumn (int x) {
		int rgX = Math.floorMod (x, WorldReigon.REIGON_SIZE);
		for (int i = 0; i < WORLD_HEIGHT; i++) {
			Point p = new Point (x * 8, i * 8);
			if (tileEntities.get (p) != null && tileEntities.get (p).getObject () instanceof StructSpawner) {
				((StructSpawner)tileEntities.get (p).getObject ()).spawnStructure ();
			}
		}
		getReigon (getReigonId (x)).loadColumn (rgX);
	}
	
	public static void unloadColumn (int x) {
		int rgX = Math.floorMod (x, WorldReigon.REIGON_SIZE);
		getReigon (getReigonId (x)).unloadColumn (rgX);
	}
	
	public static boolean columnLoaded (int x) {
		int rgX = Math.floorMod (x, WorldReigon.REIGON_SIZE);
		return getReigon (getReigonId (x)).columnLoaded (rgX);
	}
	
	public static void unloadAllColumns () {
		//TODO this needs to be entirely re-done
//		for (int i = 0; i < LOAD_SIZE; i++) {
//			loaded [i] = false;
//		}
	}
	
	public static boolean inLoadBounds (int x) {
		return columnLoaded (x);
	}
	
	public static int getReigonId (int x) {
		return Math.floorDiv (x, WorldReigon.REIGON_SIZE);
	}
	
	public static WorldReigon getReigon (int x) {
		
		//Retrieve the reigon
		WorldReigon rg = reigonsMap.get (x);
		
		if (rg == null) {
			//Reigon was not loaded, load it then return it
			return loadReigon (getReigonId (x), loadedDimension);
		} else {
			//Reigon was loaded, return it immediately
			return rg;
		}
		
	}
	
	public static boolean isReigonLoaded (int id, int dimension) {
		//TODO make dimension work here
		return reigonsMap.containsKey (id);
	}
	
	public static int unloadReigonsOutsideMap () {
		int count = 0;
		Iterator<Entry<Integer, WorldReigon>> iter = reigonsMap.entrySet ().iterator ();
		while (iter.hasNext ()) {
			WorldReigon current = iter.next ().getValue ();
			if ((current.id + 1) * WorldReigon.REIGON_SIZE < loadLeft || current.id * WorldReigon.REIGON_SIZE > loadRight) {
				current.unload ();
				iter.remove ();
				count++;
			}
		}
		return count;
	}
	
	public static void updateReigons () {
		int prevId = 0;
		for (int wx = loadLeft; wx < loadRight; wx++) {
			int rgId = getReigonId (wx);
			if (rgId != prevId || wx == loadLeft) {
				if (!isReigonLoaded (rgId, 0)) {
					loadReigon (rgId, loadedDimension);
				}
			}
			prevId = rgId;
		} //Load in new reigons
		
		unloadReigonsOutsideMap (); //If you can't figure this one out then you're a dumbass
	}
	
	public static void updateWorld () {
		//TODO this needs to be entirely re-done
	}
	
	public static WorldReigon loadReigon (int id, int dimension) {
		//TODO this needs to be entirely re-done or reconsidered
		WorldReigon rg = new WorldReigon (id, dimension);
		reigonsMap.put (id, rg);
		rg.load ();
		return rg;
	}
	
	public static void saveReigons () {
		Set<Entry<Integer, WorldReigon>> allRgs = reigonsMap.entrySet ();
		Iterator<Entry<Integer, WorldReigon>> iter = allRgs.iterator ();
		while (iter.hasNext ()) {
			iter.next ().getValue ().save ();
		}
	}
	
	public static void unloadAll () {
		Set<Entry<Integer, WorldReigon>> allRgs = reigonsMap.entrySet ();
		Iterator<Entry<Integer, WorldReigon>> iter = allRgs.iterator ();
		while (iter.hasNext ()) {
			iter.next ().getValue ().unload ();
		}
		reigonsMap = new HashMap<Integer, WorldReigon> ();
	}
	
	public static void savePlayer () {
		player.save ();
	}
	
	//Layering: 0 for foreground, 1 for background
	public static ArrayList<Integer> generateColumn (int x, int layer, int dimension) {
		
		Integer[] result = new Integer[WORLD_HEIGHT];
		ArrayList<Integer> arrList = new ArrayList<Integer> ();
		//Overworld
		if (dimension == 0) {
			int SEA_LEVEL = 63;
			int genHeight = heightGen.getTerrainHeight (x);
			if (layer == 0) {
				for (int i = 0; i < genHeight; i++) {
					if (i < SEA_LEVEL) {
						result [i] = 0;
					} else if (i == SEA_LEVEL) {
						result [i] = 11;
					} else {
						result [i] = 11;
					}
				}
				result[genHeight] = 1;
				result[genHeight + 1] = 2;
			} else if (layer == 1) {
				for (int i = 0; i < genHeight; i++) {
					if (i < SEA_LEVEL) {
						result [i] = 0;
					} else if (i == SEA_LEVEL) {
						result [i] = 0;
					} else {
						result [i] = 0;
					}
				}
				result[genHeight] = 2;
				result[genHeight + 1] = 2;
			}
			for (int i = genHeight + 2; i < WORLD_HEIGHT; i++) {
				result [i] = 16;
			}
		}
		
		//Nether
		if (dimension == 1) {
			int SEA_LEVEL = 63;
			int genHeight = heightGen.getTerrainHeight (x);
			for (int i = 0; i < genHeight; i++) {
				if (i < SEA_LEVEL) {
					result [i] = 0;
				} else if (i == SEA_LEVEL) {
					result [i] = 13;
				} else {
					result [i] = 13;
				}
			}
			result[genHeight] = 31;
			result[genHeight + 1] = 31;
			for (int i = genHeight + 2; i < WORLD_HEIGHT; i++) {
				result [i] = 31;
			}
		}
		
		//End
		if (dimension == 2) {
			
		}
		
		Collections.addAll (arrList, result);
		return arrList;
		
	}
	
	public static BufferedImage getItem (int id) {
		if (id < 256) {
			return PARSED_TILES.getImageArray ()[id];
		} else {
			return PARSED_ITEMS.getImageArray ()[id - 256];
		}
	}
	
	public static long getWorldTime () {
		return worldTime;
	}
	
	public static void spawnStructure (String id, int x, int y) {
		
		//Get the structure
		Structure s = structures.get (id);
		
		//Coordinate stuff
		Point origin = s.getOrigin ();
		Point topLeft = new Point (x - origin.x, y - origin.y);
		
		//Get important attributes
		//Replace tiles attribute
		boolean replace = false;
		if (s.getMetaProperty ("replace") != null) {
			replace = true;
		}
		
		//Spawn over attribute
		int spawnOver = -1;
		if (s.getMetaProperty ("spawn_over") != null) {
			spawnOver = (int)s.getMetaProperty ("spawn_over");
		}
		
		//Spawn odds attribute
		double spawnOdds = Double.NaN;
		if (s.getMetaProperty ("spawn_odds") != null) {
			spawnOdds = (double)s.getMetaProperty ("spawn_odds");
		}
		
		//Make our random
		Random r = new Random ();
		
		//Spawn in the tiles
		//TODO layers
		for (int wx = 0; wx < s.getWidth (); wx++) {
			int[] slice = s.getSlice (wx - origin.x);
			for (int wy = 0; wy < s.getHeight (); wy++) {
				if (!(!replace && slice [wy] == 0)) {
					int putX = topLeft.x + wx;
					int putY = topLeft.y + wy;
					r.setSeed (seed + putY * 2860486313L + putX * 49390927L); //More prime number magic
					//Big chungus of an if statement
					if (
							(spawnOver == -1 || spawnOver == getTile (putX, putY, 0)) &&
							(Double.isNaN (spawnOdds) || r.nextDouble () < spawnOdds)
					) {
						setTile (slice [wy], topLeft.x + wx, topLeft.y + wy, 0);
					}
				}
			}
		}
		
		//Place in the entities
		JSONArray entities = s.getProperties ().getJSONArray ("entities");
		JSONObject curr;
		if (entities != null) {
			for (int i = 0; i < entities.getContents ().size (); i++) {
				
				curr = (JSONObject)entities.get (i);
				String entityType = curr.getString ("type");
				
				//Add entity to the world
				Entity newEntity = new Entity ();
				newEntity.setPosition (topLeft.x * 8 + curr.getInt ("x"), topLeft.y * 8 + curr.getInt ("y"));
				
				//Create the entity's associated EntityObject
				try {
					Class<?> c = Class.forName ("gameObjects." + entityType);
					Constructor<?> constructor = c.getConstructor (Entity.class);
					EntityObject eObj = (EntityObject)constructor.newInstance (newEntity);
					eObj.initPairedEntity (newEntity);
					World.addEntity (newEntity);
				} catch (ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					e.printStackTrace ();
				}
				
				//Set the entity's parameters as specified
				JSONObject data = curr.getJSONObject ("data");
				HashMap<String, Object> dataMap = data.getContents ();
				Set<Entry<String, Object>> dataSet = dataMap.entrySet ();
				Iterator<Entry<String, Object>> iter = dataSet.iterator ();
				while (iter.hasNext ()) {
					Entry<String, Object> curr2 = iter.next ();
					newEntity.getProperties ().put (curr2.getKey (), (String)curr2.getValue ());
				}

			}
		}
		
	}
	
	public static void putStructure (String id, int x, int y) {
		Structure s = structures.get (id);
		HashMap<String, String> em = Entity.getEntityMap ();
		em.put ("type", "StructSpawner");
		em.put ("x", String.valueOf (x));
		em.put ("y", String.valueOf (y));
		em.put ("structName", id);
		Entity et = new Entity (em);
		StructSpawner sm = new StructSpawner (et);
		World.addEntity (et);
		//Run the script (if present)
		if (s.getMetaProperty ("script") != null) {
			String scriptName = (String)s.getMetaProperty ("script");
			String className = "scripts." + scriptName;
			Class<?> scriptClass;
			try {
				scriptClass = Class.forName (className);
				StructureScript scriptObj = (StructureScript)scriptClass.getConstructor ().newInstance ();
				scriptObj.run (et);
			} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public static void addEntity (Entity e) {
		
		addEntity (e, true);
		
	}
	
	private static void addEntity (Entity e, boolean doReigonUpdate) {
		
		//Add entity to global list of entites
		entities.add (e);
		
		//Add entity to its reigon
		if (doReigonUpdate) {
			updateReigon (e);
		}
		
		//Add to the tile entities (if applicable)
		JSONObject typeProperties = e.getTypeProperties ();
		if (typeProperties != null) {
			Boolean b = (Boolean)typeProperties.get ("tileEntity");
			if (b != null && b) {
				Point p = new Point (e.getInt ("x"), e.getInt ("y"));
				tileEntities.put (p, e);
			}
		}
	}
	
	public static void removeEntity (Entity e) {
		
		//Remove from the global list of entities
		entities.remove (e);
		
		//Remove entity from its reigon
		Set<Entry<Integer, WorldReigon>> allRgs = reigonsMap.entrySet ();
		Iterator<Entry<Integer, WorldReigon>> iter = allRgs.iterator ();
		while (iter.hasNext ()) {
			WorldReigon curr = iter.next ().getValue ();
			if (curr.hasEntity (e.getUUID ())) {
				curr.removeEntity (e);
			}
		}
		
		//Remove entity from tile entities (if applicable)
		Boolean b = (Boolean)e.getTypeProperties ().get ("tileEntity");
		if (b != null && b) {
			Point p = new Point (e.getInt ("x"), e.getInt ("y"));
			if (tileEntities.containsKey (p)) {
				tileEntities.remove (p);
			}
		}
	}
	
	public static void updateReigon (Entity e) {
		
		//Check reigons for the object
		Set<Entry<Integer, WorldReigon>> allRgs = reigonsMap.entrySet ();
		Iterator<Entry<Integer, WorldReigon>> iter = allRgs.iterator ();
		while (iter.hasNext ()) {
			
			//Get the current reigon
			WorldReigon r = iter.next ().getValue ();
			
			if (r.hasEntity (e.getUUID ())) {
				//Reigon has the entity, check to remove
				if (r.id != e.getReigonId ()) {
					r.removeEntity (e);
				}
			} else if (r.id == e.getReigonId ()) {
				//Reigon does not have entity and should have it
				r.addEntity (e);
			}
		}
		
	}
	
	public static JSONObject getTileProperties (int id) {
		return tileProperties.getJSONObject (String.valueOf (id));
	}
	
	public static JSONArray getDropTable (String name) {
		return dropList.getJSONArray (name);
	}
	
	public static void populateReigon (WorldReigon rg) {
		
		//Get the list of structures
		Iterator<Entry<String, Structure>> iter = structures.entrySet ().iterator ();
		
		//Iterate through all structures and spawn them accordingly
		while (iter.hasNext ()) {
			//TODO allow biome-specific structure spawning
			Entry<String, Structure> curr = iter.next ();
			Structure struct = curr.getValue ();
			String structName = curr.getKey ();
			
			//Get the reigon x and make the RNG
			int reigonX = rg.id * WorldReigon.REIGON_SIZE;
			Random r = new Random (seed + rg.id * 49390927 + rg.dimension + 71111111); //Prime number witchcraft
			
			//Generate spawn attempt values for the struct
			if (!struct.getMetaProperty ("spawn_type").equals ("none")) {
				if ((int)struct.getMetaProperty ("dimension") == rg.dimension) {
					int minAttempts = (int)struct.getMetaProperty ("min_attempts");
					int maxAttempts = (int)struct.getMetaProperty ("max_attempts");
					int numAttempts = minAttempts + (int)(r.nextDouble () * (maxAttempts - minAttempts));
					
					//Spawn for surface structures
					if (struct.getMetaProperty ("spawn_type").equals ("surface")) {
						for (int i = 0; i < numAttempts; i++) {
							int spawnX = r.nextInt (WorldReigon.REIGON_SIZE) + reigonX;
							int rgX = Math.floorMod (spawnX, WorldReigon.REIGON_SIZE);
							putStructure (structName, spawnX * 8, rg.getCeilingHeight (rgX) * 8 - 8);
						}
					}
					
					//Spawn for regular structures
					else if (struct.getMetaProperty ("spawn_type").equals ("regular")) {
						for (int i = 0; i < numAttempts; i++) {
							//Get min and max y
							int minHeight = WORLD_HEIGHT - (int)struct.getMetaProperty ("min_height");
							int maxHeight = WORLD_HEIGHT - (int)struct.getMetaProperty ("max_height");
							int randX = r.nextInt (WorldReigon.REIGON_SIZE) + reigonX;
							int randY = r.nextInt (minHeight - maxHeight) + maxHeight;
							putStructure (structName, randX * 8, randY * 8);
						}
					}
					
					//Spawn for fixed position structures
					else if (struct.getMetaProperty ("spawn_type").equals ("fixed")) {
						System.out.println ("SPAWNING FIXED STRUCTURE");
						int spawnX = (int)struct.getMetaProperty ("spawn_x") + reigonX;
						int spawnY = (int)struct.getMetaProperty ("spawn_y");
						putStructure (structName, spawnX * 8, spawnY * 8);
					}
				}
			}
			
		}
	}
	
	public static void tick () {
		//The ever-important random number generator
		Random r = new Random ();
		
		//Do spawning ticks
		int SPAWNING_ATTEMPTS = 2;
		for (int i = 0; i < SPAWNING_ATTEMPTS; i++) {
			int spawnX = (int)player.getX () + (r.nextInt (SECONDARY_LOAD_RADIUS * 2) - SECONDARY_LOAD_RADIUS) * 8;
			int spawnY = r.nextInt (WORLD_HEIGHT) * 8;
			if (getLightStrength (spawnX / 8, spawnY / 8) < 4 && getTile (spawnX / 8, spawnY / 8, 0) == 0 && getTile (spawnX / 8, spawnY / 8 - 1, 0) == 0 && getTile (spawnX / 8, spawnY / 8 + 1, 0) != 0) {
				new Zombie (spawnX, spawnY);
			}
		}
		
		//Do random ticks
		Set<Entry<Integer, WorldReigon>> allRgs = reigonsMap.entrySet ();
		Iterator<Entry<Integer, WorldReigon>> iter = allRgs.iterator ();
		while (iter.hasNext ()) {
			iter.next ().getValue ().tickReigon ();
		}
		
		//Do sched ticks
		ArrayList<Point> currSchedTicks = schedTicks.get (globalTickCount);
		if (currSchedTicks != null) {
			for (int i = 0; i < currSchedTicks.size (); i++) {
				Point currTick = currSchedTicks.get (i);
				doSchedTick (currTick.x, currTick.y);
			}
			schedTicks.remove (globalTickCount);
		}
		globalTickCount++;
		
	}
	
	public static void tickNearby (int x, int y) {
		doTileTick (x, y - 1);
		doTileTick (x - 1, y);
		doTileTick (x + 1, y);
		doTileTick (x, y + 1);
		doTileTick (x, y);
	}
	
	public static void doTileTick (int x, int y) {
		
		//TODO determine whether tile ticks can happen in the background layer; as it stands, they do not
		
		//Nab the tile id
		int id = World.getTile (x, y, 0);
		
		//Door top
		if (id == 60 || id == 62 || id == 76 || id == 78) {
			if (World.getTile (x, y + 1, 0) - id != 1) {
				World.setTile (0, x, y, 0);
			}
		}
		
		//Door bottom
		if (id == 61 || id == 63 || id == 77 || id == 79) {
			if (World.getTile (x, y - 1, 0) - id != -1) {
				World.setTile (0, x, y, 0);
			}
		}
		
		//Water
		if (Fluids.isWater (id) && checkForFluidUpdate (x, y)) {
			schedTick (x, y, 5);
		}
		
		//Lava
		if (Fluids.isLava (id) && checkForFluidUpdate (x, y)) {
			schedTick (x, y, 50);
		}
		
	}
	
	public static void doRandomTileTick (int x, int y) {
		
		//Get the id
		int id = World.getTile (x, y, 0);
		
		//Grow saplings
		if (id == 80) {
			if (Math.random () < .1) {
				World.putStructure ("enchanted_tree", x * 8, y * 8);
			} else {
				World.putStructure ("tree", x * 8, y * 8);
			}
		}
		
		//Grow wheat by 1 stage
		if (id >= 81 && id <= 83) {
			World.setTile (id + 1, x, y, 0);
		}
		
	}
	
	public static void doSchedTick (int x, int y) {
		
		//Get the tile ID
		int id = World.getTile (x, y, 0);
		
		//Handle flowing
		if (Fluids.isFluid (id)) {
			
			//Grab all the needed tile IDs
			int tid = id;
			int topId = World.getTile (x, y - 1, 0);
			int downId = World.getTile (x, y + 1, 0);
			int leftId = World.getTile (x - 1, y, 0);
			int rightId = World.getTile (x + 1, y, 0);
			
			//Check if fluid should be removed
			if (Fluids.isFlowingWater (tid)) {
				if (!Fluids.isWater (topId) && 
					(!(Fluids.isWater (leftId) && (Fluids.getFlowLevel (leftId) > Fluids.getFlowLevel (tid)))) && 
					(!(Fluids.isWater (rightId) && (Fluids.getFlowLevel (rightId) > Fluids.getFlowLevel (tid))))) {
						World.placeTile (0, x, y, 0);
						tickNearby (x, y);
				}
			}
			if (Fluids.isFlowingLava (tid)) {
				if (!Fluids.isLava (topId) && 
					(!(Fluids.isLava (leftId) && (Fluids.getFlowLevel (leftId) > Fluids.getFlowLevel (tid)))) && 
					(!(Fluids.isLava (rightId) && (Fluids.getFlowLevel (rightId) > Fluids.getFlowLevel (tid))))) {
						World.placeTile (0, x, y, 0);
						tickNearby (x, y);
				}
			}
			
			//Check flowing down first
			boolean canFlowSideways = true;
			if ((Fluids.isWater (tid) && Fluids.isWater (downId) && Fluids.getFlowLevel (downId) >= 7) || (Fluids.isLava (tid) && Fluids.isLava (downId) && Fluids.getFlowLevel (downId) >= 7)) {
				canFlowSideways = false;
			}
			if (Fluids.checkFluidPriority (tid, downId)) {
				if (!((Fluids.isWater (tid) && Fluids.isLava (downId)) || (Fluids.isLava (tid) && Fluids.isWater (downId)))) {
					canFlowSideways = false;
				}
				World.flowTo (x, y, x, y + 1);
				tickNearby (x, y + 1);
			}
			if (canFlowSideways) {
				if (Fluids.canFlow (tid)) {
					if (Fluids.checkFluidPriority (tid, leftId)) {
						World.flowTo (x, y, x - 1, y);
						tickNearby (x - 1, y);
					}
					if (Fluids.checkFluidPriority (tid, rightId)) {
						World.flowTo (x, y, x + 1, y);
						tickNearby (x + 1, y + 1);
					}
				}
			}
			
		}
		
		
		//Handle flowing lava
		
	}
	
	public static void schedTick (int x, int y, int time) {

		//Get the proper schedule time
		int schedTime = globalTickCount + time;
		
		//Add a list of ticks if not already present
		if (!schedTicks.containsKey (schedTime)) {
			schedTicks.put (schedTime, new ArrayList<Point> ());
		}
		
		//Schedule the tick
		schedTicks.get (schedTime).add (new Point (x, y));
		
	}
	
	private static boolean checkForFluidUpdate (int x, int y) {
		
		//Grab all the needed tile IDs
		int tid = World.getTile (x, y, 0);
		int topId = World.getTile (x, y - 1, 0);
		int downId = World.getTile (x, y + 1, 0);
		int leftId = World.getTile (x - 1, y, 0);
		int rightId = World.getTile (x + 1, y, 0);
		
		//Check if flow is not possible
		if (Fluids.isFlowingWater (tid)) {
			if (!Fluids.isWater (topId) && 
				(!(Fluids.isWater (leftId) && (Fluids.getFlowLevel (leftId) > Fluids.getFlowLevel (tid)))) && 
				(!(Fluids.isWater (rightId) && (Fluids.getFlowLevel (rightId) > Fluids.getFlowLevel (tid))))) {
					return true;
			}
		}
		if (Fluids.isFlowingLava (tid)) {
			if (!Fluids.isLava (topId) && 
				(!(Fluids.isLava (leftId) && (Fluids.getFlowLevel (leftId) > Fluids.getFlowLevel (tid)))) && 
				(!(Fluids.isLava (rightId) && (Fluids.getFlowLevel (rightId) > Fluids.getFlowLevel (tid))))) {
					return true;
			}
		}
		
		//Check water priorities
		if (Fluids.isWater (tid)) {
			if (!(Fluids.isWater (downId) && Fluids.getFlowLevel (downId) >= 7)) {
				if (Fluids.checkFluidPriority (tid, downId) ||
					Fluids.checkFluidPriority (tid, leftId) ||
					Fluids.checkFluidPriority (tid, rightId)) {
						return true;
				}
			}
		}
		
		//Check lava priorities
		if (Fluids.isLava (tid)) {
			if (!(Fluids.isLava (downId) && Fluids.getFlowLevel (downId) >= 7)) {
				if (Fluids.checkFluidPriority (tid, downId) ||
					Fluids.checkFluidPriority (tid, leftId) ||
					Fluids.checkFluidPriority (tid, rightId)) {
						return true;
				}
			}
		}
		
		//Return false in all other cases
		return false;
		
	}
	
	private static void flowTo (int sourceX, int sourceY, int destX, int destY) {
		
		int sId = World.getTile (sourceX, sourceY, 0);
		int dId = World.getTile (destX, destY, 0);
		
		//Water into lava case
		if (Fluids.isWater (sId) && Fluids.isLava (dId)) {
			if (Fluids.isLavaSource (dId)) {
				World.placeTile (15, destX, destY, 0); //Obsidian
			} else {
				World.placeTile (23, destX, destY, 0); //Cobble
			}
			return;
		}
		
		//Lava into water case
		if (Fluids.isLava (sId) && Fluids.isWater (dId)) {
			if (destX == sourceX || destY == sourceY + 1) {
				World.placeTile (16, destX, destY, 0); //Stone
			} else {
				World.placeTile (23, destX, destY, 0); //Cobble
			}
			return;
		}
		
		//Water general case
		if (Fluids.isWater (sId)) {
			if (destX == sourceX || destY == sourceY + 1) {
				World.placeTile (Fluids.WATER_FLOWING_START_ID, destX, destY, 0);
			} else {
				World.placeTile (Fluids.getNextFlowId (sId), destX, destY, 0);
			}
			return;
		}
		
		//Lava general case
		if (Fluids.isLava (sId)) {
			if (destX == sourceX || destY == sourceY + 1) {
				World.placeTile (Fluids.LAVA_FLOWING_START_ID, destX, destY, 0);
			} else {
				World.placeTile (Fluids.getNextFlowId (sId), destX, destY, 0);
			}
			return;
		}
		
	}
	
	public static class WorldReigon {
		
		public static final int REIGON_SIZE = LOAD_SIZE;
		
		private int id;
		private int dimension;
		private boolean filled = false;
		
		public ArrayList<ArrayList<Integer>> fgTiles;
		public ArrayList<ArrayList<Integer>> bgTiles;
		private ArrayList<Boolean> loadedColumns;
		
		public ArrayList<ArrayList<Point>> lights;
		public ArrayList<ArrayList<Integer>> lighting;
		public ArrayList<Integer> ceilingMap;
		
 		private ArrayList<Entity> entities;
		private HashMap<UUID, Entity> entityMap;
		
		public WorldReigon (int id, int dimension) {
			this.id = id;
			this.dimension = dimension; //Save id and dimension for later use
			this.filled = false;
			
			//Init lists
			this.loadedColumns = new ArrayList<Boolean> ();
			for (int i = 0; i < LOAD_SIZE; i++) {
				this.loadedColumns.add (false);
			}
			
			initLighting ();
			
		}
		
		public void initLighting () {
			
			//Init columns of point-lights
			lights = new ArrayList<ArrayList<Point>> ();
			for (int wx = 0; wx < REIGON_SIZE; wx++) {
				ArrayList<Point> column = new ArrayList<Point> ();
				lights.add (column);
			}
			
			//Init the lighting
			lighting = new ArrayList<ArrayList<Integer>> ();
			for (int wx = 0; wx < REIGON_SIZE; wx++) {
				ArrayList<Integer> lightingVals = new ArrayList<Integer> ();
				for (int wy = 0; wy < WORLD_HEIGHT; wy++) {
					lightingVals.add (-1);
				}
				lighting.add (lightingVals);
			}
			
			//Init the ceiling map
			ceilingMap = new ArrayList<Integer> ();
			for (int i = 0; i < REIGON_SIZE; i++) {
				ceilingMap.add (-1);
			}
			
		}
		
		public void putLightSource (int strength, int x, int y) {
			
			//Add light source to column lights
			Point source = new Point (x, y);
			lights.get (x).add (source);
			
			//Invalidate all lighting values around the added light source
			for (int wx = -strength; wx <= strength; wx++) {
				for (int wy = -strength; wy <= strength; wy++) {
					int putX = x + wx;
					int putY = y + wy;
					if (putY >= 0 && putY < WORLD_HEIGHT) {
						lighting.get (x).set (putY, -1);
					}
				}
			}
			
		}
		
		public void removeLightSource (int strength, int x, int y) {
			
			//Remove light source from column lights
			Point source = new Point (x, y);
			ArrayList<Point> columnLights = lights.get (x);
			for (int i = 0; i < columnLights.size (); i++) {
				Point curr = columnLights.get (i);
				if (curr.x == x && curr.y == y) {
					columnLights.remove (i);
					break;
				}
			}
			
			//Invalidate all lighting values around the removed light source
			for (int wx = -strength; wx <= strength; wx++) {
				for (int wy = -strength; wy <= strength; wy++) {
					int putX = x + wx;
					int putY = y + wy;
					if (putY >= 0 && putY < WORLD_HEIGHT) {
						int realX = Math.floorMod (putX, LOAD_SIZE);
						lighting.get (realX).set (putY, -1);
					}
				}
			}
			
		}
		
		//Adds in light sources to an un-initialized column
		public void lightColumn (int x) {
			int realX = Math.floorMod (x, LOAD_SIZE);
			ArrayList<Integer> col = fgTiles.get (realX);
			for (int i = 0; i < WORLD_HEIGHT; i++) {
				if (tileLightTable [col.get (i)] != 0) {
					putLightSource (tileLightTable [col.get (i)], x, i);
				}
			}
		}
		
		//Removes light sources from a column
		public void unlightColumn (int x) {
			int realX = Math.floorMod (x, LOAD_SIZE);
			ArrayList<Integer> col = fgTiles.get (realX);
			for (int i = 0; i < WORLD_HEIGHT; i++) {
				if (tileLightTable [col.get (i)] != 0) {
					removeLightSource (tileLightTable [col.get (i)], x, i);
				}
				lighting.get (realX).set (i, -1);
			}
		}
		
		public int getLightStrength (int x, int y) {
			int preLight = lighting.get (x).get (y);
			if (preLight != -1) {
				return preLight;
			} else {
				int lightVal = computeLight (x, y);
				lighting.get (y).set (y, lightVal);
				return lightVal;
			}
		}
		
		public void loadColumn (int x) {
			lightColumn (x);
			loadedColumns.set (x, true);
		}
		
		public void unloadColumn (int x) {
			unlightColumn (x);
			loadedColumns.set (x, false);
		}
		
		public boolean columnLoaded (int x) {
			return loadedColumns.get (x);
		}
		
		public void draw () {
			//Draw the tiles
			for (int currLayer = 1; currLayer >= 0; currLayer--) {
				for (int wy = 0; wy < SCREEN_SIZE_V; wy++) {
					for (int wx = 0; wx < SCREEN_SIZE_H; wx++) {
						if (getReigonId (viewX + wx) == this.id) {
							int tileId = getTile (Math.floorMod (viewX + wx, REIGON_SIZE), viewY + wy, currLayer);
							int lightVal = getLightStrength (Math.floorMod (viewX + wx, REIGON_SIZE), Math.floorMod (viewY + wy, REIGON_SIZE));
							if (currLayer == 1) {
								lightVal -= 4;
								lightVal = lightVal < 0 ? 0 : lightVal;
							}
							
							//Render the proper tile
							if (Fluids.isWaterSource (tileId) || Fluids.isLavaSource (tileId)) {
								int upId = getTile (Math.floorMod (viewX + wx, REIGON_SIZE), viewY + wy - 1, currLayer);
								if (Fluids.isWaterSource (tileId) && Fluids.isWater (upId)) {
									PARSED_TILES.setFrame (Fluids.WATER_FLOWING_START_ID);
								} else if (Fluids.isLavaSource (tileId) && Fluids.isLava (upId)) {
									PARSED_TILES.setFrame (Fluids.LAVA_FLOWING_START_ID);
								} else {
									PARSED_TILES.setFrame (tileId);
								}
							} else {
								PARSED_TILES.setFrame (tileId); //Draw normally
							}
							PARSED_TILES.draw (wx * 8, wy * 8);
							
							//Render the proper lighting
							if (tileId != 0) {
								PARSED_LIGHTING.setFrame (lightVal);
								PARSED_LIGHTING.draw (wx * 8, wy * 8);
							}
						}
					}
				}
			}
		}
		
		public void load () {
			
			//Create entities list/map
			entities = new ArrayList<Entity> ();
			entityMap = new HashMap<UUID, Entity> ();
			
			//Initialize stuff
			fgTiles = new ArrayList<ArrayList<Integer>> ();
			bgTiles = new ArrayList<ArrayList<Integer>> (); //Initialize the tile data
			for (int i = 0; i < REIGON_SIZE; i++) {
				fgTiles.add (null); //Fill the array to proper size
				bgTiles.add (null);
			}
			
			//Load in the tiles
			String filepath = "saves/" + worldName + "/" + getReigonFileName (id, dimension);
			File f = new File (filepath); //Get the reigon file
			if (f.exists ()) {
				Scanner s = null;
				try {
					s = new Scanner (f);
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} //Make a scanner for the file
				for (int wx = 0; wx < REIGON_SIZE; wx++) {
					ArrayList<Integer> fgColumn = new ArrayList<Integer> ();
					ArrayList<Integer> bgColumn = new ArrayList<Integer> ();
					for (int wy = 0; wy < WORLD_HEIGHT; wy++) {
						fgColumn.add (s.nextInt ());
						bgColumn.add (s.nextInt ());
					}
					fgTiles.set (wx, fgColumn);
					bgTiles.set (wx, bgColumn);
				} //Read all the tiles
				s.close (); //Close the file
			} else {
				for (int wx = 0; wx < REIGON_SIZE; wx++) {
					ArrayList<Integer> wFgTiles = generateColumn (id * REIGON_SIZE + wx, 0, dimension);
					fgTiles.set (wx, wFgTiles);
					ArrayList<Integer> wBgTiles = generateColumn (id * REIGON_SIZE + wx, 1, dimension);
					bgTiles.set (wx, wBgTiles);
				} //Generate the tiles
				World.populateReigon (this); //Populate the reigon with structures
			}
			
			//Load in the entities
			filepath = "saves/" + worldName + "/" + getEntityFileName (id, dimension);
			f = new File (filepath); //Get the reigon file
			if (f.exists ()) {
				Scanner s = null;
				try {
					s = new Scanner (f);
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} //Make a scanner for the file
				while (s.hasNextLine ()) {
					
					//Add entity to the world
					Entity newEntity = new Entity (s.nextLine ());
					this.addEntity (newEntity);
					World.addEntity (newEntity, false);
					
					//Create the entity's associated EntityObject
					String entityType = newEntity.getType ();
					try {
						Class<?> c = Class.forName ("gameObjects." + entityType);
						Constructor<?> constructor = c.getConstructor (Entity.class);
						constructor.newInstance (newEntity);
					} catch (ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
						e.printStackTrace ();
					}
				}
				s.close (); //Close the file
			} else {
				//TODO spawn in entities
			}
			this.filled = true;
		}
		
		public void unload () {

			//Sort for efficiency *puts on programmer sunglasses*
			Collections.sort (World.entities);
			
			//Setup some cool stuff
			Iterator<Entity> iter = World.entities.iterator ();
			boolean matchesReigon = false;
			
			//Remove all of this reigon's entities from the World's global entity list
			while (iter.hasNext ()) {
				
				//Get our wonderful entity
				Entity working = iter.next ();
				
				//If a run has not been found
				if (!matchesReigon) {
					if (working.getReigonId () == id) {
						matchesReigon = true; //Start the run
					}
				}
				
				//If a run has been found
				if (matchesReigon) {
					if (working.getReigonId () == id) {
						
						//Remove the Entity from the world
						working.getObject ().forget ();
						iter.remove ();
						
						//Remove the Entity from the list of Tile Entities (if applicable)
						JSONObject typeProperties = working.getTypeProperties ();
						if (typeProperties != null) {
							Boolean b = (Boolean)typeProperties.get ("tileEntity");
							if (b != null && b) {
								Point p = new Point (working.getInt ("x"), working.getInt ("y"));
								tileEntities.remove (p);
							}
						}
						
					} else {
						break;
					}
				}
			}
			
			//Save this reigon and remove it from the World's list of loaded reigons
			save ();
			reigonsMap.remove (this); //TODO figure out what to do here
			
		}
		
		public void save () {
			
			//Save tiles
			String filepath = "saves/" + worldName + "/" + getReigonFileName (id, dimension);
			File f = new File (filepath); //Get the file to save to
			if (!f.exists ()) {
				try {
					f.createNewFile ();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} //Make new reigon file to save to if it doesn't already exist
			
			//Write the reigon's tiles to the file
			try {
				FileWriter fw = new FileWriter (f);
				for (int wx = 0; wx < REIGON_SIZE; wx++) {
					for (int wy = 0; wy < WORLD_HEIGHT; wy++) {
						fw.append (Integer.toString (fgTiles.get (wx).get (wy)) + " ");
						fw.append (Integer.toString (bgTiles.get (wx).get (wy)) + " ");
					}
				} //Write the tiles to the file
				fw.close ();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			//Save entities
			filepath = "saves/" + worldName + "/" + getEntityFileName (id, dimension);
			f = new File (filepath);
			if (!f.exists ()) {
				try {
					f.createNewFile ();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} //Make new entity file to save to if it doesn't already exist
			
			//Write the entities to the file
			try {
				FileWriter fw = new FileWriter (f);
				for (int i = 0; i < entities.size (); i++) {
					if (i != 0) {
						fw.append ("\n");
					}
					fw.append (entities.get (i).toString ());
				}
				fw.close ();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
		public static String getReigonFileName (int id, int dimension) {
			return "reigon_tiles_" + dimension + "_" + id + ".txt";
		}
		
		public static String getEntityFileName (int id, int dimension) {
			return "reigon_entities_" + dimension + "_" + id + ".txt";
		}
		
		public int getCeilingHeight (int x) {
			if (ceilingMap.get (x) == -1) {
				ArrayList<Integer> column = getColumn (x, 0);
				for (int i = 0; i < column.size (); i++) {
					if (tileTpTable [column.get (i)] == 0) {
						ceilingMap.set (x, i);
						//ArrayList<Integer> lightCol = lighting.get (realX); Shouldn't be computed here if possible
						//for (int j = 0; j < column.size (); j++) {
						//	lightCol.set (j, -1);
						//}
						return i;
					}
				}
				return 255;
			} else {
				return ceilingMap.get (x);
			}
		}
		
		public ArrayList<Integer> getColumn (int x, int layer) {
			return (layer == 0 ? fgTiles : bgTiles).get (x);
		}
		
		public int getTile (int x, int y, int layer) {
			if (y < 0 || y >= WORLD_HEIGHT) {
				return 24;
			}
			return (layer == 0 ? fgTiles : bgTiles).get (x).get (y);
		}
		
		public void setTile (int id, int x, int y, int layer) {
			(layer == 0 ? fgTiles : bgTiles).get (x).set (y, id);
		}
		
		public void tickReigon () {
			int RANDOM_TICK_COUNT = 10;
			for (int i = 0; i < RANDOM_TICK_COUNT; i++) {
				int offsX = (int)(Math.random () * REIGON_SIZE);
				int tickY = (int)(Math.random () * WORLD_HEIGHT);
				int tickX = this.id * REIGON_SIZE + offsX;
				doRandomTileTick (tickX, tickY);
			}
		}
		
		//ENTITY STUFF
		public void addEntity (Entity entity) {
			entities.add (entity);
			entityMap.put (entity.getUUID (), entity);
		}
		
		public void removeEntity (Entity entity) {
			entities.remove (entity);
			entityMap.remove (entity.getUUID ());
		}
		
		public boolean hasEntity (UUID id) {
			return entityMap.containsKey (id);
		}
	}
}
