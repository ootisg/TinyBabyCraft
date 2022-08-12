package gameObjects;

import world.Entity;
import world.World;

public class StructSpawner extends EntityObject {

	public StructSpawner (Entity e) {
		super (e);
		if (World.inLoadBounds (e.getInt ("x") / 8)) {
			//spawnStructure (); //This was causing a bug
		}
		// TODO Auto-generated constructor stub
	}
	
	public void spawnStructure () {
		Entity entity = getPairedEntity ();
		int x = entity.getInt ("x");
		int y = entity.getInt ("y");
		String structType = entity.getProperty ("structName");
		World.spawnStructure (structType, x / 8, y / 8);
		World.removeEntity (entity);
	}

}
