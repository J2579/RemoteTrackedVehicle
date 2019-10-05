package model;

/**
 * Models the expected behavior of an 'H-Bridge' motor driver
 * by controlling its four gates through human-readable
 * behavior functions.
 * 
 * @see <a href="https://en.wikipedia.org/wiki/H_bridge">https://en.wikipedia.org/wiki/H_bridge</a>
 * @author William DeStaffan
 */
public class HBridge {

	private boolean s1,s2,s3,s4,inverted;
	
	/**
	 * Initializes the HBridge with the motor
	 * stopped. If invert == true, then the motor
	 * will be inverted.
	 * @param invert True if the motor should be inverted.
	 */
	public HBridge(boolean invert) {
		invert(invert);
		stop();
	}
	
	/**
	 * Forces the motor to 'brake'
	 */
	public void stop() {
		
		if(!(s1 && !s2 && s3 && !s4)) { //Not already stopped
			coast(); //Reset the bridge
			s1 = true;
			s3 = true;
		}
	}

	/**
	 * Closes all terminals of the HBridge, causing the
	 * motor to coast by disconnecting the gate from the 
	 * circuit
	 */
	public void coast() {
		s1 = false;
		s2 = false;
		s3 = false;
		s4 = false;
	}
	
	/**
	 * Opens gate 1 and 4, sending current with positive
	 * voltage into the motor, moving it clockwise (forwards)
	 */
	public void setMovingFw() {

		if(inverted) {
			if(!(!s1 && s2 && s3 && !s4)) {
				coast();
				s2 = true;
				s3 = true;
			}
		}
		else {
			if(!(s1 && !s2 && !s3 && s4)) {
				coast();
				s1 = true;
				s4 = true;
			}
		}
	}
	
	/**
	 * Opens gate 2 and 3, sending current with positive
	 * voltage into the motor, moving it counterclockwise (backwards)
	 */
	public void setMovingBw() {
		
		if(inverted) {
			if(!(s1 && !s2 && !s3 && s4)) {
				coast();
				s1 = true;
				s4 = true;
			}
		}
		else {
			if(!(!s1 && s2 && s3 && !s4)) {
				coast();
				s2 = true;
				s3 = true;
			}
		}
	}
	
	/**
	 * Returns the state of each gate in an 0-indexed boolean array.
	 * e.g. arr[0] = gate 1, arr[1] = gate 2, etc.
	 */
	public boolean[] getBridgeState() {
		return new boolean[] {s1, s2, s3, s4};
	}

	/**
	 * Returns true if the motor is inverted, false otherwise
	 * @return If the motor is inverted (fw = bw)
	 */
	public boolean isInverted() {
		return inverted;
	}

	/**
	 * Inverts the motor once called. This resets
	 * the motor to coast, to avoid any short-circuit.
	 * @param inverted the inverted to set
	 */
	public void invert(boolean inverted) {
		
		if(this.inverted != inverted) { //Call to invert would actually do something
			this.inverted = inverted;
			coast(); //Reset bridge
		}
	}
	
	/**
	 * Returns a string representation of what the motor is doing
	 */
	@Override
	public String toString() {
		
		String string = "";
		
		if(s1 && s4)
			string +=  "Forward";
		else if(s2 && s3)
			string +=  "Backward";
		else if(s1 && s3) //Terminals are shorted
			string +=  "Stopped";
		else if((s1 && s2) || (s3 && s4)) //Motor is shorted
			string +=  "Bridge shorted. [This is bad!]";
		else
			string +=  "Neutral";
		
		string += " (" + (inverted ? "inverted" : "normal") + ")";
		
		return string;
	}
}