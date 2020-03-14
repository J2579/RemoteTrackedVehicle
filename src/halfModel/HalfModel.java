package halfModel;

import java.awt.event.KeyEvent;

import gpio.HalfPinController;
import util.SBAssist;

/**
 * Interfaces two motors with the current keyboard state.
 * 
 * @author J2759
 */
public class HalfModel {

	/** Keyboard State */
	private boolean leftHeld, rightHeld, upHeld, downHeld;
	
	/** Motor Logic */
	private HalfBridge leftMotor, rightMotor;
	
	/** Enables / Disables GPIO pins */
	private HalfPinController controller;
	
	/**
	 * If the code is being run on an A.R.G.U.S unit, then this value will be set to 'true', and
	 * the electrical components of the system will update on every tick. Otherwise, the electronics
	 * will not initialize.
	 */
	private boolean updatePins; 
	
	private static final int LEFT = 0;
	private static final int UP = 1;
	private static final int RIGHT = 2;
	private static final int DOWN = 3;
	
	/**
	 * Shuts down the electronic pins, if the program is running on an A.R.G.U.S System
	 * (e.g. not testing basic functionality on other hardware)
	 */
	public void shutdownController() {
		if(updatePins) 
			controller.shutdown();
	}
	
	/**
	 * Sets up the model, and the two bridges. If the program is not running
	 * on an A.R.G.U.S system (set by updatingPins), the electrical components /
	 * controllers will not initialize
	 * @param updatingPins Are there electronic pins connected to the program to update?
	 */
	public HalfModel(boolean updatingPins) {
	
		leftHeld = false;
		rightHeld = false;
		upHeld = false;
		downHeld = false;
		
		leftMotor = new HalfBridge(true); //Inverted
		rightMotor = new HalfBridge(false);
		
		this.updatePins = updatingPins;
		if(updatePins) 
			controller = new HalfPinController();
	}
	
	/**
	 * Updates the keyboard state based on a stringified boolean array,
	 * send by the client.
	 * 
	 * @param statestr The boolean array representing the client's keyboard state
	 * @throws IllegalAr
	 */
	public void updateKBStateOnDirectCall(String statestr) {
	
		boolean[] state = SBAssist.atob(statestr);
		
		if(state.length != 4)
			throw new IllegalArgumentException();
		
		leftHeld = state[LEFT];
		rightHeld = state[RIGHT];
		upHeld = state[UP];
		downHeld = state[DOWN];
	}
	
	/**
	 * Sets the keyboard state to all false
	 */
	public void clearAll() {
		leftHeld = false;
		rightHeld = false;
		upHeld = false;
		downHeld = false;
	}
	
	/**
	 * Updates the keyboard state based on Key Input. Used for server-side functionality
	 * testing without a client.
	 * @param e KeyEvent generated from key release
	 */
	public void updateKBStateOnKeyPress(KeyEvent e) {
		int keycode = e.getKeyCode();
		
		if(keycode == KeyEvent.VK_W)
			upHeld = true;
		else if(keycode == KeyEvent.VK_S)
			downHeld = true;
		else if(keycode == KeyEvent.VK_A)
			leftHeld = true;
		else if(keycode == KeyEvent.VK_D)
			rightHeld = true;
	}
	
	/**
	 * Updates the keyboard state based on Key Input. Used for server-side functionality
	 * testing without a client.
	 * @param e KeyEvent generated from keypress
	 */
	public void updateKBStateOnKeyRelease(KeyEvent e) {
		int keycode = e.getKeyCode();

		if(keycode == KeyEvent.VK_W)
			upHeld = false;
		else if(keycode == KeyEvent.VK_S)
			downHeld = false;
		else if(keycode == KeyEvent.VK_A)
			leftHeld = false;
		else if(keycode == KeyEvent.VK_D)
			rightHeld = false;
	}
	
	/**
	 * Updates the bridges respective to the state of the keyboard.
	 */
	public void update() {
		
		if((leftHeld && rightHeld) || (upHeld && downHeld)) { //Contradictory input
			leftMotor.stop();
			rightMotor.stop();
		}
		else if(leftHeld && !rightHeld && !upHeld && !downHeld) { //Only left
			leftMotor.setBackward(); //Left track reverse, right track fw
			rightMotor.setForward();
		}
		else if(!leftHeld && rightHeld && !upHeld && !downHeld) { //Only right
			leftMotor.setForward(); //Right track reverse, left track fw
			rightMotor.setBackward();
		}
		else if(!leftHeld && !rightHeld && upHeld && !downHeld) { //Only up
			leftMotor.setForward();
			rightMotor.setForward();
		}
		else if(!leftHeld && !rightHeld && !upHeld && downHeld) { //Only down
			leftMotor.setBackward();
			rightMotor.setBackward();
		}
		else if(rightHeld && (upHeld || downHeld)) {
			leftMotor.stop();
			rightMotor.setBackward();
		}
		else if(leftHeld && (upHeld || downHeld)) {
			leftMotor.setBackward();
			rightMotor.stop();
		}
		else { //No input
			leftMotor.stop();
			rightMotor.stop();
		}
		
		if(updatePins)
			controller.updatePins(getMotorState(), 
					leftMotor.getIsEnabled(), rightMotor.getIsEnabled());
	}
	
	/**
	 * Returns the boolean pin-state of each bridge in the model
	 * @return The state of each bridge in the model
	 */
	public boolean[][] getMotorState() {
		return new boolean[][] {leftMotor.getMotorState(), rightMotor.getMotorState()};
	}
	
	/** 
	 * A string representation of both motors in the model.
	 * 
	 * @return a String array of both motor's toString() call
	 */
	public String[] getMotorStateString() {
		return new String[] {leftMotor.toString(),rightMotor.toString()};
	}
	
	/**
	 * Set the 'inverted' property the right motor of the model.
	 * 
	 * @param invert Inverted state to set
	 */
	public void invertLeft(boolean invert) {
		this.leftMotor.setIsInverted(invert);
	}
	
	/**
	 * Set the 'inverted' property the right motor of the model.
	 * 
	 * @param invert Inverted state to set
	 */
	public void invertRight(boolean invert) {
		this.rightMotor.setIsInverted(invert);
	}
	
	/**
	 * Returns a string representation of the model's keyboard state.
	 * 
	 * @return The model's keyboard state.
	 */
	@Override
	public String toString() {
		return "Left: " + leftHeld + ", Right: " + rightHeld + ", Down: " + 
				downHeld + ", Up: " + upHeld; 
	}
}
