package main;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.util.LinkedList;

import javax.swing.JFrame;

/**
 * The window which the game is displayed in. Also handles keyboard and mouse events.
 * @author nathan
 *
 */
public class GameWindow extends JFrame {

	/**
	 * Serial version ID, as specified by Swing?
	 */
	private static final long serialVersionUID = 8537802541411424289L;
	/**
	 * The image used as a drawing buffer
	 */
	private BufferedImage buffer;
	/**
	 * The InputManager used to detect input for this window
	 */
	private InputManager inputManager;
	
	/**
	 * Constructs a new GameWindow with the given width and height.
	 * @param width The initial width, in pixels, of the window content
	 * @param height The initial height, in pixels, of the window content
	 */
	public GameWindow (int width, int height) {
		//Makes sure that java closes when the window closes
		this.addWindowListener (new WindowAdapter() {
			public void windowClosing (WindowEvent e) {
				System.exit (0);
			}
		});
		//Sets the initial size of the window and makes it visible
		pack ();
		setSize (width + getInsets ().left + getInsets ().right, height + getInsets ().top + getInsets ().bottom);
		getContentPane ().setSize (width, height);
		setVisible (true);
		//Initializes the buffer for drawing
		buffer = new BufferedImage (width, height, BufferedImage.TYPE_3BYTE_BGR);
		//Sets up the input detection
		inputManager = new InputManager (this, this.getContentPane ());
	}
	
	/**
	 * Renders the contents of the buffer onto the window.
	 */
	public void refresh () {
		Graphics bufferGraphics = getBufferGraphics ();
		Graphics windowGraphics = getContentPane ().getGraphics ();
		windowGraphics.drawImage (buffer, 0, 0, getContentPane ().getWidth (), getContentPane ().getHeight (), null);	
		bufferGraphics.setColor (new Color (0xC0C0C0));
		bufferGraphics.fillRect (0, 0, buffer.getWidth (), buffer.getHeight ());
	}
	
	/**
	 * Gets the graphics object referring to this GameWindow's buffer.
	 * @return The buffer for this GameWindow's graphics
	 */
	public Graphics getBufferGraphics () {
		return buffer.getGraphics ();
	}
	
	/**
	 * Gets the dimensions of the buffer, e.g. the resolution of the output.
	 * @return The dimensions of this GameWindow's buffer as an int array, in the format [width, height]
	 */
	public int[] getResolution () {
		return new int[] {buffer.getWidth (), buffer.getHeight ()};
	}
	
	/**
	 * Sets the resolution of the buffer to the given width and height; erases its contents.
	 * @param width The width to use, in pixels
	 * @param height The height to use, in pixels
	 */
	public void setResolution (int width, int height) {
		buffer = new BufferedImage (width, height, BufferedImage.TYPE_3BYTE_BGR);
	}
	
	public InputManager getInputImage () {
		return inputManager.createImage ();
	}
	
	public void resetInputBuffers () {
		inputManager.resetKeyBuffers ();
		inputManager.resetMouseBuffers ();
	}
}