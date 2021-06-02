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
import main.GameObject;
import main.MainLoop;
import resources.Sprite;
import resources.Spritesheet;
import worldgen.TerrainHeightGen;

public class World {
	
	public static final int WORLD_HEIGHT = 128;
	public static final int LOAD_SIZE = 256;
	public static final int SECONDARY_LOAD_RADIUS = 64;
	public static final int SCREEN_SIZE_H = 24; //Number of tiles that fit on the screen horizontally
	public static final int SCREEN_SIZE_V = 18; //Number of tiles that fit on the screen vertically
	
	public static final Spritesheet TILE_SHEET = new Spritesheet ("resources/sprites/tiles.png");
	public static final Spritesheet ITEM_SHEET = new Spritesheet ("resources/sprites/items.png");
	public static final Spritesheet LIGHT_SHEET = new Spritesheet ("resources/sprites/lighting.png");
	public static final Sprite PARSED_TILES = new Sprite (TILE_SHEET, 8, 8);
	public static final Sprite PARSED_ITEMS = new Sprite (ITEM_SHEET, 8, 8);
	public static final Sprite PARSED_LIGHTING = new Sprite (LIGHT_SHEET, 8, 8);
	
	private static ArrayList<ArrayList<Integer>> tiles;
	private static ArrayList<ArrayList<Integer>> bgTiles;
	private static ArrayList<ArrayList<Integer>> lighting;
	private static ArrayList<ArrayList<ArrayList<Point>>> lights;
	private static int[] xBuffer = new int[LOAD_SIZE];
	
	private static int viewX;
	private static int viewY;
	
	private static int loadLeft;
	private static int loadRight;
	
	private static ArrayList<WorldReigon> reigons;
	private static LinkedList<Entity> entities;
	private static HashMap<Point, Entity> tileEntities;
	
	private static Player player;
	
	private static long seed = 69;
	
	private static String worldName = "default";
	
	private static HashMap<String, Structure> structures;
	
	private static long worldTime;
	
	public static JSONObject tileProperties;
	public static JSONObject dropList;
	
	private static int[] tileLightTable = new int[256];
	private static int[] tileSolidTable = new int[256];
	private static int[] tileTpTable = new int[256];
	
	private static int[] highestTile = new int[LOAD_SIZE];
	private static boolean[] loaded = new boolean[LOAD_SIZE];
	
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
		
		reigons = new ArrayList<WorldReigon> ();
		tiles = new ArrayList<ArrayList<Integer>> (); //Make the world tiles
		lighting = new ArrayList<ArrayList<Integer>> (); //Make the lighting table
		for (int i = 0; i < LOAD_SIZE; i++) {
			tiles.add (null);
			//Fill up the lighting table
			ArrayList<Integer> lightVals = new ArrayList<Integer> ();
			for (int j = 0; j < WORLD_HEIGHT; j++) {
				lightVals.add (-1);
			}
			lighting.add (lightVals);
		} //Size the tile list
		
		//Init entity properties
		Entity.initTypeProperties ();
		
		//Setup entity maps
		entities = new LinkedList<Entity> (); //Make the world entities list
		tileEntities = new HashMap<Point, Entity> ();
		
		updateReigons ();
		updateWorld (); //Fill the world
		xBuffer = new int[LOAD_SIZE];
		for (int i = 0; i < LOAD_SIZE; i++) {
			xBuffer [i] = i;
		} //Initialize the x coordinate buffer
		//Make the player
		spawnPlayer ();
		initLighting ();
	}
	
	public static void makeWorldGenResources () {
		heightGen = new TerrainHeightGen (seed);
	}
	
	public static void initLighting () {
		lights = new ArrayList<ArrayList<ArrayList<Point>>> ();
		for (int wx = 0; wx < LOAD_SIZE; wx++) {
			ArrayList<ArrayList<Point>> column = new ArrayList<ArrayList<Point>> ();
			for (int wy = 0; wy < WORLD_HEIGHT; wy++) {
				ArrayList<Point> pts = new ArrayList<Point> ();
				column.add (pts);
			}
			lights.add (column);
		}
		
		for (int i = 0; i < LOAD_SIZE; i++) {
			highestTile [i] = -1;
		}
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
				fw.append ("x:0\ny:496");
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
			
			player = new Player (playerAttributes); //Make a player with the given attributes
		}
	}
	
	public static void draw () {
		
		//Draw the background (sky)
		Graphics g = MainLoop.getWindow ().getBufferGraphics ();
		g.setColor (new Color (0x97ECEF));
		g.fillRect (0, 0, SCREEN_SIZE_H * 8, SCREEN_SIZE_V * 8);
		
		//Draw the tiles
		for (int wy = 0; wy < SCREEN_SIZE_V; wy++) {
			for (int wx = 0; wx < SCREEN_SIZE_H; wx++) {
				int tileId = tiles.get (Math.floorMod (viewX + wx, LOAD_SIZE)).get (viewY + wy);
				int lightVal = getLightStrength (viewX + wx, viewY + wy);
				
				//Render the proper tile
				PARSED_TILES.setFrame (tileId);
				PARSED_TILES.draw (wx * 8, wy * 8);
				
				//Render the proper lighting
				if (tileId != 0) {
					PARSED_LIGHTING.setFrame (lightVal);
					PARSED_LIGHTING.draw (wx * 8, wy * 8);
				}
			}
		}
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
	
	public static int getTile (int x, int y) {
		if (y < 0 || y >= WORLD_HEIGHT) {
			return 24;
		}
		return tiles.get (Math.floorMod (x, LOAD_SIZE)).get (y);
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
		int realX = Math.floorMod (x, LOAD_SIZE);
		if (highestTile [realX] == -1) {
			ArrayList<Integer> column = tiles.get (realX);
			for (int i = 0; i < column.size (); i++) {
				if (tileTpTable [column.get (i)] == 0) {
					//TODO allow skylight to pass through transparent tiles
					highestTile [realX] = i;
					ArrayList<Integer> lightCol = lighting.get (realX);
					for (int j = 0; j < column.size (); j++) {
						lightCol.set (j, -1);
					}
					return i;
				}
			}
			return 255;
		} else {
			return highestTile [realX];
		}
	}
	
	public static void setTile (int id, int x, int y) {
		try {
			int realX = Math.floorMod (x, LOAD_SIZE);
			tiles.get (realX).set (y, id);
			WorldReigon rg = getReigon (id);
			//UPDATE THE HEIGHT
			highestTile [realX] = -1;
			getCeilingHeight (realX);
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
		if (tileLightTable [getTile (x, y)] != 0) {
			removeLightSource (tileLightTable [getTile (x, y)], x, y);
		}
		if (tileLightTable [id] != 0) {
			putLightSource (tileLightTable [id], x, y);
		}
	}
	
	public static void putLightSource (int strength, int x, int y) {
		Point source = new Point (x, y);
		for (int wx = -strength; wx <= strength; wx++) {
			for (int wy = -strength; wy <= strength; wy++) {
				int putX = x + wx;
				int putY = y + wy;
				if (putY >= 0 && putY < WORLD_HEIGHT) {
					int realX = Math.floorMod (putX, LOAD_SIZE);
					lights.get (realX).get (putY).add (source);
					lighting.get (realX).set (putY, -1);
				}
			}
		}
	}
	
	public static void removeLightSource (int strength, int x, int y) {
		Point source = new Point (x, y);
		for (int wx = -strength; wx <= strength; wx++) {
			for (int wy = -strength; wy <= strength; wy++) {
				int putX = x + wx;
				int putY = y + wy;
				if (putY >= 0 && putY < WORLD_HEIGHT) {
					int realX = Math.floorMod (putX, LOAD_SIZE);
					lights.get (realX).get (putY).remove (source);
					lighting.get (realX).set (putY, -1);
				}
			}
		}
	}
	
	public static void lightColumn (int x) {
		int realX = Math.floorMod (x, LOAD_SIZE);
		ArrayList<Integer> col = tiles.get (realX);
		for (int i = 0; i < WORLD_HEIGHT; i++) {
			if (tileLightTable [col.get (i)] != 0) {
				putLightSource (tileLightTable [col.get (i)], x, i);
			}
		}
	}
	
	public static void unlightColumn (int x) {
		int realX = Math.floorMod (x, LOAD_SIZE);
		ArrayList<Integer> col = tiles.get (realX);
		for (int i = 0; i < WORLD_HEIGHT; i++) {
			if (tileLightTable [col.get (i)] != 0) {
				removeLightSource (tileLightTable [col.get (i)], x, i);
			}
		}
	}
	
	public static int getLightStrength (int x, int y) {
		int realX = Math.floorMod (x, LOAD_SIZE);
		int preLight = lighting.get (realX).get (y);
		if (preLight != -1) {
			return preLight;
		} else {
			int lightVal = computeLight (x, y);
			lighting.get (realX).set (y, lightVal);
			return lightVal;
		}
	}
	
	public static int computeLight (int x, int y) {
		//TODO add skylight computation
		int realX = Math.floorMod (x, LOAD_SIZE);
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
			int tId = getTile (ls.x, ls.y);
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
		return totalLight;
	}
	
	public static Entity getTileEntity (int x, int y) {
		Point p = new Point (x * 8, y * 8);
		return tileEntities.get (p);
	}
	
	public static void removeTileEntity (int x, int y) {
		Point p = new Point (x * 8, y * 8);
		tileEntities.remove (p);
	}
	
	public static void breakTile (int x, int y) {
		
		//Clear out the tile
		setTile (0, x, y);
		
		//Remove tile entities, if applicable
		Entity e = getTileEntity (x, y);
		if (e != null) {
			removeEntity (e);
		}
		
		//Tick the surrounding area
		tickNearby (x, y);
		
	}
	
	public static void refreshLoadAround (int x) {
		for (int i = 0; i < LOAD_SIZE; i++) {
			int colX = xBuffer [i];
			if (colX > x - SECONDARY_LOAD_RADIUS && colX < x + SECONDARY_LOAD_RADIUS) {
				if (!loaded [i]) {
					loadColumn (colX);
					loaded [i] = true;
				}
			} else {
				if (loaded [i]) {
					unloadColumn (colX);
					loaded [i] = false;
				}
			}
		}
	}
	
	public static void loadColumn (int x) {
		for (int i = 0; i < WORLD_HEIGHT; i++) {
			Point p = new Point (x * 8, i * 8);
			if (tileEntities.get (p) != null && tileEntities.get (p).getObject () instanceof StructSpawner) {
				((StructSpawner)tileEntities.get (p).getObject ()).spawnStructure ();
			}
		}
		lightColumn (x);
	}
	
	public static void unloadColumn (int x) {
		unlightColumn (x);
	}
	
	public static void unloadAllColumns () {
		for (int i = 0; i < LOAD_SIZE; i++) {
			loaded [i] = false;
		}
	}
	
	public static boolean inLoadBounds (int x) {
		int realX = Math.floorMod (x, LOAD_SIZE);
		return loaded [realX];
	}
	
	public static int getReigonId (int x) {
		return Math.floorDiv (x, WorldReigon.REIGON_SIZE);
	}
	
	public static WorldReigon getReigon (int x) {
		for (int i = 0; i < reigons.size (); i++) {
			WorldReigon current = reigons.get (i);
			if (getReigonId (x) == current.id) {
				return current;
			}
		}
		
		//Reigon was not loaded, load it then return it
		return loadReigon (getReigonId (x), 0);
	}
	
	public static boolean isReigonLoaded (int id, int dimension) {
		//Copy-pasted code, eew
		for (int i = 0; i < reigons.size (); i++) {
			WorldReigon current = reigons.get (i);
			if (id == current.id) {
				return true;
			}
		}
		return false;
	}
	
	public static int unloadReigonsOutsideMap () {
		//Works exactly as advertised
		int count = 0;
		for (int i = 0; i < reigons.size (); i++) {
			WorldReigon current = reigons.get (i);
			if ((current.id + 1) * WorldReigon.REIGON_SIZE < loadLeft || current.id * WorldReigon.REIGON_SIZE > loadRight) {
				current.unload ();
				i--;
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
					loadReigon (rgId, 0);
				}
			}
			prevId = rgId;
		} //Load in new reigons
		
		unloadReigonsOutsideMap (); //If you can't figure this one out then you're a dumbass
	}
	
	public static void updateWorld () {
		for (int wx = loadLeft; wx < loadRight; wx++) {
			int tileX = Math.floorMod (wx, LOAD_SIZE);
			WorldReigon currRg = getReigon (wx);
			tiles.set (tileX, currRg.getColumn (wx)); //Update the column of tiles to match the loaded reigon
			xBuffer [tileX] = wx;
		}
	}
	
	public static WorldReigon loadReigon (int id, int dimension) {
		WorldReigon rg = new WorldReigon (id, dimension);
		reigons.add (rg);
		return rg;
	}
	
	public static void saveReigons () {
		for (int i = 0; i < reigons.size (); i++) {
			reigons.get (i).save ();
		}
	}
	
	public static void savePlayer () {
		player.save ();
	}
	
	public static ArrayList<Integer> generateColumn (int x) {
		int SEA_LEVEL = 63;
		Integer[] result = new Integer[WORLD_HEIGHT];
		int genHeight = heightGen.getTerrainHeight (x);
		for (int i = 0; i < genHeight; i++) {
			if (i < SEA_LEVEL) {
				result [i] = 0;
			} else if (i == SEA_LEVEL) {
				result [i] = 12;
			} else {
				result [i] = 11;
			}
		}
		result[genHeight] = 1;
		result[genHeight + 1] = 2;
		for (int i = genHeight + 2; i < WORLD_HEIGHT; i++) {
			result [i] = 16;
		}
		ArrayList<Integer> arrList = new ArrayList<Integer> ();
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
		for (int wx = 0; wx < s.getWidth (); wx++) {
			int[] slice = s.getSlice (wx - origin.x);
			for (int wy = 0; wy < s.getHeight (); wy++) {
				if (!(!replace && slice [wy] == 0)) {
					int putX = topLeft.x + wx;
					int putY = topLeft.y + wy;
					r.setSeed (seed + putY * 2860486313L + putX * 49390927L); //More prime number magic
					//Big chungus of an if statement
					if (
							(spawnOver == -1 || spawnOver == getTile (putX, putY)) &&
							(Double.isNaN (spawnOdds) || r.nextDouble () < spawnOdds)
					) {
						setTile (slice [wy], topLeft.x + wx, topLeft.y + wy);
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
		HashMap<String, String> em = Entity.getEntityMap ();
		em.put ("type", "StructSpawner");
		em.put ("x", String.valueOf (x));
		em.put ("y", String.valueOf (y));
		em.put ("structName", id);
		Entity et = new Entity (em);
		StructSpawner sm = new StructSpawner (et);
		World.addEntity (et);
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
		for (int i = 0; i < reigons.size (); i++) {
			if (reigons.get (i).hasEntity (e.getUUID ())) {
				reigons.get (i).removeEntity (e);
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
		for (int i = 0; i < reigons.size (); i++) {
			
			//Nab the working WorldReigon
			WorldReigon r = reigons.get (i);
			
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
		//Scatter some trees
		
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
			Random r = new Random (seed + rg.id * 49390927); //Prime number witchcraft
			
			//Generate spawn attempt values for the struct
			int minAttempts = (int)struct.getMetaProperty ("min_attempts");
			int maxAttempts = (int)struct.getMetaProperty ("max_attempts");
			int numAttempts = minAttempts + (int)(r.nextDouble () * (maxAttempts - minAttempts));
			
			//Spawn for surface structures
			if (struct.getMetaProperty ("spawn_type").equals ("surface")) {
				for (int i = 0; i < numAttempts; i++) {
					int spawnX = r.nextInt (WorldReigon.REIGON_SIZE) + reigonX;
					putStructure (structName, spawnX * 8, rg.getCeilingHeight (spawnX) * 8 - 8);
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
			if (getLightStrength (spawnX / 8, spawnY / 8) < 4 && getTile (spawnX / 8, spawnY / 8) == 0 && getTile (spawnX / 8, spawnY / 8 - 1) == 0 && getTile (spawnX / 8, spawnY / 8 + 1) != 0) {
				new Zombie (spawnX, spawnY);
			}
		}
	}
	
	public static void tickNearby (int x, int y) {
		doTileTick (x, y - 1);
		doTileTick (x - 1, y);
		doTileTick (x + 1, y);
		doTileTick (x, y + 1);
		doTileTick (x, y);
	}
	
	public static void doTileTick (int x, int y) {
		
		int id = World.getTile (x, y);
		if (id == 60 || id == 62 || id == 76 || id == 78) {
			if (World.getTile (x, y + 1) - id != 1) {
				World.setTile (0, x, y);
			}
		}
		if (id == 61 || id == 63 || id == 77 || id == 79) {
			if (World.getTile (x, y - 1) - id != -1) {
				World.setTile (0, x, y);
			}
		}
		
	}
	
	public static class WorldReigon {
		
		public static final int REIGON_SIZE = LOAD_SIZE;
		
		private int id;
		private int dimension;
		
		public ArrayList<ArrayList<Integer>> data;
		private ArrayList<Entity> entities;
		private HashMap<UUID, Entity> entityMap;
		
		public WorldReigon (int id, int dimension) {
			this.id = id;
			this.dimension = dimension; //Save id and dimension for later use
			data = new ArrayList<ArrayList<Integer>> (); //Initialize the data array
			for (int i = 0; i < REIGON_SIZE; i++) {
				data.add (null); //Fill the array to proper size
			}
			
			//Load in the tiles
			String filepath = "saves/" + worldName + "/" + getReigonFileName (id, dimension);
			File f = new File (filepath); //Get the reigon file
			if (f.exists ()) {
				Scanner s = null;
				try {
					s = new Scanner (f);
					s.useDelimiter (",");
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} //Make a scanner for the file
				for (int wx = 0; wx < REIGON_SIZE; wx++) {
					ArrayList<Integer> column = new ArrayList<Integer> ();
					for (int wy = 0; wy < WORLD_HEIGHT; wy++) {
						column.add (s.nextInt ());
					}
					data.set (wx, column);
				} //Read all the tiles
				s.close (); //Close the file
			} else {
				for (int wx = 0; wx < REIGON_SIZE; wx++) {
					ArrayList<Integer> tiles = generateColumn (id * REIGON_SIZE + wx);
					data.set (wx, tiles);
				} //Generate the tiles
				World.populateReigon (this); //Populate the reigon with structures
			}
			
			//Load in the entities
			entities = new ArrayList<Entity> ();
			entityMap = new HashMap<UUID, Entity> ();
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
						iter.remove ();
					} else {
						break;
					}
				}
			}
			
			//Save this reigon and remove it from the World's list of loaded reigons
			save ();
			reigons.remove (this);
			
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
						if (wx == REIGON_SIZE - 1 && wy == WORLD_HEIGHT - 1) {
							fw.append (Integer.toString (data.get (wx).get (wy)));
						} else {
							fw.append (Integer.toString (data.get (wx).get (wy)) + ",");
						}
						
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
		
		public ArrayList<Integer> getColumn (int x) {
			return data.get (Math.floorMod(x, REIGON_SIZE));
		}
		
		public int getTile (int x, int y) {
			if (y < 0 || y >= WORLD_HEIGHT) {
				return 24;
			}
			return data.get (Math.floorMod(x, REIGON_SIZE)).get (y);
		}
		
		public void setTile (int id, int x, int y) {
			data.get (Math.floorMod(x, REIGON_SIZE)).set (y, id);
		}
		
		//Various generation stuffs
		public int getCeilingHeight (int x) {
			int realX = Math.floorMod (x, REIGON_SIZE);
			ArrayList<Integer> column = data.get (realX);
			for (int i = 0; i < column.size (); i++) {
				if (tileTpTable [column.get (i)] == 0) {
					return i;
				}
			}
			return 255;
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
