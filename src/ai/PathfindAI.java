package ai;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import gameObjects.EntityObject;
import world.World;

public abstract class PathfindAI extends MobAI {

	private Vec2D[] moveset;
	private int searchDist = 20;
	
	protected PathfindAI (EntityObject pairedObject) {
		super (pairedObject);
	}
	
	protected void setMoveset (Vec2D[] moveset) {
		this.moveset = moveset;
	}
	
	public void setSearchDistance (int searchDist) {
		this.searchDist = searchDist;
	}
	
	public Vec2D[] getPtsToCheck () {
		return moveset;
	}
	
	public int getSearchDistance () {
		return searchDist;
	}
	
	public LinkedList<Point> getPath () {
		
		//Setup stuff for the pathfinding
		LinkedList<Point> checkPts = new LinkedList<Point> (); //Queue for BFS
		HashMap<Point, Integer> distTable = new HashMap<Point, Integer> ();
		HashMap<Point, Point> backsteps = new HashMap<Point, Point> ();
		
		//Initialize BFS
		EntityObject pairedObj = getPairedObject ();
		Point start = new Point ((int)(pairedObj.getX () / 8), (int)(pairedObj.getY () / 8));
		checkPts.add (start);
		distTable.put (checkPts.getFirst (), 0);
		
		//Run BFS
		while (!checkPts.isEmpty ()) {
			Point initialPt = checkPts.getFirst ();
			int initialDist = distTable.get (initialPt);
			for (int i = 0; i < moveset.length; i++) {
				Point movePt = moveset [i].translate (initialPt);
				if (movePt.equals (getTarget ())) {
					//Trace back the path taken
					LinkedList<Point> path = new LinkedList<Point> ();
					path.add (movePt);
					Point currentPt = initialPt;
					do {
						path.addFirst (currentPt);
						currentPt = backsteps.get (currentPt);
					} while (currentPt != null && !currentPt.equals (start));
					return path;
				}
				if (!distTable.containsKey (movePt) && isValidStep (movePt) && initialDist <= searchDist - 1) {
					checkPts.addLast (movePt);
					distTable.put (movePt, distTable.get (initialPt) + 1);
					backsteps.put (movePt, initialPt);
					//World.markTile (movePt.x, movePt.y, initialDist + 1); For debugging
				}
			}
			checkPts.removeFirst ();
		}
		
		return null;
	}
	
	public abstract boolean isValidStep (Point pt);

}
