package network;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.Timer;

import ui.DoubleBufferedCanvas;
import util.SBAssist;

/**
 * Passes keyboard information from the client to the A.R.G.U.S server, which is decoded and
 * translated into electrical impulses to the motors.
 * 
 * @author J2579
 */
@SuppressWarnings("serial")
public class Client extends JFrame implements KeyListener, ActionListener {

	private DrawWindow gfx; //Graphical Window
	private Timer tick; //Window / Key Event Update Timer
	
	private static String host; //IP Address of server to connect to
	private static int port; //Port to connect to server on
	private static Socket connection; //Socket to read/write data from
	private static PrintWriter output, log; //Wraps the socket, allowing String I/O
	private static InputStream input;
	public static final int READ_SIZE = 16384;
	
	private static FileOutputStream fs;
	
	private boolean[] model = new boolean[4]; //Keyboard state
	private static final int LEFT = 0; //...
	private static final int UP = 1;
	private static final int RIGHT = 2;
	private static final int DOWN = 3;
	public static final byte[] EXIT_HEADER = new byte[] {65, 108, 101, 120, 97, 32, 84, 97, 115, 99, 104, 101, 114};
	
	/**
	 * Called if user specifies invalid number of command-line args.
	 */
	private static void usage() {
		System.err.println("usage: MoveClient <ip> <port>");
		System.exit(-1);
	}
	
	/**
	 * Parses IP and Port from command line args, then attempts to create a socket to
	 * connect with the A.R.G.U.S server. Fails if the server is not currently running.
	 * 
	 * @param args args[0] = IP of server, args[1] = port
	 * @throws IOException Signifies very bad things happened
	 */
	public static void main(String[] args) throws IOException {
		
		try { //Parse port 
			if(args.length != 2) { usage(); }
			host = args[0];
			port = Integer.parseInt(args[1]);
			connection = new Socket(host, port);
		} catch(NumberFormatException e) { usage(); } //Port not a number
		catch(ConnectException ce) { //Server not running
			System.err.println("Could not connect to server...Check your connection, and try again.");
			System.exit(1);
		}
		catch(UnknownHostException ue) { //IP address not an IP address
			System.err.println("Bad / Malformed IP Address: " + host);
			System.exit(1);
		}
		catch(IllegalArgumentException iae) { // Port < 0 || Port > 65535
			System.err.println("Invalid Port: " + port);
			System.exit(1);
		}
		
		OutputStream rawOutput = connection.getOutputStream(); //Get the Socket's stream...
		output = new PrintWriter(rawOutput, true); //...and wrap it in a PrintWriter
		input = connection.getInputStream();	
		
		fs = new FileOutputStream("test.h264");
		FileOutputStream lograw = new FileOutputStream("test.log");
		log = new PrintWriter(lograw, true);
		
		Client jsc = new Client();
		jsc.setupGraphics();
	}
	
	/**
	 * Configures the graphics / key-bindings of the Movement Client
	 */
	private void setupGraphics() {
		setSize(400,500); //Frame
		setTitle("Movement Client");
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		
		gfx = new DrawWindow(400, 400); //Window
		add(gfx);
		setVisible(true);
		gfx.createAndSetBuffer();
		
		JButton focus = new JButton("..."); //Something click-able to refocus the window.
		add(focus);
		
		addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {
				
				try {
					shutdown();
					System.out.println("Connection closed.");
				}
				catch(IOException ioe) {/*...*/}
				
				super.windowClosing(e);
			}
		});
		
		addKeyListener(this);
		
		
		tick = new Timer(17, this); //Write to socket / Update window on timer. 1000ms/s * tick/17ms ~ 60 tick/s
		tick.setRepeats(true);
		tick.start();

		requestFocus();
	}
	

	private void shutdown() throws IOException{
		output.println("!END_CONNECTION"); //Tell server we dc'd.
		connection.close();
		fs.close();
		log.close();
	}
	
	/**
	 * Updates the model / connection on event tick.
	 */
	@Override
	public void actionPerformed(ActionEvent ae) {
		if(ae.getSource().equals(tick)) {
			gfx.update(); //Update graphical representation of keys.
			output.println(SBAssist.btoa(model)); //Send the keyboard state to the server.
			
			//Get the image data from the server.
			byte[] data = new byte[READ_SIZE];
			try {
				int numRead = input.read(data);
				if(!Arrays.equals(Arrays.copyOf(data, EXIT_HEADER.length), EXIT_HEADER)) {
					if(numRead != -1)
						fs.write(data, 0, numRead); //Temp
					log.println(data + ": " + numRead + " bytes.");
				}
				else {
					shutdown();
					JOptionPane.showMessageDialog(null, "Connection closed by Server.");
					System.exit(0);
				}
			} catch(IOException e) { //lazy catch-all
				e.printStackTrace();
			}
		}
	}

	/**
	 * Updates keyboard state on key press.
	 */
	@Override
	public void keyPressed(KeyEvent e) {
		int keycode = e.getKeyCode();
		
		if(keycode == KeyEvent.VK_W)
			model[UP] = true;
		else if(keycode == KeyEvent.VK_S)
			model[DOWN] = true;
		else if(keycode == KeyEvent.VK_A)
			model[LEFT] = true;
		else if(keycode == KeyEvent.VK_D)
			model[RIGHT] = true;
	}
	
	/**
	 * Updates keyboard state on key release.
	 */
	@Override
	public void keyReleased(KeyEvent e) {
		int keycode = e.getKeyCode();
		
		if(keycode == KeyEvent.VK_W)
			model[UP] = false;
		else if(keycode == KeyEvent.VK_S)
			model[DOWN] = false;
		else if(keycode == KeyEvent.VK_A)
			model[LEFT] = false;
		else if(keycode == KeyEvent.VK_D)
			model[RIGHT] = false;
	}
	
	/**
	 * Does nothing.
	 */
	@Override
	public void keyTyped(KeyEvent e) {}
	
	/**
	 * Implementation of DoubleBufferedCanvas. Draw method displays green
	 * squares in the cardinal directions which correspond to keyboard presses.
	 * 
	 * @author J2579
	 */
	private class DrawWindow extends DoubleBufferedCanvas {

		public DrawWindow(int width, int height) {
			super(width, height);
		}

		/**
		 * Assume NWSE = WASD. Key presses are illuminated by green squares,
		 * BG is default black.
		 */
		@Override
		public void draw(Graphics g) {
			g.setColor(Color.GREEN);
			
			if(model[UP])
				g.fillRect(133, 0, 133, 133);
			if(model[DOWN])
				g.fillRect(133, 400-133, 133, 133);
			if(model[LEFT])
				g.fillRect(0, 400-133-133, 133, 133);
			if(model[RIGHT])
				g.fillRect(400-133, 400-133-133, 133, 133);
		}
	}
}