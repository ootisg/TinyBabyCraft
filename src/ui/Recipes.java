package ui;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class Recipes {
	
	public static ArrayList<Recipe> recipes = new ArrayList<Recipe> ();
	
	public static void init () {
		loadRecipes ();
		for (int i = 0; i < recipes.size (); i++) {
			System.out.println (recipes.get (i));
		}
	}
	
	public static void loadRecipes () {
		recipes = new ArrayList<Recipe> ();
		File f = new File ("resources/gamedata/recipes.txt");
		Scanner s;
		try {
			s = new Scanner (f);
			while (s.hasNextLine ()) {
				addRecipe (s.nextLine ());
			}
			s.close ();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static void addRecipe (String recipeString) {
		if (recipeString.charAt (0) == ';') {
			return;
		}
		Scanner s = new Scanner (recipeString);
		String fmt = s.next ();
		Inventory.Item[] cost = null;
		Inventory.Item result = null;
		int intfmt = -1;
		if (fmt.equals ("2x2")) {
			intfmt = 0;
		}
		if (fmt.equals ("3x3")) {
			intfmt = 1;
		}
		if (fmt.equals ("shapeless")) {
			intfmt = 2;
		}
		while (s.hasNext ()) {
			if (intfmt == 0) {
				cost = new Inventory.Item[4];
				for (int i = 0; i < 4; i++) {
					cost [i] = new Inventory.Item (s.next ());
				}
				result = new Inventory.Item (s.next ());
			}
			if (intfmt == 1) {
				cost = new Inventory.Item[9];
				for (int i = 0; i < 9; i++) {
					cost [i] = new Inventory.Item (s.next ());
				}
				result = new Inventory.Item (s.next ());
			}
			if (intfmt == 2) {
				ArrayList<String> allitms = new ArrayList<String> ();
				while (s.hasNext ()) {
					allitms.add (s.next ());
				}
				cost = new Inventory.Item[allitms.size () - 1];
				for (int i = 0; i < cost.length; i++) {
					cost [i] = new Inventory.Item (allitms.get (i));
				}
				result = new Inventory.Item (allitms.get (allitms.size () - 1));
			}
		}
		recipes.add (new Recipe (intfmt, cost, result));
		s.close ();
	}
	
	public static Inventory.Item queryCraft (Inventory.Item[] inventory) {
		Inventory.Item[] grid = getCraftingGrid (inventory);
		for (int i = 0; i < recipes.size (); i++) {
			if (recipes.get (i).matches (grid)) {
				return new Inventory.Item (recipes.get (i).result);
			}
		}
		return new Inventory.Item (0, 0);
	}
	
	public static void doCraft (Inventory.Item[] inventory) {
		Inventory.Item[] grid = getCraftingGrid (inventory);
		for (int i = 0; i < recipes.size (); i++) {
			if (recipes.get (i).matches (grid)) {
				recipes.get (i).craft (grid);
			}
		}
	}
	
	private static Inventory.Item[] getCraftingGrid (Inventory.Item[] inventory) {
		Inventory.Item[] items = new Inventory.Item[9];
		for (int i = 0; i < 9; i++) {
			items [i] = inventory [Inventory.CRAFTING_INDEX + i];
		}
		return items;
	}
	
	public static class Recipe {
		
		public int type; //0 is 2x2 shaped, 1 is 3x3 shaped, 2 is shapeless
		public Inventory.Item[] cost;
		public Inventory.Item result;
		
		public Recipe (int type, Inventory.Item[] cost, Inventory.Item result) {
			this.type = type;
			this.cost = cost;
			this.result = result;
		}
		
		public boolean matches (Inventory.Item[] items) {
			if (type == 0) {
				for (int i = 0; i < 4; i++) {
					if (!itemMatch (cost [i], items [i])) {
						return false;
					}
				}
				return true;
			}
			if (type == 1) {
				for (int i = 0; i < 9; i++) {
					if (!itemMatch (cost [i], items [i])) {
						return false;
					}
				}
				return true;
			}
			if (type == 2) {
				ArrayList<Inventory.Item> used = new ArrayList<Inventory.Item> ();
				for (int i = 0; i < 9; i++) {
					if (items [i].id != 0) {
						used.add (items [i]);
					}
				}
				if (used.size () != cost.length) {
					return false;
				}
				for (int i = 0; i < cost.length; i++) {
					for (int j = 0; j < used.size (); j++) {
						if (itemMatch (cost [i], used.get (j))) {
							used.remove (j);
							j--;
						}
					}
				}
				if (used.isEmpty ()) {
					return true;
				}
				return false;
			}
			
			//Invalid recipe type
			return false;
		}
		
		public boolean craft (Inventory.Item[] items) {
			if (!matches (items)) {
				return false;
			}
			if (type == 0) {
				for (int i = 0; i < 4; i++) {
					if (cost [i].id != 0) {
						removeAmt (items, i, cost [i].amount);
					}
				}
			}
			if (type == 1) {
				for (int i = 0; i < 9; i++) {
					if (cost [i].id != 0) {
						removeAmt (items, i, cost [i].amount);
					}
				}
			}
			if (type == 2) {
				for (int i = 0; i < cost.length; i++) {
					for (int j = 0; j < 9; j++) {
						if (itemMatch (cost [i], items [j])) {
							removeAmt (items, j, cost [i].amount);
						}
					}
				}
			}
			return true;
		}
		
		public void removeAmt (Inventory.Item[] items, int slot, int amt) {
			items [slot].amount -= amt;
			if (items [slot].amount <= 0) {
				items [slot].id = 0;
			}
		}
		
		public boolean itemMatch (Inventory.Item recipeItem, Inventory.Item usedItem) {
			if (recipeItem.id == usedItem.id && usedItem.amount >= recipeItem.amount) {
				return true;
			}
			return false;
		}
		
		@Override
		public String toString () {
			String res = "";
			if (type == 0) {
				res += "Cost:\n";
				res += "[" + cost [0] + ", " + cost [1] + "]\n";
				res += "[" + cost [2] + ", " + cost [3] + "]\n\n";
				res += "Result: " + result;
			} else if (type == 1) {
				res += "Cost:\n";
				res += "[" + cost [0] + ", " + cost [1] + ", " + cost [2] + "]\n";
				res += "[" + cost [3] + ", " + cost [4] + ", " + cost [5] + "]\n";
				res += "[" + cost [6] + ", " + cost [7] + ", " + cost [8] + "]\n\n";
				res += "Result: " + result;
			} else if (type == 2) {
				res += "Cost: ";
				for (int i = 0; i < cost.length; i++) {
					if (i == cost.length - 1) {
						res += cost [i];
					} else {
						res += cost [i] + ", ";
					}
				}
				res += "\n\nResult: " + result;
			} else {
				return "invalid recipe";
			}
			return res;
		}
	}
	
}
