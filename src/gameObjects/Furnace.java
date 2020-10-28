package gameObjects;

import ui.Inventory.Item;
import world.Entity;

public class Furnace extends EntityObject {

	public Furnace (Entity e) {
		super (e);
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
