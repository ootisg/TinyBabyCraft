package ui;

import gameObjects.Player;
import gameObjects.Zombie;
import world.World;

public abstract class UseItemScript {

	public UseItemScript () {
		
	}
	
	public abstract boolean doUse (int id, int x, int y);
	
	public static class Seeds extends UseItemScript {

		@Override
		public boolean doUse(int id, int x, int y) {
			int curr = World.getTile (x, y);
			int below = World.getTile (x, y + 1);
			if (curr == 0 && (below == 1 || below == 2)) {
				World.setTile (81, x, y);
				return true;
			}
			return false;
		}
		
	}
	
	public static class Apple extends UseItemScript {

		@Override
		public boolean doUse(int id, int x, int y) {
			Player player = World.getPlayer ();
			if (player.getHealth () == 100) {
				return false;
			}
			player.heal (20);
			return true;
		}
		
	}
	
	public static class GoldenApple extends UseItemScript {

		@Override
		public boolean doUse(int id, int x, int y) {
			Player player = World.getPlayer ();
			if (player.getHealth () == 100) {
				return false;
			}
			player.heal (100);
			return true;
		}
		
	}
	
	public static class Bread extends UseItemScript {

		@Override
		public boolean doUse(int id, int x, int y) {
			Player player = World.getPlayer ();
			if (player.getHealth () == 100) {
				return false;
			}
			player.heal (30);
			return true;
		}
		
	}
	
	public static class Hoe extends UseItemScript {

		@Override
		public boolean doUse(int id, int x, int y) {
			if (World.getTile (x, y) == 1) {
				World.setTile (2, x, y); //Replace with dirt
				if (Math.random () < .33) {
					World.getPlayer ().addToInventory (338, 1);
				}
				return false;
			}
			return false;
		}
		
	}
	
	public static class ZombieEgg extends UseItemScript {
		
		@Override
		public boolean doUse (int id, int x, int y) {
			new Zombie (x * 8, y * 8);
			return true;
		}
		
	}
	
}
