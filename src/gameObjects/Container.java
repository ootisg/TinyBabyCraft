package gameObjects;

import ui.Inventory.Item;
import world.Entity;

public abstract class Container extends EntityObject {
	
	private int capacity = 0;
	
	public Container (Entity e) {
		super (e);
	}
	
	public Item getItem (int slot) {
		String slotVal = "s" + slot;
		String itemVal = getPairedEntity ().getProperties ().get (slotVal);
		if (itemVal == null) {
			return new Item (0, 0);
		} else {
			String[] itemSplit = itemVal.split ("x");
			if (itemSplit.length != 2) {
				//Formatting is invalid
				return new Item (0, 0);
			}
			//Make the item
			int itemId = Integer.parseInt (itemSplit [0]);
			int itemCount = Integer.parseInt (itemSplit [1]);
			return new Item (itemId, itemCount);
		}
	}
	
	public void setItem (int slot, Item it) {
		String slotVal = "s" + slot;
		String itemStr = it.id + "x" + it.amount;
		System.out.println(itemStr);
		getPairedEntity ().getProperties ().put (slotVal, itemStr);
	}
	
	public void setCapacity (int capacity) {
		this.capacity = capacity;
	}
	
	public int getCapacity () {
		return capacity;
	}
	
	public boolean isCompatable (int slot, Item it) {
		return true;
	}

}
