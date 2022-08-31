package scripts;

import java.util.Random;

import world.Entity;
import world.World;

public class FortressScript implements StructureScript {

	@Override
	public void run (Entity et) {
		Random r = new Random ();
		int startX = Integer.parseInt (et.getProperty ("x"));
		int startY = Integer.parseInt (et.getProperty ("y"));
		System.out.println (startX);
		System.out.println (startY);
		int wx = startX;
		int unitWidth = 7 * 8;
		World.putStructure ("fortress_end", wx, startY);
		wx += unitWidth;
		int numHalls = r.nextInt (7) + 5;
		for (int i = 0; i < numHalls; i++) {
			if (r.nextInt (12) == 0) {
				World.putStructure ("fortress_chest", wx, startY);
				spawnPillar (wx / 8 + 4, startY / 8 + 7); //Pillar in the middle
			} else {
				World.putStructure ("fortress_hallway", wx, startY);
				spawnPillar (wx / 8 + 4, startY / 8 + 7); //Pillar in the middle
			}
			wx += unitWidth;
		}
		World.putStructure ("fortress_entrance", wx, startY);
		spawnPillar (wx / 8 + 4, startY / 8 + 7); //Pillar in the middle
		wx += unitWidth;
		int numWalks = r.nextInt (5) + 12;
		for (int i = 0; i < numWalks; i++) {
			if (r.nextInt (10) == 0) {
				World.putStructure ("fortress_alcove", wx, startY);
				spawnPillar (wx / 8 + 4, startY / 8 + 7); //Pillar in the middle
			} else {
				World.putStructure ("fortress_walkway", wx, startY);
				spawnPillar (wx / 8 + 4, startY / 8 + 7); //Pillar in the middle
			}
			wx += unitWidth;
		}
		World.putStructure ("fortress_spawner", wx, startY);
	}
	
	private void spawnColumn (int x, int y) {
		int wy = y;
		while (World.getTile (x, wy, 0) == 0) {
			World.placeTile (97, x, wy, 0);
			World.placeTile (97, x, wy, 1);
			wy++;
		}
	}
	
	private void spawnPillar (int x, int y) {
		spawnColumn (x - 1, y);
		spawnColumn (x, y);
		spawnColumn (x + 1, y);
	}
	
}
