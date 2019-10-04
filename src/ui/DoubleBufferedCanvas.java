package ui;


import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;



/**
* Acts as an easy implementation of a 
* double-buffer to an AWT Canvas. The only abstract method is 
* 'draw', which contains the specific commands to draw desired 
* objects to the screen.
*
* @author J2579
* @version 1.0.2
* @since 2019-07-21
*/

public abstract class DoubleBufferedCanvas extends Canvas {
	/** Serial UID */
	private static final long serialVersionUID = 1L;

	/** Front Buffer */
	private BufferStrategy frontBuffer;
	
	/** Back Buffer */
	private BufferedImage backBuffer;
	
	/** Front and Back-Buffer GFX */
	private Graphics frontBufferGFX,backBufferGFX;
	
	/** Canvas Size */
	private int width,height;

	/**
	* This constructor sets up the canvas to the proper size, and 
	* creates a back buffer from the size.
	*
	* @param width The width of the canvas.
	* @param height The height of the canvas.
	*/
	public DoubleBufferedCanvas(int width, int height) {
		//set up the canvas.
		setBackground(Color.WHITE);
		setIgnoreRepaint(true);
		this.width = width;
		this.height = height;
		
		setSize(width, height);
		setVisible(true);
	}
		
	/**
	 * Returns the width of the canvas
	 * 
	 * @return The width of the canvas
	 */
	public int getWidth() {
		return width;
	}
	
	/**
	 * Returns the height of the canvas
	 * 
	 * @return The height of the canvas
	 */
	public int getHeight() {
		return height;
	}
	
	/**
	* Takes the canvas, and creates and sets 
	* its BufferStrategy.
	* 
	* @precondition Canvas MUST be added to its container before this method is called! 
	*/
	public void createAndSetBuffer() {
		try {
			//Create a buffer strategy.
			createBufferStrategy(2);
			
			//Set the Front-buffer.
			frontBuffer = getBufferStrategy();
			
			//Create a compatible Image
			Image compatibleImage = super.createImage(this.width,this.height);
			
			//Set the Back-Buffer
			backBuffer = (BufferedImage)compatibleImage;
		}
		catch(IllegalStateException ise) {
			System.out.print("Canvas must be added to JFrame before createAndSetBuffer() can be called!");
			System.exit(-1);
		}
	}
		
	/**
	* Overrides the 'repaint' function normally called
	* by the Canvas whenever the frame is moved or resized - Instead,
	* it now calls our desired repaint method.
	*
	* @param g The Graphics to draw on.
	*/
	
	public void repaint(Graphics g) {
		//If repaint is called automatically, force the program to use our method
		this.update();
	}
	
	/**
	* This abstract method draws the graphics to the back-buffer.
	* 
	* @param g Graphics to draw on.
	*/
	public abstract void draw(Graphics g);

	/**
	* Update the screen, and should be called on every 
	* program 'tick'. This method involves using Double-Buffering, where an image
	* is drawn to memory (offscreen), and the memory is pasted onto the Canvas.
	*/
	
	public void update()
	{
		//Get the Graphics of the Back-Buffer, a BufferedImage.
		backBufferGFX = backBuffer.createGraphics(); 
			
		//Overwrite the visuals of the Back-Buffer with a black box.
		backBufferGFX.setColor(Color.BLACK);
		backBufferGFX.fillRect(0,0,width,height);
			
		//Use the abstract method 'draw' to edit the image on the Back-Buffer.
		draw(backBufferGFX);
			
		//Create the Graphics of the Front-Buffer.
		frontBufferGFX = frontBuffer.getDrawGraphics(); 
			
		//Draw the Back-Buffer to the Front-Buffer's Graphics.
		frontBufferGFX.drawImage(backBuffer,0,0,null);
			
		//If the Front-Buffer didn't mess up, 'show' the Front Buffer on the Canvas.
		if(!frontBuffer.contentsLost())
			frontBuffer.show(); 
		
		//Dump the Front and Back-Buffer's Graphics.
		if(frontBufferGFX != null) 
			frontBufferGFX.dispose();
		if(backBufferGFX != null) 
			backBufferGFX.dispose();
	}
}