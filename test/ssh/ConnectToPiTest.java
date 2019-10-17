package ssh;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.io.IOException;

import javax.swing.JOptionPane;

public class ConnectToPiTest {
	
	public static void typeWord(String word, Robot rob) throws InterruptedException {
		
		char[] wordArr = word.toCharArray();
		boolean upperCase = false;
		int keyCode;
		
		for(int idx = 0; idx < wordArr.length; ++idx) {
			
			keyCode = KeyEvent.getExtendedKeyCodeForChar(wordArr[idx]);
			
			if(Character.isUpperCase(wordArr[idx])) //Hold Shift
				rob.keyPress(KeyEvent.VK_SHIFT);
			
			rob.keyPress(keyCode);
			Thread.sleep(155);
			rob.keyRelease(keyCode);
			
			if(Character.isUpperCase(wordArr[idx])) //Release Shift
				rob.keyRelease(KeyEvent.VK_SHIFT);
		}
	}

	public static void main(String[] args) throws IOException, InterruptedException, AWTException {
		
		String pw;
		
		do {
			pw = JOptionPane.showInputDialog("Machine Password: ");
		}
		while(pw == null || pw.length() == 0);
		
		Robot rob;
		Runtime r = Runtime.getRuntime();
		Process p;
		
		try {
			p = r.exec("putty -load Pi");
		}
		catch(IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		Thread.sleep(300); //Let the process create itself
		
		rob = new Robot();
		
		//Login
		typeWord("pi", rob);
		rob.keyPress(KeyEvent.VK_ENTER);
		typeWord(pw, rob);
		rob.keyPress(KeyEvent.VK_ENTER);
		
		//Navigate to folder
		typeWord("cd Desktop", rob);
		rob.keyPress(KeyEvent.VK_ENTER);
		
		Thread.sleep(500);
	}
}
