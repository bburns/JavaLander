
// JavaLander.java

import java.awt.*;
import java.applet.*;
import java.awt.event.*;
//import java.util.Vector;
//import java.awt.geom.*;


// World
// Contains sprites - ship, land, stars, moon, clouds - each can be fixed or movable.
// Also contains a view which it uses in rendering itself and its sprites.
//-------------------------------------------------------------------------------------------------------
class World
{
	// Width and height of world, in world units (meters)
	public float width;
	public float height;
	
	public float radiansPerDegree = 2.0f * (float) Math.PI / 360.0f;		// conversion factor
	public float g = 4.0f;		// gravity (m/s/s)

	// views should probably belong to the applet, since that's the main window
	// will want a view for the stats also, which could be its own class?
	View viewMain = new View();
//	View viewShip = new View();

	Land land = new Land();
	Ship ship = new Ship();
//	Stars stars = new Stars();
//	Clouds clouds = new Clouds();
//	Moon moon = new Moon();
	
	public void init(int appletWidth, int appletHeight) // width and height in pixels
	{
		// Initialize world
		float widthWindow = 500.0f; // the view window looks on this many meters of the world horizontally
		width = widthWindow * 2; // let's make the world two view width's wide
		height = widthWindow / 2.0f;
		
		// Initialize view
		viewMain.init(this, appletWidth, appletHeight, widthWindow);

		// Zoom scale
		viewMain.setScale(1.0f);
		
		// Initialize sprites
		land.init(this);
		ship.init(this);

		// set ship in middle of world
		ship.setPos(width, height / 2.0f);
					
		// Tell view to track the ship sprite
//		view.trackSprite(ship);
	}

	
	public void step(float timeStep)
	{
			// Move ship and any other moving sprites
			ship.step(timeStep);
//			land.step(timeStep);
			
			// Center the view on the ship
			viewMain.centerOn(ship);
	}

	
	// Draw the world and all the sprites it contains
	public void draw(Graphics g)
	{
		viewMain.drawBorder(g);
		land.draw(g, viewMain);
		ship.draw(g, viewMain);
		ship.drawStats(g);
	}
	
}



// Affine transform - rotates, scales, and translates a point or points in 2D
class Transform
{
	float a = 1.0f; // default
	float b;
	float c;
	float d;
	float e = 1.0f; // default
	float f;
	
	public void setScale(float xscale, float yscale)
	{
		a = xscale;
		e = yscale;
	}
	
	public void setRotation(float r)
	{
		a = (float) Math.cos(r);
		b = (float) -Math.sin(r);
		d = (float) Math.sin(r);
		e = (float) Math.cos(r);
	}
	
	public void setTranslation(float x, float y)
	{
		c = x;
		f = y;
	}
	
	public void multiply(Transform t)
	{
	}
	
}



// Sprite
//-------------------------------------------------------------------------------------------------------
class Sprite
{
	World world;
	Transform tModelToWorld = new Transform(); // transformation from model coordinates to world coordinates
	ShapeX shapeModel = new ShapeX(); // model shape in model coordinates
	ShapeX shapeDraw = new ShapeX(); // model that will be transformed to view coordinates
	
	// World coords: (kg, meters, m/s, m/s/s for now)
	int mass;
	float x;
	float y;
	float vx;
	float vy;
	float ax;
	float ay;
	float rotation = 0.0f; // radians
	float scale = 1.0f;		// unitless
	
	public void init(World w) {}
	public void explode() {}
	public void step(float timeStep) {}

	public void setPos(float xWorld, float yWorld)
	{
		x = xWorld;
		y = yWorld;
		// Update the transformation matrix
		tModelToWorld.setTranslation(x, y);
	}

	public void setVelocity(float vxWorld, float vyWorld)
	{
		vx = vxWorld;
		vy = vyWorld;
	}

	// Set the zoom scale and update the drawing polygon.
	// Better to do this once here than with each draw call!
	public void setScale(float s)
	{
		// Save the new scale
		scale = s;
//		scaleX = s;
//		scaleY = s;
		tModelToWorld.setScale(scale, scale);
	}

	// Set the rotation amount for the ship in radians and update the drawing polygon
	public void setRotation(float r)
	{
		rotation = r;		
		tModelToWorld.setRotation(rotation);
	}
	
	// Rotate the ship by the specified amount (in radians) and update the drawing polygon
	public void rotate(float rdelta)
	{
		rotation += rdelta;
		tModelToWorld.setRotation(rotation);
	}
	
	// Check for a collision between this sprite and the specified sprite
	public boolean checkCollision(Sprite s) 
	{
		// test for intersecting line segments
		// would help if all sprites used the same format to store their line segments
		// ie array of points, then array of segments
		// will need something like that for a space station, for instance	
		// or maybe combine with idea of parts that join together, like for evolution
		return false;
	}

	
	// Draw the sprite on the screen using the given view transformation
	public void draw(Graphics g, View view) 
	{
		// We have the model coordinates, need to convert to world coordinates using the sprite transformation,
		// then convert to view coordinates using the view transformation.
		// can combine these into one transform for speed
//		shapeDraw = new ShapeX();
		shapeDraw.copyFrom(shapeModel);
		shapeDraw.transform(tModelToWorld);
		shapeDraw.transform(view.tWorldToView);
		shapeDraw.drawShape(g);
	}		
	
}




// Land
//-------------------------------------------------------------------------------------------------------
class Land extends Sprite
{
	
	public void init(World w)
	{	
		world = w;
		
		// set width 
		int width = (int) world.width;
		int height = (int) world.height;
		int hillHeight = height / 5; //. 20% of world height
		
		// create horizon line
		int nPoints = 40;
		for (int i = 0; i < nPoints; i++)
		{
			int x = width * i / (nPoints - 1);
			int y = height - (int) (Math.random() * hillHeight);
			shapeModel.addPoint(x, y);
			shapeModel.addLineTo(i);
		}
		
		// set scale
		setScale(1.0f);
	}

	
}




// Ship
//-------------------------------------------------------------------------------------------------------
class Ship extends Sprite
{
	int massShip;			// kg
	int massFuel; 			// kg
	float rotationUnit;	// radians
	int throttle;				// 0 - 10. thrust = throttle * thrustunit
	boolean outOfFuel;	// flag
	float burnRate;				// kg/s
	float exhaustVelocity;	// 
	float thrustUnit;
	
	float shipSize = 30.0f; // rough size of ship

	Flame flame = new Flame();
	
	public void init(World w)
	{
		world = w;
	
		flame.init(w);
		flame.ship = this;
		
		massShip = 1000; // kg
		massFuel = 5000; // kg
		mass = massShip + massFuel;
		rotation = 0.0f;
		rotationUnit = 1.0f * world.radiansPerDegree; // degrees converted to radians
		exhaustVelocity = 250.0f; // m/s (very approx 1 mach)

		// Calculate burnRate that will balance out gravity at throttle 5 and fuel tank half empty
//		burnRate = 1; // kg/s
		burnRate = (world.g * massShip + (massFuel / 2.0f)) / (5.0f * exhaustVelocity); // 2.8 kg/s for g=1m/s/s 
		burnRate *= 6.0f;
		thrustUnit = burnRate * exhaustVelocity; // kgm/s/s = newtons	
		
		// Set position and velocity (m and m/s)
		x = 310.0f;
		y = 100.0f;
		vx = 0.0f;
		vy = 0.0f;
		
		// Define ship's shape, in world units (meters)
		shapeModel.addPoint(0, -25); // 0
		shapeModel.addPoint(-10, 10); // 1
		shapeModel.addPoint(-7, 1); // 2
		shapeModel.addPoint(-21, 15); // 3
		shapeModel.addPoint(10, 10); // 4
		shapeModel.addPoint(21, 15); // 5
		shapeModel.addPoint(7, 1); // 6
		
		shapeModel.addLineTo(0);
		shapeModel.addLineTo(1);
		shapeModel.addLineTo(2);
		shapeModel.addLineTo(3);
		shapeModel.addLineTo(1);
		shapeModel.addLineTo(4);
		shapeModel.addLineTo(5);
		shapeModel.addLineTo(6);
		shapeModel.addLineTo(4);
		shapeModel.addLineTo(0);
		
		setScale(1.0f);
		setRotation(rotation);
	}

	public void setThrottle(int t)
	{
		if (outOfFuel)
			throttle = 0;
		else
			throttle = t;
	}
	
	// Move the ship according to its velocity, gravity, thrust, etc. and update the drawing polygon
	public void step(float timeStep)
	{
		// Get amount of fuel burned
		mass = massShip + massFuel;
		float fuelBurned = throttle * burnRate * timeStep;
		float thrust = throttle * thrustUnit;
		float thrustAccel = thrust / mass;
		
		// Move ship according to gravity, thrust, etc.
		ax = thrustAccel * (float) Math.sin(rotation);
		ay = - thrustAccel * (float) Math.cos(rotation) + world.g;
		vx += ax * timeStep;
		vy += ay * timeStep;
		x += vx * timeStep;
		y += vy * timeStep;

		// Keep in world
		if (y > world.height - shipSize)
		{
			y = world.height - shipSize;
			vy = 0.0f;
			vx = 0.0f;
		}
		
		// Update fuel remaining
		massFuel -= fuelBurned;
		if (massFuel < 0)
		{
			massFuel = 0;
			outOfFuel = true;	
		}

		// Update the transformation matrix
		tModelToWorld.setTranslation(x, y);
	}

	
	// Make the ship explode!
	public void explode()
	{
		// draw orange and yellow circles filled
		// create a bunch of sub-sprites for pieces of ship,
		// give them all rnd velocities (plus ships velocity)
		// on draw just draw these instead of the ship
		// on step move these instead of ship
		// have them all stop at some depth under the horizon
	}
	
	
	// Draw ship according to the specified view transformations
	public void draw(Graphics g, View view)
	{
		// Call base class to draw model
		super.draw(g, view);
		
		// Draw flame
		// how do we handle this? step could turn this subsprite on and off, ie set a flag in it
		// sprite.draw could check for subsprites and transform them the same as the
		// parent sprite, if the lock flag was set, otherwise would use their own transform
		// this would make it easier to have things detach, like rocket boosters, and let
		// them fall away - they would get same vel as ship, but only gravity would work on them.
		// also each sprite could have different colors, or each segment could?
		// might need to override draw for flame to get it to flicker correctly but that's okay
		if (throttle > 0)
			flame.draw(g, view);	
	}
		
	// Draw ship stats
	public void drawStats(Graphics g)
	{
		// Show stats
		String s = "Position: (" + x + ", " + y + ")";
		g.drawString(s, 4, 20);
		s = "Velocity: (" + vx + ", " + vy + ")";
		g.drawString(s, 4, 30);
		s = "Acceleration: (" + ax + ", " + ay + ")";
		g.drawString(s, 4, 40);
		s = "Rotation: (" + rotation + ")";
		g.drawString(s, 4, 50);
		s = "Throttle: (" + throttle + ")";
		g.drawString(s, 4, 60);
		s = "Fuel (kg): (" + massFuel + ")";
		if (massFuel < 500)
			g.setColor(Color.red);
		g.drawString(s, 4, 70);
		g.setColor(Color.black);
//		s = "view pos: (" + polyDraw.xpoints[0] + ", " + polyDraw.ypoints[0] + ")";
//		g.drawString(s, 4, 80);
	}
	
}


// Ship flame
//-------------------------------------------------------------------------------------------------------
class Flame extends Sprite
{
	Ship ship;
	
	public void init(World w)
	{
		world = w;
	
		// Define flame's shape, in world units (meters)
		shapeModel.addPoint(-5, 11);
		shapeModel.addPoint(0, 60);
		shapeModel.addPoint(5, 11);

		shapeModel.addLineTo(0);
		shapeModel.addLineTo(1);
		shapeModel.addLineTo(2);
		
	}

	// note: will eventually just use base class to draw this sprite -
	// it has a parent which is where draw will get tModelToWorld from
	public void draw(Graphics g, View view)
	{
		// Set color for flames
		if (Math.random() > 0.5)
			g.setColor(Color.yellow);
		else
			g.setColor(Color.orange);

		// Draw shape using base class
//		super.draw(g, view);		
//		shapeDraw = new ShapeX();
		shapeDraw.copyFrom(shapeModel);
		shapeDraw.transform(ship.tModelToWorld);
		shapeDraw.transform(view.tWorldToView);
		shapeDraw.drawShape(g);
		
		//, set color back
		g.setColor(Color.black);
	}
}




class Stars extends Sprite
{
}

class Clouds extends Sprite
{
}

class Moon extends Sprite
{
}



// View
//-------------------------------------------------------------------------------------------------------
class View
{
	World world; // reference to world that this view is looking at
	Sprite trackSprite; // reference to sprite that we want to track
	Transform tWorldToView = new Transform(); // transform from world coordinates to view coordinates
	
	// Position and size of view, in world coordinates
	public float xWorld;
	public float yWorld;
	public float widthWorld;
	public float heightWorld;

	// Size of view, in pixels
	public int widthPixels;
	public int heightPixels;
	
	// Scale
//	private float aspectRatio = 1.2f;
	public float xscale; // pixels per world unit
	public float yscale; // pixels per world unit
	public float scaleFactor; // unitless zoom factor (eg 2.0 means zoom in by factor of 2, 0.5 is zoom out)
	
	public void init(World w, int viewWidthPixels, int viewHeightPixels, float viewWidthWorldUnits)
	{
		world = w;
		widthPixels = viewWidthPixels;
		heightPixels = viewHeightPixels;
		scaleFactor = 1.0f; // initialize zoom factor (unitless)

		// Get conversion factor between pixels and world units
		xscale = widthPixels / viewWidthWorldUnits; // pixels per world unit
		yscale = xscale; // for now
		
		// Save width and height of view in world units
		widthWorld = viewWidthWorldUnits;
		heightWorld = heightPixels / yscale;

		// Initialize the view transform, which converts from world coordinates to view coordinates (pixels)
		tWorldToView.setScale(xscale, yscale);
		tWorldToView.setTranslation(- xscale * xWorld, - yscale * yWorld);
		tWorldToView.setRotation(0.0f);
	}

	
	// Track the specified sprite to keep it centered in the view if possible
	public void trackSprite(Sprite s)
	{
		trackSprite = s;
	}
	
	public void setScale(float s)
	{
		scaleFactor = s;
		tWorldToView.setScale(s, s);
//		tWorldToView.setTranslation(- xscale * xWorld, - yscale * yWorld);
	}
	
	// Center view on the specified sprite
	//. also include approx size of sprite
	public void centerOn(Sprite s)
	{
		// Set position of view in world coords so sprite will be centered in the view
		xWorld = s.x - widthWorld / 2 / scaleFactor;
		yWorld = s.y - heightWorld / 2 / scaleFactor;
		
		// Keep view in world vertically
		if ((yWorld + heightWorld / scaleFactor) > world.height)
			yWorld = world.height - heightWorld / scaleFactor;
		if (yWorld < 0)
			yWorld = 0;
		
		// Wraparound view horizontally
		if (xWorld > world.width)
			xWorld -= world.width;
		if (xWorld < 0)
			xWorld += world.width;
		
		// Update transform
//		tWorldToView.setTranslation(xWorld, yWorld);
		tWorldToView.setTranslation(- xscale * xWorld, - yscale * yWorld);
	}
		
	// Draw border around view
	public void drawBorder(Graphics g)
	{
		g.drawRect(0, 0, widthPixels - 1, heightPixels - 1);
	}

}



/*
class Polygon2 extends Polygon
{
	// Copy another polygon into this one
	public void copyFrom(Polygon2 p)
	{
		for (int i = 0; i < p.npoints; i++)
		{
			addPoint(p.xpoints[i], p.ypoints[i]);
		}
	}
	
	// Transform this polygon by the given 2d transform
	// Includes scale, rotate, and translate.
	public void transform(Transform t)
	{
		for (int i = 0; i < npoints; i++)
		{
			int x = xpoints[i];
			int y = ypoints[i];
			xpoints[i] = (int) (t.a * x + t.b * y + t.c);
			ypoints[i] = (int) (t.d * x + t.e * y + t.f);
		}
	}	
}
*/



// This is our version of a polygon - lets you define different segments
//, Might be able to implement Shape interface eventually?
class ShapeX
{
	int max = 50;
		  
	// Array of points
	int nPoints;
	int xPoints[] = new int[max];
	int yPoints[] = new int[max];
	
	// Array of line segments
	// index is point number, 0 means stop the line segment
	int nLines;
	int nLine[] = new int[max];

	// Add a point
	public void addPoint(int x, int y)
	{
		xPoints[nPoints] = x;
		yPoints[nPoints] = y;
		nPoints++;
	}
	
	// Add a line segment to the given point
	public void addLineTo(int nPoint)
	{
		nLine[nLines] = nPoint;
		nLines++;
	}
	
	// Copy another shapex into this one
	public void copyFrom(ShapeX p)
	{
		for (int i = 0; i < p.nPoints; i++)
		{
			addPoint(p.xPoints[i], p.yPoints[i]);
		}
		for (int i = 0; i < p.nLines; i++)
		{
			addLineTo(p.nLine[i]);
		}
	}
	
	// Transform this polygon by the given 2d transform
	// Includes scale, rotate, and translate.
	public void transform(Transform t)
	{
		for (int i = 0; i < nPoints; i++)
		{
			int x = xPoints[i];
			int y = yPoints[i];
			xPoints[i] = (int) (t.a * x + t.b * y + t.c);
			yPoints[i] = (int) (t.d * x + t.e * y + t.f);
		}
	}		

	
	// Draw this shape on the given graphics output
	public void drawShape(Graphics g)
	{
		int xOld = 0;
		int yOld = 0;
		boolean start = true;
		for (int i = 0; i < nLines; i++)
		{
			int nPoint = nLine[i];
			if (nPoint == -1)
				start = true;
			else
			{
				int xNew = xPoints[nPoint];
				int yNew = yPoints[nPoint];
				if (start == false)
					g.drawLine(xOld, yOld, xNew, yNew);
				xOld = xNew;
				yOld = yNew;
				start = false;
			}
		}
	}
}




// JavaLander
//-------------------------------------------------------------------------------------------------------
public class JavaLander extends Applet implements KeyListener, Runnable
{
	Thread thisThread;

	float timeStep = 0.1f; // seconds
	int delay = 100; // milliseconds
	
	World world = new World();
	
	// Keyboard 'flags'
	float rdelta = 0.0f;
	float rdeltaamount = 0.2f;
	int throttle = 0;
	int throttleamount = 10;
		
	public void init()
	{
		// Initialize world and all the sprites it contains
		world.init(getSize().width, getSize().height);

		setBackground(Color.white);
		setForeground(Color.black);
//		setBackground(Color.black);
//		setForeground(Color.white);

		this.addKeyListener(this);
		
		thisThread = new Thread(this);
		thisThread.start();
	}
	
	public void keyPressed(KeyEvent e)
	{
		switch (e.getKeyCode())
		{
			case KeyEvent.VK_LEFT:
				rdelta = -rdeltaamount;
				break;			
			case KeyEvent.VK_RIGHT:
				rdelta = rdeltaamount;
				break;			
			case KeyEvent.VK_UP:
				throttle = throttleamount;
				break;
			case KeyEvent.VK_DOWN:
				throttle = -throttleamount;
				break;
		}		
	}
	
	public void keyReleased(KeyEvent e)
	{
		switch (e.getKeyCode())
		{
			case KeyEvent.VK_LEFT:
			case KeyEvent.VK_RIGHT:
				rdelta = 0.0f;
				break;			
			case KeyEvent.VK_UP:
			case KeyEvent.VK_DOWN:
				throttle = 0;
				break;
		}		
	}
	
	public void keyTyped(KeyEvent e)
	{
	}


	// Run thread
	public void run()
	{
		while (true)
		{
			// Adjust ship rotation and throttle 
			world.ship.rotate(rdelta);
			world.ship.setThrottle(throttle);
			
			// Advance sprites
			world.step(timeStep);
			
			// Pause
			try
			{
				thisThread.sleep(delay);
			}
			catch(InterruptedException ex) {}
			
			// Repaint display
			repaint();
		}
	}

	// Draw world
	public void paint(Graphics g)
	{
		world.draw(g);
	}
	
}


