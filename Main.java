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
        static final int GAME_SPEED = 5;
		KeyList KL;
		myJFrame frame;
		ArrayList<Note> notes;
        int score;
        int seconds;
        int ticks; // 200 ticks a second

		public myJPanel(KeyList KL1, myJFrame frame1) {
			KL = KL1;	
			frame = frame1;
			setBackground(new Color(230, 100, 200));
            score = 0;
            seconds = 0;
            ticks = 0;

            notes = new ArrayList<>();
			notes.add(new Note(100, 100, 1));
		}
		
		public void notecheck(MouseEvent e) {
            Note n;
            for (int i = 0; i < notes.size(); i++) {
                n = notes.get(i);
			    n.isHit(e.getX(), e.getY());
            }
		}
		public void notecheck(KeyEvent e) {
			Point mouse = MouseInfo.getPointerInfo().getLocation();
            Note n;
            for (int i = 0; i < notes.size(); i++) {
                n = notes.get(i);
			    n.isHit((int)mouse.getX(), (int)mouse.getY());
            }
		}
        public int convertToTicks(int s, int t) {
            return (200 * s) + t;
        }

		public void startGame() {
			while(true) {
				repaint();
				try {Thread.sleep(GAME_SPEED);}
				catch(Exception e) {}
			}
		}
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			int x = frame.getWidth();
			int y = frame.getHeight();

            Note n;
            for (int i = 0; i < notes.size(); i++) {
                n = notes.get(i);
                if (!n.over)
                    n.draw(g);
                else {
                    notes.remove(i);
                    i--;
                }
            }
		}
	}

	class Note {
		static final int NOTE_SIZE = 50;
		static final int CIRCLE_SIZE = 60;
        static final int C_DECREASE = 3;
		int x;
		int y;
		int r;
        int c_r;
        int c_smaller;
        int order;
		boolean over;

		public Note(int x1, int y1, int o) {
			r = NOTE_SIZE;
            c_r = CIRCLE_SIZE;
            c_smaller = 0;
			over = false;
			x = x1 + r;
			y = y1 + r;
            order = o;
		}

		public void draw(Graphics g) {
            // update the outer circle
            if (c_r <= -10)
                over = true;
            if (c_smaller >= C_DECREASE) {
                c_r--;
                c_smaller = 0;
            }

            // outer circle
            Graphics2D g2 = (Graphics2D) g;
            int cr = r + c_r;
            int dia = 2 * cr;
            g2.setColor(new Color(101, 224, 243, 180));
            g2.setStroke(new BasicStroke(2));
            g2.drawOval(x - cr, y - cr, dia, dia);
            c_smaller++;

            // actual circle to hit
            dia = 2 * r;
			g2.setColor(new Color(101, 224, 243));
			g2.fillOval(x - r, y - r, dia, dia);

            // order # + border
			g2.setColor(Color.white);
            g2.setFont(new Font("Sans Serif", Font.BOLD, 30));
            g2.drawString(String.valueOf(order), x - 7, y + 11);
            g2.setStroke(new BasicStroke(8));
			g2.drawOval(x - r + 4, y - r + 4, dia - 8, dia - 8);
		}

		public void isHit(int mx, int my) {
			int x1 = Math.abs(mx - x);
			int y1 = Math.abs(my - y);
			double distance = Math.pow(x1, 2) + Math.pow(y1, 2);
			distance = Math.sqrt(distance);

			if (distance > r)
				over = false;
			else {
				over = true;
                System.out.println("Hit!");
            }
		}
	}
}