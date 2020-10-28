package json;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Scanner;

public class JSONUtil {
	
	/**
	 * Gets the value of the given JSON literal
	 * @param jsonString the literal to use
	 * @return the value of the given literal, excluding JSONObject/JSONArray. Possible return types are String, Double, Int, and Boolean
	 */
	public static Object getValueOfJSONLiteral (String jsonString) {
		if (jsonString.charAt (0) == '\"' && jsonString.charAt (jsonString.length () - 1) == '\"') {
			return jsonString.substring (1, jsonString.length () - 1);
		} else if (jsonString.charAt (jsonString.length () - 1) == 'd') {
			return Double.parseDouble (jsonString.substring (0, jsonString.length () - 1));
		} else if (jsonString.contains (".")) {
			return Double.parseDouble (jsonString);
		} else {
			Integer intVal;
			Boolean boolVal;
			try {
				intVal = Integer.parseInt (jsonString);
				return intVal;
			} catch (NumberFormatException e) {
				try {
					boolVal = Boolean.parseBoolean (jsonString);
					return boolVal;
				} catch (NumberFormatException e2) {
					return null;
				}
			}
		}
	}
	
	public static JSONObject loadJSONFile (String filepath) throws JSONException {
		String fileString = "";
		File f = new File (filepath);
		Scanner s;
		try {
			s = new Scanner (f);
			while (s.hasNextLine ()) {
				fileString += s.nextLine () + "\n";
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new JSONObject (fileString);
	}
	
}