package model;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.Timer;

import halfModel.HalfModel;
import ui.DoubleBufferedCanvas;
import util.SBAssist;

/**
 * GUI Controller / Server for the A.R.G.U.S system. This could probably be split up into
 * two classes, with the server itself being another model as part of the MVC.
 * 
 * @author J2579
 */
@SuppressWarnings("serial")
public class HalfModelUIWithNetwork extends JFrame implements ActionListener {

	
	/**********************************************************************
	 * For testing non-output components, disable this. Doing so prevents *
	 * PinController from being initialized, and prevents the drivers for *
	 * GPIO pins from being loaded (which most computers don't have)      *
	 **********************************************************************/
	private static final boolean RUNNING_ON_PI = false;

	private ServerSocket server; //Server
	private Socket connection; //Socket connected to the client
	private BufferedReader input; //Read in data from client. Encapsulates the raw bytestream
	private boolean end = false; //observe connection
	private JLabel status; //get reference to connection status
	
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
		int port = parsePort(args);
		test.setup(port);
	}
	
	/**
	 * Given the command-line arguments, either attempts to parse a port from a JOptionPane (0 arg),
	 * or read directly from the command line (1 arg). If the user enters an invalid port in the JOptionPane,
	 * they are prompted to re-input; However, an invalid cmd arg causes the program to exit with an error message.
	 * 
	 * @param args The command-line arguments passed into main()
	 * @return The specified port number to listen for connections on
	 */
	private static int parsePort(String[] args) {

		int port = -1;
		
		if(args.length == 0) { //
			String portstr; //scoping
			do {
				portstr = JOptionPane.showInputDialog("Enter Port Number","3333");
			} while((port = validatePort(portstr)) == -1);	
		}
		else if(args.length == 1) {
			if((port = validatePort(args[0])) == -1)
				usage();
		}
		else
			usage();
		
		return port;
	}

	/**
	 * Validates a port number. If the string is a number n,
	 * where 0 <= n < 65536, the number is returned as an integer.
	 * Otherwise, returns -1.
	 * 
	 * @param portstr The port to validate
	 * @return The parsed port number, if valid. Otherwise, returns -1 on failure.
	 */
	private static int validatePort(String portstr) {
		
		int port;
		
		try {
			port = Integer.parseInt(portstr); 
		}
		catch(NumberFormatException e) {
			return -1;
		}
		if(port < 0 || port > 65535)
			port = -1;
		
		return port;
	}

	/**
	 * Initializes the Frame, Graphics Window, and Server Connection
	 * @param port Port to listen for a client connection on
	 * @throws IOException If an IOError occurs when creating the server
	 */
	public void setup(int port) throws IOException {
		
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
		quit.addActionListener(this);
		quitPnl.add(quit);
		
		status = new JLabel("Status: Waiting for connection...", SwingConstants.CENTER);
		quitPnl.add(status);
		
		//Shutdown behavior
		addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {
				shutdown();
				super.windowClosing(e);
			}
				
		});
		
		add(motorLeft); //Add elements to the frame
		add(motorRight);
		add(quitPnl);
		setVisible(true); 
		
		leftMotorWindow.createAndSetBuffer(); //Finalize graphics
		rightMotorWindow.createAndSetBuffer();

		startServerAndWaitForConnection(port); //Set up server	
		status.setText("Status: Connected!");
		
		
		tick = new Timer(17, this); //Event tick
		tick.setRepeats(true);
		tick.start();
	}

	/**
	 * Starts the server, and waits for a piece of Client code to make the connection.
	 * The InputStream is then wrapped in a BufferedReader because bytes are hard
	 * 
	 * @param port The port to listen for a connection on
	 * @throws IOException If an I/O ERROR occurs when setting up the Socket connection
	 */
	private void startServerAndWaitForConnection(int port) throws IOException {

		server = new ServerSocket(port);
		
		try {
			connection = server.accept(); //BLOCKING!!!
		}
		catch(SocketException e) {
			/*If this exception was thrown, the program was shut down while waiting
			for the client to connect. This just means that the server let us know
			an error occurred when trying to create the connection- nothing to worry 
			about! */
		}
		
		if(connection != null) {
			InputStream rawInput = connection.getInputStream();
			input = new BufferedReader(new InputStreamReader(rawInput));
		}
	}

	/**
	 * Shuts down the electronic pins, as well as the server connection.
	 */
	private void shutdown() {
		model.shutdownController();
		
		try {
			if(server != null)
				server.close();
			if(connection != null)
				connection.close();
		}
		catch(IOException e) { /*...*/ }
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
			
			
			//Update connection - READ
			
			String line = null; //Get input from client
			try {
				line = input.readLine();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			
			//Is the client sending us information?
			if(line != null) {
				
				if(line.startsWith("!")) { //special information header
					if(line.substring(1).equals("END_CONNECTION")) //Client closed connection
						end = true;
				}
				else //Client just sent tread movement data
					model.updateKBStateOnDirectCall(SBAssist.atob(line));
				
			}
			
			//Shutdown the timer, and set all pins to off if the connection has been closed
			if(end) {
				tick.stop(); //Stop updating the model
				model.updateKBStateOnDirectCall(new boolean[] {false, false, false, false});
				status.setText("Status: Disconnected.");
				JOptionPane.showMessageDialog(null, "Connection closed by client...");
				
			}
		}
		
		else if(e.getSource().equals(quit)) { //Exit button
			shutdown();
			System.exit(0);
		}
	}
	
	/**
	 * Instantiation of DoubleBufferedCanvas. Made for easily drawing motor data to the screen.
	 * @author i99sh
	 *
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