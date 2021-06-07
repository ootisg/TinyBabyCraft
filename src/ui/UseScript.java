package ui;

import world.World;

public abstract class UseScript {
	
	public UseScript () {
		
	}
	
	public abstract boolean doUse (int id, int x, int y);
	
	public static class DoorInteract extends UseScript {

		@Override
		public boolean doUse (int id, int x, int y) {
			switch (id) {
				case 60:
					World.setTile (76, x, y);
					World.setTile (77, x, y + 1);
					break;
				case 61:
					World.setTile (76, x, y - 1);
					World.setTile (77, x, y);
					break;
				case 62:
					World.setTile (78, x, y);
					World.setTile (79, x, y + 1);
					break;
				case 63:
					World.setTile (78, x, y - 1);
					World.setTile (79, x, y);
					break;
				case 76:
					World.setTile (60, x, y);
					World.setTile (61, x, y + 1);
					break;
				case 77:
					World.setTile (60, x, y - 1);
					World.setTile (61, x, y);
					break;
				case 78:
					World.setTile (62, x, y);
					World.setTile (63, x, y + 1);
					break;
				case 79:
					World.setTile (62, x, y - 1);
					World.setTile (53, x, y);
					break;
				default:
					break;
			}
			return false;
		}
		
	}

}
