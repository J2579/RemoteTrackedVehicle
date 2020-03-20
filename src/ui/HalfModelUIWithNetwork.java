package ui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.InputStream;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.Timer;

import halfModel.HalfModel;
import network.Server;

/**
 * GUI Controller / Server for the A.R.G.U.S system. This could probably be split up into
 * two classes, with the server itself being another model as part of the MVC.
 * 
 * @author J2579
 */
@SuppressWarnings("serial")
public class HalfModelUIWithNetwork extends JFrame implements ActionListener, KeyListener {

	
	/**********************************************************************
	 * For testing non-output components, disable this. Doing so prevents *
	 * PinController from being initialized, and prevents the drivers for *
	 * GPIO pins from being loaded (which most computers don't have)      *
	 **********************************************************************/
	private static final boolean RUNNING_ON_PI = true;
	
	/*******************************************************************
	 * If we want to test functionality of A.R.G.U.S without a client, *
	 * then we can set this to 'true' to directly control the motor    *
	 * state from the program.										   *
	 *******************************************************************/
	private static final boolean FORCE_DISABLE_CONNECTION = false;

	private Server server;
	private JLabel status; //get reference to connection status
	public static final int WRITE_SIZE = 16384;
	
	private InputStream cameraDataStream;
	
	private JPanel motorLeft, motorRight; //Panel to hold pin gfx
	private JButton quit; //Exit
	private ModelWindow leftMotorWindow, rightMotorWindow; //Graphical representation of pin states
	private HalfModel model; //Logic for pin state
	
	private Timer tick; //Event tick
	
	private static final int WIDTH = 800;  //Window width
	private static final int HEIGHT = 450; //Window height
	
	/**
	 * 'Usage' message displayed when the user inputs an invalid number
	 * of command-line arguments.
	 */
	private static void usage() {
		System.err.println("usage: Server <port>\nusage: Server");
		System.exit(1);
	}
	
	/**
	 * Main method. Initializes the Server connection as well as the GUI Frame.
	 * @param args args[0] = Port to host server on.
	 * @throws IOException If an I/O error occurs while creating the server
	 */
	public static void main(String[] args) throws IOException {
		HalfModelUIWithNetwork test = new HalfModelUIWithNetwork();
		test.setup(args);
	}

	/**
	 * Initializes the Frame, Graphics Window, and Server Connection
	 * @param port Port to listen for a client connection on
	 * @throws IOException If an IOError occurs when creating the server
	 */
	public void setup(String[] args) throws IOException {
		
		//Set up server
		server = new Server();
		
		if(!FORCE_DISABLE_CONNECTION) {
			if(!server.setUpPortFromCMDArgs(args)) //port
				usage();
		}
		
		
		setTitle("Half-H Bridge Model Test"); //Frame Properties
		setSize(WIDTH,HEIGHT);
		setLocation(0,0);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setLayout(new GridLayout(1, 3));
		
		model = new HalfModel(RUNNING_ON_PI); //Electronic Model
		model.invertLeft(true);
		
		motorLeft = new JPanel(); //Electronic Model Graphics
		leftMotorWindow = new ModelWindow((WIDTH / 3), HEIGHT);
		leftMotorWindow.isLeftMotor(true);
		motorLeft.add(leftMotorWindow);
		
		motorRight = new JPanel();
		rightMotorWindow = new ModelWindow((WIDTH / 3), HEIGHT);
		rightMotorWindow.isLeftMotor(false);
		motorRight.add(rightMotorWindow);
		
		JPanel quitPnl = new JPanel(); //More Frame Components
		quitPnl.setLayout(new GridLayout(2,1));
		quit = new JButton("Exit");
		quit.addActionListener(new QuitListener());
		quitPnl.add(quit);
		
		status = new JLabel("Status: Waiting for connection...", SwingConstants.CENTER);
		quitPnl.add(status);
		
		//Shutdown behavior
		addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {
				shutdown(0);
				super.windowClosing(e);
			}
				
		});
		
		add(motorLeft); //Add elements to the frame
		add(motorRight);
		add(quitPnl);
		setVisible(true); 
		
		leftMotorWindow.createAndSetBuffer(); //Finalize graphics
		rightMotorWindow.createAndSetBuffer();
		
		//MOST RECENT CHANGE: PLACED BEFORE SERVER START TO CLEAR OUT SYSTEM.IN
		tick = new Timer(17, this); //Event tick
		tick.setRepeats(true);
		tick.start();
		
		if(!FORCE_DISABLE_CONNECTION) {
			System.out.println(3);
			if(!server.startServerAndWaitForConnection()) //Set up server	
				shutdown(1);
			status.setText("Status: Connected!");
		}
		else {
			addKeyListener(this);
			status.setText("Status: Connection Disabled [DEBUG]");
			requestFocus();
		}
		
		beginCameraTick(); //Wait for the server to link before starting the camera
	}

	private void beginCameraTick() throws IOException {
		Runtime r = Runtime.getRuntime();
		Process p = r.exec("raspivid -t 0 -w 300 -h 300 -o -");
		cameraDataStream = p.getInputStream();
	}

	/**
	 * Shuts down the electronic pins, as well as the server connection.
	 * @param code The exit code.
	 */
	private void shutdown(int code) {
		model.shutdownController();
		server.shutdown();
		System.exit(code);
	}
	
	/**
	 * Updates the model on key-press directly. Used for debugging.
	 */
	@Override
	public void keyPressed(KeyEvent e) {
		model.updateKBStateOnKeyPress(e);
	}

	/**
	 * Updates the model on key release directly. Used for debugging.
	 */
	@Override
	public void keyReleased(KeyEvent e) {
		model.updateKBStateOnKeyRelease(e);
	}

	/**
	 * Required by interface.
	 */
	@Override
	public void keyTyped(KeyEvent e) {}
	
	
	private class QuitListener implements ActionListener { //System.in.read() is blocking, thus we									   //need more than 2 threads
		public void actionPerformed(ActionEvent e) {	   //need an additional thread
			shutdown(0);
		}
	}
	
	/**
	 * Updates the graphics, model, and connection on tick.
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource().equals(tick)) {
			
			model.update(); //Update pin model
			leftMotorWindow.update(); //Update graphics
			rightMotorWindow.update();
			
			//Do we have information to send to the client?
			byte[] cameraData = new byte[WRITE_SIZE];
			int numRead = -1;
			
			if(!FORCE_DISABLE_CONNECTION && server.isConnected()) {
				
				//Recieve movement information from the client
				String line = server.getNextLine(); //Get input from client
				if(line != null) 
					model.updateKBStateOnDirectCall(line);
				
				//Send video information back into the client
				try {
					numRead = cameraDataStream.read(cameraData); //Read data when the program starts, but discard it.
				} catch (IOException e1) {			             //this is a side-effect of piping [TODO: Fix with
					e1.printStackTrace();				         //pipe-by-PID?]
				}
				if(numRead != -1) 
					server.write(cameraData,numRead);
				
				//If the connection has been closed, shutdown the timer, and set all pins to off.
				if(server.getIsStopped()) {
					tick.stop(); //Stop updating the model
					model.clearAll();
					status.setText("Status: Disconnected.");
					JOptionPane.showMessageDialog(null, "Connection closed by client...");
				}
			}
		}
	}
	
	/**
	 * Instantiation of DoubleBufferedCanvas. Made for easily drawing motor data to the screen.
	 * 
	 * @author J2579
	 */
	private class ModelWindow extends DoubleBufferedCanvas {

		private boolean left; //Use the left (true) or right (false) motor
		
		public ModelWindow(int width, int height) {
			super(width, height);
		}
		
		public void isLeftMotor(boolean left) {
			this.left = left;
		}

		/**
		 * Draw the state of the motor pins to the screen
		 */
		@Override
		public void draw(Graphics g) {
			
			g.setColor(Color.WHITE);
			g.fillRect(0, 0, getWidth(), getHeight()); //White out screen
			g.setColor(Color.BLACK);
			

			boolean[] bridgeState;
			
			//Status text / Setup
			if(left) {
				g.drawString(model.getMotorStateString()[0], 100, 400);
				bridgeState = model.getMotorState()[0];
			}
			else { //right
				g.drawString(model.getMotorStateString()[1], 100, 400);
				bridgeState = model.getMotorState()[1];
			}
			
			//Fill in the gates with the appropriate markings
			for(int side = 0; side <= 1; ++side) {
				if(bridgeState[side])
					g.setColor(Color.GREEN);
				else
					g.setColor(Color.RED);
				
				g.fillRect(side * (getWidth()/2), 0, getWidth(), getHeight() - 100);
			}
		}
		
	}
}