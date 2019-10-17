package ssh;

import java.awt.Robot;
import java.awt.event.KeyEvent;

import javax.swing.JOptionPane;

public class RuntimeIOTest {
	
	public static void main(String[] args) throws Exception {
		
		char[] word;
		
		do {
			word = JOptionPane.showInputDialog("Input: ").toCharArray();
		}
		while(word == null || word.length == 0);
		
		Robot rob;
		Runtime r = Runtime.getRuntime();
		Process p;
		
		
		p = r.exec("notepad.exe");
		Thread.sleep(1000); //Let the process create itself
		
		rob = new Robot();
		
		for(int idx = 0; idx < word.length; ++idx) {
			rob.keyPress(KeyEvent.getExtendedKeyCodeForChar(word[idx]));
			Thread.sleep(155);
		}
		
		Thread.sleep(500);
		p.destroy();
	}
}
