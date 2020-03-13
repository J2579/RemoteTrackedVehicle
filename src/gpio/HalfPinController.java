package gpio;

import java.util.ArrayList;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiBcmPin;
import com.pi4j.io.gpio.RaspiGpioProvider;
import com.pi4j.io.gpio.RaspiPinNumberingScheme;

/**
 * Controls direct GPIO pin enable / disable.
 * 14, 15: M1
 * 18, 23: M2
 * 24: Enable M1
 * 25: Enable M2
 * @author J2579
 */
public class HalfPinController {

	private GpioController controller;
	private ArrayList<GpioPinDigitalOutput> pins;
	private GpioPinDigitalOutput enableLeft, enableRight;
	
	/**
	 * Shuts down all active pins.
	 */
	public void shutdown() {
		for(int idx = 0; idx < pins.size(); ++idx) { //Motor Pins
			pins.get(idx).low();
		}
		
		enableLeft.low(); //Enable Pins
		enableRight.low();
		controller.shutdown(); //pi4j internal shutdown
	}
	
	/**
	 * Sets up the six pins, as well as the controller.
	 */
	public HalfPinController() {
		
		GpioFactory.setDefaultProvider(new RaspiGpioProvider(RaspiPinNumberingScheme.BROADCOM_PIN_NUMBERING));
		controller = GpioFactory.getInstance();
	
		GpioPinDigitalOutput m1p1 = controller.provisionDigitalOutputPin(RaspiBcmPin.GPIO_14, PinState.LOW);
		GpioPinDigitalOutput m1p2 = controller.provisionDigitalOutputPin(RaspiBcmPin.GPIO_15, PinState.LOW);
		GpioPinDigitalOutput m2p1 = controller.provisionDigitalOutputPin(RaspiBcmPin.GPIO_18, PinState.LOW);
		GpioPinDigitalOutput m2p2 = controller.provisionDigitalOutputPin(RaspiBcmPin.GPIO_23, PinState.LOW);
	
		enableLeft = controller.provisionDigitalOutputPin(RaspiBcmPin.GPIO_24, PinState.LOW);
		enableRight = controller.provisionDigitalOutputPin(RaspiBcmPin.GPIO_25, PinState.LOW);
		
		enableLeft.high(); //Both motors start enable
		enableRight.high();
		
		pins = new ArrayList<GpioPinDigitalOutput>(); //Data Structures PogChamp
		
		pins.add(m1p1); 
		pins.add(m1p2);
		pins.add(m2p1);
		pins.add(m2p2);
	}
	
	/**
	 * Update the state of each of the electronic pins.
	 * 
	 * @param motorState Data structure holding data for motor pins.
	 * @param leftEnabled Is the left motor enabled?
	 * @param rightEnabled Is the right motor enabled?
	 */
	public void updatePins(boolean[][] motorState, boolean leftEnabled, boolean rightEnabled) {
		
		//Update motor pins from motorState
		for(int idx = 0; idx < pins.size(); ++idx) {
			boolean state = motorState[idx / 2][idx % 2];
			
			if(state)
				pins.get(idx).high();
			else
				pins.get(idx).low();
		}
		
		//Update 'enable' pins from enable state.
		if(leftEnabled)
			enableLeft.high();
		else
			enableLeft.low();
		
		if(rightEnabled)
			enableRight.high();
		else
			enableRight.low();
	}
}
