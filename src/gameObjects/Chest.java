package gameObjects;

import world.Entity;

public class Chest extends Container {

	public Chest (Entity e) {
		super (e);
		setCapacity (15);
		setAiTime (1);
	}

}
