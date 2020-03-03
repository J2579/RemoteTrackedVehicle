package video;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import javax.media.CannotRealizeException;
import javax.media.Manager;
import javax.media.NoPlayerException;
import javax.media.Player;

@SuppressWarnings("serial")
public class VideoTest extends JFrame {
	
	public void run() throws NoPlayerException, CannotRealizeException, IOException {
		setTitle("Video Playback Test");
		setSize(500,500);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		
		URL videoURL =  new File("C:\\Users\\i99sh\\Desktop\\rtvt.avi").toURI().toURL();
		Player mediaPlayer = Manager.createRealizedPlayer( videoURL );
		Component video = mediaPlayer.getVisualComponent();         
		add( video, BorderLayout.CENTER ); // add video component
		mediaPlayer.start();

		setVisible(true);
	}
	
	public static void main(String[] args) throws NoPlayerException, CannotRealizeException, IOException {
		VideoTest vt = new VideoTest();
		vt.run();
	}
}
