package main;

import java.awt.Toolkit;

import resources.SpriteContainer;

public class MainLoop {
	//Main class
	private static GameWindow gameWindow;
	private static SpriteContainer sprites;
	private static ObjectMatrix objectMatrix;
	private static Console console;
	private static InputManager inputImage;
	private static long delay; //Used for loop timing
	public static void main (String[] args) {
		//Main method
		double framerate = 30; //The framerate; pretty self-explanitory
		boolean running = true; //Currently unused, but setting this to false would stop the game
		long startTime; //Used for loop timing
		delay = 0; //Used for loop timing
		gameWindow = new GameWindow (640, 480); //Create the window
		objectMatrix = new ObjectMatrix (); //Create the object matrix
		sprites = new SpriteContainer (); //Create the sprite container
		console = new Console (); //Create the dev console
		GameCode gameCode = new GameCode (); //Initialize game code
		gameCode.initialize (); //Note: runs before gameCode.gameLoop ()
		while (running) {
			//Everything in here is run once per frame
			startTime = System.currentTimeMillis(); //Used for loop timing
			try {
				//This try block catches any errors while the game is running
				inputImage = gameWindow.getInputImage ();
				if (!console.isEnabled ()) {
					//Only run if the console is disabled
					gameCode.gameLoop ();
					objectMatrix.callAll (); //For each child of GameObject for which the method declare has been called, this calls that object's draw method, then its frameEvent method
				}
				gameWindow.resetInputBuffers ();
				gameWindow.refresh (); //Refreshes the game window
			}
			catch (Throwable e) {
				//Displays the console if an error occurs, and print out information usable for debugging
				e.printStackTrace ();
				console.enable ("A runtime error has occured: " + e.getClass ());
			}
			delay = System.currentTimeMillis () - startTime; //Used for timing of the loop
			//System.out.println (System.currentTimeMillis() - startTime);
			//The following is used for delaying the loop to a rate equal to the framerate
			if (delay < (1000 / framerate)) {
				try {
					if (1000 / (long) framerate - delay - 1 > 0) {
						Thread.sleep (1000 / (long) framerate - delay - 1);
					}
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			while (System.currentTimeMillis() - startTime < 1000 / (long) framerate) {
			}
		}
	}
	public static GameWindow getWindow () {
		return gameWindow;
	}
	public static SpriteContainer getSprites () {
		return sprites;
	}
	public static ObjectMatrix getObjectMatrix () {
		return objectMatrix;
	}
	public static Console getConsole () {
		return console;
	}
	public static InputManager getInputImage () {
		return inputImage;
	}
	public static long getDelay () {
		return delay;
	}
}