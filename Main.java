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
	static final int CIRCLE_SIZE = 90;
	static final int C_DECREASE = 2;

	// hit values
	static final int[] hitWindow2 = {80 / GAME_SPEED, 140 / GAME_SPEED, 200 / GAME_SPEED};
	// static final int metronome = 78;
	// static final int offset = 187;
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
				song1(notes);
			}
			numNotes = notes.size();
			// System.out.println(numNotes);
			song = s;
			menu = true;
			stats = false;

			// temp = metronome; // redundant
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
			// System.out.println("notes.add(new Note(" + (int)mouse.getX() + ", " + (int)mouse.getY() + ", " + seconds + ", " + ticks + ", 1));");
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

				if (currentTicks >= 38400) {
					stats = true;
				}
			}

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
			// if (comp == 1)
			// 	totTicks += 30;
			if (comp == 2)
				totTicks -= 20;
			if (comp == 3)
				totTicks -= 50;
			hitTick = totTicks;
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

			if (distance <= r && c_r < 70) { // mouse inside the circle

				int diffTicks = gameTicks - hitTick; // human error
				// int dt = diffTicks;
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
				else {
					scoring = 0;
				}
				// System.out.println("Note " + order + ": " + dt + "\t" + scoring + "\t" + hitTick);
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

	public void song1(ArrayList<Beat> notes) { // arraylist of notes in the song
		// every 78 ticks = 1 beat
		// abt 154 bpm
		notes.add(new Note(1, 100, 100, 4, 170, 1));
		notes.add(new Note(1, 200, 100, 6, 80, 2));
		notes.add(new Note(1, 300, 100, 9, 90, 3));
		notes.add(new Note(1, 400, 100, 11, 150, 4));
		notes.add(new Note(1, 400, 300, 13, 70, 1));
		notes.add(new Note(1, 600, 300, 14, 180, 2));
		notes.add(new Note(1, 600, 100, 15, 130, 3));
		notes.add(new Note(1, 800, 300, 16, 80, 4));
		notes.add(new Note(1, 1000, 400, 17, 30, 5));
		notes.add(new Note(1, 1000, 500, 17, 100, 1));
		notes.add(new Note(1, 1000, 600, 17, 190, 2));
		notes.add(new Note(1, 900, 500, 18, 60, 3));
		notes.add(new Note(1, 800, 400, 18, 150, 4));
		notes.add(new Note(1, 600, 400, 19, 10, 5));
		notes.add(new Note(1, 400, 400, 19, 100, 6));
		notes.add(new Note(1, 400, 600, 20, 50, 7));
		notes.add(new Note(1, 200, 600, 21, 170, 1));
		notes.add(new Note(1, 200, 500, 25, 0, 2));
		notes.add(new Note(1, 200, 400, 26, 100, 3));
		notes.add(new Note(1, 300, 300, 28, 20, 4));
		notes.add(new Note(1, 400, 200, 30, 0, 1));
		notes.add(new Note(1, 300, 200, 30, 80, 2));
		notes.add(new Note(1, 200, 200, 30, 150, 3));
		notes.add(new Note(1, 100, 200, 31, 30, 4));
		notes.add(new Note(1, 100, 100, 31, 100, 5));
		notes.add(new Note(1, 200, 100, 31, 180, 6));
		notes.add(new Note(1, 300, 100, 32, 60, 7));
		notes.add(new Note(1, 400, 100, 32, 140, 8));
		notes.add(new Note(1, 500, 100, 33, 20, 9));
		notes.add(new Note(1, 300, 200, 34, 70, 1));
		notes.add(new Note(1, 500, 100, 35, 10, 2));
		notes.add(new Note(1, 700, 300, 35, 160, 3));
		notes.add(new Note(1, 500, 500, 36, 90, 4));
		notes.add(new Note(1, 700, 500, 38, 30, 1));
		notes.add(new Note(1, 700, 100, 39, 160, 2));
		notes.add(new Note(1, 900, 100, 41, 70, 3));
		notes.add(new Note(1, 1000, 100, 42, 180, 4));
		notes.add(new Note(1, 900, 200, 44, 80, 5));
		notes.add(new Note(1, 900, 300, 46, 0, 6));
		notes.add(new Note(1, 900, 400, 47, 110, 7));
		notes.add(new Note(1, 900, 500, 49, 20, 8));
		notes.add(new Note(1, 600, 500, 49, 190, 1));
		notes.add(new Note(1, 300, 300, 50, 150, 2));
		notes.add(new Note(1, 300, 400, 52, 30, 3));
		notes.add(new Note(1, 400, 500, 52, 130, 4));
		notes.add(new Note(1, 500, 400, 53, 0, 5));
		notes.add(new Note(1, 500, 300, 53, 80, 6));
		notes.add(new Note(1, 600, 300, 55, 70, 1));
		notes.add(new Note(1, 700, 300, 56, 180, 2));
		notes.add(new Note(1, 900, 300, 58, 90, 3));
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
		notes.add(new Note(1, 300, 450, 78, 50, 7));
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
		notes.add(new Note(2, 1000, 575, 98, 76, 3));
		notes.add(new Note(2, 800, 550, 99, 30, 4));
		notes.add(new Note(2, 450, 425, 99, 179, 5));
		notes.add(new Note(2, 800, 265, 100, 135, 6));
		notes.add(new Note(2, 1125, 250, 101, 88, 7));
		notes.add(new Note(2, 1000, 280, 102, 43, 8));
		notes.add(new Note(2, 1100, 400, 102, 199, 9));
		notes.add(new Note(2, 1000, 500, 103, 157, 10));
		notes.add(new Note(2, 1100, 500, 104, 111, 12));
		notes.add(new Note(2, 900, 600, 106, 27, 1));
		notes.add(new Note(2, 1150, 500, 106, 188, 2));
		notes.add(new Note(2, 975, 500, 107, 129, 3));
		notes.add(new Note(2, 575, 550, 109, 52, 1));
		notes.add(new Note(2, 550, 450, 110, 0, 2));
		notes.add(new Note(2, 525, 350, 110, 161, 3));
		notes.add(new Note(2, 575, 300, 111, 123, 4));
		notes.add(new Note(2, 620, 200, 112, 77, 5));
		notes.add(new Note(2, 650, 300, 113, 36, 6));
		notes.add(new Note(2, 875, 200, 113, 193, 1));
		notes.add(new Note(2, 1000, 230, 114, 69, 2));
		notes.add(new Note(2, 1100, 250, 114, 145, 3));
		notes.add(new Note(2, 1000, 300, 115, 100, 4));


		notes.add(new Note(2, 1025, 425, 116, 49, 1));
		notes.add(new Note(2, 1125, 350, 117, 8, 2));
		notes.add(new Note(2, 950, 450, 117, 80, 3));
		notes.add(new Note(2, 1075, 525, 117, 160, 4));
		notes.add(new Note(2, 900, 550, 118, 116, 5));
		notes.add(new Note(2, 1000, 575, 119, 80, 6));
		notes.add(new Note(2, 1100, 550, 120, 33, 1));
		notes.add(new Note(2, 825, 500, 120, 106, 2));
		notes.add(new Note(2, 600, 500, 120, 190, 3));
		notes.add(new Note(2, 450, 500, 121, 149, 4));
		notes.add(new Note(2, 225, 500, 122, 104, 5));
		notes.add(new Note(2, 200, 475, 123, 56, 6));
		notes.add(new Note(2, 350, 500, 123, 128, 7));
		notes.add(new Note(2, 500, 500, 124, 13, 8));
		notes.add(new Note(2, 650, 520, 124, 173, 9));
		notes.add(new Note(2, 800, 520, 125, 126, 10));
		notes.add(new Note(2, 925, 520, 126, 80, 11));
		notes.add(new Note(2, 1050, 520, 126, 154, 12));
		notes.add(new Note(2, 1150, 500, 127, 33, 13));
		notes.add(new Note(2, 1025, 420, 128, 2, 1));
		notes.add(new Note(2, 1150, 575, 128, 154, 2));
		notes.add(new Note(2, 950, 525, 129, 104, 3));
		notes.add(new Note(2, 725, 475, 129, 175, 4));
		notes.add(new Note(2, 550, 450, 130, 61, 5));
		notes.add(new Note(2, 500, 250, 131, 22, 6));
		notes.add(new Note(2, 700, 350, 131, 178, 7));
		notes.add(new Note(2, 900, 350, 132, 125, 8));
		notes.add(new Note(2, 1025, 400, 133, 1, 9));
		notes.add(new Note(2, 1150, 450, 133, 81, 10));
		notes.add(new Note(2, 1025, 500, 134, 33, 1));
		notes.add(new Note(2, 950, 525, 134, 190, 2));
		notes.add(new Note(2, 800, 520, 135, 149, 3));
		notes.add(new Note(2, 650, 500, 136, 106, 4));
		notes.add(new Note(2, 425, 500, 136, 186, 5));
		notes.add(new Note(2, 275, 450, 137, 64, 6));
		notes.add(new Note(2, 400, 285, 138, 14, 1));
		notes.add(new Note(2, 650, 250, 138, 174, 2));
		notes.add(new Note(2, 850, 200, 139, 50, 3));
		notes.add(new Note(2, 1050, 200, 139, 132, 4));
		notes.add(new Note(2, 1180, 250, 140, 81, 1));
		notes.add(new Note(2, 1025, 400, 141, 36, 2));
		notes.add(new Note(2, 1000, 525, 141, 197, 3));
		notes.add(new Note(2, 1100, 450, 142, 66, 4));
		notes.add(new Note(2, 1000, 575, 142, 147, 5));
		notes.add(new Note(2, 800, 500, 143, 109, 1));
		notes.add(new Note(2, 650, 500, 144, 69, 2));
		notes.add(new Note(2, 450, 500, 145, 21, 3));
		notes.add(new Note(2, 500, 400, 145, 184, 4)); // break time

		
		notes.add(new Note(3, 625, 325, 165, 106, 1));
		notes.add(new Note(3, 775, 325, 166, 54, 2));
		notes.add(new Note(3, 875, 325, 167, 9, 3));
		notes.add(new Note(3, 1000, 325, 167, 89, 4));
		notes.add(new Note(3, 1100, 325, 167, 171, 5));
		notes.add(new Note(3, 1100, 400, 168, 129, 1));
		notes.add(new Note(3, 1000, 450, 169, 86, 2));
		notes.add(new Note(3, 1075, 550, 170, 43, 3));
		notes.add(new Note(3, 925, 450, 170, 120, 4));
		notes.add(new Note(3, 800, 450, 170, 194, 5));
		notes.add(new Note(3, 700, 450, 171, 153, 6));
		notes.add(new Note(3, 575, 450, 172, 102, 7));
		notes.add(new Note(3, 425, 450, 173, 62, 8));
		notes.add(new Note(3, 250, 425, 173, 135, 9));
		notes.add(new Note(3, 175, 375, 174, 14, 10));
		notes.add(new Note(3, 150, 475, 174, 179, 1));
		notes.add(new Note(3, 275, 475, 175, 135, 2));
		notes.add(new Note(3, 425, 475, 176, 88, 3));
		notes.add(new Note(3, 575, 475, 176, 158, 4));
		notes.add(new Note(3, 700, 475, 177, 32, 5));
		notes.add(new Note(3, 850, 450, 177, 178, 6));


		notes.add(new Note(1, 900, 450, 178, 100, 7));
		notes.add(new Note(1, 900, 375, 179, 57, 8));
		notes.add(new Note(1, 800, 300, 179, 137, 9));
		notes.add(new Note(1, 700, 300, 180, 14, 10));
		notes.add(new Note(1, 575, 325, 180, 162, 1));
		notes.add(new Note(1, 425, 325, 181, 125, 2));
		notes.add(new Note(1, 350, 400, 182, 80, 3));
		notes.add(new Note(1, 425, 450, 182, 156, 4));
		notes.add(new Note(1, 475, 550, 183, 36, 5));
		notes.add(new Note(1, 400, 585, 183, 190, 6));
		notes.add(new Note(1, 275, 525, 184, 146, 7));
		notes.add(new Note(1, 175, 450, 185, 104, 8));
		notes.add(new Note(1, 200, 325, 185, 177, 9));
		notes.add(new Note(1, 225, 225, 186, 56, 10));
		notes.add(new Note(1, 325, 175, 187, 8, 1));
		notes.add(new Note(1, 475, 150, 187, 166, 2));
		notes.add(new Note(1, 600, 150, 188, 124, 3));
		notes.add(new Note(1, 725, 150, 188, 171, 4));
		notes.add(new Note(1, 825, 150, 189, 49, 5));
		notes.add(new Note(1, 925, 150, 189, 94, 6));
	}
}