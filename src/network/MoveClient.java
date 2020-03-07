package network;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.net.*;
import java.io.*;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.Timer;

import ui.DoubleBufferedCanvas;
import util.SBAssist;

@SuppressWarnings("serial")
public class MoveClient extends JFrame implements KeyListener, ActionListener {

	private DrawWindow gfx;
	private Timer tick;
	
	private static String host;
	private static int port;
	private static Socket connection;
	private static PrintWriter output;
	
	private boolean[] model = new boolean[4]; //too lazy to write an actual class
	private static final int LEFT = 0;
	private static final int UP = 1;
	private static final int RIGHT = 2;
	private static final int DOWN = 3;
	
	private static void usage() {
		System.err.println("usage: MoveClient <ip> <port>");
		System.exit(-1);
	}
	
	public static void main(String[] args) throws IOException {
		
		try { //Parse port 
			if(args.length != 2) { usage(); }
			host = args[0];
			port = Integer.parseInt(args[1]);
		} catch(Exception e) { usage(); }
				
		connection = new Socket(host, port);
		OutputStream rawOutput = connection.getOutputStream();
		output = new PrintWriter(rawOutput, true);			
			
		MoveClient jsc = new MoveClient();
		jsc.run();
	}
	
	private void run() {
		setSize(400,500);
		setTitle("Movement Client");
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		
		gfx = new DrawWindow(400, 400);
		add(gfx);
		setVisible(true);
		gfx.createAndSetBuffer();
		
		JButton focus = new JButton("...");
		add(focus);
		
		addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {
				
				try {
					connection.close();
					System.out.println("Connection closed.");
				}
				catch(IOException ioe) {/*...*/}
				
				super.windowClosing(e);
			}
			
		});
		
		addKeyListener(this);
		
		
		tick = new Timer(17, this);
		tick.setRepeats(true);
		tick.start();

		requestFocus();
	}
	

	@Override
	public void actionPerformed(ActionEvent ae) {
		if(ae.getSource().equals(tick)) {
			gfx.update();

			//Send the information to the server's outputstream
			output.println(SBAssist.btoa(model));
		}
	}

	public void keyPressed(KeyEvent e) {
		int keycode = e.getKeyCode();
		
		if(keycode == KeyEvent.VK_W)
			model[UP] = true;
		else if(keycode == KeyEvent.VK_S)
			model[DOWN] = true;
		else if(keycode == KeyEvent.VK_A)
			model[LEFT] = true;
		else if(keycode == KeyEvent.VK_D)
			model[RIGHT] = true;
	}
	
	public void keyReleased(KeyEvent e) {
		int keycode = e.getKeyCode();
		
		if(keycode == KeyEvent.VK_W)
			model[UP] = false;
		else if(keycode == KeyEvent.VK_S)
			model[DOWN] = false;
		else if(keycode == KeyEvent.VK_A)
			model[LEFT] = false;
		else if(keycode == KeyEvent.VK_D)
			model[RIGHT] = false;
	}
	
	@Override
	public void keyTyped(KeyEvent e) {}
	
	private class DrawWindow extends DoubleBufferedCanvas {

		public DrawWindow(int width, int height) {
			super(width, height);
		}

		@Override
		public void draw(Graphics g) {
			g.setColor(Color.GREEN);
			
			if(model[UP])
				g.fillRect(133, 0, 133, 133);
			if(model[DOWN])
				g.fillRect(133, 400-133, 133, 133);
			if(model[LEFT])
				g.fillRect(0, 400-133-133, 133, 133);
			if(model[RIGHT])
				g.fillRect(400-133, 400-133-133, 133, 133);
		}
	}
}