package ui;

import json.JSONObject;
import main.GameAPI;
import world.World;

public abstract class PlaceScript {
	
	public PlaceScript () {
		
	}

	public abstract boolean doPlace (int id, int x, int y);
	
	public static class Stair extends PlaceScript {

		@Override
		public boolean doPlace(int id, int x, int y) {
			int localX = GameAPI.getCursorX () % 8;
			int localY = GameAPI.getCursorY () % 8;
			if (localX < 4) {
				if (localY < 4) {
					id += 3; //Top Left
				} else {
					id += 1; //Bottom Left
				}
			} else {
				if (localY < 4) {
					id += 2; //Top Right
				} else {
					//Bottom Right
				}
			}
			World.setTile (id, x, y);
			return true;
		}
		
	}
	
	public static class Slab extends PlaceScript {

		@Override
		public boolean doPlace(int id, int x, int y) {
			int localY = GameAPI.getCursorY () % 8;
			if (localY < 4) {
				id += 1; //Top slab
			}
			World.setTile (id, x, y);
			return true;
		}
		
	}
	
	public static class Door extends PlaceScript {

		@Override
		public boolean doPlace(int id, int x, int y) {
			if (World.getPlayer ().facingLeft ()) {
				id += 2;
			}
			boolean below1Solid = World.isSolid (World.getTile (x, y + 1));
			boolean below2Solid = World.isSolid (World.getTile (x, y + 2));
			if (below1Solid) {
				y--; //Door top is on ground, move it up
			}
			if (below1Solid || below2Solid) {
				if (World.getTile (x, y) == 0 && World.getTile (x, y + 1) == 0) {
					World.setTile(id, x, y);
					World.setTile(id + 1, x, y + 1);
					return true;
				}
			}
			return false;
		}
		
	}
	
}