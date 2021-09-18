package gameObjects;

import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

import json.JSONObject;
import json.JSONUtil;
import main.GameObject;
import resources.Sprite;
import resources.Spritesheet;
import ui.DeathScreen;
import ui.Hud;
import ui.Inventory;
import ui.TileInterface;
import ui.UseItemScript;
import world.Entity;
import world.World;

public class Player extends GameObject {

	public static int STATE_FACING_LEFT = 0;
	public static int STATE_FACING_RIGHT = 1;
	public static int STATE_WIELDING_LEFT = 2;
	public static int STATE_WIELDING_RIGHT = 3;
	
	public static int STATE_MASK_DIRECTION = 0x1;
	public static int STATE_MASK_WIELDING = 0x2;
	
	public static int SCROLL_THRESHOLD = 4;
	public static int SCROLL_BOUND_LEFT = SCROLL_THRESHOLD;
	public static int SCROLL_BOUND_RIGHT = World.SCREEN_SIZE_H - SCROLL_THRESHOLD;
	public static int SCROLL_BOUND_TOP = SCROLL_THRESHOLD;
	public static int SCROLL_BOUND_BOTTOM = World.SCREEN_SIZE_V - SCROLL_THRESHOLD;
	
	public static int LOAD_DISTANCE = 128;
	
	public static Spritesheet PLAYER_SHEET = new Spritesheet ("resources/sprites/steve.png");
	public static Sprite PLAYER_SPRITES = new Sprite (PLAYER_SHEET, 8, 12);
	
	public static Spritesheet HELMET_SHEET = new Spritesheet ("resources/sprites/helmets.png");
	public static Spritesheet CHESTPLATE_SHEET = new Spritesheet ("resources/sprites/chestplates.png");
	public static Spritesheet LEGGINGS_SHEET = new Spritesheet ("resources/sprites/leggings.png");
	public static Spritesheet BOOTS_SHEET = new Spritesheet ("resources/sprites/boots.png");
	public static Spritesheet HELMET_INV_SHEET = new Spritesheet ("resources/sprites/helmets_inv.png");
	public static Spritesheet CHESTPLATE_INV_SHEET = new Spritesheet ("resources/sprites/chestplates_inv.png");
	public static Spritesheet LEGGINGS_INV_SHEET = new Spritesheet ("resources/sprites/leggings_inv.png");
	public static Spritesheet BOOTS_INV_SHEET = new Spritesheet ("resources/sprites/boots_inv.png");
	
	public static Sprite HELMET_SPRITE = new Sprite (HELMET_SHEET, 4, 4);
	public static Sprite CHESTPLATE_SPRITE = new Sprite (CHESTPLATE_SHEET, 4, 4);
	public static Sprite LEGGINGS_SPRITE = new Sprite (LEGGINGS_SHEET, 4, 3);
	public static Sprite BOOTS_SPRITE = new Sprite (BOOTS_SHEET, 4, 1);
	public static Sprite HELMET_INV_SPRITE = new Sprite (HELMET_INV_SHEET, 8, 8);
	public static Sprite CHESTPLATE_INV_SPRITE = new Sprite (CHESTPLATE_INV_SHEET, 8, 8);
	public static Sprite LEGGINGS_INV_SPRITE = new Sprite (LEGGINGS_INV_SHEET, 8, 6);
	public static Sprite BOOTS_INV_SPRITE = new Sprite (BOOTS_INV_SHEET, 8, 2);
	
	private int state = 0;
	
	private Inventory inventory;
	private TileInterface tileInterface;
	private Hud hud;
	private DeathScreen deathScreen;
	
	private int uiState = 0;
	private int storedUiState = 0;
	
	private long lastMove = 0;
	private int moveTime = 60;
	
	private double health = 100;
	
	private boolean noclip = false;
	
	public static final int[] helmetIds = {0, 268, 285, 300, 316, 332};
	public static final int[] chestplateIds = {0, 269, 286, 301, 317, 333};
	public static final int[] leggingsIds = {0, 270, 287, 302, 318, 334};
	public static final int[] bootsIds = {0, 271, 288, 303, 319, 335};
	
	private HashMap<String, UseItemScript> useItemScripts;
	
	public Player () {
		setSprite (PLAYER_SPRITES);
		getAnimationHandler ().setAnimationSpeed (0);
		deathScreen = new DeathScreen ();
		deathScreen.declare (0, 0);
		inventory = new Inventory ();
		inventory.declare (0, 0);
		tileInterface = new TileInterface ();
		tileInterface.declare (0, 0);
		
		hud = new Hud ();
		hud.declare (0, 0);
	}
	
	public Player (HashMap<String, String> attributes) {
		this ();
		try {
			int x = Integer.parseInt (attributes.get ("x"));
			int y = Integer.parseInt (attributes.get ("y"));
			inventory.loadFromMap (attributes);
			declare (x, y);
		} catch (Exception e) {
			declare (0, 64);
		}
	}
	
	@Override
	public void frameEvent () {
		//Handle movement
		scrollAboutPlayer ();
		if (keyDown ('A')) {
			doMove (-8, 0);
		} else if (keyDown ('D')) {
			doMove (8, 0);
		} else if (keyDown ('S')) {
			doMove (0, 8);
		} else if (keyDown ('W')) {
			doMove (0, -8);
		}
		if (keyPressed ('T')) {
			World.putStructure ("tree", (int)getX (), (int)getY ());
		}
		if (keyPressed ('Z')) {
			new Zombie (getX (), getY ());
		}
		if (keyPressed ('E') || (keyPressed (KeyEvent.VK_ESCAPE) && uiState != 0)) {
			toggleInventory ();
		} else if (keyPressed (KeyEvent.VK_ESCAPE) && uiState == 0) {
			//Handle ESC key when inventory is closed
			World.savePlayer ();
			World.saveReigons ();
			System.exit (0);
		}
		if (keyPressed ('N')) {
			noclip = !noclip;
		}
		
		//Handle loading of the world
		//World.updateReigons ();
		//World.updateWorld ();
		
		//Key detection for hotbar
		if (keyPressed ('1')) {
			inventory.setSelected (0);
		} else if (keyPressed ('2')) {
			inventory.setSelected (1);
		} else if (keyPressed ('3')) {
			inventory.setSelected (2);
		} else if (keyPressed ('4')) {
			inventory.setSelected (3);
		} else if (keyPressed ('5')) {
			inventory.setSelected (4);
		}
		doMove (0, 0);
		
		//Load/unload important stuff about the player
		World.refreshLoadAround ((int)(getX () / 8));
		
		//Handle death
		if (health <= 0) {
			deathScreen.show ();
		}
	}
	
	public void doMove (int xOffset, int yOffset) {
		if (xOffset != 0 || yOffset != 0) {
			if (World.getWorldTime () - lastMove < moveTime) {
				return;
			} else {
				lastMove = World.getWorldTime ();
			}
		}
		if (xOffset < 0) {
			state &= ~(STATE_MASK_DIRECTION);
		} else if (xOffset > 0) {
			state |= STATE_MASK_DIRECTION;
		}
		setX (getX () + xOffset);
		setY (getY () + yOffset);
		//Collision checks
		if (!noclip) {
			if (checkTile (0, -1)) {
				forceMove (-xOffset, -yOffset); //"no thats illegal move"
			}
			if (checkTile (0, 0)) {
				if (!checkTile (0, -1) && !checkTile (0, -2)) {
					forceMove (0, -8); //"go up the stairs move"
				} else {
					forceMove (-xOffset, -yOffset); //"no thats illegal move"
				}
			} else if (!checkTile (0, 1)) {
				int offs = 1;
				while (!checkTile (0, offs) && getTile (0, offs) != 9) {
					//Tile 9 is a ladder
					offs += 1;
				}
				if (offs > 5) {
					damage ((offs - 5) * 5);
				} //Fall damage
				forceMove (0, (offs - 1) * 8); //Falling
			}
		}
		
		//Scroll the screen to match
		scrollAboutPlayer ();
		
		//Update the world n stuff
		World.updateReigons ();
		World.updateWorld ();

	}
	
	public void forceMove (int xOffset, int yOffset) {
		setX (getX () + xOffset);
		setY (getY () + yOffset);
	}
	
	public int getTile (int xOffset, int yOffset) {
		int tileX = (int)getX () / 8 + xOffset;
		int tileY = (int)getY () / 8 + yOffset;
		return World.getTile (tileX, tileY);
	}
	
	public int getUiState () {
		return uiState;
	}
	
	public boolean checkTile (int xOffset, int yOffset) {
		return World.isSolid (getTile (xOffset, yOffset));
	}
	
	public void toggleInventory () {
		if (uiState == 0) {
			storedUiState = uiState;
			uiState = 1;
		} else {
			uiState = storedUiState;
		}
		updateUi ();
	}
	
	public void open3x3CraftingGrid () {
		if (uiState != 2) {
			storedUiState = uiState;
			uiState = 2;
		}
		updateUi ();
	}
	
	public void openFurnace () {
		if (uiState != 3) {
			storedUiState = uiState;
			uiState = 3;
		}
		updateUi ();
	}
	
	public void openChest () {
		if (uiState != 4) {
			storedUiState = uiState;
			uiState = 4;
		}
		updateUi ();
	}
	
	public void updateUi () {
		tileInterface.disable ();
		inventory.disable ();
		switch (uiState) {
			case 0:
				tileInterface.enable ();
				break;
			case 1:
				Inventory.setLayout (Inventory.LAYOUT_CRAFTING_2X2);
				inventory.enable ();
				break;
			case 2:
				Inventory.setLayout (Inventory.LAYOUT_CRAFTING_3X3);
				inventory.enable ();
				break;
			case 3:
				Inventory.setLayout (Inventory.LAYOUT_FURNACE);
				inventory.enable ();
				break;
			case 4:
				Inventory.setLayout (Inventory.LAYOUT_CHEST);
				inventory.enable ();
				break;
			default:
				break;
		}
	}
	
	public int addToInventory (int id, int amt) {
		return inventory.addToInventory (id, amt);
	}
	
	public int useFromInventory (int id, int amt) {
		return inventory.useFromInventory (id, amt);
	}
	
	public int getSelectedItem () {
		return inventory.getSelectedItem ();
	}
	
	public int useSelectedItem () {
		if (inventory.getSelectedItem () >= 256) {
			//This is an item
			if (doUseItem (inventory.getSelectedItem ())) {
				return inventory.useSelectedItem ();
			} else {
				return 0;
			}
		} else {
			//This is a tile
			return inventory.useSelectedItem ();
		}
	}
	
	public boolean doUseItem (int id) {
		
		//Init the use item scripts if null
		if (useItemScripts == null) {
			loadUseItemScripts ();
		}
		
		//Run the item's script (if it has one)
		JSONObject itemProperties = Inventory.itemProperties.getJSONObject (Integer.toString(id));
		String script = (String)itemProperties.get ("useItemScript");
		boolean used = false;
		if (script != null && useItemScripts.containsKey (script)) {
			used = useItemScripts.get (script).doUse (id, TileInterface.getHoveredTileX (), TileInterface.getHoveredTileY ());
		}
		return used;
		
	}
	
	public Inventory getInventory () {
		return inventory;
	}
	
	public void setUiState (int state) {
		uiState = state;
	}
	
	public boolean facingLeft () {
		return state == STATE_FACING_LEFT || state == STATE_WIELDING_LEFT;
	}
	
	public boolean facingRight () {
		return state == STATE_FACING_RIGHT || state == STATE_WIELDING_RIGHT;
	}
	
	public boolean isWielding () {
		return state == STATE_WIELDING_LEFT || state == STATE_WIELDING_RIGHT;
	}
	
	public void scrollAboutPlayer () {
		//Adjust scroll in x direction
		int renderedX = (int)(getX () - World.getViewX () * 8);
		if (renderedX < SCROLL_BOUND_LEFT * 8) {
			World.setViewX ((int)getX () / 8 - SCROLL_BOUND_LEFT);
		} else if (renderedX > SCROLL_BOUND_RIGHT * 8) {
			World.setViewX ((int)getX () / 8 - SCROLL_BOUND_RIGHT);
		}
		//Adjust scroll in y direction
		int renderedY = (int)(getY () - World.getViewY () * 8);
		if (renderedY < SCROLL_BOUND_TOP * 8) {
			World.setViewY (Math.max (((int)getY () / 8 - SCROLL_BOUND_TOP), 0));
		} else if (renderedY > SCROLL_BOUND_BOTTOM * 8) {
			World.setViewY (Math.min (((int)getY () / 8 - SCROLL_BOUND_BOTTOM), World.WORLD_HEIGHT - World.SCREEN_SIZE_V));
		}
		//Adjust loaded area around player
		int loadLeft = (int)(getX () / 8) - World.LOAD_SIZE / 2;
		int loadRight = loadLeft + World.LOAD_SIZE;
		World.setLoadBounds (loadLeft, loadRight);
	}
	
	public double getHealth () {
		return health;
	}
	
	public void setHealth (double health) {
		this.health = health;
	}
	
	public void damage (double amount) {
		//Calculate armor
		int helmetId = getInventory ().getItemByIndex (20).id;
		int chestplateId = getInventory ().getItemByIndex (21).id;
		int leggingsId = getInventory ().getItemByIndex (22).id;
		int bootsId = getInventory ().getItemByIndex (23).id;
		JSONObject helmetStats = Inventory.itemProperties.getJSONObject (Integer.toString(helmetId));
		JSONObject chestplateStats = Inventory.itemProperties.getJSONObject (Integer.toString(chestplateId));
		JSONObject leggingsStats = Inventory.itemProperties.getJSONObject (Integer.toString(leggingsId));
		JSONObject bootsStats = Inventory.itemProperties.getJSONObject (Integer.toString(bootsId));
		int helmetDefense = helmetStats.get ("protection") == null ? 0 : helmetStats.getInt ("protection");
		int chestplateDefense = chestplateStats.get ("protection") == null ? 0 : chestplateStats.getInt ("protection");
		int leggingsDefense = leggingsStats.get ("protection") == null ? 0 : leggingsStats.getInt ("protection");
		int bootsDefense = bootsStats.get ("protection") == null ? 0 : bootsStats.getInt ("protection");
		int totalDefense = helmetDefense + chestplateDefense + leggingsDefense + bootsDefense;
		double scalar = 1 - ((double)totalDefense / 30); //Max protection is 22, maxes out at ~73% damage reduction
		health -= amount * scalar;
		if (health < 0) {
			health = 0;
		}
	}
	
	public void heal (double amount) {
		health += amount;
		if (health > 100) {
			health = 100; //TODO adjustable max health cap
		}
	}
	
	public void save () {
		String filepath = "saves/" + World.getWorldName () + "/playerdat.txt";
		File f = new File (filepath); //Get the file to save to
		FileWriter fw;
		try {
			fw = new FileWriter (f);
			fw.append ("x:" + (int)getX () + "\n");
			fw.append ("y:" + (int)getY () + "\n");
			fw.append (inventory.toString ());
			fw.close ();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void loadUseItemScripts () {
		
		//Init the script map
		useItemScripts = new HashMap<String, UseItemScript> ();
		
		//Init all the scripts
		useItemScripts.put ("Seeds", new UseItemScript.Seeds ());
		useItemScripts.put ("Apple", new UseItemScript.Apple ());
		useItemScripts.put ("GoldenApple", new UseItemScript.GoldenApple ());
		useItemScripts.put ("Bread", new UseItemScript.Bread ());
		useItemScripts.put ("Hoe", new UseItemScript.Hoe ());
		useItemScripts.put ("ZombieEgg", new UseItemScript.ZombieEgg ());
		useItemScripts.put ("SkeletonEgg", new UseItemScript.SkeletonEgg ());
		
	}
	
	public TileInterface getTileInterface () {
		return tileInterface;
	}
	
	public boolean noclipEnabled () {
		return noclip;
	}
	
	@Override
	public void draw () {
		//Offset for drawing
		getAnimationHandler ().setFrame (state);
		setY (this.getY () - 4);
		super.draw ();
		setY (this.getY () + 4);
		
		//Set the correct armor sprites
		HELMET_SPRITE.setFrame (indexSearch (helmetIds, World.getPlayer ().getInventory ().getItemByIndex (20).id));
		CHESTPLATE_SPRITE.setFrame (indexSearch (chestplateIds, World.getPlayer ().getInventory ().getItemByIndex (21).id));
		LEGGINGS_SPRITE.setFrame (indexSearch (leggingsIds, World.getPlayer ().getInventory ().getItemByIndex (22).id));
		BOOTS_SPRITE.setFrame (indexSearch (bootsIds, World.getPlayer ().getInventory ().getItemByIndex (23).id));
		//Flip the helmet if applicable
		if ((state & STATE_MASK_DIRECTION) == STATE_FACING_RIGHT) {
			HELMET_SPRITE.setFrame (HELMET_SPRITE.getFrame () + 6);
		}
		//Use the weilding chestplate variant if applicable
		if ((state & STATE_MASK_WIELDING) != 0) {
			CHESTPLATE_SPRITE.setFrame (CHESTPLATE_SPRITE.getFrame () + 6);
		}
		
		//Draw the armor
		HELMET_SPRITE.draw ((int)getX () - World.getViewX () * 8 + 2, (int)getY () - World.getViewY () * 8 - 4);
		CHESTPLATE_SPRITE.draw ((int)getX () - World.getViewX () * 8 + 2, (int)getY () - World.getViewY () * 8);
		LEGGINGS_SPRITE.draw ((int)getX () - World.getViewX () * 8 + 2, (int)getY () - World.getViewY () * 8 + 4);
		BOOTS_SPRITE.draw ((int)getX () - World.getViewX () * 8 + 2, (int)getY () - World.getViewY () * 8 + 7);
		
		//Draw the inventory
		inventory.drawHotbar (); //Hotbar is always visible
		if (uiState == 1 || uiState == 2 || uiState == 3 ||  uiState == 4) {
			inventory.drawMenu ();
			//Draw the armor on the player, in the inventory
			HELMET_INV_SPRITE.setFrame (HELMET_SPRITE.getFrame () - (HELMET_SPRITE.getFrame () >= 6 ? 6 : 0));
			CHESTPLATE_INV_SPRITE.setFrame (CHESTPLATE_SPRITE.getFrame ());
			LEGGINGS_INV_SPRITE.setFrame (LEGGINGS_SPRITE.getFrame ());
			BOOTS_INV_SPRITE.setFrame (BOOTS_SPRITE.getFrame ());
			int invX = Inventory.menuOffsetX + Inventory.ARMOR_DISPLAY_START_X;
			int invY = Inventory.menuOffsetY + Inventory.ARMOR_DISPLAY_START_Y;
			HELMET_INV_SPRITE.draw (invX, invY);
			CHESTPLATE_INV_SPRITE.draw (invX, invY + 8);
			LEGGINGS_INV_SPRITE.draw (invX, invY + 16);
			BOOTS_INV_SPRITE.draw (invX, invY + 22);
		}
		
	}
	
	private int indexSearch (int[] arr, int val) {
		int idx = 0;
		for (int i = 0; i < arr.length; i++) {
			if (arr[i] == val) {
				idx = i;
			}
		}
		return idx;
	}
	
}
