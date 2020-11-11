package world;

import java.awt.Graphics;
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
import java.util.Scanner;
import java.util.UUID;

import gameObjects.EntityObject;
import gameObjects.Player;
import json.JSONArray;
import json.JSONException;
import json.JSONObject;
import json.JSONUtil;
import main.GameObject;
import main.MainLoop;
import resources.Sprite;
import resources.Spritesheet;

public class World {
	
	public static final int WORLD_HEIGHT = 128;
	public static final int LOAD_SIZE = 256;
	public static final int SCREEN_SIZE_H = 24; //Number of tiles that fit on the screen horizontally
	public static final int SCREEN_SIZE_V = 18; //Number of tiles that fit on the screen vertically
	
	public static final Spritesheet TILE_SHEET = new Spritesheet ("resources/sprites/tiles.png");
	public static final Spritesheet ITEM_SHEET = new Spritesheet ("resources/sprites/items.png");
	public static final Sprite PARSED_TILES = new Sprite (TILE_SHEET, 8, 8);
	public static final Sprite PARSED_ITEMS = new Sprite (ITEM_SHEET, 8, 8);
	
	private static boolean[] solidMap;
	
	private static ArrayList<ArrayList<Integer>> tiles;
	private static int[] xBuffer;
	
	private static int viewX;
	private static int viewY;
	
	private static int loadLeft;
	private static int loadRight;
	
	private static ArrayList<WorldReigon> reigons;
	private static LinkedList<Entity> entities;
	private static HashMap<Point, Entity> tileEntities;
	
	private static Player player;
	
	private static long seed;
	
	private static String worldName = "default";
	
	private static HashMap<String, Structure> structures;
	
	private static long worldTime;
	
	public static JSONObject tileProperties;
	public static JSONObject dropList;
	
	public static void initWorld () {
		
		try {
			tileProperties = JSONUtil.loadJSONFile ("resources/gamedata/tiles.txt");
			dropList = JSONUtil.loadJSONFile ("resources/gamedata/drops.txt");
		} catch (JSONException e) {
			e.printStackTrace ();
			System.exit (1);
		}
		
		viewX = 0;
		viewY = 0; //Initialize view
		loadLeft = -128;
		loadRight = 128; //Initialize loading bounds
		
		loadStructures ();
		
		File f = new File ("saves/" + worldName);
		if (!f.exists ()) {
			f.mkdir (); //TODO check if directory was not created
		} //Makes the directory for the world if it wasn't already created
		
		reigons = new ArrayList<WorldReigon> ();
		tiles = new ArrayList<ArrayList<Integer>> (); //Make the world tiles
		for (int i = 0; i < LOAD_SIZE; i++) {
			tiles.add (null);
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
		solidMap = new boolean[PARSED_TILES.getFrameCount ()];
		for (int i = 0; i < solidMap.length; i++) {
			solidMap [i] = true;
		}
		solidMap [0] = false; //air
		solidMap [9] = false; //ladder
		solidMap [10] = false; //torch
		//Make the player
		spawnPlayer ();
		System.out.println(tileEntities);
	}
	
	public static void worldFrame () {
		worldTime = System.currentTimeMillis ();
	}
	
	public static void loadStructures () {
		structures = new HashMap<String, Structure> ();
		
		File dir = new File ("resources/gamedata/structures");
		File[] structPaths = dir.listFiles ();
		HashMap<String, File> structures = new HashMap<String, File> ();
		for (int i = 0; i < structPaths.length; i++) {
			structures.put (structPaths [i].getName (), structPaths [i]);
		}
		int count = 0;
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
		}
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
		for (int wy = 0; wy < SCREEN_SIZE_V; wy++) {
			for (int wx = 0; wx < SCREEN_SIZE_H; wx++) {
				int tileId = tiles.get (Math.floorMod (viewX + wx, LOAD_SIZE)).get (viewY + wy);
				PARSED_TILES.setFrame (tileId);
				PARSED_TILES.draw (wx * 8, wy * 8);
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
		return solidMap [id];
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
	
	public static void setTile (int id, int x, int y) {
		tiles.get (Math.floorMod (x, LOAD_SIZE)).set (y, id);
		WorldReigon rg = getReigon (id);
		
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
		Integer[] result = new Integer[WORLD_HEIGHT];
		for (int i = 0; i < 63; i++) {
			result [i] = 0;
		}
		result[63] = 1;
		result[64] = 2;
		for (int i = 65; i < WORLD_HEIGHT; i++) {
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
		boolean replace = false;
		if (s.getProperty ("replace") != null) {
			replace = true;
		}
		
		for (int wx = 0; wx < s.getWidth (); wx++) {
			int[] slice = s.getSlice (wx - origin.x);
			for (int wy = 0; wy < s.getHeight (); wy++) {
				if (!(!replace && slice [wy] == 0)) {
					setTile (slice [wy], topLeft.x + wx, topLeft.y + wy);
				}
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
				for (int wx = 0; wx < REIGON_SIZE; wx++) {
					ArrayList<Integer> tiles = generateColumn (id * REIGON_SIZE + wx);
					data.set (wx, tiles);
				} //Generate the tiles
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
			return data.get (Math.floorMod(x, REIGON_SIZE)).get (y);
		}
		
		public void setTile (int id, int x, int y) {
			data.get (Math.floorMod(x, REIGON_SIZE)).set (y, id);
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