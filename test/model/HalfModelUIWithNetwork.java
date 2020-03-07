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


@SuppressWarnings("serial")
public class HalfModelUIWithNetwork extends JFrame implements ActionListener {

	
	/**********************************************************************
	 * For testing non-output components, disable this. Doing so prevents *
	 * PinController from being initialized, and prevents the drivers for *
	 * GPIO pins from being loaded (which most computers don't have)      *
	 **********************************************************************/
	private static final boolean RUNNING_ON_PI = false;

	private ServerSocket server;
	private Socket connection;
	private BufferedReader input;
	private long timeout = 0;
	private JLabel status; //get reference to connection status
	
	private JPanel motorLeft, motorRight;
	private JButton quit;
	private ModelWindow leftMotorWindow, rightMotorWindow;
	private HalfModel model;
	
	private Timer tick;
	
	private static final int WIDTH = 800;
	private static final int HEIGHT = 450;
	
	public static void main(String[] args) throws IOException {
		HalfModelUIWithNetwork test = new HalfModelUIWithNetwork();
		test.run();
	}
	
	public void run() throws IOException {
		
		setTitle("Half-H Bridge Model Test");
		setSize(WIDTH,HEIGHT);
		setLocation(0,0);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		
		setLayout(new GridLayout(1, 3));
		
		model = new HalfModel(RUNNING_ON_PI); //Init model
		
		
		model.invertLeft(true);
		
		motorLeft = new JPanel();
		leftMotorWindow = new ModelWindow((WIDTH / 3), HEIGHT);
		leftMotorWindow.isLeftMotor(true);
		motorLeft.add(leftMotorWindow);
		
		motorRight = new JPanel();
		rightMotorWindow = new ModelWindow((WIDTH / 3), HEIGHT);
		rightMotorWindow.isLeftMotor(false);
		motorRight.add(rightMotorWindow);
		
		JPanel quitPnl = new JPanel();
		quitPnl.setLayout(new GridLayout(2,1));
		quit = new JButton("Exit");
		quit.addActionListener(this);
		quitPnl.add(quit);
		
		status = new JLabel("Status: Waiting for connection...", SwingConstants.CENTER);
		quitPnl.add(status);
		
		//If the window is closed in a non-standard way, still clear the pin states
		addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {
				shutdown();
				super.windowClosing(e);
			}
				
		});
		
		add(motorLeft);
		add(motorRight);
		add(quitPnl);
		setVisible(true); //TODO: add "waiting for connection"
		
		leftMotorWindow.createAndSetBuffer();
		rightMotorWindow.createAndSetBuffer();

		startServerAndWaitForConnection();		
		status.setText("Status: Connected!");
		
		
		tick = new Timer(17, this);
		tick.setRepeats(true);
		tick.start();
	}

	private void startServerAndWaitForConnection() throws IOException {
		
		int port = -1;
		do {
			try {
				port = Integer.parseInt(JOptionPane.showInputDialog("Enter Port Number","3333"));
			}
			catch(NumberFormatException e) {
				port = -1;
			}
			if(port < 0 || port > 65535)
				port = -1;
		} while(port == -1);
		
		server = new ServerSocket(port);
		
		try {
			connection = server.accept();
		}
		catch(SocketException e) {
			/*If this exception was thrown, the program was shut down while waiting
			for the client to connect. This just means that the server let us know
			an error occurred - nothing to worry about. */
		}
		
		if(connection != null) {
			InputStream rawInput = connection.getInputStream();
			input = new BufferedReader(new InputStreamReader(rawInput));
		}
	}

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
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource().equals(tick)) {
			
			model.update();
			leftMotorWindow.update();
			rightMotorWindow.update();
			
			
			//If there is information readable inside the stream...
			String line = null;
			try {
				line = input.readLine();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			
			//Now, parse the information
			if(line != null) {
				timeout = 0;
				model.updateKBStateOnDirectCall(SBAssist.atob(line));
				System.out.println(line); //debug
			}
			else
				++timeout;
			
			
			/*If we determined that the connection has been closed, time out. We'll say that if the client hasn't
			 * sent us any data in 300 ticks (where 60 ticks = 1 second), then we're good to stop.
			 */
			if(timeout > 300) { //TODO: Why does this not call when the client closes the socket?
				tick.stop(); //Stop updating the model
				model.updateKBStateOnDirectCall(new boolean[] {false, false, false, false});
				status.setText("Status: Disconnected.");
				JOptionPane.showMessageDialog(null, "Connection closed by client...");
				
			}
		}
		else if(e.getSource().equals(quit)) {
			shutdown();
			System.exit(0);
		}
	}
	
	private class ModelWindow extends DoubleBufferedCanvas {

		private boolean left; //Use the left (true) or right (false) motor
		
		public ModelWindow(int width, int height) {
			super(width, height);
		}
		
		public void isLeftMotor(boolean left) {
			this.left = left;
		}

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