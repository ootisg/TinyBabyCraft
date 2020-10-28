package main;

import java.util.ArrayList;

import resources.AnimationHandler;
import resources.Sprite;
import world.World;

public abstract class GameObject extends GameAPI {
	protected int[] matrixLocation;
	private double x;
	private double y;
	private double xprevious;
	private double yprevious;
	private int hitboxXOffset;
	private int hitboxYOffset;
	private Sprite sprite;
	private AnimationHandler animationHandler = new AnimationHandler (sprite);
	private Hitbox hitbox;
	private boolean animationEnabled;
	private boolean flipHorizontal = false;
	private boolean flipVertical = false;
	public void declare (double x, double y) {
		//Adds this GameObject to the object matrix
		matrixLocation = MainLoop.getObjectMatrix ().add (this);
		this.x = x;
		this.y = y;
		xprevious = x;
		yprevious = y;
	}
	public void forget () {
		//Removes this GameObject from the object matrix
		MainLoop.getObjectMatrix ().remove (matrixLocation);
	}
	public void setPosition (double x, double y) {
		//Sets the position of this GameObject to (x, y)
		xprevious = this.x;
		yprevious = this.y;
		this.x = x;
		this.y = y;
	}
	/*public boolean isInBounds () {
		//Returns true if this GameObject is in bounds of the room
		return isInBounds (this.getX (), this.getY ());
	}*/
	/*public boolean isInBounds (double x, double y) {
		//Returns true if the given point is in bounds of the room
		if (x >= 0 && x <= room.getWidth () * 16 && y >= 0 && y <= room.getHeight () * 16) {
			return true;
		} else {
			return false;
		}
	}*/
	public void setX (double x) {
		//Sets the x coordinate of this GameObject to the given value for x
		xprevious = this.x;
		this.x = x;
	}
	public void setY (double y) {
		//Sets the y coordinate of this GameObject to the given value for y
		yprevious = this.y;
		this.y = y;
	}
	public void setX (int x) {
		//Functions identically to the setX method above but uses an integer argument
		xprevious = (double) this.x;
		this.x = (double) x;
	}
	public void setY (int y) {
		//Functions identically to the setY method above but uses an integer argument
		yprevious = (double) this.y;
		this.y = (double) y;
	}
	public void backstep () {
		//Sets coords to previous values
		x = xprevious;
		y = yprevious;
	}
	public void backstepX () {
		//Sets the x coordinate to its previous value
		x = xprevious;
	}
	public void backstepY () {
		//Sets the y coordinate to its previous value
		y = yprevious;
	}
	public void setHitboxXOffset (int hitboxXOffset) {
		//Sets the hitbox x offset to the given value
		this.hitboxXOffset = hitboxXOffset;
	}
	public void setHitboxYOffset (int hitboxYOffset) {
		//Sets the hitbox y offset to the given value
		this.hitboxYOffset = hitboxYOffset;
	}
	public void setSprite (Sprite sprite) {
		//Sets this GameObject's Sprite to the given Sprite
		this.sprite = sprite;
		if (sprite.getIsAnimated ()) {
			animationHandler.setSprite (sprite);
			animationEnabled = true;
		} else {
			animationEnabled = false;
		}
		animationHandler.setFrame (0);
	}
	public void createHitbox (int xoffset, int yoffset, int width, int height) {
		//Creates a hitbox attached to this GameObject
		hitboxXOffset = xoffset;
		hitboxYOffset = yoffset;
		hitbox = new Hitbox ((int) x + xoffset, (int) y + yoffset, width, height);
	}
	public Hitbox getHitbox () {
		//Returns the hitbox object associated with this GameObject
		if (hitbox != null) {
			hitbox.x = (int) x + hitboxXOffset;
			hitbox.y = (int) y + hitboxYOffset;
			return hitbox;
		} else {
			return null;
		}
	}
	public void destroyHitbox () {
		//Destroys the hitbox object associated with this GameObject
		hitbox = null;
	}
	public boolean isColliding (int x, int y, int width, int height) {
		//Returns true if this GameObject is overlapping with a rectangle with the given values for x, y, width, and height
		Hitbox gameHitbox = new Hitbox (0, 0, width, height);
		if (gameHitbox == null || getHitbox () == null) {
			return false;
		} else if (getHitbox ().checkOverlap (gameHitbox)) {
			return true;
		}
		return false;
	}
	public boolean isColliding (GameObject gameObject) {
		//Returns true if this GameObject is overlapping the given GameObject
		if (gameObject.hitbox == null || getHitbox () == null) {
			return false;
		} else if (getHitbox ().checkOverlap (gameObject.hitbox)) {
			return true;
		}
		return false;
	}
	public boolean isColliding (GameObject gameObject, double xTo, double yTo) {
		//Returns true if this GameObject overlaps the given GameObject when moving to (xTo, yTo)
		if (gameObject.hitbox == null || getHitbox () == null) {
			return false;
		} else if (getHitbox ().checkVectorCollision (gameObject.hitbox, xTo, yTo) != null) {
			return true;
		}
		return false;
	}
	public boolean isColliding (String objectType) {
		//Returns true if this GameObject is colliding with a GameObject on the object matrix of the type objectType
		if (this.hitbox != null) {
			ObjectMatrix objectMatrix = MainLoop.getObjectMatrix ();
			int nameListLength = objectMatrix.classNameList.size ();
			ArrayList<GameObject> objectList = null;
			for (int i = 0; i < nameListLength; i ++) {
				if (objectType.equals (objectMatrix.classNameList.get (i))) {
					objectList = objectMatrix.objectMatrix.get (i);
				}
			}			
			if (objectList != null) {
				int objectListLength = objectList.size ();
				for (int i = 0; i < objectListLength; i ++) {
					if (objectList.get (i) != null) {
						if (objectList.get (i).getHitbox () != null) {
							if (getHitbox ().checkOverlap (objectList.get (i).getHitbox ())) {
								return true;
							}
						}
					}
				}
				return false;
			}
		}
		return false;
	}
	public double[] getCollidingCoords (String objectType, double xTo, double yTo) {
		//Returns the coordinates at which a collision with this GameObject and an object on the object matrix of type objectType occurs
		if (this.hitbox != null) {
			ObjectMatrix objectMatrix = MainLoop.getObjectMatrix ();
			int nameListLength = objectMatrix.classNameList.size ();
			ArrayList<GameObject> objectList = null;
			for (int i = 0; i < nameListLength; i ++) {
				if (objectType.equals (objectMatrix.classNameList.get (i))) {
					objectList = objectMatrix.objectMatrix.get (i);
				}
			}			
			if (objectList != null) {
				int objectListLength = objectList.size ();
				for (int i = 0; i < objectListLength; i ++) {
					if (objectList.get (i) != null) {
						if (objectList.get (i).getHitbox () != null) {
							double[] result = getHitbox ().checkVectorCollision (objectList.get (i).getHitbox (), xTo, yTo);
							if (result != null) {
								return result;
							}
						}
					}
				}
				return null;
			}
		}
		return null;
	}
	public GameObject getCollidingObject (String objectType, double xTo, double yTo) {
		//Returns the object on the object matrix that this GameObject collides with when moving to (xTo, yTo). Returns null if no collision occurs.
		if (this.hitbox != null) {
			ObjectMatrix objectMatrix = MainLoop.getObjectMatrix ();
			int nameListLength = objectMatrix.classNameList.size ();
			ArrayList<GameObject> objectList = null;
			for (int i = 0; i < nameListLength; i ++) {
				if (objectType.equals (objectMatrix.classNameList.get (i))) {
					objectList = objectMatrix.objectMatrix.get (i);
				}
			}			
			if (objectList != null) {
				int objectListLength = objectList.size ();
				for (int i = 0; i < objectListLength; i ++) {
					if (objectList.get (i) != null) {
						if (objectList.get (i).getHitbox () != null) {
							if (getHitbox ().checkVectorCollision (objectList.get (i).getHitbox (), xTo, yTo) != null) {
								return objectList.get (i);
							}
						}
					}
				}
				return null;
			}
		}
		return null;
	}
	public boolean isColliding (String objectType, double xTo, double yTo) {
		//Returns true if this GameObject collides with a GameObject on the object matrix of type objectType when moving to (xTo, yTo)
		if (this.getCollidingCoords (objectType, xTo, yTo) != null) {
			return true;
		}
		return false;
	}
	public boolean isCollidingSameType () {
		//Returns true if this GameObject is colliding with a GameObject on the object matrix of the same type (excluding itself)
		String objectType = this.getClass ().getSimpleName ();
		if (this.hitbox != null) {
			ObjectMatrix objectMatrix = MainLoop.getObjectMatrix ();
			int nameListLength = objectMatrix.classNameList.size ();
			ArrayList<GameObject> objectList = null;
			objectList = objectMatrix.objectMatrix.get (this.matrixLocation [0]);		
			if (objectList != null) {
				int objectListLength = objectList.size ();
				for (int i = 0; i < objectListLength; i ++) {
					if (objectList.get (i) != null) {
						if (objectList.get (i).getHitbox () != null) {
							if (getHitbox ().checkOverlap (objectList.get (i).getHitbox ()) && i != this.matrixLocation [1]) {
								return true;
							}
						}
					}
				}
				return false;
			}
		}
		return false;
	}
	public double[] getCollisionCoordsSameType (double xTo, double yTo) {
		//Returns the coordinates of a collision between this GameObject and an object on the object matrix of the same type (excluding itself) when this GameObject is moving to (xTo, yTo)
		String objectType = this.getClass ().getSimpleName ();
		if (this.hitbox != null) {
			ObjectMatrix objectMatrix = MainLoop.getObjectMatrix ();
			int nameListLength = objectMatrix.classNameList.size ();
			ArrayList<GameObject> objectList = null;
			objectList = objectMatrix.objectMatrix.get (this.matrixLocation [0]);		
			if (objectList != null) {
				int objectListLength = objectList.size ();
				for (int i = 0; i < objectListLength; i ++) {
					if (objectList.get (i) != null) {
						if (objectList.get (i).getHitbox () != null) {
							double[] result = getHitbox ().checkVectorCollision (objectList.get (i).getHitbox (), xTo, yTo);
							if (result != null && i != this.matrixLocation [1]) {
								return result;
							}
						}
					}
				}
				return null;
			}
		}
		return null;
	}
	public GameObject getCollidingObjectSameType (double xTo, double yTo) {
		//Returns the GameObject of the same type on the object matrix colliding with this GameObject when moving to (xTo, yTo)
		String objectType = this.getClass ().getSimpleName ();
		if (this.hitbox != null) {
			ObjectMatrix objectMatrix = MainLoop.getObjectMatrix ();
			int nameListLength = objectMatrix.classNameList.size ();
			ArrayList<GameObject> objectList = null;
			objectList = objectMatrix.objectMatrix.get (this.matrixLocation [0]);		
			if (objectList != null) {
				int objectListLength = objectList.size ();
				for (int i = 0; i < objectListLength; i ++) {
					if (objectList.get (i) != null) {
						if (objectList.get (i).getHitbox () != null) {
							if (getHitbox ().checkVectorCollision (objectList.get (i).getHitbox (), xTo, yTo) != null && i != this.matrixLocation [1]) {
								return objectList.get (i);
							}
						}
					}
				}
				return null;
			}
		}
		return null;
	}
	public boolean isCollidingSameType (double xTo, double yTo) {
		//Returns true if this GameObject collides with another GameObject on the object matrix of the same type when moving to (xTo, yTo)
		if (getCollisionCoordsSameType (xTo, yTo) != null) {
			return true;
		}
		return false;
	}
	public double getX () {
		//Returns the x-coordinate of this GameObject
		return x;
	}
	public double getY () {
		//Returns the y-coordinate of this GameObject
		return y;
	}
	public int getHitboxXOffset () {
		//Returns the x offset of the Hitbox associated with this GameObject
		return hitboxXOffset;
	}
	public int getHitboxYOffset () {
		//Returns the y offset of the Hitbox associated with this GameObject
		return hitboxYOffset;
	}
	public int[] getMatrixLocation () {
		//Returns the position of this object on the object matrix in the format [x, y]
		return matrixLocation;
	}
	public void draw () {
		//Draws the Sprite associated with this GameObject; called once per frame by ObjectMatrix.callAll ()
		if (sprite != null) {	
			if (animationEnabled) {
				animationHandler.animate ((int) x - World.getViewX () * 8, (int) y - World.getViewY () * 8, flipHorizontal, flipVertical);
			} else {
				sprite.setFrame (0);
				sprite.draw ((int) x - World.getViewX () * 8, (int) y - World.getViewY () * 8, flipHorizontal, flipVertical);
			}
		}
	}
	public void frameEvent () {
		//Called once per frame by ObjectMatrix.callAll (), after GameObject.draw ()
	}
	public AnimationHandler getAnimationHandler () {
		//Returns the AnimationHandler object associated with this GameObject
		return animationHandler;
	}
	public void setAnimationHandler (AnimationHandler animationHandler) {
		//Sets the AnimationHandler of this GameObject to the given AnimationHandler
		this.animationHandler = animationHandler;
	}
	public void setFlipHorizontal (boolean flip) {
		//Sets the horizontal flip state of this GameObject
		flipHorizontal = flip;
	}
	public void setFlipVertical (boolean flip) {
		//Sets the vertical flip state of this GameObject
		flipVertical = flip;
	}
	public boolean getFlipHorizontal () {
		//Returns the horizontal filp state of this GameObject
		return flipHorizontal;
	}
	public boolean getFlipVertical () {
		//Returns the vertical flip state of this GameObject
		return flipVertical;
	}
	public int getId () {
		//Returns the numerical ID of this GameObject's type on the object matrix
		return getTypeId (this.getClass ().getName ());
	}
	public Sprite getSprite () {
		//Returns this object's Sprite
		return this.sprite;
	}
	public boolean hasParent (String parentId) {
		Class c;
		c = this.getClass ();
		while (!c.getSuperclass ().getName ().equals ("main.GameObject")) {
			if (c.getSuperclass ().getName ().equals (parentId)) {
				return true;
			}
			c = c.getSuperclass ();
		}
		return false;
	}
}