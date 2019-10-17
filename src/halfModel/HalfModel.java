package halfModel;

import java.awt.event.KeyEvent;

import gpio.HalfPinController;

public class HalfModel {

	private boolean leftHeld, rightHeld, upHeld, downHeld;
	private HalfBridge leftMotor, rightMotor;
	private HalfPinController controller;
	private boolean updatePins;
	
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
	
	public void updateKBStateOnKeyPress(KeyEvent e) {
		int keycode = e.getKeyCode();
		
		if(keycode == KeyEvent.VK_W)
			setUpHeld(true);
		else if(keycode == KeyEvent.VK_S)
			setDownHeld(true);
		else if(keycode == KeyEvent.VK_A)
			setLeftHeld(true);
		else if(keycode == KeyEvent.VK_D)
			setRightHeld(true);
	}
	
	public void updateKBStateOnKeyRelease(KeyEvent e) {
		int keycode = e.getKeyCode();
		
		if(keycode == KeyEvent.VK_W)
			setUpHeld(false);
		else if(keycode == KeyEvent.VK_S)
			setDownHeld(false);
		else if(keycode == KeyEvent.VK_A)
			setLeftHeld(false);
		else if(keycode == KeyEvent.VK_D)
			setRightHeld(false);
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
	 * For easy debug of returned boolean array
	 */
	public String[] getMotorStateString() {
		return new String[] {leftMotor.toString(),rightMotor.toString()};
	}
	
	/**
	 * Is the model working?
	 */
	@Override
	public String toString() {
		return "Left: " + leftHeld + ", Right: " + rightHeld + ", Down: " + 
				downHeld + ", Up: " + upHeld; 
	}

	public void setLeftHeld(boolean leftHeld) {
		this.leftHeld = leftHeld;
	}

	public void setRightHeld(boolean rightHeld) {
		this.rightHeld = rightHeld;
	}

	public void setUpHeld(boolean upHeld) {
		this.upHeld = upHeld;
	}

	public void setDownHeld(boolean downHeld) {
		this.downHeld = downHeld;
	}
	
	public void invertLeft(boolean invert) {
		this.leftMotor.setIsInverted(invert);
	}
	
	public void invertRight(boolean invert) {
		this.rightMotor.setIsInverted(invert);
	}
}
