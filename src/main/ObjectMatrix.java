package main;

import java.util.ArrayList;

public class ObjectMatrix {
	public ArrayList<ArrayList<GameObject>> objectMatrix;
	public ArrayList<String> classNameList;
	public Class testClass;
	public ObjectMatrix () {
		objectMatrix = new ArrayList<ArrayList<GameObject>> ();
		classNameList = new ArrayList<String> ();
	}
	public int[] add (GameObject gameObject) {
		//Adds an object to the object matrix and returns its position as [x, y]
		boolean typeDeclared = false;
		int[] coords = new int[2];
		for (int i = 0; i < classNameList.size (); i ++) {
			if (gameObject.getClass ().getName ().equals (classNameList.get (i))) {
				coords [0] = i;
				if (objectMatrix.get (i).indexOf (null) != -1) {
					coords [1] = objectMatrix.get (i).indexOf (null);
					objectMatrix.get (i).set (coords [1], gameObject);
				} else {
					coords [1] = objectMatrix.get (i).size ();
					objectMatrix.get (i).add (gameObject);
				}
				typeDeclared = true;
			}
		}
		if (!typeDeclared) {
			coords [0] = classNameList.size ();
			coords [1] = 0;
			classNameList.add (gameObject.getClass ().getName ());
			objectMatrix.add (new ArrayList<GameObject> ());
			objectMatrix.get (objectMatrix.size () - 1).add (gameObject);
		}
		return coords;
	}
	public void remove (int[] coords) {
		//Removes an object from the object matrix with the coordinates [x, y]
		objectMatrix.get (coords [0]).set (coords [1], null);
	}
	public void callAll () {
		//Calls the draw method and the frameEvent method
		int objectArrayLength1 = objectMatrix.size ();
		int objectArrayLength2;
		for (int i = 0; i < objectArrayLength1; i ++) {
			ArrayList<GameObject> objectArray = objectMatrix.get (i);
			objectArrayLength2 = objectArray.size ();
			for (int c = 0; c < objectArrayLength2; c ++) {
				if (objectArray.get (c) != null) {
					objectArray.get (c).frameEvent ();
					if (objectArray.get (c) != null) {
						objectArray.get (c).draw ();
					}
				}
			}
		}
	}
	public GameObject get (int x, int y) {
		//Gets the GameObject with an index of x, y
		return objectMatrix.get (x).get (y);
	}
	public int getTypeId (String objectName) {
		//Gets the x-coordinate on the object matrix of the name objectName
		for (int i = 0; i < classNameList.size (); i ++) {
			if (classNameList.get (i).equals (objectName)) {
				return i;
			}
		}
		return -1;
	}
	public String getStringId (int objectId) {
		//Gets the string id associated with the x-coordinate objectId on the object matrix
		return classNameList.get (objectId);
	}
}