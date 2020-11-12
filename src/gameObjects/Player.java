package gameObjects;

import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

import main.GameObject;
import resources.Sprite;
import resources.Spritesheet;
import ui.Inventory;
import ui.TileInterface;
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
	
	private int state = 0;
	
	private Inventory inventory;
	private TileInterface tileInterface;
	
	private int uiState = 0;
	private int storedUiState = 0;
	
	private long lastMove = 0;
	private int moveTime = 60;
	
	public Player () {
		setSprite (PLAYER_SPRITES);
		getAnimationHandler ().setAnimationSpeed (0);
		inventory = new Inventory ();
		inventory.declare (0, 0);
		tileInterface = new TileInterface ();
		tileInterface.declare (0, 0);
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
			World.spawnStructure ("tree", (int)getX () / 8, (int)getY () / 8);
		}
		if (keyPressed ('Z')) {
			new Zombie (getX (), getY ());
		}
		if (keyPressed ('E')) {
			toggleInventory ();
		}
		
		//Handle ESC key
		if (keyPressed (KeyEvent.VK_ESCAPE)) {
			World.savePlayer ();
			World.saveReigons ();
			System.exit (0);
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
			forceMove (0, (offs - 1) * 8); //Falling
		}
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
		return inventory.useSelectedItem ();
	}
	
	public Inventory getInventory () {
		return inventory;
	}
	
	public void setUiState (int state) {
		uiState = state;
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
	
	@Override
	public void draw () {
		//Offset for drawing
		getAnimationHandler ().setFrame (state);
		setY (this.getY () - 4);
		super.draw ();
		setY (this.getY () + 4);
		inventory.drawHotbar (); //Hotbar is always visible
		if (uiState == 1 || uiState == 2 || uiState == 3) {
			inventory.drawMenu ();
		}
	}
	
}
