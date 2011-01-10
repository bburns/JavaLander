
// JavaLander.java

import java.awt.*;
import java.applet.*;
import java.awt.event.*;
// import java.awt.geom;


// Sprite
//-------------------------------------------------------------------------------------------------------
class Sprite
{
	World world;
	int mass;
	float x;
	float y;
	float vx;
	float vy;
	float ax;
	float ay;
	
//	public Sprite(World w) 
//	{
//		world = w;
//	}
	public void draw(Graphics g) {}
	public void step(float timeStep) {}
	public void init(World w) {}
	public void rotate(float rotation) {}
	
}


// Ship
//-------------------------------------------------------------------------------------------------------
class Ship extends Sprite
{
	float radiansPerDegree = 2.0f * (float) Math.PI / 360.0f; //. make global?
	float pixelsPerMeter = 40.0f / 20.0f; // 40 pixels for ship at about 20m high

	int massShip;			// kg
	int massFuel; 			// kg
	float orientation; 		// radians
	float rotationUnit;	// radians
	int throttle;				// 0 - 10. thrust = throttle * thrustunit
	float g;						// gravity (m/s/s) //. make part of world, dependent on height
	boolean outOfFuel;	// flag
	float burnRate;				// kg/s
	float exhaustVelocity;	// 
	float thrustUnit;
	
	Polygon polyNormal;	// describes ship shape
	Polygon polyRotated;	// ship shape after rotation
//	AffineTransform at;	// used to rotate ship shape

						  
    public void init(World w)
	{
		world = w;
		
//		g = 9.8; // m/s/s
		g = 1.0f; // m/s/s

		massShip = 1000; // kg
		massFuel = 5000; // kg
		mass = massShip + massFuel;
		orientation = - 90.0f * radiansPerDegree; // radians (pointing up)
		rotationUnit = 1.0f * radiansPerDegree; // degrees converted to radians
		exhaustVelocity = 250.0f; // m/s (very approx 1 mach)

//		burnRate = 1; // kg/s
		// calculate burnRate that will balance out gravity at throttle 5 and fuel tank half empty
		burnRate = (g * massShip + (massFuel / 2.0f)) / (5.0f * exhaustVelocity); // 2.8 kg/s for g=1m/s/s 
		thrustUnit = burnRate * exhaustVelocity; // kgm/s/s = newtons	
			
//		throttle = 1;
		
		x = 300.0f;
		y = 100.0f;
		vx = 0.0f;
		vy = 0.0f;
		
		polyNormal = new Polygon();
		
		polyNormal.addPoint(0, -25);
		polyNormal.addPoint(-10, 10);
		polyNormal.addPoint(-7, 1);
		polyNormal.addPoint(-21, 15);
		polyNormal.addPoint(-10, 10);

		polyNormal.addPoint(10, 10);
		polyNormal.addPoint(21, 15);
		polyNormal.addPoint(7, 1);
		polyNormal.addPoint(10, 10);
		polyNormal.addPoint(0, -25);

//		at = getRotateInstance(orientation);
//		polyRotated = new Polygon();
		polyRotated = polyNormal;
		polyRotated.translate((int) x, (int) y);
	}

	public void step(float timeStep)
	{
		// Get amount of fuel burned
		mass = massShip + massFuel;
		float fuelBurned = throttle * burnRate * timeStep;
		float thrust = throttle * thrustUnit;
		float thrustAccel = thrust / mass;
		
		// Move ship according to gravity, thrust, etc.
		ax = thrustAccel * (float) Math.cos(orientation);
		ay = thrustAccel * (float) Math.sin(orientation) + g;
		vx += ax * timeStep;
		vy += ay * timeStep;
		float dx = vx * timeStep * pixelsPerMeter;
		float dy = vy * timeStep * pixelsPerMeter;
		x += dx;
		y += dy;
		polyRotated.translate((int) dx, (int) dy);

		massFuel -= fuelBurned;
		if (massFuel < 0)
			outOfFuel = true;
		
	}

	public void draw(Graphics g)
	{
		// draw ship
		g.drawPolygon(polyRotated);
		
		// show stats
		String s = "Position: (" + x + ", " + y + ")";
		g.drawString(s, 0, 20);
		s = "Velocity: (" + vx + ", " + vy + ")";
		g.drawString(s, 0, 30);
		s = "Acceleration: (" + ax + ", " + ay + ")";
		g.drawString(s, 0, 40);
	}
	
	public void rotate(float rotation)
	{
//		orientation += rotation;	
	}
	
}



// World
//-------------------------------------------------------------------------------------------------------
class World
{
	public int nWorldWidth;
	public int nWorldHeight;
}


// View
//-------------------------------------------------------------------------------------------------------
class View
{
	public int nViewWidth;
	public int nViewHeight;

	public int xPos;
	public int yPos;
	
	public void draw(Graphics g)
	{
		// draw border around view
		g.drawRect(0, 0, nViewWidth - 1, nViewHeight - 1);
	}

}




// Land
//-------------------------------------------------------------------------------------------------------
class Land extends Sprite
{

	int nWidth;
	int nHeight;
	int nHillHeight;
	
	int nPoints = 20;
	
	int [] xPoints = new int[nPoints];
	int [] yPoints = new int[nPoints];
	
	public void init(World w)
	{	
		world = w;
		
		// set width 
		nWidth = world.nWorldWidth;
		nHeight = world.nWorldHeight;
		nHillHeight = nHeight / 5;
		
		// create horizon line
		for (int i = 0; i < nPoints; i++)
		{
			xPoints[i] = nWidth * i / (nPoints - 1);
			yPoints[i] = nHeight - (int) (Math.random() * nHillHeight);
		}
	}

	public void draw(Graphics g)
	{
		// draw horizon line
		g.drawPolyline(xPoints, yPoints, nPoints);
	}
}





// JavaLander
//-------------------------------------------------------------------------------------------------------
public class JavaLander extends Applet implements KeyListener, Runnable
{
	View view = new View();
	World world = new World();
	Land land = new Land();
	Ship ship = new Ship();
						 
	float timeStep = 0.05f; // seconds
	int delay = 25; // milliseconds
	
	Thread thisThread;
	
	public void init()
	{
//		this.showStatus("init applet!");

		this.addKeyListener(this);
		
		// Initialize view
		view.nViewWidth = getSize().width;
		view.nViewHeight = getSize().height;
		setBackground(Color.white);
		setForeground(Color.black);
		
		// Initialize world
		world.nWorldWidth = view.nViewWidth; // * 2;
		world.nWorldHeight = view.nViewHeight;
		
		// Initialize sprites
		land.init(world);
		ship.init(world);
		
		thisThread = new Thread(this);
		thisThread.start();
	}
	
	//. start and stop - pause and restart game
	public void start()
	{
//		this.showStatus("start applet!");
	}
	
	public void stop()
	{
//		this.showStatus("stop applet!");
	}
	
	public void destroy()
	{
//		this.showStatus("destroy applet!");
	}
	
	public void paint(Graphics g)
	{
//		this.showStatus("paint applet!");
//		g.drawString("First applet!", 10, 15);
//		g.draw3DRect(0, 0, 100, 20, true);
//		g.draw3DRect(20, 20, 400, 200, false);		
		// draw border around applet
//		g.drawRect(0, 0, xExtent - 1, yExtent - 1);
		
		if (view != null)
			view.draw(g);
		if (land != null)
			land.draw(g);
		if (ship != null)
			ship.draw(g);
	}

	
	public void step()
	{
		ship.step(timeStep);
		this.repaint();
	}
	
	public void keyPressed(KeyEvent e)
	{
//		this.showStatus("key pressed!");
		
		switch (e.getKeyCode())
		{
			case KeyEvent.VK_LEFT:
				ship.rotate(-0.2f);
//				this.repaint();
				break;			
			case KeyEvent.VK_UP:
				ship.throttle += 1;
				this.showStatus("throttle up to " + ship.throttle);
				break;
			case KeyEvent.VK_DOWN:
				ship.throttle -= 1;
				this.showStatus("throttle down to " + ship.throttle);
				break;
		}
		
	}
	
	public void keyReleased(KeyEvent e)
	{
	}
	
	public void keyTyped(KeyEvent e)
	{
	}

	
	// run thisThread!
	public void run()
	{
		while (true)
		{
			ship.step(timeStep);
			try
			{
				thisThread.sleep(delay);
			}
			catch(InterruptedException ex) {}
			repaint();
		}
	}
	
}


