package gameObjects;

import json.JSONObject;
import ui.Inventory;
import ui.Inventory.Item;
import world.Entity;
import world.World;

public class Furnace extends Container {

	public static final int SMELT_TIME = 150;
	
	public Furnace (Entity e) {
		super (e);
		setCapacity (3);
		setAiTime (1);
	}
	
	public boolean placeItem (int slot, Item item) {
		setItem (slot, item);
		return false;
	}
	
	public int getFuel () {
		return getPairedEntity ().getInt ("fuel");
	}
	
	public int getTime () {
		return getPairedEntity ().getInt ("time");
	}
	
	public boolean canSmelt () {
		Item toSmelt = getItem (0);
		JSONObject smeltProps = Inventory.itemProperties.getJSONObject (String.valueOf (toSmelt.id));
		String smeltResult = smeltProps.getString ("smelt");
		if (smeltResult != null) {
			int smeltId = 0;
			int smeltAmt = 1; //TODO allow for modifications
			
			//Parse out smelting recipe
			try {
				smeltId = Integer.parseInt (smeltResult);
			} catch (NumberFormatException e) {
				//Put code here to handle drop tables
			}
			
			//Check for smeltability
			Item smeltOut = getItem (2);
			if (smeltOut.id == 0 || (smeltId == smeltOut.id && smeltOut.amount <= 63)) {
				return true;
			}
		}
		return false;
	}
	
	public void doSmelt () {
		Item toSmelt = getItem (0);
		JSONObject smeltProps = Inventory.itemProperties.getJSONObject (String.valueOf (toSmelt.id));
		String smeltResult = smeltProps.getString ("smelt");
		System.out.println(smeltResult);
		if (smeltResult != null) {
			int smeltId = 0;
			int smeltAmt = 1; //TODO allow for modifications
			
			//Parse out smelting recipe
			try {
				smeltId = Integer.parseInt (smeltResult);
			} catch (NumberFormatException e) {
				//Put code here to handle drop tables
			}
			
			//Check for smeltability
			Item smeltOut = getItem (2);
			if (canSmelt ()) {
				//Smelt the item
				Item fromItem = getItem (0);
				
				//Remove from ingredient slot
				if (fromItem.amount <= 1) {
					setItem (0, new Item (0, 0));
				} else {
					setItem (0, new Item (fromItem.id, fromItem.amount - 1));
				}
				
				//Add to result slot
				setItem (2, new Item (smeltId, smeltOut.amount + 1));
			}
		}
	}
	
	public void doRefuel () {
		
		Item it = getItem (1);
		
		if (it.amount != 0) {
			
			//Extract fuel time
			JSONObject props = Inventory.itemProperties.getJSONObject (String.valueOf (it.id));
			int fuelTime = props.getInt ("fuelTime");
			
			//Set the fuel accordingly
			getPairedEntity ().getProperties ().put ("fuel", String.valueOf (fuelTime));
			
			//Deplete fuel slot
			if (it.amount <= 1) {
				setItem (1, new Item (0, 0));
			} else {
				setItem (1, new Item (it.id, it.amount - 1));
			}
			
		}
	}
	
	@Override
	public void aiStep () {
		int fuel = getFuel ();
		int time = getTime ();
		if (!canSmelt ()) {
			//Reset time when not smelting
			time = 0;
		}
		
		if (fuel > 0) {
			//Trade fuel for smelt time
			time++;
		}
		
		if (time >= SMELT_TIME) {
			//Do smelting if smelt time is sufficient
			time = 0;
			doSmelt ();
		}
		
		if (fuel > 0) {
			//Deplete fuel
			fuel--;
		} else {
			if (getItem (1).id != 0) {
				doRefuel ();
				fuel = getFuel ();
			}
		}
		
		if (fuel == 0) {
			World.setTile (26, (int)getX () / 8, (int)getY () / 8);
			time = 0;
		} else {
			World.setTile (27, (int)getX () / 8, (int)getY () / 8);
		}
		
		getPairedEntity ().getProperties ().put ("fuel", String.valueOf (fuel));
		getPairedEntity ().getProperties ().put ("time", String.valueOf (time));
	}
	
	@Override
	public boolean isCompatable (int slot, Item it) {
		if (slot == 1) {
			JSONObject itemProps = Inventory.itemProperties.getJSONObject (String.valueOf (it.id));
			Integer fuelTime = (Integer)itemProps.get ("fuelTime");
			if (fuelTime == null || fuelTime == 0) {
				return false;
			}
		}
		return true;
	}

}
