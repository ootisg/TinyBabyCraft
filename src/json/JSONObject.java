package json;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

public class JSONObject {
	
	HashMap<String, Object> values;
	
	/**
	 * Constructs a JSONObject from the given String
	 * @param value the JSON String representing the JSONObject
	 * @throws JSONException if the given JSON String is invalid
	 */
	public JSONObject (String value) throws JSONException {
		//Parse the input text to remove whitespace
		values = new HashMap<String, Object> ();
		String noWhitespace = "";
		boolean remove = true;
		long ct = System.nanoTime ();
		for (int i = 0; i < value.length(); i ++) {
			if (value.charAt(i) == '"') {
				remove = !remove;
			}
			if (!remove || !Character.isWhitespace(value.charAt(i))) {
				noWhitespace = noWhitespace + value.charAt(i);
			}
		}
		if (noWhitespace.charAt (0) == '{' && noWhitespace.charAt (noWhitespace.length () - 1) == '}') {
			noWhitespace = noWhitespace.substring (1, noWhitespace.length () - 1);
		} else {
			throw new JSONException ("Input String is not a valid JSON Object");
		}
		
		//Main parsing loop
		int i = 0;
		while (i < noWhitespace.length ()) {
			String key = "";
			String working = "";
			
			//Parse out key
			while (noWhitespace.charAt (i) != ':') {
				key += noWhitespace.charAt (i++);
			}
			if (key.charAt (0) == '\"' && key.charAt (key.length () - 1) == '\"') {
				key = key.substring (1, key.length () - 1);
			} else {
				throw new JSONException ("Invalid JSON formatting");
			}
			i++;
			
			//Parse out value
			if (noWhitespace.charAt (i) == '{') {
				//Value is a JSONObject
				working = "{";
				i++;
				int bracketCount = 1;
				while (true) {
					if (noWhitespace.charAt (i) == '{') {
						bracketCount++;
					} else if (noWhitespace.charAt (i) == '}') {
						if (bracketCount == 1) {
							//End of bracket
							working += '}';
							break;
						} else {
							bracketCount--;
						}
					}
					working += noWhitespace.charAt (i);
					i++;
					if (i >= noWhitespace.length ()) {
						throw new JSONException ("Missing } when parsing token " + value);
					}
				}
				values.put (key, new JSONObject (working));
			} else if (noWhitespace.charAt (i) == '[') {
				//Value is a JSONArray
				working = "[";
				i++;
				int bracketCount = 1;
				while (true) {
					if (noWhitespace.charAt (i) == '[') {
						bracketCount++;
					} else if (noWhitespace.charAt (i) == ']') {
						if (bracketCount == 1) {
							//End of bracket
							working += ']';
							break;
						} else {
							bracketCount--;
						}
					}
					working += noWhitespace.charAt (i);
					i++;
					if (i >= noWhitespace.length ()) {
						throw new JSONException ("Missing ] when parsing token " + value);
					}
				}
				values.put (key, new JSONArray (working));
			} else {
				//Value is a JSON literal
				working = "";
				while (i < noWhitespace.length () && noWhitespace.charAt (i) != ',') {
					working += noWhitespace.charAt (i++);
				}
				values.put (key, JSONUtil.getValueOfJSONLiteral (working));
			}
			i++;
			if (i < noWhitespace.length () && noWhitespace.charAt (i) == ',') {
				i++;
			}
		}
	}
	
	/**
	 * Returns the Object associated with the given key
	 * @param key the key to use
	 * @return the associated Object
	 */
	public Object get (String key) {
		return values.get (key);
	}
	
	/**
	 * Returns the JSONObject associated with the given key
	 * @param key the key to use
	 * @return the associated JSONObject
	 */
	public JSONObject getJSONObject (String key) {
		return (JSONObject)values.get (key);
	}
	
	/**
	 * Returns the JSONObject associated with the given key
	 * @param key the key to use
	 * @return the associated JSONObject
	 */
	public JSONArray getJSONArray (String key) {
		return (JSONArray)values.get (key);
	}
	
	/**
	 * Returns the String associated with the given key
	 * @param key the key to use
	 * @return the associated String
	 */
	public String getString (String key) {
		return (String)values.get (key);
	}
	
	/**
	 * Returns the integer associated with the given key
	 * @param key the key to use
	 * @return the associated int
	 */
	public int getInt (String key) {
		Object val = values.get (key);
		if (val instanceof String) {
			return Integer.parseInt((String) values.get (key));
		} else {
			return (Integer)val;
		}
	}
	
	/**
	 * Returns the double associated with the given key
	 * @param key the key to use
	 * @return the associated double
	 */
	public double getDouble (String key) {
		Object val = values.get (key);
		if (val instanceof String) {
			return Double.parseDouble((String) values.get (key));
		} else {
			return (Double)val;
		}
	}
	
	@Override
	public String toString () {
		String working = "{";
		Set<Entry<String, Object>> entries = values.entrySet ();
		Iterator<Entry<String, Object>> iter = entries.iterator ();
		while (iter.hasNext ()) {
			Entry<String, Object> entry = iter.next ();
			working += entry.getKey () + ":";
			Object o = entry.getValue ();
			if (o instanceof String) {
				working += '\"' + (String)o + '\"';
			} else {
				working += o.toString ();
			}
			if (iter.hasNext ()) {
				working += ",";
			}
		}
		working += "}";
		return working;
	}
	
}