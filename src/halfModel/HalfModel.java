package halfModel;

import java.awt.event.KeyEvent;

import gpio.HalfPinController;

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
	
	public void shutdownController() {
		if(updatePins) 
			controller.shutdown();
	}
	
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
	 * Throws IAE if state.length != 4
	 * @param state State to set model to.
	 */
	public void updateKBStateOnDirectCall(boolean[] state) {
		
		if(state.length != 4)
			throw new IllegalArgumentException();
		
		leftHeld = state[LEFT];
		rightHeld = state[RIGHT];
		upHeld = state[UP];
		downHeld = state[DOWN];
	}
	
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
	
	public void updateKBStateOnKeyRelease(KeyEvent e) {
		int keycode = e.getKeyCode();
		
		if(keycode == KeyEvent.VK_W)
			
		if(keycode == KeyEvent.VK_W)
			upHeld = false;
		else if(keycode == KeyEvent.VK_S)
			downHeld = false;
		else if(keycode == KeyEvent.VK_A)
			leftHeld = false;
		else if(keycode == KeyEvent.VK_D)
			rightHeld = false;
	}
	
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
