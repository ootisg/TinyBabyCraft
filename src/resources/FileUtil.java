package resources;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class FileUtil {

	public static double[] populateFromFile (String filename) {
		File f = new File (filename);
		try {
			Scanner s = new Scanner (f);
			
			//Check if right file type
			if (!s.nextLine ().equals ("ARRAY")) {
				return null;
			}
			
			//Get the array length
			String len = s.nextLine ();
			String[] lenArgs = len.split (":");
			if (lenArgs.length != 2 || !lenArgs[0].equals ("size")) {
				return null;
			}
			int lenInt = Integer.parseInt (lenArgs [1]);
			
			//Get the default element
			String def = s.nextLine ();
			String[] defArgs = def.split (":");
			if (defArgs.length != 2 || !defArgs[0].equals ("default")) {
				return null;
			}
			double defVal = Double.parseDouble (defArgs [1]);
			
			//Setup the array
			double[] arr = new double[lenInt];
			for (int i = 0; i < arr.length; i++) {
				arr [i] = defVal;
			}
			
			//Populate the array
			while (s.hasNextLine ()) {
				String working = s.nextLine ();
				String[] split = working.split (":");
				int index = Integer.parseInt (split[0]);
				
				//Populate
				if (split [1].contains (",")) {
					//Populate run
					String[] fillVals = split [1].split (",");
					for (int i = 0; i < fillVals.length; i++) {
						arr [index + i] = Double.parseDouble (fillVals [i]);
					}
				} else {
					//Populate single value
					arr [index] = Double.parseDouble (split [1]);
				}
			}
			
			return arr;
			
		} catch (FileNotFoundException e) {
			return null;
		}
	}
	
}
