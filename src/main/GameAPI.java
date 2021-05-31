package main;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.Iterator;
import java.util.LinkedList;

import resources.SpriteContainer;

/**
 * An abstract class with various useful methods to make interacting with the game engine easier.
 * @author ootisg
 *
 */
public abstract class GameAPI {
	
	/**
	 * Makes a deep copy of the given LinkedList of characters.
	 * @param keys The list to copy
	 * @return A copy of the original list
	 */
	private static LinkedList<Character> cloneKeyList (LinkedList<Character> keys) {
		LinkedList<Character> copy = new LinkedList<Character> ();
		Iterator<Character> iter = keys.iterator ();
		while (iter.hasNext ()) {
			copy.add (new Character (iter.next ()));
		}
		return copy;
	}

	/**
	 * Returns true if the key with the given character code was pressed down since the last call to resetKeyBuffers().
	 * @param charCode The character code to check for
	 * @return Whether the given key was pressed
	 */
	public boolean keyPressed (int charCode) {
		return MainLoop.getInputImage ().keyPressed (charCode);
	}
	
	/**
	 * Returns true if the key with the given character code was released since the last call to resetKeyBuffers().
	 * @param charCode The character code to check for
	 * @return Whether the given key was released
	 */
	public boolean keyReleased (int charCode) {
		return MainLoop.getInputImage ().keyReleased (charCode);
	}
	
	/**
	 * Returns true if the key with the given character code is currently being pressed down.
	 * @param charCode The character code to check for
	 * @return Whether the given key is pressed down
	 */
	public boolean keyDown (int charCode) {
		return MainLoop.getInputImage ().keyDown (charCode);
	}
	
	/**
	 * Gets a list of the keys currently pressed down, sorted from lowest to highest character code.
	 * @return A char[] representing all the keys currently pressed down
	 */
	public char[] getKeysDown () {
		return MainLoop.getInputImage ().getKeysDown ();
	}
	
	/**
	 * Gets a list of all the keys events fired since the last call to resetKeyBuffers() in the order they were pressed.
	 * @return A LinkedList representing all of the key events
	 */
	public LinkedList<KeyEvent> getKeyEvents () {
		return MainLoop.getInputImage ().getKeyEvents ();
	}
	
	/**
	 * Gets a list of the text typed since the last call to resetKeyBuffers(); responds to key combinations.
	 * @return The text typed since calling resetKeyBuffers
	 */
	public String getTyped () {
		return MainLoop.getInputImage ().getTyped ();
	}
	
	/**
	 * Returns true if the given mouse button was clicked.
	 * @param button The mouse button to check
	 * @return Whether the button is clicked
	 */
	public boolean mouseButtonClicked (int button) {
		return MainLoop.getInputImage ().mouseButtonClicked (button);
	}
	public boolean mouseButtonPressed(int button) {
		return MainLoop.getInputImage().mouseButtonPressed(button);
	}
	/**
	 * Returns true if the given mouse button was released.
	 * @param button The mouse button to check
	 * @return Whether the button was released
	 */
	public boolean mouseButtonReleased (int button) {
		return MainLoop.getInputImage ().mouseButtonReleased (button);
	}
	
	/**
	 * Returns true if the given mouse button is held down.
	 * @param button The mouse button to check
	 * @return Whether the button is held down
	 */
	public boolean mouseButtonDown (int button) {
		return MainLoop.getInputImage ().mouseButtonDown (button);
	}
	
	/**
	 * Gets the current x-coordinate of the mouse cursor.
	 * @return The cursor x-coordinate
	 */
	public static int getCursorX () {
		return (int) (MainLoop.getInputImage ().getCursorX () * MainLoop.getWindow ().getResolution () [0]);
	}
	
	/**
	 * Gets the current y-coordinate of the mouse cursor.
	 * @return The cursor y-coordinate
	 */
	public static int getCursorY () {
		return (int) (MainLoop.getInputImage ().getCursorY () * MainLoop.getWindow ().getResolution () [1]);
	}
	
	/**
	 * Gets a list of all the mosue events fired since the last call to resetKeyBuffers() in the order they were pressed.
	 * @return A LinkedList representing all of the recent mouse events
	 */
	public LinkedList<MouseEvent> getMouseEvents () {
		return MainLoop.getInputImage ().getMouseEvents ();
	}
	
	/**
	 * Gets the object from the object matrix at the position (x, y). Returns null if no object is found there or if either index is out of bounds.
	 * @param x The x-coordinate on the object matrix of the object to find
	 * @param y The y-coordinate on the object matrix of the object to find
	 * @return The object on the object matrix at the position (x, y); may be null.
	 */
	public GameObject getObject (int x, int y) {
		return MainLoop.getObjectMatrix ().get (x, y);
	}
	
	/**
	 * Gets the x-coordinate on the object matrix representing the String representing the type objectName.
	 * @param objectName The name of the desired type as a string. Must match the value obtained by calling getClass().getName() on the type.
	 * @return The x-coordinate representing the index of the desired type on the object matrix.
	 */
	public int getTypeId (String objectName) {
		return MainLoop.getObjectMatrix ().getTypeId (objectName);
	}
}