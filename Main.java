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
	// game speeds
	static final int GAME_SPEED = 5;
	static final int tps = 1000 / GAME_SPEED;

	// circle sizes
	static final int NOTE_SIZE = 50;
	static final int CIRCLE_SIZE = 60;
	static final int C_DECREASE = 3;

	// scoring
	static final int tickError300 = 0;
	static final int tickError100 = 1;
	static final int tickError50 = 2;

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
	// private class ExitListener implements ActionListener {
	// 	public void actionPerformed(ActionEvent e) {
	// 		System.exit(0);
	// 	}
	// }

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
			// Point mouse = MouseInfo.getPointerInfo().getLocation();
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
		ArrayList<Note> notes;
        int score;
		int currentTicks; // 200 ticks a second
		int combo;

        int seconds;
        int ticks; // 200 ticks a second

		public myJPanel(KeyList KL1, myJFrame frame1) {
			KL = KL1;	
			frame = frame1;
			setBackground(new Color(230, 100, 200));
            score = 0;
            currentTicks = 0;
			combo = 0;

			seconds = 0;
			ticks = 0;

            notes = new ArrayList<>();
			song1(notes);
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
            return ((tps) * s) + t;
        }

		public void startGame() {
			while(true) {
				repaint();
				try {Thread.sleep(GAME_SPEED);}
				catch(Exception e) {}

				// between each frame
				currentTicks++;

				ticks++;
				if (ticks >= (tps)) {
					ticks = 0;
					seconds++;
				}
			}
		}
		public void paintComponent(Graphics g) {
			super.paintComponent(g);

			g.setColor(Color.white);
			g.setFont(new Font("Sans Serif", Font.BOLD, 30));
			g.drawString(String.valueOf(seconds), 100, 500);
			g.drawString(String.valueOf(ticks), 100, 550);

            Note n;
			int noteTicks = 0;
            for (int i = 0; i < notes.size(); i++) {
                n = notes.get(i);
				noteTicks = convertToTicks(n.sec, n.tick);
				if (currentTicks >= noteTicks)
					n.draw(g);
                if (n.over) {
                    notes.remove(i);
                    i--;
                }
            }
		}
	}

	class Note {
		final int cDecrease;
		int x;
		int y;
		int r;
        int c_r;
        int c_smaller;
        int order;
		boolean over;
		int sec;
		int tick;

		// default constructor with coords, time, and order
		public Note(int x1, int y1, int s, int t, int o) {
			r = NOTE_SIZE;
            c_r = CIRCLE_SIZE;
			cDecrease = C_DECREASE;
            c_smaller = 0;
			over = false;
			x = x1 + r;
			y = y1 + r;
            order = o;
			int totTicks = ((tps) * s) + t;
			totTicks -= CIRCLE_SIZE * cDecrease;
			sec = totTicks / (tps);
			tick = totTicks % (tps);
		}

		// 2nd constructor in case slower note
		public Note(int x1, int y1, int cs, int s, int t, int o) {
			r = NOTE_SIZE;
            c_r = CIRCLE_SIZE;
            c_smaller = 0;
			cDecrease = cs;
			over = false;
			x = x1 + r;
			y = y1 + r;
            order = o;
			int totTicks = ((tps) * s) + t;
			totTicks -= CIRCLE_SIZE * cDecrease;
			sec = totTicks / (tps);
			tick = totTicks % (tps);
		}

		public void draw(Graphics g) {
            // update the outer circle
            if (c_r <= -5)
                over = true;
            if (c_smaller >= cDecrease) {
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
	
	// songs
	public void song1(ArrayList<Note> notes) {
		notes.add(new Note(100, 100, 1, 0, 1));
		notes.add(new Note(200, 100, 2, 0, 2));
		notes.add(new Note(300, 100, 3, 0, 3));
		notes.add(new Note(400, 100, 4, 0, 4));
		notes.add(new Note(500, 100, 5, 0, 5));
	}
}



/*
TODO:
- Change timing of circles so that the s, t is the time to HIT the circles
- Add in timing of 50s, 100s, 300s
- Combo
- Score
- Audio

Optional:
- Sliders
- Spinners
- Transitions
- Instructions page
- Map Selection page
- Score overview
- Scoreboard using files?
*/