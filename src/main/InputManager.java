package main;

import java.awt.Component;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.Iterator;
import java.util.LinkedList;

public class InputManager {

	/**
	 * The component this object is attached to for keystroke detection
	 */
	Component keyComponent;
	/**
	 * The component this object is attached to for mouse input detection
	 */
	Component mouseComponent;
	/**
	 * the key listener to be used by this InputManager
	 */
	InputManagerKeyListener keyListener;
	/**
	 * The mouse listener to be used by this InputManager
	 */
	InputManagerMouseListener mouseListener;
	/**
	 * Whether this InputManager is an image or not
	 */
	private boolean isImage = false;
	/**
	 * The buffer used for storing the keys which are currently pressed down
	 */
	private boolean[] keysDown = new boolean[256];
	/**
	 * The buffer used for storing the keys which were recently pressed down
	 * WARNING: ACCESSED ACROSS MULTIPLE THREADS
	 */
	private boolean[] keysPressed = new boolean[256];
	/**
	 * The buffer used for storing they keys which were recently released
	 * WARNING: ACCESSED ACROSS MULTIPLE THREADS
	 */
	private boolean[] keysReleased = new boolean[256];
	/**
	 * An ordered list of recent keyEvents
	 * WARNING: ACCESSED ACROSS MULTIPLE THREADS
	 */
	private LinkedList<KeyEvent> keyEvents = new LinkedList<KeyEvent> ();
	/**
	 * An ordered list of recent mouseEvents
	 * WARNING: ACCESSED ACROSS MULTIPLE THREADS
	 */
	private LinkedList<MouseEvent> mouseEvents = new LinkedList<MouseEvent> ();
	/**
	 * A string of recently typed text
	 */
	private String chars = "";
	/**
	 * Buffer of mouse button clicks
	 */
	private boolean[] clicks = new boolean[3];
	/**
	 * Buffer of mouse buttons released
	 */
	private boolean[] buttonsReleased = new boolean[3];
	/**
	 * Map of mouse buttons pressed down
	 */
	private boolean[] buttonsDown = new boolean[3];
	/**
	 * The current x-coordinate of the mouse cursor
	 */
	private double cursorX;
	/**
	 * The current y-coordinate of the mouse cursor
	 */
	private double cursorY;
	/**
	 * Whether or not the game is copying inputs
	 * WARNING: ACCESSED ACROSS MULTIPLE THREADS
	 */
	private volatile boolean copyingInputs = false;
	
	/**
	 * Default no-arg constructor
	 */
	protected InputManager () {
		
	}
	
	/**
	 * Constructs a new InputManager for the given component.
	 * @param component The component to attach this InputManager to
	 */
	public InputManager (Component component) {
		this (component, component);
	}
	
	/**
	 * Constructs a new InputManager for the given components.
	 * @param keyboardComponent The component to attch a KeyListener to
	 * @param mouseComponent The component to attach a MouseListener and MouseMotionListener to
	 */
	public InputManager (Component keyboardComponent, Component mouseComponent) {
		attach (keyboardComponent, mouseComponent);
	}
	
	/**
	 * Attaches this InputManager to a given AWT component.
	 * @param keyboardComponent The component to attch a KeyListener to
	 * @param mouseComponent The component to attach a MouseListener and MouseMotionListener to
	 */
	public void attach (Component keyboardComponent, Component mouseComponent) {
		this.keyComponent = keyboardComponent;
		this.mouseComponent = mouseComponent;
		keyListener = new InputManagerKeyListener ();
		mouseListener = new InputManagerMouseListener ();
		keyboardComponent.addKeyListener (keyListener);
		mouseComponent.addMouseListener (mouseListener);
		mouseComponent.addMouseMotionListener (mouseListener);
	}
	
	/**
	 * Creates an image of the input state; i.e. makes a deep copy of this InputManager which is not attached to a component.
	 * @return An image of the current input state
	 */
	public InputManager createImage () {
		copyingInputs = true;
		InputManager image = new InputManager ();
		System.arraycopy (keysDown, 0, image.keysDown, 0, keysDown.length);
		System.arraycopy (keysPressed, 0, image.keysPressed, 0, keysPressed.length);
		System.arraycopy (keysReleased, 0, image.keysReleased, 0, keysReleased.length);
		Iterator<KeyEvent> iter = keyEvents.iterator ();
		while (iter.hasNext ()) {
			image.keyEvents.add (iter.next ());
		}
		Iterator<MouseEvent> iter2 = this.mouseEvents.iterator ();
		while (iter2.hasNext ()) {
			image.mouseEvents.add (iter2.next ());
		}
		for (int i = 0; i < clicks.length; i ++) {
			image.clicks [i] = this.clicks [i];
			image.buttonsDown [i] = this.buttonsDown [i];
			image.buttonsReleased [i] = this.buttonsReleased [i];
		}
		image.chars = chars;
		image.cursorX = cursorX;
		image.cursorY = cursorY;
		image.isImage = true;
		copyingInputs = false;
		return image;
	}
	
	private class InputManagerKeyListener implements KeyListener {

		@Override
		public void keyPressed (KeyEvent e) {
			lockIfCopying ();
			keysPressed [e.getKeyCode ()] = true;
			keysDown [e.getKeyCode ()] = true;
			keyEvents.add (e);
		}

		@Override
		public void keyReleased (KeyEvent e) {
			lockIfCopying ();
			keysReleased [e.getKeyCode ()] = true;
			keysDown [e.getKeyCode ()] = false;
			keyEvents.add (e);
		}

		@Override
		public void keyTyped (KeyEvent e) {
			lockIfCopying ();
			chars += e.getKeyChar ();
			keyEvents.add (e);
		}
		
	}
	
	private class InputManagerMouseListener implements MouseListener, MouseMotionListener {
		
		private boolean wasDragged;
		
		@Override
		public void mouseDragged (MouseEvent e) {
			lockIfCopying ();
			mouseEvents.add (e);
			int index = getButtonIndex (e.getButton ());
			if (index != -1 && !wasDragged) {
				clicks [index] = true;
				buttonsDown [index] = true;
			}
			wasDragged = true;
			updateMouseCoords (e);
		}

		@Override
		public void mouseMoved (MouseEvent e) {
			lockIfCopying ();
			mouseEvents.add (e);
			updateMouseCoords (e);
		}

		@Override
		public void mouseClicked (MouseEvent e) {
			/*
			 * Click detection is already achieved through pressed/released events
			 * 
			 * lockIfCopying ();
			mouseEvents.add (e);
			int index = getButtonIndex (e.getButton ());
			if (index != -1) {
				clicks [index] = true;
				buttonsDown[index] = false;
			}
			updateMouseCoords (e);*/
		}

		@Override
		public void mouseEntered (MouseEvent e) {
			lockIfCopying ();
			mouseEvents.add (e);
			updateMouseCoords (e);
		}

		@Override
		public void mouseExited (MouseEvent e) {
			lockIfCopying ();
			mouseEvents.add (e);
			updateMouseCoords (e);
		}

		@Override
		public void mousePressed (MouseEvent e) {
			lockIfCopying ();
			mouseEvents.add (e);
			int index = getButtonIndex (e.getButton ());
			if (index != -1) {
				clicks [index] = true;
				buttonsDown [index] = true;
			}
			updateMouseCoords (e);
		}

		@Override
		public void mouseReleased (MouseEvent e) {
			lockIfCopying ();
			mouseEvents.add (e);
			int index = getButtonIndex (e.getButton ());
			if (index != -1) {
				buttonsReleased [index] = true;
				buttonsDown [index] = false;
				clicks [index] = false;
			}
			updateMouseCoords (e);
			wasDragged = false;
		}
		
		private void updateMouseCoords (MouseEvent e) {
			cursorX = ((double)e.getX ()) / mouseComponent.getWidth ();
			cursorY = ((double)e.getY ()) / mouseComponent.getHeight ();
		}
		
		private int getButtonIndex (int button) {
			switch (button) {
				case MouseEvent.BUTTON1:
					return 0;
				case MouseEvent.BUTTON2:
					return 1;
				case MouseEvent.BUTTON3:
					return 2;
				default:
					return -1;
			}
		}
	}
	
	/**
	 * Returns true if the key with the given character code was pressed down since the last call to resetKeyBuffers().
	 * @param charCode The character code to check for
	 * @return Whether the given key was pressed
	 */
	public boolean keyPressed (int charCode) {
		return keysPressed [charCode];
	}
	
	/**
	 * Returns true if the key with the given character code was released since the last call to resetKeyBuffers().
	 * @param charCode The character code to check for
	 * @return Whether the given key was released
	 */
	public boolean keyReleased (int charCode) {
		return keysReleased [charCode];
	}
	
	/**
	 * Returns true if the key with the given character code is currently being pressed down.
	 * @param charCode The character code to check for
	 * @return Whether the given key is pressed down
	 */
	public boolean keyDown (int charCode) {
		return keysDown [charCode];
	}
	
	/**
	 * Gets a list of the keys currently pressed down, sorted from lowest to highest character code.
	 * @return A char[] representing all the keys currently pressed down
	 */
	public char[] getKeysDown () {
		return makeCharList (keysDown);
	}
	
	/**
	 * Gets a list of the keys pressed since the last call to clearKeyBuffers (), sorted from lowest to highest character code.
	 * @return A char[] representing all the keys pressed since the last call to clearKeyBuffers ()
	 */
	public char[] getKeysPressed () {
		return makeCharList (keysPressed);
	}
	
	/**
	 * Gets a list of the keys released since the last call to clearKeyBuffers (), sorted from lowest to highest character code.
	 * @return A char[] representing all the keys released since the last call to clearKeyBuffers ()
	 */
	public char[] getKeysReleased () {
		return makeCharList (keysReleased);
	}
	
	/**
	 * Gets a list of all the keyEvents fired since the last call to clearKeyBuffers().
	 * @return A LinkedList with all of the KeyEvents since the last call to clearKeyBuffers ()
	 */
	public LinkedList<KeyEvent> getKeyEvents () {
		return keyEvents;
	}
	
	/**
	 * Creates a list of characters from the given character map.
	 * @param charMap The character map to use
	 * @return A list of characters generated from the given character map, sorted by character code
	 */
	private char[] makeCharList (boolean[] charMap) {
		int totalChars = 0;
		for (int i = 0; i < charMap.length; i ++) {
			if (charMap [i]) {
				totalChars ++;
			}
		}
		char[] result = new char[totalChars];
		int j = 0;
		for (int i = 0; i < charMap.length; i ++) {
			if (charMap [i]) {
				result [j] = (char)i;
				j --;
			}
		}
		return result;
	}
	
	/**
	 * Gets a list of the text typed since the last call to resetKeyBuffers(); responds to key combinations.
	 * @return The text typed since calling resetKeyBuffers
	 */
	public String getTyped () {
		return chars;
	}
	
	/**
	 * Returns true if the given mouse button was clicked.
	 * @param button The mouse button to check
	 * @return Whether the button is clicked
	 */
	public boolean mouseButtonClicked (int button) {
		if (button >= 0 && button <= 2) {
			return clicks [button];
		}
		return false;
	}
	
	/**
	 * Returns true if the given mouse button was released.
	 * @param button The mouse button to check
	 * @return Whether the button was released
	 */
	public boolean mouseButtonReleased (int button) {
		if (button >= 0 && button <= 2) {
			return buttonsReleased [button];
		}
		return false;
	}
	public boolean mouseButtonPressed(int button) {
		if (button >= 0 && button <= 2) {
			if (clicks [button]) {
				return buttonsDown[button];
			} 
		}
		return false;
	}
	/**
	 * Returns true if the given mouse button is held down.
	 * @param button The mouse button to check
	 * @return Whether the button is held down
	 */
	public boolean mouseButtonDown (int button) {
		if (button >= 0 && button <= 2) {
			return buttonsDown [button];
		}
		return false;
	}
	
	/**
	 * Gets the current x-coordinate of the mouse cursor, where (0, 0) is the top left of the component and (1, 1) is the bottom right.
	 * @return The cursor x-coordinate
	 */
	public double getCursorX () {
		return cursorX;
	}
	
	/**
	 * Gets the current y-coordinate of the mouse cursor, where (0, 0) is the top left of the component and (1, 1) is the bottom right.
	 * @return The cursor y-coordinate
	 */
	public double getCursorY () {
		return cursorY;
	}
	
	/**
	 * Gets a list of the mouse events fired since the last call to clearMouseBuffers ()
	 * @return A LinkedList with the mouse events since the last call to clearMouseBuffers ()
	 */
	public LinkedList<MouseEvent> getMouseEvents () {
		return mouseEvents;
	}
	
	/**
	 * Gets the component this InputManager is attached to for keyboard input.
	 * @return The component used for keyboard input
	 */
	public Component getKeyboardComponent () {
		return keyComponent;
	}
	
	/**
	 * Gets the component this InputManager is attached to for mouse input.
	 * @return The component used for mouse input
	 */
	public Component getMouseComponent () {
		return mouseComponent;
	}
	/**
	 * Resets all the buffers holding data for keystrokes, with the exception of the buffer holding the keys currently pressed down.
	 */
	public void resetKeyBuffers () {
		lockIfCopying ();
		keysPressed = new boolean[255];
		keysReleased = new boolean[255];
		keyEvents = new LinkedList<KeyEvent> ();
		chars = "";
	}
	
	/**
	 * Resets all the buffers holding data for mouse presses, with the exception of the buffer holding the mouse buttons currently pressed down.
	 */
	public void resetMouseBuffers () {
		lockIfCopying ();
		clicks = new boolean[3];
		buttonsReleased = new boolean[3];
		mouseEvents = new LinkedList<MouseEvent> ();
	}
	
	/**
	 * Returns true if this InputManager is an image; returns false otherwise.
	 * @return true if this InputManager is an image; false otherwise
	 */
	public boolean isImage () {
		return isImage;
	}
	
	/**
	 * Gets whether or not an image is currently being created from this InputManager
	 * @return true if an image is being created; false otherwise
	 */
	public boolean isCopyingInputs () {
		return copyingInputs;
	}
	
	public void lockIfCopying () {
		//TODO CHECK HERE FOR POTENTIAL DROPPED INPUTS
		while (copyingInputs) {}
	}
}