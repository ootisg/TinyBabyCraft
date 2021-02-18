package world;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import json.JSONArray;
import json.JSONObject;

public class Structure {
	
	int[][] tiles;
	JSONObject properties;
	
	public Structure () {
		
	}
	
	public void setTiles (int[][] tiles) {
		this.tiles = tiles;
	}
	
	public void setTiles (JSONArray tileArray) {
		
		//Make the tiles array
		tiles = new int[getWidth ()][getHeight ()];
		
		//Fill with the required tiles
		ArrayList<Object> rows = tileArray.getContents ();
		for (int row = 0; row < rows.size (); row++) {
			ArrayList<Object> cols = ((JSONArray) rows.get (row)).getContents ();
			for (int col = 0; col < cols.size (); col++) {
				Integer tile = (Integer)cols.get (col);
				tiles [col][row] = tile;
			}
		}
	}
	
	public void setProperties (JSONObject properties) {
		this.properties = properties;
		setTiles (properties.getJSONObject ("tiles").getJSONArray ("base"));
	}
	
	public JSONObject getProperties () {
		return properties;
	}
	
	public int[][] getTiles () {
		return tiles;
	}
	
	public Object getMetaProperty (String s) {
		return getProperties ().getJSONObject ("meta").get (s);
	}
	
	public int getWidth () {
		return (int) getMetaProperty ("width");
	}
	
	public int getHeight () {
		return (int) getMetaProperty ("height");
	}
	
	public Point getOrigin () {
		JSONArray coords = ((JSONArray)getMetaProperty ("center"));
		return new Point ((int)coords.get (0), (int)coords.get (1));
	}
	
	public String getName () {
		if (getMetaProperty ("name") != null) {
			return ((String)getMetaProperty ("name"));
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
