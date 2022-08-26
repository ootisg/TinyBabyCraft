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
			} else {
				World.putStructure ("fortress_hallway", wx, startY);
			}
			wx += unitWidth;
		}
		World.putStructure ("fortress_entrance", wx, startY);
		wx += unitWidth;
		int numWalks = r.nextInt (5) + 12;
		for (int i = 0; i < numWalks; i++) {
			if (r.nextInt (10) == 0) {
				World.putStructure ("fortress_alcove", wx, startY);
			} else {
				World.putStructure ("fortress_walkway", wx, startY);
			}
			wx += unitWidth;
		}
		World.putStructure ("fortress_spawner", wx, startY);
	}
	
}
