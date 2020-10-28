package world;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class Structure {
	
	int[][] tiles;
	HashMap<String, String> properties;
	
	public Structure () {
		
	}
	
	public void setTiles (int[][] tiles) {
		this.tiles = tiles;
	}
	
	public void setTiles (ArrayList<String> rowStrings) {
		tiles = new int[getWidth ()][getHeight ()];
		int wx = 0;
		for (int wy = 0; wy < rowStrings.size (); wy++) {
			Scanner tileScanner = new Scanner (rowStrings.get (wy));
			while (tileScanner.hasNextInt ()) {
				tiles [wx][wy] = tileScanner.nextInt ();
				wx++;
			}
			wx = 0;
		}
	}
	
	public void setProperties (HashMap<String, String> properties) {
		this.properties = properties;
	}
	
	public void setProperties (ArrayList<String> propertyStrings) {
		properties = new HashMap<String, String> ();
		for (int i = 0; i < propertyStrings.size (); i++) {
			String[] working = propertyStrings.get (i).split (":");
			properties.put (working [0], working [1]);
		}
	}
	
	public int[][] getTiles () {
		return tiles;
	}
	
	public HashMap<String, String> getProperties () {
		return properties;
	}
	
	public String getProperty (String s) {
		return properties.get (s);
	}
	
	public int getWidth () {
		return Integer.parseInt (properties.get ("width"));
	}
	
	public int getHeight () {
		return Integer.parseInt (properties.get ("height"));
	}
	
	public Point getOrigin () {
		String[] coords = properties.get ("center").split (",");
		return new Point (Integer.parseInt (coords [0]), Integer.parseInt (coords [1]));
	}
	
	public String getName () {
		if (properties.get ("name") != null) {
			return properties.get ("name");
		} else {
			return "undefined";
		}
	}
	
	public int[] getSlice (int x) {
		Point start = getOrigin ();
		int sliceX = x + start.x;
		return tiles [sliceX];
	}
	
}
