import javax.imageio.ImageIO;
import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.event.*;
import java.io.*;
import java.awt.*;
import java.awt.image.*;
import java.util.*;
// import javafx.BackgroundImage;

public class Main {
	public static void main(String[] args) {
		myJFrame frame = new myJFrame();
		frame.show();
		frame.startGame();
	}
}
class myJFrame extends JFrame {
	myJPanel panel;
	// menuPage menu;
	int maxX;
	int maxY;
	// game speeds
	static final int GAME_SPEED = 5;
	static final int tps = 1000 / GAME_SPEED;

	// circle sizes
	static final int NOTE_SIZE = 50;
	static final int CIRCLE_SIZE = 70;
	static final int C_DECREASE = 3;

	// hit values
	static final int[] hitWindow2 = {65 / GAME_SPEED, 125 / GAME_SPEED, 170 / GAME_SPEED};
	static final int metronome = 78;
	static final int offset = 187;
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
		
		maxX = (int)getSize().getWidth();
		maxY = (int)getSize().getHeight();

		System.out.println(maxX + " " + maxY);
		// NOTE_SIZE = (int)(((maxX + maxY) / 2) * 0.1);
		// CIRCLE_SIZE = (int)(NOTE_SIZE * 1.2);
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
					if (!panel.stats)
						panel.stats = true;
					else
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
					if (panel.menu) {
						panel.menu = false;
						break;
					}
					panel.notecheck(e);
					break;
			}
		}
	}
	class MouseList implements MouseListener {
		public void mousePressed(MouseEvent e) {}
		public void mouseReleased(MouseEvent e) {}
		public void mouseClicked(MouseEvent e) {
			if (panel.menu) {
				panel.menu = false;
				return;
			}
			panel.notecheck(e);
		}
		public void mouseEntered(MouseEvent e) {}
		public void mouseExited(MouseEvent e) {}
	}
	

	class myJPanel extends JPanel {
		KeyList KL;
		myJFrame frame;
		ArrayList<Beat> notes;
		int song;
        int score;
		int combo;
		int maxCombo = 0;
		int num300s = 0;
		int num100s = 0;
		int num50s = 0;
		int num0s = 0;
		int notesOver = 0;
		int numNotes;

		int currentTicks; // 200 ticks a second

        int seconds;
        int ticks; // 200 ticks a second
		BufferedImage logo;
		BufferedImage[] ranks;
		int temp;
		boolean menu;
		boolean stats;

		public myJPanel(KeyList KL1, myJFrame frame1, int s) {
			KL = KL1;	
			frame = frame1;
			ranks = new BufferedImage[7];
			try {
				logo = ImageIO.read(new File("logo.png"));
				ranks[0] = ImageIO.read(new File("ssrank.png"));
				ranks[1] = ImageIO.read(new File("srank.png"));
				ranks[2] = ImageIO.read(new File("arank.png"));
				ranks[3] = ImageIO.read(new File("brank.png"));
				ranks[4] = ImageIO.read(new File("crank.png"));
				ranks[5] = ImageIO.read(new File("drank.png"));
				ranks[6] = ImageIO.read(new File("zrank.png"));
			}
			catch(Exception e) {}
			setBackground(new Color(250, 180, 220));
            score = 0;
            currentTicks = 0;
			combo = 0;

			seconds = 0;
			ticks = 0;

            notes = new ArrayList<>();
			if (s == 1) {
				song1(notes);
			}
			numNotes = notes.size();
			song = s;
			menu = true;
			stats = false;

			temp = metronome;
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
            Beat n;
            for (int i = 0; i < notes.size(); i++) {
                n = notes.get(i);
				if (!n.hit)
			    	n.isHit(e.getX(), e.getY(), currentTicks);
            }
		}
		public void notecheck(KeyEvent e) {
			Point mouse = MouseInfo.getPointerInfo().getLocation();
            Beat n;
            for (int i = 0; i < notes.size(); i++) {
                n = notes.get(i);
				if (!n.hit)
			    	n.isHit((int)mouse.getX(), (int)mouse.getY(), currentTicks);
            }
		}
        public int convertToTicks(int s, int t) {
            return ((tps) * s) + t;
        }

		public void startGame() {
			while (menu) {
				try {Thread.sleep(1);}
				catch (Exception e) {}
				repaint();
			}

			if (song == 1)
				playSong1();

			long previous = System.currentTimeMillis();
			long current;

			while (!stats) {
				try {Thread.sleep(1);}
				catch(Exception e) {}
				// if (menu) {
				// 	repaint();
				// 	continue;
				// }
				current = System.currentTimeMillis();
				while ((current - previous) >= GAME_SPEED) {
					repaint();
					currentTicks++;
					ticks++;
					if (ticks >= (tps)) {
						ticks = 0;
						seconds++;
					}
					previous += GAME_SPEED;
				}
			}

			// while (stats) {
			// 	try {Thread.sleep(1);}
			// 	catch (Exception e) {}
			// 	repaint();
			// }
			repaint();
		}


		public void paintComponent(Graphics g) {
			super.paintComponent(g);

			if (menu) {
				int width = (int)(maxX * 0.2);
				int logoX = (int)(maxX * 0.4);
				int logoY = (int)(maxY * 0.08);
				int center = (int)(maxX * 0.5);
				g.drawImage(logo, logoX, logoY, width, width, this);

				String text = "Note: You will need audio to play.";
				int tempY = logoY + width + 70;
				g.setColor(new Color(199, 0, 73));
				g.setFont(new Font("Trebuchet MS", Font.BOLD, 30));
				g.drawString(text, center - (text.length() * 7), tempY);
				tempY += 70;
				g.setColor(new Color(129, 87, 255));
				g.setFont(new Font("Trebuchet MS", Font.PLAIN, 30));
				text = "Hit the circles along with the beat of the song.";
				g.drawString(text, center - (text.length() * 7), tempY);
				tempY += 40;
				text = "Move your mouse to the circle,";
				g.drawString(text, center - (text.length() * 7), tempY);
				tempY += 40;
				text = "and press z, x, or left click to hit it.";
				g.drawString(text, center - (text.length() * 6), tempY);
				tempY += 40;
				text = "press ESC to see stats, and ESC again to exit.";
				g.drawString(text, center - (text.length() * 7), tempY);
				tempY += 40;
				text = "Remember to time your hits and good luck!";
				g.drawString(text, center - (text.length() * 7), tempY);
				g.setColor(new Color(255, 103, 69));
				g.setFont(new Font("Trebuchet MS", Font.BOLD, 30));
				tempY += 70;
				text = "Press x/z/click to start...";
				g.drawString(text, center - (text.length() * 7), tempY);
				return;
			}

			String adjScore = String.valueOf(score);
			String adjTime = String.valueOf(seconds % 60);
			if (adjTime.length() < 2)
				adjTime = "0" + adjTime;
			adjTime = (seconds / 60) + ":" + adjTime;
			while (adjScore.length() < 10)
				adjScore = "0" + adjScore;

			if (stats) {
				int top = (6 * num300s) + (2 * num100s) + num50s;
				double accuracy;
				if (notesOver == 0)
					accuracy = 0;
				else
					accuracy = (double)top / (6 * notesOver);
				String acc = String.format("%.2f", accuracy * 100);
				String text = "Senbonzakura - Lindsey Stirling";
				g.setColor(new Color(250, 130, 70));
				g.setFont(new Font("Trebuchet MS", Font.BOLD, 30));
				g.drawString(text, 70, 80);
				text = "Score";
				g.setColor(new Color(240, 240, 240));
				g.setFont(new Font("Trebuchet MS", Font.BOLD, 40));
				g.drawString(text, 70, 160);
				g.drawString("Accuracy", 70, 500);
				g.setFont(new Font("Trebuchet MS", Font.BOLD, 60));
				g.drawString("Ranking", maxX - 200, 50);
				g.setFont(new Font("Trebuchet MS", Font.BOLD, 40));
				text = "" + adjScore;
				g.setColor(Color.white);
				g.drawString(text, 350, 160);
				g.drawString("" + num300s, 250, 300);
				g.drawString("" + num100s, 250, 360);
				g.drawString("" + num50s, 250, 420);
				g.drawString("" + num0s, 720, 300);
				g.drawString("" + combo, 720, 360);
				g.drawString("" + maxCombo, 720, 420);
				g.drawString(acc + "%", 350, 500);
				g.setColor(new Color(73, 92, 201));
				g.drawString("300", 70, 300);
				g.setColor(new Color(48, 201, 63));
				g.drawString("100", 70, 360);
				g.setColor(new Color(68, 164, 219));
				g.drawString("50", 70, 420);
				g.setColor(new Color(227, 27, 27));
				g.drawString("X", 430, 300);
				g.setColor(new Color(255, 219, 46));
				g.drawString("Combo", 430, 360);
				g.setColor(new Color(212, 21, 129));
				g.drawString("Max Combo", 430, 420);
				if (accuracy >= 99) { // ss rank
					//
				}
				else if (accuracy >= 95) { // s rank
					//
				}
				else if (accuracy >= 90) { // a rank
					//
				}
				else if (accuracy >= 80) { // b rank
					//
				}
				else if (accuracy >= 70) { // c rank
					//
				}
				else if (accuracy >= 60) { // d rank
					//
				}
				else { // z rank
					//
				}

				return;
			}

			g.setColor(Color.white);
			g.setFont(new Font("Trebuchet MS", Font.BOLD, 30));
			g.drawString(adjScore, maxX - 200, 50);
			g.drawString(combo + "X", 50, maxY - 30);
			g.setFont(new Font("Trebuchet MS", Font.PLAIN, 26));
			g.drawString(adjTime, maxX - 100, maxY - 30);

			// if (currentTicks >= offset && currentTicks % metronome == (offset % metronome)) { // start including hit sounds
			// 		playHitSound();
			// }

            Beat n;
            for (int i = 0; i < notes.size(); i++) {
                n = notes.get(i);
				if (currentTicks >= n.startTick) {
					n.draw(g);
				}
                if (n.hit && !n.scoreAdded) {
					if (combo > 0) // if there is a combo, include multiplier
						score += n.scoring * (1 + ((combo - 1) / 25.0));
					else // otherwise just add raw score
						score += n.scoring;

					if (n.scoring == 300) // counting # of 300, 100, 50s
						num300s++;
					else if (n.scoring == 100)
						num100s++;
					else if (n.scoring == 50)
						num50s++;

					if (n.scoring != 0) // if hit the circle
						combo++; // add to combo
					n.scoreAdded = true;
				}

				if (n.over || n.hitTick < 0) { // done animating
					if (!n.hit) { // missed circle
						combo = 0; // don't add to combo
						num0s++;
					}

					if (combo > maxCombo) // max combo
						maxCombo = combo;
					// note is over
                    notes.remove(i);
                    i--;
					notesOver++;
                }
            }
		}

		public void playHitSound() { // temp
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

	abstract class Beat {
		int x; // coords
		int y;
		int c_r; // radius of outer circle
		int order; // number in the middle of circle
		boolean over; // if beat is over
		boolean hit;
		boolean scoreAdded;
		int startTick;
		int hitTick;
		int scoring;
		int scoreAnim;
		int fadeIn;

		int c_smaller; // counter var for outer circle getting smaller
		int cDecrease; // rate the outer circle moves in

		public void isHit(int mx, int my, int gametick) {}
		public void draw(Graphics g) {}
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
			hit = false;
			scoreAdded = false;
			x = x1 + r;
			y = y1 + r;
            order = o;
			int totTicks = (tps * s) + t;
			hitTick = totTicks;
			totTicks -= CIRCLE_SIZE * cDecrease;
			startTick = totTicks;
			scoring = 0;
			scoreAnim = 0;
			fadeIn = 0;
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
			int totTicks = (tps * s) + t;
			hitTick = totTicks;
			totTicks -= CIRCLE_SIZE * cDecrease;
			startTick = totTicks;
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
			int totTicks = (tps * s) + t;
			hitTick = totTicks;
			totTicks -= CIRCLE_SIZE * cDecrease;
			startTick = totTicks;
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
			int totTicks = (tps * s) + t;
			hitTick = totTicks;
			totTicks -= CIRCLE_SIZE * cDecrease;
			startTick = totTicks;
			scoring = 0;
		}

		public void draw(Graphics g) {
			Graphics2D g2 = (Graphics2D) g;
            g2.setFont(new Font("Trebuchet MS", Font.BOLD, 30));
            // update the outer circle
            if (c_r <= -1 * hitWindow2[2] / cDecrease || hit) {
				if (scoreAnim >= 120) { // stop animation
					over = true;
				}
				scoreAnim++; // increment animation
				String value;

				if (scoring == 0)
					value = "X";
				else {
					value = "" + scoring;
				}

				if (scoreAnim <= 40) { // first 20/100 ticks
					if (scoring == 0)
						g.setColor(new Color(227, 27, 27, 6 * scoreAnim));
					else if (scoring == 50)
						g.setColor(new Color(68, 164, 219, 6 * scoreAnim));
					else if (scoring == 100)
						g.setColor(new Color(48, 201, 63, 6 * scoreAnim));
					else if (scoring == 300)
						g.setColor(new Color(73, 92, 201, 6 * scoreAnim));
				}
				else if (scoreAnim >= 100) { // last 20/100 ticks
					if (scoring == 0)
						g.setColor(new Color(227, 27, 27, 12 * (121 - scoreAnim)));
						else if (scoring == 50)
							g.setColor(new Color(68, 164, 219, 12 * (121 - scoreAnim)));
						else if (scoring == 100)
							g.setColor(new Color(48, 201, 63, 12 * (121 - scoreAnim)));
						else if (scoring == 300)
							g.setColor(new Color(73, 92, 201, 12 * (121 - scoreAnim)));
				}
				else { // in between
					if (scoring == 0)
						g.setColor(new Color(227, 27, 27));
						else if (scoring == 50)
							g.setColor(new Color(68, 164, 219));
						else if (scoring == 100)
							g.setColor(new Color(48, 201, 63));
						else if (scoring == 300)
							g.setColor(new Color(73, 92, 201));
				}
				g.drawString(value, x - (9 * value.length()), y + 11); // draw the scoring
				if (scoreAnim >= 40) { // if after 40 seconds, don't draw circle (faded out already)
					return;
				}
			}


            if (c_smaller >= cDecrease) { // decrease outer circle size
                c_r--;
                c_smaller = 0;
            }

			// outer circle
			int cr = r + c_r;
			int dia = 2 * cr;
			if (c_r >= 0) {
				g2.setColor(new Color(101, 224, 243, 180));
				g2.setStroke(new BasicStroke(2));
				g2.drawOval(x - cr + 5, y - cr + 5, dia - 10, dia - 10);
			}
			c_smaller++;

            // actual circle to hit
            dia = 2 * r;
			if (fadeIn < 40)
				g2.setColor(new Color(101, 224, 243, 6 * fadeIn));
			else if (scoring > 0 || c_r <= 0)
				g2.setColor(new Color(101, 224, 243, 255 - (6 * scoreAnim)));
			else
				g2.setColor(new Color(101, 224, 243));
			g2.fillOval(x - r, y - r, dia, dia);

            // order # + border
			if (fadeIn < 40) {
				g2.setColor(new Color(255, 255, 255, 6 * fadeIn));
				fadeIn++;
			}
			else if (scoring > 0 || c_r <= 0)
				g2.setColor(new Color(255, 255, 255, 255 - (6 * scoreAnim)));
			else
				g2.setColor(Color.white);
            g2.drawString(String.valueOf(order), x - 7, y + 11);
            g2.setStroke(new BasicStroke(8));
			g2.drawOval(x - r + 4, y - r + 4, dia - 8, dia - 8);
		}

		public void isHit(int mx, int my, int gameTicks) {
			int x1 = Math.abs(mx - x);
			int y1 = Math.abs(my - y);
			double distance = Math.pow(x1, 2) + Math.pow(y1, 2);
			distance = Math.sqrt(distance);

			if (distance <= r) {
				int diffTicks = gameTicks - hitTick; // human error
				System.out.println("Note " + order + ": " + diffTicks + "\t" + c_r);
				diffTicks = Math.abs(diffTicks);

				if (diffTicks <= hitWindow2[0]) { // 300 pts
					scoring = 300;
					hit = true;
					playHitSound();
				}
				else if (diffTicks <= hitWindow2[1]) { // 100 pts
					scoring = 100;
					hit = true;
					playHitSound();
				}
				else if (diffTicks <= hitWindow2[2]) { // 50 pts
					scoring = 50;
					hit = true;
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

		public Slider(int x1, int y1, int s, int t, int o, int dir) {}
	}

	// songs
	public void song1(ArrayList<Beat> notes) {
		// every 78 ticks = 1 beat
		// abt 154 bpm
		// notes.add(new Note(0, 0, -10, 0, 0)); // insignificant
		notes.add(new Note(100, 100, 4, 190, 1)); // 980
		notes.add(new Note(200, 100, 6, 100, 2)); // 1305
		notes.add(new Note(300, 100, 9, 105, 3)); // 1905
		notes.add(new Note(400, 100, 11, 170, 4)); // 2370
		notes.add(new Note(400, 300, 13, 90, 1)); // 2685
		notes.add(new Note(600, 300, 15, 0, 2)); // 2980
		notes.add(new Note(600, 100, 15, 150, 3)); // 3150
		notes.add(new Note(800, 300, 16, 100, 4)); // 3300
		notes.add(new Note(1000, 400, 17, 60, 5)); // 3450
		notes.add(new Note(1000, 500, 17, 120, 1)); // 3530
		notes.add(new Note(1000, 600, 18, 10, 2)); // 3610
		notes.add(new Note(900, 500, 18, 110, 3)); // 3700
		notes.add(new Note(800, 400, 18, 180, 4)); // 3780
		notes.add(new Note(600, 400, 19, 50, 5)); // 3850
		notes.add(new Note(400, 600, 19, 120, 6)); // 3930
		// notes.add(new Note(1000, 600, 17, 120, 6)); // 3530
		// notes.add(new Note(1000, 600, 17, 120, 6)); // 3530
		// notes.add(new Note(1000, 600, 17, 120, 6)); // 3530
		// notes.add(new Note(1000, 600, 17, 120, 6)); // 3530
		// notes.add(new Note(1000, 600, 17, 120, 6)); // 3530
		// notes.add(new Note(1000, 600, 17, 120, 6)); // 3530
		// notes.add(new Note(1000, 600, 17, 120, 6)); // 3530
		// notes.add(new Note(1000, 600, 17, 120, 6)); // 3530
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
- 2:17
 */

/* 
Possible fonts:
- Calibri
- Corbel
- SansSerif
- Trebuchet MS
*/