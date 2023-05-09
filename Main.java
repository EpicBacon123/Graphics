import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import java.util.*;

public class Main {	
	public static void main(String[] args) {	    
		myJFrame frame = new myJFrame();	
		frame.show();
		frame.startGame();
	}
}
class myJFrame extends JFrame {
	myJPanel panel;
	public myJFrame() {
		setSize(new Dimension(850,600));
		setExtendedState(JFrame.MAXIMIZED_BOTH);  // stackoverflow - fullscreen
		setUndecorated(true);
		setVisible(true);

		//KeyListener
		KeyList KL = new KeyList();
		addKeyListener(KL);

		//MouseListener
		MouseList ML = new MouseList();
		addMouseListener(ML);
		
		//PANELS
		panel = new myJPanel(KL, this);
		Container container = getContentPane();
		container.add(panel);
		
		repaint();
		
	}
	public void startGame() {
		panel.startGame();
	}
	private class ExitListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			System.exit(0);
		}
	}

	class KeyList implements KeyListener {
		boolean up, down, left, right, enter, space;
		public void keyPressed(KeyEvent e) {
			switch(e.getKeyCode()) {
				case 27:	
				case KeyEvent.VK_E:
					System.exit(0);
					break;
			}
		}
		public void keyReleased(KeyEvent e) {}
		public void keyTyped(KeyEvent e) {
			Point mouse = MouseInfo.getPointerInfo().getLocation();
			switch (e.getKeyChar()) {
				case 'z':
				case 'Z':
				case 'x':
				case 'X':
					panel.notecheck(e);
					break;
			}
		}
	}
	class MouseList implements MouseListener {
		public void mousePressed(MouseEvent e) {}
		public void mouseReleased(MouseEvent e) {}
		public void mouseClicked(MouseEvent e) {
			panel.notecheck(e);
		}
		public void mouseEntered(MouseEvent e) {}
		public void mouseExited(MouseEvent e) {}
	}
	
	class myJPanel extends JPanel {
		KeyList KL;
		myJFrame frame;
		
		Note n;
		public myJPanel(KeyList KL1, myJFrame frame1) {
			KL = KL1;	
			frame = frame1;
			setBackground(Color.red);
			n = new Note(0, 0);
		}
		
		public void notecheck(MouseEvent e) {
			n.isHit(e.getX(), e.getY());
		}
		public void notecheck(KeyEvent e) {
			Point mouse = MouseInfo.getPointerInfo().getLocation();
			n.isHit((int)mouse.getX(), (int)mouse.getY());
		}

		public void startGame() {
			while(true) {
				repaint();
				try {
					Thread.sleep(5);
				}
				catch(Exception e) {}
			}
		}
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			int x = frame.getWidth();
			int y = frame.getHeight();
			
			g.setColor(new Color(230, 100, 200));
			g.fillRect(0, 0, x, y);


			if (!n.isHit)
				n.draw(g);
		}
	}

	class Note {
		static final int NOTE_SIZE = 80;
		static final int CIRCLE_SIZE = 80;
		int x;
		int y;
		int r;
		boolean isHit;

		public Note(int x1, int y1) {
			r = NOTE_SIZE;
			isHit = false;
			x = x1 + (r/2);
			y = y1 + (r/2);
		}

		public void draw(Graphics g) {
			g.setColor(Color.white);
			g.fillOval(x - (r/2), y - (r/2), r, r);
			g.setColor(Color.gray);
			g.drawOval(x - (r/2), y - (r/2), r, r);
		}

		public void isHit(int mx, int my) {
			System.out.println(mx + ", " + my);
			int x1 = Math.abs(mx - x);
			int y1 = Math.abs(my - y);
			double distance = Math.pow(x1, 2) + Math.pow(y1, 2);
			distance = Math.sqrt(distance);
			System.out.println(distance);
			if (distance > (r / 2))
				isHit = false;
			else
				isHit = true;
		}
	}
}