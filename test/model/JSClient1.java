package model;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JFrame;
import javax.swing.Timer;

import ui.DoubleBufferedCanvas;

@SuppressWarnings("serial")
public class JSClient1 extends JFrame implements KeyListener, ActionListener {

	private DrawWindow gfx;
	private Timer tick;
	
	private boolean[] model = new boolean[4]; //too lazy to write an actual class
	private static final int LEFT = 0;
	private static final int UP = 1;
	private static final int RIGHT = 2;
	private static final int DOWN = 3;
	
	public static void main(String[] args) {
		JSClient1 jsc = new JSClient1();
		jsc.run();
	}
	
	private void run() {
		setSize(400,450);
		setTitle("Movement Client");
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		
		gfx = new DrawWindow(400, 400);
		add(gfx);
		setVisible(true);
		gfx.createAndSetBuffer();
		
		tick = new Timer(17, this);
		tick.setRepeats(true);
		tick.start();
		
		addKeyListener(this);
		requestFocus();
	}
	

	@Override
	public void actionPerformed(ActionEvent ae) {
		if(ae.getSource().equals(tick))
			gfx.update();
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