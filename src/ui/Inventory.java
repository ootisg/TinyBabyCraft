package ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.HashMap;

import gameObjects.Container;
import gameObjects.Furnace;
import json.JSONException;
import json.JSONObject;
import json.JSONUtil;
import main.GameObject;
import main.MainLoop;
import resources.Sprite;
import world.World;

public class Inventory extends GameObject {
	
	public static int menuOffsetX = 32;
	public static int menuOffsetY = 23;
	public static int craftingOffsetX = 100;
	public static int craftingOffsetY = 40;
	public static int furnaceOffsetX = 0;
	public static int furnaceOffsetY = 0;
	public static int chestOffsetX = 0;
	public static int chestOffsetY = 0;
	
	public static final Layout LAYOUT_CRAFTING_2X2 = new Layout (32, 23, 
																 100, 40, 2, 
																 0, 0, false, 
																 0, 0, false);
	public static final Layout LAYOUT_CRAFTING_3X3 = new Layout (32, 23, 
																 100, 40, 3, 
																 0, 0, false, 
																 0, 0, false);
	public static final Layout LAYOUT_FURNACE = new Layout (32, 23, 
															0, 0, 0, 
															100, 40, true, 
															0, 0, false);
	public static final Layout LAYOUT_CHEST = new Layout (0, 0, 
														  0, 0, 0, 
														  0, 0, false, 
														  0, 0, true);
	
	public static int craftingSize = 3;
	public static boolean showChest = true;
	public static boolean showFurnace = false;
	
	public static int HOTBAR_OFFSET_X = 4;
	public static int HOTBAR_OFFSET_Y = 4;
	
	public static int CELL_SIZE = 12;
	public static int ITEM_OFFSET_X = 1;
	public static int ITEM_OFFSET_Y = 2;
	
	public static int INV_START_X = 2;
	public static int INV_START_Y = 48;
	
	public static int ARMOR_START_X = 8;
	public static int ARMOR_START_Y = 17;
	
	public static int HB_START_X = 2;
	public static int HB_START_Y = 1;
	
	public static int CRAFTING_2X2_START_X = 2;
	public static int CRAFTING_2X2_START_Y = 17;
	public static int CRAFTING_2X2_RESULT_X = 50;
	public static int CRAFTING_2X2_RESULT_Y = 22;
	
	public static int CRAFTING_3X3_START_X = 2;
	public static int CRAFTING_3X3_START_Y = 18;
	public static int CRAFTING_3X3_RESULT_X = 62;
	public static int CRAFTING_3X3_RESULT_Y = 30;
	
	public static int FURNACE_ITEM_X = 5;
	public static int FURNACE_ITEM_Y = 18;
	public static int FURNACE_FUEL_X = 5;
	public static int FURNACE_FUEL_Y = 42;
	public static int FURNACE_RESULT_X = 41;
	public static int FURNACE_RESULT_Y = 18;
	
	public static int CHEST_START_X = 0;
	public static int CHEST_START_Y = 0;
	
	public static int TEXT_OFFSET_X = 5;
	public static int TEXT_OFFSET_Y = 9;
	public static int TEXT_CHAR_WIDTH = 5;
	
	public static int FURNACE_SMELT_X = 5;
	public static int FURNACE_SMELT_Y = 30;
	public static int FURNACE_PROGRESS_X = 20;
	public static int FURNACE_PROGRESS_Y = 16;
	
	public static int MAX_STACK = 64;
	
	public static Sprite INVENTORY_BACKGROUND = new Sprite ("resources/sprites/inventory.png");
	public static Sprite HOTBAR_BACKGROUND = new Sprite ("resources/sprites/hotbar.png");
	public static Sprite HOTBAR_SELECTOR = new Sprite ("resources/sprites/selector.png");
	public static Sprite CRAFTING_2X2_BACKGROUND = new Sprite ("resources/sprites/crafting_2x2.png");
	public static Sprite CRAFTING_3X3_BACKGROUND = new Sprite ("resources/sprites/crafting_3x3.png");
	public static Sprite FURNACE_BACKGROUND = new Sprite ("resources/sprites/furnace.png");
	public static Sprite CHEST_BACKGROUND = new Sprite ("resources/sprites/chest.png");
	
	private Item[] items;
	private Item held;
	private static Container container;
	
	public static int INVENTORY_INDEX = 0;
	public static int ARMOR_INDEX = 20;
	public static int CRAFTING_INDEX = 25;
	public static int CRAFTING_RESULT_INDEX = 34;
	public static int FURNACE_ITEM_INDEX = 35;
	public static int FURNACE_FUEL_INDEX = 36;
	public static int FURNACE_RESULT_INDEX = 37;
	
	private int selectedCell = 0;
	
	private boolean enabled = false;
	
	public static JSONObject itemProperties;
	
	public Inventory () {
		//Load up the item properties
		try {
			itemProperties = JSONUtil.loadJSONFile ("resources/gamedata/items.txt");
		} catch (JSONException e) {
			e.printStackTrace ();
			System.exit (1);
		}
		
		for (int i = 0; i < 512; i++) {
			String ind = String.valueOf (i);
			JSONObject jobj = itemProperties.getJSONObject (ind);
			if (jobj != null) {
				//System.out.println (jobj);
			}
		}
		
		//Initialize the items
		items = new Item[100];
		for (int i = 0; i < items.length; i++) {
			items [i] = new Item (0, 0);
		}
		
		//Set initial layout
		setLayout (LAYOUT_CRAFTING_2X2);
		
		//Initialize the crafting recipes
		Recipes.init ();
	}
	
	@Override
	public void frameEvent () {
		
		//Sync entity contents with inventory
		if (container != null) {
			syncContainerToInventory ();
		}
		
		if ((mouseButtonClicked (0) || mouseButtonClicked (2)) && enabled) {
			
			//Calculate click areas
			int cellIndex = -1;
			int cellsStartX = menuOffsetX + INV_START_X;
			int cellsStartY = menuOffsetY + INV_START_Y;
			int armorStartX = menuOffsetX + ARMOR_START_X;
			int armorStartY = menuOffsetY + ARMOR_START_Y;
			//Different per crafting menu type
			int craftingStartX = 0;
			int craftingStartY = 0;
			int craftResultX = 0;
			int craftResultY = 0;
			if (craftingSize == 2) {
				craftingStartX = craftingOffsetX + CRAFTING_2X2_START_X;
				craftingStartY = craftingOffsetY + CRAFTING_2X2_START_Y;
				craftResultX = craftingOffsetX + CRAFTING_2X2_RESULT_X;
				craftResultY = craftingOffsetY + CRAFTING_2X2_RESULT_Y;
			} else if (craftingSize == 3) {
				craftingStartX = craftingOffsetX + CRAFTING_3X3_START_X;
				craftingStartY = craftingOffsetY + CRAFTING_3X3_START_Y;
				craftResultX = craftingOffsetX + CRAFTING_3X3_RESULT_X;
				craftResultY = craftingOffsetY + CRAFTING_3X3_RESULT_Y;
			}
			//Time to do furnace yay
			int furnaceInX = furnaceOffsetX + FURNACE_ITEM_X;
			int furnaceInY = furnaceOffsetY + FURNACE_ITEM_Y;
			int furnaceFuelX = furnaceOffsetX + FURNACE_FUEL_X;
			int furnaceFuelY = furnaceOffsetY + FURNACE_FUEL_Y;
			int furnaceResX = furnaceOffsetX + FURNACE_RESULT_X;
			int furnaceResY = furnaceOffsetY + FURNACE_RESULT_Y;
			//And the chest
			int chestStartX = chestOffsetX;
			int chestStartY = chestOffsetY;
			
			//Make us some rectangles
			Rectangle invBounds = new Rectangle (cellsStartX, cellsStartY, CELL_SIZE * 5, CELL_SIZE * 4);
			Rectangle armorBounds = new Rectangle (armorStartX, armorStartY, CELL_SIZE * 2, CELL_SIZE * 2);
			//Crafting stuff
			Rectangle craftingBounds = new Rectangle (craftingStartX, craftingStartY, CELL_SIZE * craftingSize, CELL_SIZE * craftingSize);
			Rectangle craftResultBounds = new Rectangle (craftResultX, craftResultY, CELL_SIZE, CELL_SIZE);
			//Furnace stuff
			Rectangle furnaceInBounds = new Rectangle (furnaceInX, furnaceInY, CELL_SIZE, CELL_SIZE);
			Rectangle furnaceFuelBounds = new Rectangle (furnaceFuelX, furnaceFuelY, CELL_SIZE, CELL_SIZE);
			Rectangle furnaceResultBounds = new Rectangle (furnaceResX, furnaceResY, CELL_SIZE, CELL_SIZE);
			//Chest stuff
			Rectangle chestBounds = new Rectangle (chestStartX, chestStartY, CELL_SIZE * 5, CELL_SIZE * 3);
			int menuClicked = 0; //0 for inventory
			
			//Check for mouse clicks in the relevant areas
			if (cursorInBounds (invBounds)) {
				//Inside the items menu
				int cellX = (getCursorX () - cellsStartX) / CELL_SIZE;
				int cellY = (getCursorY () - cellsStartY) / CELL_SIZE;
				cellIndex = cellY * 5 + cellX; //We now have the clicked-on cell
			} else if (cursorInBounds (armorBounds)) {
				//Inside the armor menu
				int cellX = (getCursorX () - armorStartX) / CELL_SIZE;
				int cellY = (getCursorY () - armorStartY) / CELL_SIZE;
				cellIndex = ARMOR_INDEX + cellY * 2 + cellX; //Here's the clicked-on armor cell
			} else if (craftingSize != 0 && cursorInBounds (craftingBounds)) {
				//Inside the crafting menu
				int cellX = (getCursorX () - craftingStartX) / CELL_SIZE;
				int cellY = (getCursorY () - craftingStartY) / CELL_SIZE;
				cellIndex = CRAFTING_INDEX + cellY * craftingSize + cellX; //Here's the clicked-on crafting cell
				menuClicked = 1; //1 for crafting menu
			} else if (craftingSize != 0 && cursorInBounds (craftResultBounds)) {
				//Clicked on the crafting result
				cellIndex = CRAFTING_RESULT_INDEX; //Here's the clicked-on crafting cell
				menuClicked = 2; //2 for crafted item
			} else if (showChest && cursorInBounds (chestBounds)) {
				//Inside the chest menu
				int cellX = (getCursorX () - chestStartX) / CELL_SIZE;
				int cellY = (getCursorY () - chestStartY) / CELL_SIZE;
				cellIndex = cellY * craftingSize + cellX; //Here's the clicked-on chest cell
				menuClicked = 3; //3 for chest
			} else if (showFurnace && cursorInBounds (furnaceInBounds)) {
				cellIndex = FURNACE_ITEM_INDEX;
				menuClicked = 4;
			} else if (showFurnace && cursorInBounds (furnaceFuelBounds)) {
				cellIndex = FURNACE_FUEL_INDEX;
				menuClicked = 4;
			} else if (showFurnace && cursorInBounds (furnaceResultBounds)) {
				cellIndex = FURNACE_RESULT_INDEX;
				menuClicked = 4;
			}
			
			if (cellIndex != -1) {
				
				//Inventory or crafting grid
				if (menuClicked == 0 || menuClicked == 1 || (menuClicked == 4 && cellIndex != FURNACE_RESULT_INDEX)) {
					if (mouseButtonClicked (0)) {
						//Deal with held item-ness
						if (held == null && items [cellIndex].id != 0) {
							//Pick up the item
							held = items [cellIndex];
							items [cellIndex] = new Item (0, 0);
						} else if (held != null) {
							placeHeldItem (cellIndex, held.amount);
						}
					} else if (mouseButtonClicked (2)) {
						if (held == null && items [cellIndex].id != 0) {
							//If picking up half
							held = new Item (items [cellIndex].id, (items [cellIndex].amount + 1) / 2);
							items [cellIndex].amount = items [cellIndex].amount - held.amount;
							if (items [cellIndex].amount <= 0) {
								items [cellIndex] = new Item (0, 0);
							}
						} else if (held != null) {
							//If placing down one
							placeHeldItem (cellIndex, 1);
						}
					}
				}
				
				//Crafting result
				if (menuClicked == 2 || (menuClicked == 4 && cellIndex == FURNACE_RESULT_INDEX)) {
					if (mouseButtonClicked (0)) {
						//Pick up the whole stack
						if (held == null && items [cellIndex].id != 0) {
							//Pick up to empty
							doCraft ();
							held = items [cellIndex];
							items [cellIndex] = new Item (0, 0);
						} else if (held.id == items [cellIndex].id && held.amount + items [cellIndex].amount <= MAX_STACK) {
							//Merge with held stack
							doCraft ();
							held.amount += items [cellIndex].amount;
							items [cellIndex] = new Item (0, 0);
						}
					} else if (mouseButtonClicked (2)) {
						//Pick up half the stack
						if (held == null && items [cellIndex].id != 0) {
							held = new Item (items [cellIndex].id, (items [cellIndex].amount + 1) / 2);
							items [cellIndex].amount = items [cellIndex].amount - held.amount;
							if (items [cellIndex].amount <= 0) {
								items [cellIndex] = new Item (0, 0);
							}
						}
					}
				}
				
				//Sync inventory with container
				syncInventoryToContainer ();
				
			}
			
			predictCraft ();
		}
		
	}
	
	private boolean cursorInBounds (Rectangle r) {
		return r.contains (getCursorX (), getCursorY ());
	}
	
	public void placeHeldItem (int slot, int amt) {
		if (items [slot].id == 0) {
			//Place item down
			if (isCompatable (slot, held)) {
				items [slot] = new Item (held.id, amt);
				held.amount -= amt;
			}
		} else if (items [slot].id == held.id) {
			//Merge the stacks
			int total = amt + items [slot].amount;
			if (total > MAX_STACK) {
				items [slot].amount = MAX_STACK;
				held.amount = (held.amount + items [slot].amount) - MAX_STACK;
			} else {
				items [slot].amount = total;
				held.amount -= amt;
			}
		} else if (amt == held.amount) {
			//Swap the items, only applies for full amount
			Item temp = held;
			held = items [slot];
			items [slot] = temp;
		}
		if (held.amount <= 0) {
			held = null;
		}
	}
	
	public void drawMenu () {
		//Drawing the inventory:
		//Draw the background
		INVENTORY_BACKGROUND.draw (menuOffsetX, menuOffsetY);
		
		//Draw the items in the inventory
		for (int i = 0; i < 20; i++) {
			int xdraw = menuOffsetX + INV_START_X + ITEM_OFFSET_X + (i % 5) * CELL_SIZE;
			int ydraw = menuOffsetY + INV_START_Y + ITEM_OFFSET_Y + (i / 5) * CELL_SIZE;
			if (items [i].id != 0) {
				drawItem (items [i].id, items [i].amount, xdraw, ydraw);
			}
		}
		
		//Draw the items in the armor slots
		for (int i = 0; i < 4; i++) {
			int xdraw = menuOffsetX + ARMOR_START_X + ITEM_OFFSET_X + (i % 2) * CELL_SIZE;
			int ydraw = menuOffsetY + ARMOR_START_Y + ITEM_OFFSET_Y + (i / 2) * CELL_SIZE;
			if (items [i + 20].id != 0) {
				drawItem (items [i + 20].id, items [i + 20].amount, xdraw, ydraw);
			}
		}
		
		//Drawing the crafting grid:
		if (craftingSize == 2) {
			
			//Draw the background
			CRAFTING_2X2_BACKGROUND.draw (craftingOffsetX, craftingOffsetY);
			
			//Draw the items in the crafting grid
			for (int i = 0; i < 4; i++) {
				int xdraw = craftingOffsetX + CRAFTING_2X2_START_X + ITEM_OFFSET_X + (i % 2) * CELL_SIZE;
				int ydraw = craftingOffsetY + CRAFTING_2X2_START_Y + ITEM_OFFSET_Y + (i / 2) * CELL_SIZE;
				if (items [CRAFTING_INDEX + i].id != 0) {
					drawItem (items [CRAFTING_INDEX + i].id, items [CRAFTING_INDEX + i].amount, xdraw, ydraw);
				}
			}
			
			//Draw the crafting result
			int xdraw = craftingOffsetX + CRAFTING_2X2_RESULT_X + ITEM_OFFSET_X;
			int ydraw = craftingOffsetY + CRAFTING_2X2_RESULT_Y + ITEM_OFFSET_Y;
			if (items [CRAFTING_RESULT_INDEX].id != 0) {
				Item working = items [CRAFTING_RESULT_INDEX];
				drawItem (working.id, working.amount, xdraw, ydraw);
			}
			
		} else if (craftingSize == 3) {
			
			//Draw the background
			CRAFTING_3X3_BACKGROUND.draw (craftingOffsetX, craftingOffsetY);
			
			//Draw the items in the crafting grid
			for (int i = 0; i < 9; i++) {
				int xdraw = craftingOffsetX + CRAFTING_3X3_START_X + ITEM_OFFSET_X + (i % 3) * CELL_SIZE;
				int ydraw = craftingOffsetY + CRAFTING_3X3_START_Y + ITEM_OFFSET_Y + (i / 3) * CELL_SIZE;
				if (items [CRAFTING_INDEX + i].id != 0) {
					drawItem (items [CRAFTING_INDEX + i].id, items [CRAFTING_INDEX + i].amount, xdraw, ydraw);
				}
			}
			
			//Draw the crafting result
			int xdraw = craftingOffsetX + CRAFTING_3X3_RESULT_X + ITEM_OFFSET_X;
			int ydraw = craftingOffsetY + CRAFTING_3X3_RESULT_Y + ITEM_OFFSET_Y;
			if (items [CRAFTING_RESULT_INDEX].id != 0) {
				Item working = items [CRAFTING_RESULT_INDEX];
				drawItem (working.id, working.amount, xdraw, ydraw);
			}
			
		}
		
		//Draw the furnace
		if (showFurnace) {
			
			//Draw the background
			FURNACE_BACKGROUND.draw (furnaceOffsetX, furnaceOffsetY);
			
			//Draw the furnace flames and progress arrow
			Furnace f = (Furnace)container;
			BufferedImage flameSprite = f.getFuelSprite ();
			BufferedImage pogSprite = f.getProgressSprite ();
			Sprite.draw (flameSprite, furnaceOffsetX + FURNACE_SMELT_X, furnaceOffsetY + FURNACE_SMELT_Y);
			Sprite.draw (pogSprite, furnaceOffsetX + FURNACE_PROGRESS_X, furnaceOffsetY + FURNACE_PROGRESS_Y);
			
			//Draw the furnace in item
			int xdraw = furnaceOffsetX + FURNACE_ITEM_X + ITEM_OFFSET_X;
			int ydraw = furnaceOffsetY + FURNACE_ITEM_Y + ITEM_OFFSET_Y;
			if (items [FURNACE_ITEM_INDEX].id != 0) {
				Item working = items [FURNACE_ITEM_INDEX];
				drawItem (working.id, working.amount, xdraw, ydraw);
			}
			
			//Draw the furnace fuel item
			xdraw = furnaceOffsetX + FURNACE_FUEL_X + ITEM_OFFSET_X;
			ydraw = furnaceOffsetY + FURNACE_FUEL_Y + ITEM_OFFSET_Y;
			if (items [FURNACE_FUEL_INDEX].id != 0) {
				Item working = items [FURNACE_FUEL_INDEX];
				drawItem (working.id, working.amount, xdraw, ydraw);
			}
			
			//Draw the furnace result item
			xdraw = furnaceOffsetX + FURNACE_RESULT_X + ITEM_OFFSET_X;
			ydraw = furnaceOffsetY + FURNACE_RESULT_Y + ITEM_OFFSET_Y;
			if (items [FURNACE_RESULT_INDEX].id != 0) {
				Item working = items [FURNACE_RESULT_INDEX];
				drawItem (working.id, working.amount, xdraw, ydraw);
			}
			
		}
		
		//Draw the 'held' item
		if (held != null) {
			drawItem (held.id, held.amount, getCursorX (), getCursorY ());
		}
		
	}
	
	public void drawHotbar () {
		//Draw the background
		HOTBAR_BACKGROUND.draw (HOTBAR_OFFSET_X, HOTBAR_OFFSET_Y);
		
		//Draw the selector
		int xdraw = HOTBAR_OFFSET_X + HB_START_X - 1 + selectedCell * CELL_SIZE;
		int ydraw = HOTBAR_OFFSET_Y + HB_START_Y - 1;
		HOTBAR_SELECTOR.draw (xdraw, ydraw);
		
		//Draw the items in the hotbar
		for (int i = 15; i < 20; i++) {
			xdraw = HOTBAR_OFFSET_X + HB_START_X + ITEM_OFFSET_X + (i % 5) * CELL_SIZE;
			ydraw = HOTBAR_OFFSET_Y + HB_START_Y + ITEM_OFFSET_Y;
			if (items [i].id != 0) {
				drawItem (items [i].id, items [i].amount, xdraw, ydraw);
			}
		}
	}
	
	public void drawItem (int id, int amt, int x, int y) {
		BufferedImage icon = World.getItem (id);
		Graphics2D g = (Graphics2D)MainLoop.getWindow ().getBufferGraphics ();
		g.drawImage (icon, x, y, null);
		g.setColor (new Color (0xFFFFFF));
		Font f = new Font ("Courier", 8, 8);
		g.setFont (f);
		int charOffset = (Integer.toString (amt).length () - 1) * -5;
		g.drawString (Integer.toString (amt), x + TEXT_OFFSET_X + charOffset, y + TEXT_OFFSET_Y);
	}
	
	public int addToInventory (int id, int amt) {
		int total = 0;
		for (int i = 0; i < amt; i++) {
			for (int j = 0; j < items.length; j++) {
				if (items[j].id == id && items[j].amount < MAX_STACK) {
					items[j].amount++;
					total++;
					break;
				}
			}
		}
		if (total < amt) {
			for (int i = 0; i < amt; i++) {
				for (int j = 0; j < items.length; j++) {
					if (items[j].id == 0) {
						items[j].id = id;
						items[j].amount = 1;
						total++;
						break;
					} else if (items[j].id == id && items[j].amount < MAX_STACK) {
						items[j].amount++;
						total++;
						break;
					}
				}
			}
		}
		return total;
	}
	
	public int useFromInventory (int id, int amt) {
		int total = 0;
		for (int i = 0; i < amt; i++) {
			for (int j = 0; j < items.length; j++) {
				if (items[j].id == id) {
					items[j].amount--;
					total++;
					if (items [j].amount == 0) {
						items [j].id = 0;
					}
					break;
				}
			}
		}
		return total;
	}
	
	public void predictCraft () {
		Item working = Recipes.queryCraft (items);
		items [CRAFTING_RESULT_INDEX] = working;
	}
	
	public void doCraft () {
		Recipes.doCraft (items);
	}
	
	public int getSelected () {
		return selectedCell;
	}
	
	public void setSelected (int cell) {
		selectedCell = cell;
	}
	
	public int getSelectedItem () {
		return items [selectedCell + 15].id;
	}
	
	public int useSelectedItem () {
		if (items [selectedCell + 15].id == 0) {
			return 0;
		} else {
			items [selectedCell + 15].amount--;
			if (items [selectedCell + 15].amount == 0) {
				items [selectedCell + 15] = new Item (0, 0);
			}
			return 1;
		}
	}
	
	public static void setContainer (Container c) {
		container = c;
	}
	
	public void syncContainerToInventory () {
		//TODO support mappings other than furnace?
		if (container != null) {
			for (int i = 0; i < container.getCapacity (); i++) {
				items [FURNACE_ITEM_INDEX + i] = container.getItem (i);
			}
		}
	}
	
	public void syncInventoryToContainer () {
		//TODO support mappings other than furnace?
		if (container != null) {
			for (int i = 0; i < container.getCapacity (); i++) {
				container.setItem (i, items [FURNACE_ITEM_INDEX + i]);
			}
		}
	}
	
	public boolean isCompatable (int slot, Item it) {
		if (slot <= FURNACE_ITEM_INDEX) {
			return true;
		} else {
			return container.isCompatable (slot - FURNACE_ITEM_INDEX, it);
		}
	}
	
	public void enable () {
		enabled = true;
	}
	
	public void disable () {
		enabled = false;
	}
	
	public void loadFromMap (HashMap<String, String> invSlots) {
		for (int i = 0; i < 24; i++) {
			if (invSlots.containsKey ("inv" + i)) {
				String item = invSlots.get ("inv" + i);
				String[] split = item.split (",");
				int itemId = Integer.parseInt (split [0]);
				int itemAmt = Integer.parseInt (split [1]);
				items [i] = new Item (itemId, itemAmt);
			}
		}
	}
	
	public static void setLayout (Layout layout) {
		menuOffsetX = layout.menuOffsetX;
		menuOffsetY = layout.menuOffsetY;
		craftingOffsetX = layout.craftingOffsetX;
		craftingOffsetY = layout.craftingOffsetY;
		craftingSize = layout.craftingSize;
		furnaceOffsetX = layout.furnaceOffsetX;
		furnaceOffsetY = layout.furnaceOffsetY;
		showFurnace = layout.showFurnace;
		chestOffsetX = layout.chestOffsetX;
		chestOffsetY = layout.chestOffsetY;
		showChest = layout.showChest;
	}
	
	@Override
	public String toString () {
		String result = "";
		for (int i = 0; i < 24; i++) {
			if (items [i].id != 0) {
				if (result.length () != 0) {
					result += "\n";
				}
				result += ("inv" + i + ":" + items [i].id + "," + items[i].amount);
			}
		}
		return result;
	}
	
	public static class Item {
		
		public int id;
		public int amount;
		
		public Item (int id, int amount) {
			this.id = id;
			this.amount = amount;
		}
		
		public Item (Item item) {
			this.id = item.id;
			this.amount = item.amount;
		}
		
		public Item (String stringRep) {
			String[] params = stringRep.split (",");
			this.id = Integer.parseInt (params [0]);
			this.amount = Integer.parseInt (params [1]);
		}
		
		@Override
		public String toString () {
			return id + "x" + amount;
		}
		
	}
	
	public static class Layout {
		
		//YES, I know this solution is very bad. I would use a HashMap if it mattered.
		
		public int menuOffsetX;
		public int menuOffsetY;
		public int craftingOffsetX;
		public int craftingOffsetY;
		public int craftingSize;
		public int furnaceOffsetX;
		public int furnaceOffsetY;
		public boolean showFurnace;
		public int chestOffsetX;
		public int chestOffsetY;
		public boolean showChest;
		
		public Layout (int menuOffsetX, int menuOffsetY, int craftingOffsetX, int craftingOffsetY, int craftingSize, int furnaceOffsetX, int furnaceOffsetY, boolean showFurnace, int chestOffsetX, int chestOffsetY, boolean showChest) {
			this.menuOffsetX = menuOffsetX;
			this.menuOffsetY = menuOffsetY;
			this.craftingOffsetX = craftingOffsetX;
			this.craftingOffsetY = craftingOffsetY;
			this.craftingSize = craftingSize;
			this.furnaceOffsetX = furnaceOffsetX;
			this.furnaceOffsetY = furnaceOffsetY;
			this.showFurnace = showFurnace;
			this.chestOffsetX = chestOffsetX;
			this.chestOffsetY = chestOffsetY;
			this.showChest = showChest;
		}
		
	}
	
}
