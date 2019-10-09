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

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;

import halfModel.HalfModel;
import ui.DoubleBufferedCanvas;
import util.Helper;


@SuppressWarnings("serial")
public class HalfModelUITest extends JFrame implements KeyListener, ActionListener {

	
	/**********************************************************************
	 * For testing non-output components, disable this. Doing so prevents *
	 * PinController from being initialized, and prevents the drivers for *
	 * GPIO pins from being loaded (which most computers don't have)      *
	 **********************************************************************/
	private static final boolean RUNNING_ON_PI = false;

	private JPanel motorLeft, motorRight;
	private ModelWindow leftMotorWindow, rightMotorWindow;
	private HalfModel model;
	
	private Timer tick;
	
	private static final int WIDTH = 800;
	private static final int HEIGHT = 450;
	
	public static void main(String[] args) {
		HalfModelUITest test = new HalfModelUITest();
		test.run();
	}
	
	public void run() {
		
		setTitle("Half-H Bridge Model Test");
		setSize(WIDTH,HEIGHT);
		setLocation(0,0);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		
		setLayout(new GridLayout(1, 2));
		
		model = new HalfModel(RUNNING_ON_PI); //Init model
		
		
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

	@Override
	public void keyPressed(KeyEvent e) {
		model.updateKBStateOnKeyPress(e);
	}

	
	@Override
	public void keyReleased(KeyEvent e) {
		model.updateKBStateOnKeyRelease(e);
	}
	
	@Override
	public void keyTyped(KeyEvent e) { return; }
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource().equals(tick)) {
			
			model.update();
			leftMotorWindow.update();
			rightMotorWindow.update();
			
//			System.out.println(Helper.printBoolArr(model.getMotorState()[0]) + "," + Helper.printBoolArr(model.getMotorState()[1]));
			
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
				
				g.fillRect(side * (getWidth()/2) + 15, 0, getWidth() - 15, getHeight() - 100);
			}
			
		}
		
	}
}