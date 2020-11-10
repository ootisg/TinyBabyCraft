package world;

import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;

import gameObjects.EntityObject;
import json.JSONException;
import json.JSONObject;
import json.JSONUtil;
import main.GameObject;

public class Entity implements Comparable<Entity> {

	private UUID uuid;
	private int reigonId;
	private boolean active;
	
	private HashMap<String, String> properties;
	
	private String entityType;
	private EntityObject obj;
	
	private static JSONObject typeProperties;
	
	public Entity (String properties) {
		this.properties = new HashMap<String, String> ();
		
		//Get attribute-value pairs
		String mapString = properties.substring (1, properties.length () - 1);
		String[] vals = mapString.split (", ");
		
		//Parse the pairs
		for (int i = 0; i < vals.length; i++) {
			String[] parsed = vals [i].split ("=");
			this.properties.put (parsed [0], parsed [1]);
		}
		
		//Set the UUID field
		String uuidStr = this.properties.get ("UUID");
		uuid = UUID.fromString (uuidStr);
		
		//Set the type field
		String eType = this.properties.get ("type");
		if (eType != null) {
			entityType = eType;
		}
	}
	
	public Entity (HashMap<String, String> properties) {
		
		//Set properties
		this.properties = properties;
		
		//Set the UUID field
		String uuidStr = this.properties.get ("UUID");
		uuid = UUID.fromString (uuidStr);
		
		//Set the type field
		String eType = this.properties.get ("type");
		if (eType != null) {
			entityType = eType;
		}
	}
	
	public Entity () {
		properties = getEntityMap ();
	}
	
	public UUID getUUID () {
		return uuid;
	}
	
	public int getReigonId () {
		return World.getReigonId ((int)obj.getX ());
	}
	
	public boolean isActive () {
		return active; //TODO yeah some stuff
	}
	
	public HashMap<String, String> getProperties () {
		return properties;
	}
	
	public String getProperty (String propertyName) {
		return properties.get (propertyName);
	}
	
	public int getInt (String propertyName) {
		return Integer.parseInt (properties.get (propertyName));
	}
	
	public JSONObject getTypeProperties () {
		return typeProperties.getJSONObject (getType ());
	}
	
	public GameObject getObject () {
		return obj;
	}
	
	public String getType () {
		return entityType;
	}
	
	public void setPairedObject (EntityObject obj) {
		this.obj = obj;
		entityType = obj.getClass ().getSimpleName ();
		properties.put ("type", entityType);
	}
	
	public void setType (String type) {
		entityType = type;
	}

	public static HashMap<String, String> getEntityMap () {
		HashMap<String, String> newMap = new HashMap<String, String> ();
		newMap.put ("UUID", UUID.randomUUID ().toString ());
		return newMap;
	}
	
	public static void initTypeProperties () {
		try {
			typeProperties = JSONUtil.loadJSONFile ("resources/gamedata/entities/properties.json");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public int compareTo (Entity entity) {
		return (int)(obj.getX () - ((Entity)entity).getObject ().getX ());
	}
	
	@Override
	public String toString () {
		return properties.toString ();
	}
	
}