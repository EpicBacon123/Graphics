import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.event.*;
import java.io.*;
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

	// hit values
	static final int[] hitWindow2 = {70 / GAME_SPEED, 125 / GAME_SPEED, 180 / GAME_SPEED};
	// originally: 68, 124, 180 ms

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
		panel = new myJPanel(KL, this, 1);
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
		int combo;

		int currentTicks; // 200 ticks a second

        int seconds;
        int ticks; // 200 ticks a second

		public myJPanel(KeyList KL1, myJFrame frame1, int song) {
			KL = KL1;	
			frame = frame1;
			setBackground(new Color(230, 100, 200));
            score = 0;
            currentTicks = 0;
			combo = 0;

			seconds = 0;
			ticks = 0;

            notes = new ArrayList<>();
			if (song == 1) {
				song1(notes);
				playSong1();
			}
		}

		public void playSong1() {
			try {
				AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File("./Senbonzakura.wav").getAbsoluteFile());
				Clip clip = AudioSystem.getClip();
				clip.open(audioInputStream);
				clip.start();
			}
			catch(Exception e) {
				System.out.println("Error with playing sound.");
				e.printStackTrace();
			}
		}

		public void notecheck(MouseEvent e) {
            Note n;
            for (int i = 0; i < notes.size(); i++) {
                n = notes.get(i);
			    n.isHit(e.getX(), e.getY(), currentTicks);
            }
		}
		public void notecheck(KeyEvent e) {
			Point mouse = MouseInfo.getPointerInfo().getLocation();
            Note n;
            for (int i = 0; i < notes.size(); i++) {
                n = notes.get(i);
			    n.isHit((int)mouse.getX(), (int)mouse.getY(), currentTicks);
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

			g.drawString("Score: " + score, 300, 500);
			g.drawString("Combo: " + combo, 300, 550);

            Note n;
			int noteTicks = 0;
            for (int i = 0; i < notes.size(); i++) {
                n = notes.get(i);
				noteTicks = convertToTicks(n.sec, n.tick);
				if (currentTicks >= noteTicks)
					n.draw(g);
                if (n.over) {
					if (combo > 0) // if there is a combo, include multiplier
						score += n.scoring * (1 + ((combo - 1) / 25.0));
					else // otherwise just add raw score
						score += n.scoring;

					if (n.scoring != 0) // if hit the circle
						combo++; // add to combo
					else // missed the circle
						combo = 0; // don't add to combo

					// note is over
                    notes.remove(i);
                    i--;
                }
            }
		}
	}

	abstract class Beat {
		int x; // coords
		int y;
		int c_r; // radius of outer circle
		int c_smaller; // counter var for outer circle getting smaller
		int order; // number in the middle of circle
		boolean over; // if beat is over
		int cDecrease; // rate the outer circle moves in
		int sec; // timing of s, t
		int tick;

		int totalTicks;
		int scoring;
	}
	class Note extends Beat {
		int r; // radius of hit-circle

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
			totalTicks = totTicks;
			totTicks -= CIRCLE_SIZE * cDecrease;
			sec = totTicks / (tps);
			tick = totTicks % (tps);
			scoring = 0;
		}

		// 2nd constructor in case slower/faster note
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
			totalTicks = totTicks;
			totTicks -= CIRCLE_SIZE * cDecrease;
			sec = totTicks / (tps);
			tick = totTicks % (tps);
			scoring = 0;
		}

		// 3rd constructor in case larger/smaller notes
		public Note(int x1, int y1, double rad, int s, int t, int o) {
			r = (int)rad;
            c_r = CIRCLE_SIZE;
			cDecrease = C_DECREASE;
            c_smaller = 0;
			over = false;
			x = x1 + r;
			y = y1 + r;
            order = o;
			int totTicks = ((tps) * s) + t;
			totalTicks = totTicks;
			totTicks -= CIRCLE_SIZE * cDecrease;
			sec = totTicks / (tps);
			tick = totTicks % (tps);
			scoring = 0;
		}

		// 4th constructor in case larger/smaller + slower/faster notes
		public Note(int x1, int y1, double rad, int cs, int s, int t, int o) {
			r = (int)rad;
            c_r = CIRCLE_SIZE;
			cDecrease = cs;
            c_smaller = 0;
			over = false;
			x = x1 + r;
			y = y1 + r;
            order = o;
			int totTicks = ((tps) * s) + t;
			totalTicks = totTicks;
			totTicks -= CIRCLE_SIZE * cDecrease;
			sec = totTicks / (tps);
			tick = totTicks % (tps);
			scoring = 0;
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
			if (c_r >= 0) {
				g2.setColor(new Color(101, 224, 243, 180));
				g2.setStroke(new BasicStroke(2));
				g2.drawOval(x - cr, y - cr, dia, dia);
			}
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

		public void isHit(int mx, int my, int gameTicks) { // TODO: INSERT SCORE VALUE
			int x1 = Math.abs(mx - x);
			int y1 = Math.abs(my - y);
			double distance = Math.pow(x1, 2) + Math.pow(y1, 2);
			distance = Math.sqrt(distance);

			if (distance > r)
				over = false;
			else {
				int diffTicks = Math.abs(gameTicks - totalTicks); // human error
				// g.setColor(Color.white);
				// g.setFont(new Font("Sans Serif", Font.BOLD, 24));

				if (diffTicks <= hitWindow2[0]) { // 300 pts
					scoring = 300;
					over = true;
					// g.drawString("300", x-10, y + 11);
					playHitSound();
				}
				else if (diffTicks <= hitWindow2[1]) { // 100 pts
					scoring = 100;
					over = true;
					// g.drawString("300", x-10, y + 11);
					playHitSound();
				}
				else if (diffTicks <= hitWindow2[2]) { // 50 pts
					scoring = 50;
					over = true;
					// g.drawString("300", x-10, y + 11);
					playHitSound();
				}
            }
		}

		public void playHitSound() {
			try {
				AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File("./hit.wav").getAbsoluteFile());
				Clip clip = AudioSystem.getClip();
				clip.open(audioInputStream);
				clip.start();
			}
			catch(Exception e) {
				System.out.println("Error with playing sound.");
				e.printStackTrace();
			}
		}
	}
	
	class Slider extends Beat {
		static final int UP = 0;
		static final int DOWN = 1;
		static final int LEFT = 2;
		static final int RIGHT = 3;
		int orientation;
		int length;
		int s_speed;
		int first; // the 50, 100, 300

		// public Slider(int x, )
	}

	// songs
	public void song1(ArrayList<Note> notes) {
		notes.add(new Note(100, 100, 3, 0, 1));
		notes.add(new Note(200, 100, 4, 0, 2));
		notes.add(new Note(300, 100, 5, 0, 3));
		notes.add(new Note(400, 100, 6, 0, 4));
		notes.add(new Note(500, 100, 7, 0, 5));
	}
}



/*
Optional:
- Sliders
- Spinners
- Transitions
- Instructions page
- Map Selection page
- Score overview
- Scoreboard using files?
*/

/*
Timing: .1 speed
- 20:00
 */