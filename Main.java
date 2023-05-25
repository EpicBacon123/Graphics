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
	public static void main(String[] args) { // create and show frame/game
		myJFrame frame = new myJFrame();
		frame.show();
		frame.startGame();
	}
}
class myJFrame extends JFrame {
	myJPanel panel;
	int maxX;
	int maxY;
	// game speeds
	static final int GAME_SPEED = 5;
	static final int tps = 1000 / GAME_SPEED; // ticks per second

	// circle sizes
	static final int NOTE_SIZE = 50;
	static final int CIRCLE_SIZE = 70;
	static final int C_DECREASE = 3;

	// hit values
	static final int[] hitWindow2 = {80 / GAME_SPEED, 140 / GAME_SPEED, 200 / GAME_SPEED};
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
		// dimensions of screen
		maxX = (int)getSize().getWidth();
		maxY = (int)getSize().getHeight();

		// System.out.println(maxX + " " + maxY);
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
		public void keyPressed(KeyEvent e) {
			switch(e.getKeyCode()) {
				case 27: // ESC
				case KeyEvent.VK_E:
					if (!panel.stats) // show stats
						panel.stats = true;
					else // exit
						System.exit(0);
					break;
			}
		}
		public void keyReleased(KeyEvent e) {}
		public void keyTyped(KeyEvent e) {
			// Point mouse = MouseInfo.getPointerInfo().getLocation();
			switch (e.getKeyChar()) {
				case 'z': // hit circle
				case 'Z':
				case 'x':
				case 'X':
					if (panel.menu) { // stop showing menu
						panel.menu = false;
						break;
					}
					panel.notecheck(e); // call method to check if hit note
					break;
			}
		}
	}
	class MouseList implements MouseListener {
		public void mousePressed(MouseEvent e) {}
		public void mouseReleased(MouseEvent e) {}
		public void mouseClicked(MouseEvent e) {
			if (panel.menu) { // stop showing menu
				panel.menu = false;
				return;
			}
			panel.notecheck(e); // call method to check if hit note
		}
		public void mouseEntered(MouseEvent e) {}
		public void mouseExited(MouseEvent e) {}
	}
	

	class myJPanel extends JPanel { // basis for game
		KeyList KL;
		myJFrame frame;
		ArrayList<Beat> notes; // arraylist of circles to hit
		int song; // kind of redundant - used to pick a map/song

		// scoring
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
		
		// booleans for pages
		boolean menu;
		boolean stats;

		public myJPanel(KeyList KL1, myJFrame frame1, int s) {
			KL = KL1;	
			frame = frame1;
			ranks = new BufferedImage[7]; // add ranking images
			try {
				logo = ImageIO.read(new File("logo.png"));
				ranks[0] = ImageIO.read(new File("./rank/ssrank.png"));
				ranks[1] = ImageIO.read(new File("./rank/srank.png"));
				ranks[2] = ImageIO.read(new File("./rank/arank.png"));
				ranks[3] = ImageIO.read(new File("./rank/brank.png"));
				ranks[4] = ImageIO.read(new File("./rank/crank.png"));
				ranks[5] = ImageIO.read(new File("./rank/drank.png"));
				ranks[6] = ImageIO.read(new File("./rank/zrank.png"));
			}
			catch(Exception e) {}
			setBackground(new Color(250, 180, 220));
            score = 0;
            currentTicks = 0;
			combo = 0;

			seconds = 0;
			ticks = 0;

            notes = new ArrayList<>();
			if (s == 1) { // fill up arraylist with circles/notes
				songtest(notes);
			}
			numNotes = notes.size();
			System.out.println(numNotes);
			song = s;
			menu = true;
			stats = false;

			temp = metronome; // redundant
		}

		public void playSong1() { // play .wav file - stackoverflow
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

		public void notecheck(MouseEvent e) { // for mouseEvent
            Beat n;
            for (int i = 0; i < notes.size(); i++) { // check all notes
                n = notes.get(i);
				if (!n.hit) // if not hit already, check
			    	n.isHit(e.getX(), e.getY(), currentTicks);
            }
		}
		public void notecheck(KeyEvent e) { // for keyEvent
			Point mouse = MouseInfo.getPointerInfo().getLocation(); // get mouse location
			System.out.println("notes.add(new Note(" + (int)mouse.getX() + ", " + (int)mouse.getY() + ", " + seconds + ", " + ticks + ", 1));");
            Beat n;
            for (int i = 0; i < notes.size(); i++) { // check all notes
                n = notes.get(i);
				if (!n.hit) // if not hit already, check
			    	n.isHit((int)mouse.getX(), (int)mouse.getY(), currentTicks);
            }
		}
        public int convertToTicks(int s, int t) { // helper method
            return ((tps) * s) + t;
        }

		public void startGame() {
			while (menu) { // in menu page
				try {Thread.sleep(1);}
				catch (Exception e) {}
				repaint();
			}

			if (song == 1) // start playing song
				playSong1();

			// variables for timing system
			long previous = System.currentTimeMillis(); // previous time
			long current;

			while (!stats) { // actual game page
				try {Thread.sleep(1);}
				catch(Exception e) {}
				// if (menu) {
				// 	repaint();
				// 	continue;
				// }
				current = System.currentTimeMillis(); // current time
				while ((current - previous) >= GAME_SPEED) { // if passed a tick
					repaint(); // update panel + add to ticks
					currentTicks++;
					ticks++;
					if (ticks >= (tps)) { // update ticks + seconds
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

			if (menu) { // in menu page
				int width = (int)(maxX * 0.2); // coords
				int logoX = (int)(maxX * 0.4);
				int logoY = (int)(maxY * 0.08);
				int center = (int)(maxX * 0.5);
				g.drawImage(logo, logoX, logoY, width, width, this); // draw logo

				// instructions
				String text = "Note: You will need audio to play."; // temp string + y coord var
				int tempY = logoY + width + 70;
				g.setColor(new Color(199, 0, 73));
				g.setFont(new Font("Trebuchet MS", Font.BOLD, 30));
				g.drawString(text, center - (text.length() * 7), tempY); // use text.length() to center text
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

			String adjScore = String.valueOf(score); // string version of score
			String adjTime = String.valueOf(seconds % 60); // mins : secs
			if (adjTime.length() < 2) // add 0 in front of single digit secs
				adjTime = "0" + adjTime;
			adjTime = (seconds / 60) + ":" + adjTime; // add mins + ":"
			while (adjScore.length() < 10) // add 0s in front of score
				adjScore = "0" + adjScore;

			if (stats) { // stats screen
				int top = (6 * num300s) + (2 * num100s) + num50s; // numerator in accuracy
				double accuracy;
				if (notesOver == 0) // can't / by 0
					accuracy = 0;
				else
					accuracy = (double)top / (6 * notesOver);
				String acc = String.format("%.2f", accuracy * 100); // string form of acc in %
				String text = "Senbonzakura - Lindsey Stirling"; // song name
				g.setColor(new Color(250, 130, 70));
				g.setFont(new Font("Trebuchet MS", Font.BOLD, 30));
				g.drawString(text, 70, 80);
				text = "Score";
				g.setColor(new Color(240, 240, 240)); // display light gray text - labels
				g.setFont(new Font("Trebuchet MS", Font.BOLD, 40));
				g.drawString(text, 70, 160);
				g.drawString("Accuracy", 70, 500);
				g.setFont(new Font("Trebuchet MS", Font.BOLD, 60));
				g.drawString("Ranking", maxX - 350, 230);
				g.setFont(new Font("Trebuchet MS", Font.BOLD, 40));
				text = "" + adjScore;
				g.setColor(Color.white); // white text - scoring values
				g.drawString(text, 350, 160);
				g.drawString("" + num300s, 250, 300);
				g.drawString("" + num100s, 250, 360);
				g.drawString("" + num50s, 250, 420);
				g.drawString("" + num0s, 720, 300);
				g.drawString("" + combo, 720, 360);
				g.drawString("" + maxCombo, 720, 420);
				g.drawString(acc + "%", 350, 500);
				g.setColor(new Color(73, 92, 201)); // colored text to show 300s, 100s, 50s, misses
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
				// display images of ranking
				if (accuracy >= .99)// ss rank
					g.drawImage(ranks[0], maxX - 470, 300, 430, 350, this);
				else if (accuracy >= .95)// s rank
					g.drawImage(ranks[1], maxX - 390, 300, 280, 350, this);
				else if (accuracy >= .90) // a rank
					g.drawImage(ranks[2], maxX - 410, 300, 300, 350, this);
				else if (accuracy >= .80) // b rank
					g.drawImage(ranks[3], maxX - 360, 300, 280, 350, this);
				else if (accuracy >= .70) // c rank
					g.drawImage(ranks[4], maxX - 360, 300, 280, 330, this);
				else if (accuracy >= .60) // d rank
					g.drawImage(ranks[5], maxX - 400, 300, 300, 330, this);
				else // z rank
					g.drawImage(ranks[6], maxX - 360, 300, 250, 300, this);

				return;
			}

			// text in game page
			g.setColor(Color.white);
			g.setFont(new Font("Trebuchet MS", Font.BOLD, 30));
			g.drawString(adjScore, maxX - 200, 50); // score
			g.drawString(combo + "X", 50, maxY - 30); // combo
			g.setFont(new Font("Trebuchet MS", Font.PLAIN, 26));
			g.drawString(adjTime, maxX - 100, maxY - 30); // time

			// if (currentTicks >= offset && currentTicks % metronome == (offset % metronome)) { // metronome
			// 		playHitSound();
			// }

            Beat n;
            for (int i = 0; i < notes.size(); i++) { // loop thru notes
                n = notes.get(i);
				if (currentTicks >= n.startTick) { // if time to display note
					n.draw(g);
				}
                if (n.hit && !n.scoreAdded) { // note is hit
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
					// note is over - remove from arraylist
                    notes.remove(i);
                    i--;
					notesOver++;
                }
            }
		}

		// public void playHitSound() { // temp
		// 	try {
		// 		AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File("./hit.wav").getAbsoluteFile());
		// 		Clip clip = AudioSystem.getClip();
		// 		clip.open(audioInputStream);
		// 		clip.start();
		// 	}
		// 	catch(Exception e) {
		// 		System.out.println("Error with playing sound.");
		// 		e.printStackTrace();
		// 	}
		// }
	}

	abstract class Beat { // could've added sliders if had more time
		int x; // coords
		int y;
		int c_r; // radius of outer circle
		int order; // number in the middle of circle
		boolean over; // if beat is over
		boolean hit; // if circle is hit
		boolean scoreAdded; // if done updating scoring
		int startTick; // when to start display
		int hitTick; // when to hit circle
		int scoring; // 300, 100, 50, or 0
		int scoreAnim; // fade in/out the scoring of the note
		int fadeIn; // circle fades in

		int c_smaller; // counter var for outer circle getting smaller
		int cDecrease; // rate the outer circle moves in

		public void isHit(int mx, int my, int gametick) {} // abstract methods
		public void draw(Graphics g) {}
	}


	class Note extends Beat { // circle method
		int r; // radius of hit-circle

		// default constructor with coords, time, and order
		public Note(int comp, int x1, int y1, int s, int t, int o) {
			r = NOTE_SIZE; // default values
            c_r = CIRCLE_SIZE;
			cDecrease = C_DECREASE;
            c_smaller = 0;
			over = false; // set to false - indicate if notes are over/hit
			hit = false;
			scoreAdded = false;
			x = x1 + r; // coords
			y = y1 + r;
            order = o;
			int totTicks = (tps * s) + t; // temp var for timing
			hitTick = totTicks;
			if (comp == 1)
				hitTick += 30;
			totTicks -= CIRCLE_SIZE * cDecrease;
			startTick = totTicks;
			scoring = 0; // scoring - 300, 100, 50, 0
			scoreAnim = 0; // animation vars
			fadeIn = 0;
		}

		public void draw(Graphics g) {
			Graphics2D g2 = (Graphics2D) g;
            g2.setFont(new Font("Trebuchet MS", Font.BOLD, 30));
            // update the outer circle
            if (c_r <= -1 * hitWindow2[2] / cDecrease || hit) { // if hit/reached hitTick
				if (scoreAnim >= 120) { // stop animation
					over = true;
				}
				scoreAnim++; // increment animation var
				String value;

				if (scoring == 0) // score value
					value = "X";
				else {
					value = "" + scoring;
				}

				if (scoreAnim <= 40) { // first 40 ticks
					if (scoring == 0) // fade in the score value
						g.setColor(new Color(227, 27, 27, 6 * scoreAnim));
					else if (scoring == 50)
						g.setColor(new Color(68, 164, 219, 6 * scoreAnim));
					else if (scoring == 100)
						g.setColor(new Color(48, 201, 63, 6 * scoreAnim));
					else if (scoring == 300)
						g.setColor(new Color(73, 92, 201, 6 * scoreAnim));
				}
				else if (scoreAnim >= 100) { // last 20 ticks
					if (scoring == 0) // fade out score value
						g.setColor(new Color(227, 27, 27, 12 * (121 - scoreAnim)));
						else if (scoring == 50)
							g.setColor(new Color(68, 164, 219, 12 * (121 - scoreAnim)));
						else if (scoring == 100)
							g.setColor(new Color(48, 201, 63, 12 * (121 - scoreAnim)));
						else if (scoring == 300)
							g.setColor(new Color(73, 92, 201, 12 * (121 - scoreAnim)));
				}
				else { // in between
					if (scoring == 0) // set color to be 100% opaque
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
			if (c_r >= 0) { // only draw when outer circle > inner circle
				g2.setColor(new Color(101, 224, 243, 180));
				g2.setStroke(new BasicStroke(2));
				g2.drawOval(x - cr + 5, y - cr + 5, dia - 10, dia - 10);
			}
			c_smaller++;

            // actual circle to hit
            dia = 2 * r;
			if (fadeIn < 40) // fade in the circle in for first 40 ticks
				g2.setColor(new Color(101, 224, 243, 6 * fadeIn));
			else if (scoring > 0 || c_r <= 0) // fade out the circle for last 40 ticks
				g2.setColor(new Color(101, 224, 243, 255 - (6 * scoreAnim)));
			else // in between
				g2.setColor(new Color(101, 224, 243));
			g2.fillOval(x - r, y - r, dia, dia);

            // order # + border
			if (fadeIn < 40) { // fade in the border
				g2.setColor(new Color(255, 255, 255, 6 * fadeIn));
				fadeIn++;
			}
			else if (scoring > 0 || c_r <= 0) // fade out the circle
				g2.setColor(new Color(255, 255, 255, 255 - (6 * scoreAnim)));
			else // in between
				g2.setColor(Color.white);
			String o = order + "";
            g2.drawString(o, x - (7 * o.length()), y + 11);
            g2.setStroke(new BasicStroke(8));
			g2.drawOval(x - r + 4, y - r + 4, dia - 8, dia - 8);
		}

		public void isHit(int mx, int my, int gameTicks) {
			int x1 = Math.abs(mx - x); // pythag calculation
			int y1 = Math.abs(my - y);
			double distance = Math.pow(x1, 2) + Math.pow(y1, 2);
			distance = Math.sqrt(distance); // distance between mouse and circle radius
			int diffTicks = gameTicks - hitTick; // human error
			diffTicks = Math.abs(diffTicks);

			if (distance <= r && c_r < 70) { // mouse inside the circle

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
				if (hitTick > 29184) {
					// System.out.println("Note " + order + ": " + diffTicks + "\t" + scoring);
				}
            }
		}

		public void playHitSound() { // play .wav file - stackoverflow
			try {
				AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File("./soft-hitclap2.wav").getAbsoluteFile()); //stop at 1:23
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
	
	// class Slider extends Beat { // not enough time to implement
	// 	static final int UP = 0;
	// 	static final int DOWN = 1;
	// 	static final int LEFT = 2;
	// 	static final int RIGHT = 3;
	// 	int orientation;
	// 	int length;
	// 	int s_speed;
	// 	int first; // the 50, 100, 300

	// 	public Slider(int x1, int y1, int s, int t, int o, int dir) {}
	// }

	// songs
	public void songtest(ArrayList<Beat> notes) {
		song1(notes);
		notes.add(new Note(2, 860, 690, 143, 134, 1));
		notes.add(new Note(2, 717, 690, 144, 93, 1));
		notes.add(new Note(2, 481, 536, 145, 46, 1));
		notes.add(new Note(2, 530, 459, 146, 0, 1));
		notes.add(new Note(2, 631, 328, 165, 106, 1));
		notes.add(new Note(2, 770, 329, 166, 54, 1));
		notes.add(new Note(2, 884, 339, 167, 9, 1));
		notes.add(new Note(2, 1002, 342, 167, 89, 1));
		notes.add(new Note(2, 1104, 348, 167, 171, 1));
		notes.add(new Note(2, 1267, 362, 168, 129, 1));
		notes.add(new Note(2, 1274, 451, 169, 86, 1));
		notes.add(new Note(2, 1078, 455, 170, 43, 1));
		notes.add(new Note(2, 918, 445, 170, 120, 1));
		notes.add(new Note(2, 817, 446, 170, 194, 1));
		notes.add(new Note(2, 730, 446, 171, 153, 1));
		notes.add(new Note(2, 584, 445, 172, 102, 1));
		notes.add(new Note(2, 430, 440, 173, 62, 1));
		notes.add(new Note(2, 267, 431, 173, 135, 1));
		notes.add(new Note(2, 175, 377, 174, 14, 1));
		notes.add(new Note(2, 164, 472, 174, 179, 1));
		notes.add(new Note(2, 272, 478, 175, 135, 1));
		notes.add(new Note(2, 412, 474, 176, 88, 1));
		notes.add(new Note(2, 580, 473, 176, 158, 1));
		notes.add(new Note(2, 697, 473, 177, 32, 1));
		notes.add(new Note(2, 865, 465, 177, 178, 1));
		// notes.add(new Note(2, 923, 405, 178, 13, 1));
		// notes.add(new Note(2, 927, 324, 178, 65, 1));
		// notes.add(new Note(2, 922, 323, 178, 96, 1));
		// notes.add(new Note(2, 784, 304, 178, 125, 1));
		// notes.add(new Note(2, 695, 316, 178, 156, 1));
		// notes.add(new Note(2, 661, 321, 178, 195, 1));
		// notes.add(new Note(2, 558, 340, 179, 37, 1));
		// notes.add(new Note(2, 507, 352, 179, 62, 1));
		// notes.add(new Note(2, 477, 359, 179, 89, 1));
		// notes.add(new Note(2, 448, 398, 179, 121, 1));
		// notes.add(new Note(2, 438, 427, 179, 158, 1));
		// notes.add(new Note(2, 430, 492, 179, 195, 1));
		// notes.add(new Note(2, 433, 527, 180, 21, 1));
		// notes.add(new Note(2, 477, 561, 180, 49, 1));
		// notes.add(new Note(2, 638, 587, 180, 77, 1));
		// notes.add(new Note(2, 667, 591, 180, 109, 1));
		// notes.add(new Note(2, 759, 599, 180, 152, 1));
		// notes.add(new Note(2, 892, 616, 181, 22, 1));
		// notes.add(new Note(2, 1056, 616, 181, 95, 1));
		// notes.add(new Note(2, 1073, 517, 181, 173, 1));
		// notes.add(new Note(2, 1027, 406, 182, 49, 1));
		// notes.add(new Note(2, 906, 372, 182, 134, 1));
		// notes.add(new Note(2, 833, 390, 183, 6, 1));
		// notes.add(new Note(2, 684, 400, 183, 94, 1));
		// notes.add(new Note(2, 500, 396, 183, 169, 1));
		// notes.add(new Note(2, 442, 276, 184, 46, 1));
		// notes.add(new Note(2, 470, 219, 184, 129, 1));
		// notes.add(new Note(2, 672, 190, 185, 6, 1));
		// notes.add(new Note(2, 795, 198, 185, 86, 1));
		// notes.add(new Note(2, 928, 213, 185, 160, 1));
		// notes.add(new Note(2, 998, 233, 186, 38, 1));
		// notes.add(new Note(2, 1036, 314, 186, 113, 1));
		// notes.add(new Note(2, 1032, 371, 186, 190, 1));
		// notes.add(new Note(2, 1005, 434, 187, 67, 1));
		// notes.add(new Note(2, 886, 523, 187, 141, 1));
		// notes.add(new Note(2, 678, 571, 188, 22, 1));
		// notes.add(new Note(2, 465, 554, 188, 97, 1));
		// notes.add(new Note(2, 270, 483, 188, 177, 1));
		// notes.add(new Note(2, 239, 409, 189, 48, 1));
		// notes.add(new Note(2, 224, 274, 189, 142, 1));
	}

	public void song1(ArrayList<Beat> notes) { // arraylist of songs
		// every 78 ticks = 1 beat
		// abt 154 bpm
		// notes.add(new Note(0, 0, -10, 0, 0)); // insignificant
		notes.add(new Note(1, 100, 100, 4, 170, 1)); // 980
		notes.add(new Note(1, 200, 100, 6, 80, 2)); // 1305
		notes.add(new Note(1, 300, 100, 9, 90, 3)); // 1905
		notes.add(new Note(1, 400, 100, 11, 150, 4)); // 2370
		notes.add(new Note(1, 400, 300, 13, 70, 1)); // 2685
		notes.add(new Note(1, 600, 300, 14, 180, 2)); // 2980
		notes.add(new Note(1, 600, 100, 15, 130, 3)); // 3150
		notes.add(new Note(1, 800, 300, 16, 80, 4)); // 3300
		notes.add(new Note(1, 1000, 400, 17, 60, 5)); // 3450
		notes.add(new Note(1, 1000, 500, 17, 100, 1)); // 3530
		notes.add(new Note(1, 1000, 600, 17, 190, 2)); // 3610
		notes.add(new Note(1, 900, 500, 18, 90, 3)); // 3700
		notes.add(new Note(1, 800, 400, 18, 160, 4)); // 3780
		notes.add(new Note(1, 600, 400, 19, 10, 5)); // 3850
		notes.add(new Note(1, 400, 400, 19, 100, 6)); // 3920
		notes.add(new Note(1, 400, 600, 20, 80, 7)); // 4000
		notes.add(new Note(1, 200, 600, 22, 10, 1)); // 4480
		notes.add(new Note(1, 200, 500, 25, 20, 2)); // 4990
		notes.add(new Note(1, 200, 400, 26, 120, 3)); // 5290
		notes.add(new Note(1, 300, 300, 28, 40, 4)); // 5610
		notes.add(new Note(1, 400, 200, 30, 0, 1)); // 5940
		notes.add(new Note(1, 300, 200, 30, 80, 2)); // 6020
		notes.add(new Note(1, 200, 200, 30, 150, 3)); // 6100
		notes.add(new Note(1, 100, 200, 31, 50, 4)); // 6180
		notes.add(new Note(1, 100, 100, 31, 120, 5)); // 6250
		notes.add(new Note(1, 200, 100, 32, 0, 6)); // 6330
		notes.add(new Note(1, 300, 100, 32, 80, 7)); // 6400
		notes.add(new Note(1, 400, 100, 32, 150, 8)); // 6480
		notes.add(new Note(1, 500, 100, 33, 30, 9)); // 6550
		notes.add(new Note(1, 300, 200, 34, 70, 1)); // 6860
		notes.add(new Note(1, 500, 100, 35, 10, 2)); // 7020
		notes.add(new Note(1, 700, 300, 35, 160, 3)); // 7170
		notes.add(new Note(1, 500, 500, 36, 90, 4)); // 7320
		notes.add(new Note(1, 700, 500, 38, 30, 1)); // 7642
		notes.add(new Note(1, 700, 100, 39, 160, 2)); // 7954
		notes.add(new Note(1, 900, 100, 41, 70, 3)); // 8266
		notes.add(new Note(1, 1000, 100, 43, 0, 4)); // 8578
		notes.add(new Note(1, 900, 200, 44, 80, 5)); // 8890
		notes.add(new Note(1, 900, 300, 46, 0, 6)); // 9200
		notes.add(new Note(1, 900, 400, 47, 110, 7)); // 9510
		notes.add(new Note(1, 900, 500, 49, 20, 8)); // 9820
		notes.add(new Note(1, 600, 500, 49, 190, 1)); // 9970
		notes.add(new Note(1, 300, 300, 50, 150, 2)); // 10130
		notes.add(new Note(1, 300, 400, 52, 30, 3)); // 10400
		notes.add(new Note(1, 400, 500, 52, 130, 4)); // 10470
		notes.add(new Note(1, 500, 400, 53, 0, 5)); // 10550
		notes.add(new Note(1, 500, 300, 53, 80, 6)); // 10630
		notes.add(new Note(1, 600, 300, 55, 70, 1)); // 11010
		notes.add(new Note(1, 700, 300, 56, 180, 2)); // 11320
		notes.add(new Note(1, 900, 300, 58, 90, 3)); // 11630
		notes.add(new Note(1, 650, 300, 60, 6, 1));
		notes.add(new Note(1, 800, 325, 60, 163, 2));
		notes.add(new Note(1, 700, 325, 61, 41, 3));
		notes.add(new Note(1, 500, 350, 62, 67, 5));
		notes.add(new Note(1, 375, 350, 62, 141, 6));
		notes.add(new Note(1, 275, 350, 63, 22, 7));
		notes.add(new Note(1, 275, 450, 63, 179, 8));
		notes.add(new Note(1, 430, 450, 64, 131, 1));
		notes.add(new Note(1, 530, 450, 65, 9, 2));
		notes.add(new Note(1, 650, 430, 65, 100, 3));
		notes.add(new Note(1, 800, 440, 66, 60, 4));
		notes.add(new Note(1, 950, 425, 67, 2, 5));
		notes.add(new Note(1, 950, 350, 67, 79, 6));
		notes.add(new Note(1, 930, 280, 67, 165, 7));
		notes.add(new Note(1, 750, 280, 68, 119, 8));
		notes.add(new Note(1, 650, 280, 68, 197, 9));
		notes.add(new Note(1, 515, 285, 69, 75, 10));
		notes.add(new Note(1, 400, 300, 69, 150, 11));
		notes.add(new Note(1, 300, 300, 70, 29, 12));
		notes.add(new Note(1, 300, 450, 70, 179, 13));
		notes.add(new Note(1, 550, 300, 71, 136, 1));
		notes.add(new Note(1, 780, 275, 72, 101, 2));
		notes.add(new Note(1, 430, 275, 73, 58, 3));
		notes.add(new Note(1, 450, 425, 73, 130, 4));
		notes.add(new Note(1, 700, 460, 74, 12, 5));
		notes.add(new Note(1, 880, 460, 74, 90, 6));
		notes.add(new Note(1, 850, 375, 74, 165, 7));
		notes.add(new Note(1, 825, 250, 75, 120, 1));
		notes.add(new Note(1, 950, 385, 76, 94, 2));
		notes.add(new Note(1, 920, 450, 76, 158, 3));
		notes.add(new Note(1, 775, 500, 77, 33, 4));
		notes.add(new Note(1, 600, 485, 77, 106, 5));
		notes.add(new Note(1, 450, 475, 77, 179, 6));
		notes.add(new Note(1, 300, 450, 78, 34, 7));
		notes.add(new Note(1, 350, 200, 79, 100, 1));
		notes.add(new Note(1, 635, 165, 79, 184, 2));
		notes.add(new Note(1, 775, 165, 80, 65, 3));
		notes.add(new Note(1, 885, 180, 80, 133, 4));
		notes.add(new Note(1, 900, 225, 81, 15, 5));
		notes.add(new Note(1, 1050, 300, 81, 83, 6));
		notes.add(new Note(1, 1075, 375, 81, 159, 7));
		notes.add(new Note(1, 1065, 450, 82, 36, 8));
		notes.add(new Note(1, 975, 550, 82, 118, 9));
		notes.add(new Note(1, 800, 585, 83, 78, 10)); // break time
		notes.add(new Note(2, 1000, 550, 96, 171, 1));
		notes.add(new Note(2, 1150, 565, 97, 120, 2));
		notes.add(new Note(2, 1000, 625, 98, 76, 3));
		notes.add(new Note(2, 800, 650, 99, 30, 4));
		notes.add(new Note(2, 450, 425, 99, 179, 5));
		notes.add(new Note(2, 800, 265, 100, 135, 6));
		notes.add(new Note(2, 1125, 250, 101, 88, 7));
		notes.add(new Note(2, 1000, 280, 102, 43, 8));
		notes.add(new Note(2, 1100, 400, 102, 199, 9));
		notes.add(new Note(2, 1000, 500, 103, 157, 10));
		notes.add(new Note(2, 1100, 550, 104, 111, 12));
		notes.add(new Note(2, 1100, 625, 106, 27, 1));
		notes.add(new Note(2, 1150, 500, 106, 188, 2));
		notes.add(new Note(2, 975, 500, 107, 129, 3));
		notes.add(new Note(2, 575, 550, 109, 52, 1));
		notes.add(new Note(2, 550, 450, 110, 0, 2));
		notes.add(new Note(2, 525, 350, 110, 161, 3));
		notes.add(new Note(2, 575, 300, 111, 123, 4));
		notes.add(new Note(2, 620, 200, 112, 77, 5));
		notes.add(new Note(2, 650, 300, 113, 36, 6));
		notes.add(new Note(2, 875, 200, 113, 193, 7));
		notes.add(new Note(2, 1000, 230, 114, 69, 1));
		notes.add(new Note(2, 1100, 250, 114, 145, 2));
		notes.add(new Note(2, 1000, 300, 115, 100, 3));
		notes.add(new Note(2, 1025, 425, 116, 49, 1));
		notes.add(new Note(2, 1125, 350, 117, 8, 1));
		notes.add(new Note(2, 950, 450, 117, 80, 1));
		notes.add(new Note(2, 1075, 525, 117, 160, 1));
		notes.add(new Note(2, 1125, 650, 118, 116, 1));
		notes.add(new Note(2, 1000, 625, 119, 80, 1));
		notes.add(new Note(2, 1100, 625, 120, 33, 1));
		notes.add(new Note(2, 825, 600, 120, 106, 1));
		notes.add(new Note(2, 600, 600, 120, 190, 1));
		notes.add(new Note(2, 450, 600, 121, 149, 1));
		notes.add(new Note(2, 225, 600, 122, 104, 1));
		notes.add(new Note(2, 200, 475, 123, 56, 1));
		notes.add(new Note(2, 350, 600, 123, 128, 1));
		notes.add(new Note(2, 500, 600, 124, 13, 1));
		notes.add(new Note(2, 650, 620, 124, 173, 1));
		notes.add(new Note(2, 800, 620, 125, 126, 1));
		notes.add(new Note(2, 925, 620, 126, 80, 1));
		notes.add(new Note(2, 1050, 620, 126, 154, 1));
		notes.add(new Note(2, 1150, 500, 127, 33, 1));
		notes.add(new Note(2, 1025, 620, 128, 2, 1));
		notes.add(new Note(2, 1150, 575, 128, 154, 1));
		notes.add(new Note(2, 950, 525, 129, 104, 1));
		notes.add(new Note(2, 725, 475, 129, 175, 1));
		notes.add(new Note(2, 550, 450, 130, 61, 1));
		notes.add(new Note(2, 500, 250, 131, 22, 1));
		notes.add(new Note(2, 700, 350, 131, 178, 1));
		notes.add(new Note(2, 900, 350, 132, 125, 1));
		notes.add(new Note(2, 1025, 400, 133, 1, 1));
		notes.add(new Note(2, 1150, 450, 133, 81, 1));
		notes.add(new Note(2, 1025, 600, 134, 33, 1));
		notes.add(new Note(2, 950, 525, 134, 190, 1));
		notes.add(new Note(2, 800, 620, 135, 149, 1));
		notes.add(new Note(2, 650, 600, 136, 106, 1));
		notes.add(new Note(2, 425, 500, 136, 186, 1));
		notes.add(new Note(2, 275, 450, 137, 64, 1));
		notes.add(new Note(2, 400, 285, 138, 14, 1));
		notes.add(new Note(2, 650, 250, 138, 174, 1));
		notes.add(new Note(2, 850, 200, 139, 50, 1));
		notes.add(new Note(2, 1050, 200, 139, 132, 1));
		notes.add(new Note(2, 1180, 250, 140, 81, 1));
		notes.add(new Note(2, 1025, 400, 141, 36, 1));
		notes.add(new Note(2, 1000, 525, 141, 197, 1));
		notes.add(new Note(2, 1100, 650, 142, 66, 1));
		notes.add(new Note(2, 1000, 675, 142, 147, 1));
		notes.add(new Note(2, 800, 650, 143, 109, 1));
		notes.add(new Note(2, 650, 650, 144, 69, 1));
		notes.add(new Note(2, 450, 500, 145, 21, 1));
		notes.add(new Note(2, 500, 400, 145, 184, 1)); // break time
		// notes.add(new Note(2, 400, 300, 165, 0, 1));
	}
}