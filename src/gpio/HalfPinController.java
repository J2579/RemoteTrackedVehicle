package gpio;

import java.util.ArrayList;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiBcmPin;
import com.pi4j.io.gpio.RaspiGpioProvider;
import com.pi4j.io.gpio.RaspiPinNumberingScheme;

public class HalfPinController {

	private GpioController controller;
	private ArrayList<GpioPinDigitalOutput> pins;
	private GpioPinDigitalOutput enableLeft, enableRight;
	
	public void shutdown() {
		for(int idx = 0; idx < pins.size(); ++idx) {
			pins.get(idx).low();
		}
		
		enableLeft.low();
		enableRight.low();
		
		controller.shutdown();
	}
	
	public HalfPinController() {
		
		GpioFactory.setDefaultProvider(new RaspiGpioProvider(RaspiPinNumberingScheme.BROADCOM_PIN_NUMBERING));
		controller = GpioFactory.getInstance();
	
		GpioPinDigitalOutput m1p1 = controller.provisionDigitalOutputPin(RaspiBcmPin.GPIO_14, PinState.LOW);
		GpioPinDigitalOutput m1p2 = controller.provisionDigitalOutputPin(RaspiBcmPin.GPIO_15, PinState.LOW);
		GpioPinDigitalOutput m2p1 = controller.provisionDigitalOutputPin(RaspiBcmPin.GPIO_18, PinState.LOW);
		GpioPinDigitalOutput m2p2 = controller.provisionDigitalOutputPin(RaspiBcmPin.GPIO_23, PinState.LOW);
	
		enableLeft = controller.provisionDigitalOutputPin(RaspiBcmPin.GPIO_24, PinState.LOW);
		enableRight = controller.provisionDigitalOutputPin(RaspiBcmPin.GPIO_25, PinState.LOW);
		
		enableLeft.high();
		enableRight.high();
		
		pins = new ArrayList<GpioPinDigitalOutput>();
		
		pins.add(m1p1);
		pins.add(m1p2);
		pins.add(m2p1);
		pins.add(m2p2);
	}
	
	public void updatePins(boolean[][] motorState, boolean leftEnabled, boolean rightEnabled) {
		
		for(int idx = 0; idx < pins.size(); ++idx) {
			boolean state = motorState[idx / 2][idx % 2];
			
			if(state)
				pins.get(idx).high();
			else
				pins.get(idx).low();
		}
		
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
