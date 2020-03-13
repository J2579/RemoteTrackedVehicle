package halfModel;

/**
 * Converts basic motor movement commands (stop, forward, backward)
 * into the appropriate state for the two controlling pins for the 
 * physical Half-Bridge. PinController reads these inputs, converting them
 * into electrical impulses.
 * 
 * @author J2579
 */
public class HalfBridge {

	private boolean inverted; //Is the motor inverted? (swap FW and BW)
	private boolean[] state; //State of the associated pins
	private boolean enabled; //Is the motor enabled?
	
	/**
	 * Initializes the half-bridge. A half-bridge is stopped and enabled by default
	 * 
	 * @param inverted Should the motor initialize as inverted?
	 */
	public HalfBridge(boolean inverted) {
		
		enabled = true; //A motor should start as enabled by default.
		this.inverted = inverted;
		state = new boolean[] {false, false};
	}
	
	
	/**
	 * Stops the motor. Pins set to LOW/LOW
	 */
	public void stop() {
		state[0] = false;
		state[1] = false;
	}
	
	/**
	 * Motor moves forward. Pins set to LOW/HIGH
	 * 
	 * If the bridge is inverted, pins set to HIGH/LOW
	 */
	public void setForward() {
		
		if(!inverted) {
			state[0] = false;
			state[1] = true;
		}
		else {
			state[0] = true;
			state[1] = false;
		}
	}
	
	/**
	 * Motor moves forward. Pins set to HIGH/LOW
	 * 
	 * If the bridge is inverted, pins set to LOW/HIGH
	 */
	public void setBackward() {
		if(!inverted) {
			state[0] = true;
			state[1] = false;
		}
		else {
			state[0] = false;
			state[1] = true;
		}
	}
	
	/**
	 * Returns the state of the pins. Read by PinController and converted
	 * to analog input to the motors.
	 * 
	 * @return The current state of the pins.
	 */
	public boolean[] getMotorState() {
		return state;
	}
	
	/**
	 * String representation of the bridge.
	 * 
	 * Stylized:
	 * MOVEMENT STATE (INVERSION STATE) (ENABLED STATE)
	 * 
	 * Movement State: FORWARD | BACKWARD | STOPPED
	 * Inversion State: INVERTED | NORMAL
	 * Enabled State: ENABLED | DISABLED
	 * 
	 * @return A string representation of the bridge.
	 */
	public String toString() {
		
		String stateString, invertedString, enabledString;
		
		if(!state[0] && state[1])
			stateString = "FORWARD ";
		else if(state[0] && !state[1])
			stateString = "BACKWARD ";
		else
			stateString = "STOPPED ";
		
		invertedString = inverted ? " (INVERTED) " : " (NORMAL) ";
		
		enabledString = enabled ? " (Enabled)" : " (DISABLED)";
		
		return stateString + invertedString + enabledString;
	}

	/**
	 * Enable / Disable the bridge.
	 * 
	 * @param enabled true - bridge is enabled; false - bridge is disabled
	 */
	public void setIsEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	
	/**
	 * Returns the "enabled" state of the bridge.
	 * 
	 * @return true - bridge is enabled; false - bridge is disabled
	 */
	public boolean getIsEnabled() {
		return enabled;
	}
	
	/**
	 * Sets the 'inverted' property of the bridge to 'b'
	 * 
	 * @param b State that the 'inverted' property should be set to.
	 */
	public void setIsInverted(boolean b) {
		this.inverted = b;
	}
	
	/**
	 * Returns the 'inverted' property of the bridge
	 * 
	 * @return true - Bridge is inverted, false - Bridge is not inverted
	 */
	public boolean getIsInverted() {
		return inverted;
	}
}