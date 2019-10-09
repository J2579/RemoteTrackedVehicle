package halfModel;

public class HalfBridge {

	private boolean inverted;
	private boolean[] state;
	private boolean enabled;
	
	public HalfBridge(boolean inverted) {
		
		enabled = true;
		
		this.inverted = inverted;
		state = new boolean[] {false, false};
		stop();
	}
	
	public void setIsInverted(boolean b) {
		this.inverted = b;
	}
	
	public boolean getIsInverted() {
		return inverted;
	}
	
	public void stop() {
		state[0] = false;
		state[1] = false;
	}
	
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
	
	public boolean[] getMotorState() {
		return state;
	}
	
	public String toString() {
		
		String stateString, invertedString, enabledString;
		
		if(!state[0] && state[1])
			stateString = "Forward ";
		else if(state[0] && !state[1])
			stateString = "Backward ";
		else
			stateString = "Stopped ";
		
		invertedString = inverted ? " (Inverted) " : " (Normal) ";
		
		enabledString = enabled ? " (Enabled)" : " (Disabled)";
		
		return stateString + invertedString + enabledString;
	}

	public void setIsEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	
	public boolean getIsEnabled() {
		return enabled;
	}
}