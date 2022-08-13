package ui;

import json.JSONObject;
import main.GameAPI;
import world.World;

public abstract class PlaceScript {
	
	public PlaceScript () {
		
	}

	public abstract boolean doPlace (int id, int x, int y, int layer);
	
	public static class Stair extends PlaceScript {

		@Override
		public boolean doPlace(int id, int x, int y, int layer) {
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
			World.setTile (id, x, y, layer);
			return true;
		}
		
	}
	
	public static class Slab extends PlaceScript {

		@Override
		public boolean doPlace(int id, int x, int y, int layer) {
			int localY = GameAPI.getCursorY () % 8;
			if (localY < 4) {
				id += 1; //Top slab
			}
			World.setTile (id, x, y, layer);
			return true;
		}
		
	}
	
	public static class Door extends PlaceScript {

		@Override
		public boolean doPlace(int id, int x, int y, int layer) {
			if (World.getPlayer ().facingLeft ()) {
				id += 2;
			}
			boolean below1Solid = World.isSolid (World.getTile (x, y + 1, layer));
			boolean below2Solid = World.isSolid (World.getTile (x, y + 2, layer));
			if (below1Solid) {
				y--; //Door top is on ground, move it up
			}
			if (below1Solid || below2Solid) {
				if (World.getTile (x, y, layer) == 0 && World.getTile (x, y + 1, layer) == 0) {
					World.setTile(id, x, y, layer);
					World.setTile(id + 1, x, y + 1, layer);
					return true;
				}
			}
			return false;
		}
		
	}
	
	public static class Sapling extends PlaceScript {

		@Override
		public boolean doPlace(int id, int x, int y, int layer) {
			int soilId = World.getTile (x, y + 1, layer);
			if (soilId == 1 || soilId == 2) {
				World.setTile (80, x, y, layer);
				return true;
			} else {
				return false;
			}
		}
		
	}
	
}