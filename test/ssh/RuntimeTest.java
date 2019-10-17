package ssh;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.io.IOException;

public class RuntimeTest {
	
	public static void main(String[] args) throws InterruptedException, IOException, AWTException {
		Robot rob;
		Runtime r = Runtime.getRuntime();
		Process p;

		p = r.exec("notepad.exe");
		rob = new Robot();
		
		rob.keyPress(KeyEvent.VK_H);
		Thread.sleep(150);
		rob.keyPress(KeyEvent.VK_E);
		Thread.sleep(150);
		rob.keyPress(KeyEvent.VK_L);
		Thread.sleep(150);
		rob.keyPress(KeyEvent.VK_L);
		Thread.sleep(150);
		rob.keyPress(KeyEvent.VK_O);
		
		Thread.sleep(500);
		p.destroy();
	}
}
