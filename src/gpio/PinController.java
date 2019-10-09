package gpio;

import java.util.ArrayList;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiBcmPin;
import com.pi4j.io.gpio.RaspiGpioProvider;
import com.pi4j.io.gpio.RaspiPinNumberingScheme;

public class PinController {

	private GpioController controller; 
	private ArrayList<GpioPinDigitalOutput> pins;

	public void shutdown() {
		
		for(int idx = 0; idx < 8; ++idx) {
			pins.get(idx).low();
		}
		
		controller.shutdown();
	}
	
	public PinController() {
		
		//BCM ---> GPIO
		GpioFactory.setDefaultProvider(new RaspiGpioProvider(RaspiPinNumberingScheme.BROADCOM_PIN_NUMBERING));
		
		//Create instance from factory
		controller = GpioFactory.getInstance();
		
		//Configure output pins
		GpioPinDigitalOutput b1s1 = controller.provisionDigitalOutputPin(RaspiBcmPin.GPIO_14, PinState.LOW);
		GpioPinDigitalOutput b1s2 = controller.provisionDigitalOutputPin(RaspiBcmPin.GPIO_15, PinState.LOW);
		GpioPinDigitalOutput b1s3 = controller.provisionDigitalOutputPin(RaspiBcmPin.GPIO_18, PinState.LOW);
		GpioPinDigitalOutput b1s4 = controller.provisionDigitalOutputPin(RaspiBcmPin.GPIO_23, PinState.LOW);

		GpioPinDigitalOutput b2s1 = controller.provisionDigitalOutputPin(RaspiBcmPin.GPIO_24, PinState.LOW);
		GpioPinDigitalOutput b2s2 = controller.provisionDigitalOutputPin(RaspiBcmPin.GPIO_25, PinState.LOW);
		GpioPinDigitalOutput b2s3 = controller.provisionDigitalOutputPin(RaspiBcmPin.GPIO_08, PinState.LOW);
		GpioPinDigitalOutput b2s4 = controller.provisionDigitalOutputPin(RaspiBcmPin.GPIO_07, PinState.LOW);
		
		//Init the list
		pins = new ArrayList<GpioPinDigitalOutput>();
		
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
			
			System.out.print(idx + ": " + state + " | ");
			
			if(state)
				pins.get(idx).high();
			else
				pins.get(idx).low();
			
			
		}
		System.out.println();

	}

}
