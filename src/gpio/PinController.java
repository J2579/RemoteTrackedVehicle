package gpio;

import java.util.ArrayList;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;

public class PinController {

	private GpioController controller = GpioFactory.getInstance();
	private ArrayList<GpioPinDigitalOutput> pins;

	public void shutdown() {
		controller.shutdown();
	}
	
	public PinController() {
		
		GpioPinDigitalOutput b1s1 = controller.provisionDigitalOutputPin(RaspiPin.GPIO_14, PinState.LOW);
		GpioPinDigitalOutput b1s2 = controller.provisionDigitalOutputPin(RaspiPin.GPIO_15, PinState.LOW);
		GpioPinDigitalOutput b1s3 = controller.provisionDigitalOutputPin(RaspiPin.GPIO_18, PinState.LOW);
		GpioPinDigitalOutput b1s4 = controller.provisionDigitalOutputPin(RaspiPin.GPIO_23, PinState.LOW);
		GpioPinDigitalOutput b2s1 = controller.provisionDigitalOutputPin(RaspiPin.GPIO_24, PinState.LOW);
		GpioPinDigitalOutput b2s2 = controller.provisionDigitalOutputPin(RaspiPin.GPIO_25, PinState.LOW);
		GpioPinDigitalOutput b2s3 = controller.provisionDigitalOutputPin(RaspiPin.GPIO_08, PinState.LOW);
		GpioPinDigitalOutput b2s4 = controller.provisionDigitalOutputPin(RaspiPin.GPIO_07, PinState.LOW);
		
		pins.add(b1s1);
		pins.add(b1s2);
		pins.add(b1s3);
		pins.add(b1s4);
		pins.add(b2s1);
		pins.add(b2s2);
		pins.add(b2s3);
		pins.add(b2s4);
	}

	public void updatePins(boolean[][] motorState) {
		for(int idx = 0; idx < 8; ++idx) {
			boolean state = motorState[idx / 4][idx % 4];
			
			if(state)
				pins.get(idx).high();
			else
				pins.get(idx).low();
		}
		
	}

}
