package gameObjects;

import ui.Inventory;
import ui.Inventory.Item;
import world.Entity;

public class Furnace extends Container {

	public Furnace (Entity e) {
		super (e);
		Inventory.setContainer (this);
		setAiTime (1);
	}
	
	public boolean placeItem (int slot, Item item) {
		return false;
	}
	
	@Override
	public void aiStep () {
		//Furnace 'AI' is to cook items
	}

}
