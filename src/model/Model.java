package model;

import gpio.PinController;

public class Model {

	private boolean leftHeld, rightHeld, upHeld, downHeld;
	private HBridge leftMotor, rightMotor;
	private PinController controller;
	private boolean updatePins;
	
	public void shutdownController() {
		
		if(updatePins)
			controller.shutdown();
	}
	
	
	public Model(boolean updatePins) {
		leftHeld = false;
		rightHeld = false;
		upHeld = false;
		downHeld = false;
		
		leftMotor = new HBridge(true);
		rightMotor = new HBridge(false);
		
		this.updatePins = updatePins;
		
		if(this.updatePins)
			controller = new PinController(); //Init with default pins
	}
	
	public void update() {
		
		if((leftHeld && rightHeld) || (upHeld && downHeld)) { //Contradictory input
			leftMotor.stop();
			rightMotor.stop();
		}
		else if(leftHeld && !rightHeld && !upHeld && !downHeld) { //Only left
			leftMotor.setMovingBw(); //Left track reverse, right track fw
			rightMotor.setMovingFw();
		}
		else if(!leftHeld && rightHeld && !upHeld && !downHeld) { //Only right
			leftMotor.setMovingFw(); //Right track reverse, left track fw
			rightMotor.setMovingBw();
		}
		else if(!leftHeld && !rightHeld && upHeld && !downHeld) { //Only up
			leftMotor.setMovingFw();
			rightMotor.setMovingFw();
		}
		else if(!leftHeld && !rightHeld && !upHeld && downHeld) { //Only down
			leftMotor.setMovingBw();
			rightMotor.setMovingBw();
		}
		else if(rightHeld && (upHeld || downHeld)) {
			leftMotor.coast();
			rightMotor.setMovingBw();
		}
		else if(leftHeld && (upHeld || downHeld)) {
			leftMotor.setMovingBw();
			rightMotor.coast();
		}
		else { //No input
			leftMotor.stop();
			rightMotor.stop();
		}
		
		if(updatePins)
			controller.updatePins(getMotorState());
	}
	
	public boolean[][] getMotorState() {
		return new boolean[][]{leftMotor.getBridgeState(),rightMotor.getBridgeState()};
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
		this.leftMotor.invert(invert);
	}
	
	public void invertRight(boolean invert) {
		this.rightMotor.invert(invert);
	}
}