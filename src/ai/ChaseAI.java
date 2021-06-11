package ai;

import java.awt.Point;
import java.util.ArrayList;
import java.util.LinkedList;

import gameObjects.EntityObject;
import world.World;

public class ChaseAI extends PathfindAI {

	public static final Vec2D[] standardMoveset = new Vec2D[]
	{
		new Vec2D (-1, -1),
		new Vec2D (-1, 0),
		new Vec2D (-1, 1),
		new Vec2D (-1, 2),
		new Vec2D (1, -1),
		new Vec2D (1, 0),
		new Vec2D (1, 1),
		new Vec2D (1, 2)
	}; //Standard moveset
	
	public ChaseAI (EntityObject pairedObject) {
		super (pairedObject);
		setMoveset (standardMoveset);
	}

	@Override
	public boolean isValidStep (Point pt) {
		return !World.isSolid (World.getTile (pt.x, pt.y)) && !World.isSolid (World.getTile (pt.x, pt.y - 1)) && World.isSolid (World.getTile (pt.x, pt.y + 1));
	}

	@Override
	public void selectTarget () {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void aiStep () {
		setTarget (new Point ((int)(World.getPlayer ().getX () / 8), (int)(World.getPlayer ().getY () / 8)));
		LinkedList<Point> path = getPath ();
		if (path != null && !path.isEmpty ()) {
			Point first = path.getFirst ();
			EntityObject obj = getPairedObject ();
			obj.setX (first.x * 8);
			obj.setY (first.y * 8);
		}
	}

}
