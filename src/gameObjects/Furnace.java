package gameObjects;

import ui.Inventory;
import ui.Inventory.Item;
import world.Entity;

public class Furnace extends Container {

	public Furnace (Entity e) {
		super (e);
		setCapacity (3);
		setAiTime (1);
	}
	
	public boolean placeItem (int slot, Item item) {
		setItem (slot, item);
		return false;
	}
	
	@Override
	public void aiStep () {
		//Furnace 'AI' is to cook items
	}

}
