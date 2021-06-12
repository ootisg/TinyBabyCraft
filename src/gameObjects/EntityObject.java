package gameObjects;

import java.util.HashMap;

import main.GameObject;
import world.Entity;
import world.World;

public class EntityObject extends GameObject {
	
	private Entity pairedEntity;
	
	private int aiTime = 0;
	private long prevAiTime = 0;
	
	public EntityObject (double x, double y) {
		pairedEntity = new Entity ();
		pairedEntity.setPairedObject (this);
		World.addEntity (pairedEntity);
		//Set the entity's x and y properties so the game doesn't get confused
		pairedEntity.getProperties ().put ("x", String.valueOf ((int)x));
		pairedEntity.getProperties ().put ("y", String.valueOf ((int)y));
		declare (x, y);
	}
	
	public EntityObject (Entity e) {
		pairedEntity = e;
		e.setPairedObject (this);
		int xPos, yPos;
		if (pairedEntity.getProperties ().get ("x") != null && pairedEntity.getProperties ().get ("y") != null) {
			xPos = Integer.parseInt (pairedEntity.getProperties ().get ("x"));
			yPos = Integer.parseInt (pairedEntity.getProperties ().get ("y"));
		} else {
			xPos = -8;
			yPos = -8;
		}
		declare (xPos, yPos);
	}
	
	public Entity getPairedEntity () {
		return pairedEntity;
	}
	
	public void setPairedEntity (Entity e) {
		pairedEntity = e;
	}
	
	public String getProperty (String name) {
		return pairedEntity.getProperty (name);
	}
	
	public void setProperty (String name, String value) {
		pairedEntity.getProperties ().put (name, value);
	}
	
	public void setAiTime (int time) {
		aiTime = time;
	}
	
	public int getAiTime () {
		return aiTime;
	}
	
	public void aiStep () {
		//Do nothing, this is intentional
	}
	
	public void initPairedEntity (Entity e) {
		//Do nothing
	}
	
	public void setPosition (int x, int y) {
		setX (x);
		setY (y);
	}
	
	@Override
	public void frameEvent () {
		if (prevAiTime == 0 || (World.getWorldTime () - prevAiTime >= aiTime)) {
			if (World.inLoadBounds ((int)getX () / 8)) {
				prevAiTime = World.getWorldTime ();
				aiStep ();
			}
		}
	}
	
	@Override
	public void setX (double x) {
		super.setX (x);
		pairedEntity.getProperties ().put ("x", String.valueOf ((int)x));
		World.updateReigon (this.getPairedEntity ());
	}
	
	@Override
	public void setY (double y) {
		super.setY (y);
		pairedEntity.getProperties ().put ("y", String.valueOf ((int)y));
		World.updateReigon (this.getPairedEntity ());
	}

}
