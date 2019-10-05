package model;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;

import ui.DoubleBufferedCanvas;

@SuppressWarnings("serial")
public class ModelUITest extends JFrame implements KeyListener, ActionListener {

	
	private static final boolean RUNNING_ON_PI = false;
	
	private BufferedImage bridge;
	
	private JPanel motorLeft, motorRight;
	private ModelWindow leftMotorWindow, rightMotorWindow;
	private Model model;
	
	private Timer tick;
	
	private static final int WIDTH = 800;
	private static final int HEIGHT = 450;
	
	public static void main(String[] args) {
		ModelUITest test = new ModelUITest();
		test.run();
	}
	
	public void run() {
		
		loadBridge();
		
		setTitle("H Bridge Model Test");
		setSize(WIDTH,HEIGHT);
		setLocation(0,0);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		
		setLayout(new GridLayout(1, 3));
		
		model = new Model(RUNNING_ON_PI); //Init model
		
		
		model.invertLeft(true);
		
		motorLeft = new JPanel();
		leftMotorWindow = new ModelWindow(WIDTH / 2, HEIGHT);
		leftMotorWindow.isLeftMotor(true);
		motorLeft.add(leftMotorWindow);
		
		motorRight = new JPanel();
		rightMotorWindow = new ModelWindow(WIDTH / 2, HEIGHT);
		rightMotorWindow.isLeftMotor(false);
		motorRight.add(rightMotorWindow);
		
		add(motorLeft);
		add(motorRight);
		setVisible(true);
		
		leftMotorWindow.createAndSetBuffer();
		rightMotorWindow.createAndSetBuffer();
		
		addKeyListener(this);
		
		//Cleanup code on close
		addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {
				model.shutdownController();
				super.windowClosing(e);
			}
			
		});
		
		tick = new Timer(17, this);
		tick.setRepeats(true);
		tick.start();
	}

	private void loadBridge() {
		try {
			URL bridgeUrl = ModelUITest.class.getResource("/EMPTY_BRIDGE.png");
			bridge = ImageIO.read(bridgeUrl);
		}
		catch(IOException e) {
			System.out.print("Unable to load assets. Check installation and try again.");
			bridge = null;
		}
	}

	@Override
	public void keyPressed(KeyEvent e) {
		int keycode = e.getKeyCode();
		
		if(keycode == KeyEvent.VK_W)
			model.setUpHeld(true);
		else if(keycode == KeyEvent.VK_S)
			model.setDownHeld(true);
		else if(keycode == KeyEvent.VK_A)
			model.setLeftHeld(true);
		else if(keycode == KeyEvent.VK_D)
			model.setRightHeld(true);
	}

	
	@Override
	public void keyReleased(KeyEvent e) {
		int keycode = e.getKeyCode();
		
		if(keycode == KeyEvent.VK_W)
			model.setUpHeld(false);
		else if(keycode == KeyEvent.VK_S)
			model.setDownHeld(false);
		else if(keycode == KeyEvent.VK_A)
			model.setLeftHeld(false);
		else if(keycode == KeyEvent.VK_D)
			model.setRightHeld(false);
	}
	
	@Override
	public void keyTyped(KeyEvent e) { return; }
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource().equals(tick)) {
			
			model.update();
			leftMotorWindow.update();
			rightMotorWindow.update();
			
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
			
			g.drawImage(bridge, 0, 0, null); //Draw the bridge
			
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
			if(bridgeState[0]) //s1
				g.drawLine(190, 150, 190, 75);
			else
				g.drawLine(190, 150, 210, 75);
			
			if(bridgeState[1]) //s3
				g.drawLine(190, 325, 190, 205);
			else
				g.drawLine(190, 325, 210, 205);
			
			if(bridgeState[2]) //s2
				g.drawLine(375, 150, 375, 75);
			else
				g.drawLine(375, 150, 390, 75);
			
			if(bridgeState[3]) //s4
				g.drawLine(375, 325, 375, 205);
			else
				g.drawLine(375, 325, 390, 205);
			
			
		}
		
	}
}