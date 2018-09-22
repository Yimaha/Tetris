package aha;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.*;
public class Tetris extends JPanel {

	/*This is a triple array that contain specific information about the tetris pieces
	 * first level contains the type of piece it is
	 * second level contains the position the piece is in
	 * Third level contains the specific point location to form the certain position of this piece*/

	private final Point[][][] Tetraminos = 
		{
				// I-Piece
				{
					{ new Point(0, 1), new Point(1, 1), new Point(2, 1), new Point(3, 1) },
					{ new Point(1, 0), new Point(1, 1), new Point(1, 2), new Point(1, 3) },
					{ new Point(0, 1), new Point(1, 1), new Point(2, 1), new Point(3, 1) },
					{ new Point(1, 0), new Point(1, 1), new Point(1, 2), new Point(1, 3) }
				},

				// J-Piece
				{
					{ new Point(0, 1), new Point(1, 1), new Point(2, 1), new Point(2, 0) },
					{ new Point(1, 0), new Point(1, 1), new Point(1, 2), new Point(2, 2) },
					{ new Point(0, 1), new Point(1, 1), new Point(2, 1), new Point(0, 2) },
					{ new Point(1, 0), new Point(1, 1), new Point(1, 2), new Point(0, 0) }
				},

				// L-Piece
				{
					{ new Point(0, 1), new Point(1, 1), new Point(2, 1), new Point(2, 2) },
					{ new Point(1, 0), new Point(1, 1), new Point(1, 2), new Point(0, 2) },
					{ new Point(0, 1), new Point(1, 1), new Point(2, 1), new Point(0, 0) },
					{ new Point(1, 0), new Point(1, 1), new Point(1, 2), new Point(2, 0) }
				},

				// O-Piece
				{
					{ new Point(0, 0), new Point(0, 1), new Point(1, 0), new Point(1, 1) },
					{ new Point(0, 0), new Point(0, 1), new Point(1, 0), new Point(1, 1) },
					{ new Point(0, 0), new Point(0, 1), new Point(1, 0), new Point(1, 1) },
					{ new Point(0, 0), new Point(0, 1), new Point(1, 0), new Point(1, 1) }
				},

				// S-Piece
				{
					{ new Point(1, 0), new Point(2, 0), new Point(0, 1), new Point(1, 1) },
					{ new Point(0, 0), new Point(0, 1), new Point(1, 1), new Point(1, 2) },
					{ new Point(1, 0), new Point(2, 0), new Point(0, 1), new Point(1, 1) },
					{ new Point(0, 0), new Point(0, 1), new Point(1, 1), new Point(1, 2) }
				},

				// T-Piece
				{
					{ new Point(1, 0), new Point(0, 1), new Point(1, 1), new Point(2, 1) },
					{ new Point(1, 0), new Point(0, 1), new Point(1, 1), new Point(1, 2) },
					{ new Point(0, 1), new Point(1, 1), new Point(2, 1), new Point(1, 2) },
					{ new Point(1, 0), new Point(1, 1), new Point(2, 1), new Point(1, 2) }
				},

				// Z-Piece
				{
					{ new Point(0, 0), new Point(1, 0), new Point(1, 1), new Point(2, 1) },
					{ new Point(1, 0), new Point(0, 1), new Point(1, 1), new Point(0, 2) },
					{ new Point(0, 0), new Point(1, 0), new Point(1, 1), new Point(2, 1) },
					{ new Point(1, 0), new Point(0, 1), new Point(1, 1), new Point(0, 2) }
				}
		};

	//There are a total of 7 different colors inside this array, which would be randomly chosen for any new piece
	private final Color[] tetraminoColors = 
		{
				Color.cyan, Color.blue, Color.orange, Color.yellow, Color.green, Color.pink, Color.red
		};

	//Variables that help us keep in track of the game
	//things that help out organizing the current falling piece
	private Point pieceOrigin;
	private int currentPiece;
	private int rotation;
	//variable in charge of changing the difficulty
	private static int difficulty_changes = 0;
	//variable in charge of changing the color of a piece
	private int ramdoncolor;
	//variable that is controlled by int "difficulty_changes" that actually influence the difficulty
	private static int sleep = 900;
	//the boolean that activate when the game is over
	static boolean aha = true;
	//the boolean that activate when the game is paused
	static boolean pauser = true;
	//the boolean that ensure leader board only refresh once due to the thread
	static boolean controler = true;
	//the boolean that tells painter to paint leader board only once at the beginning
	static boolean PaintLeaderBoardAtTheStartOfTheGame = true;
	//A much better type of array that make sure a new piece appear after all the piece has appeared once already.
	//This type of array doesn't have a index and can be extended into any length. Much better than normal array
	private ArrayList<Integer> nextPieces = new ArrayList<Integer>();

	//integer that record user's score.
	private static long score;
	//array to record and print the screen.
	private Color[][] well;
	//music component
	static Clip clip = null;

	// Creates a border around the well and initializes the dropping piece
	private void  init() 
	{
		well = new Color[12][23];
		for (int i = 0; i < 12; i++) 
		{
			for (int j = 0; j < 23; j++) 
			{
				if (i == 0 || i == 11 || j == 22) 
				{
					well[i][j] = Color.GRAY;
				} 
				else 
				{
					well[i][j] = Color.BLACK;
				}
			}
		}
		//repaint is a method from painting component. You need one of this to refresh the interface.
		//(I will only mention it once here since it is a method largely used in the program)
		repaint();
		//look down, the method is right below.
		newPiece();
	}

	// Put a new, random piece into the dropping position
	public void newPiece() 
	{
		//the position where the piece appear.
		pieceOrigin = new Point(5, 0);
		//rotation were automatically set to 0 which is the first rotation
		rotation = 0;
		//if all the pieces has been chosen
		if (nextPieces.isEmpty()) 
		{
			//add number 0, 1, 2, 3, 4, 5, 6 to the array list
			Collections.addAll(nextPieces, 0, 1, 2, 3, 4, 5, 6);
			//shuffle it so the order is random.
			Collections.shuffle(nextPieces);
		}
		//get the first index in the array
		currentPiece = nextPieces.get(0);
		//remove the first index so the new piece would be chosen in the left over.
		nextPieces.remove(0);
		//checking if the old piece collide with the new piece.
		for(Point l : Tetraminos[currentPiece][rotation])
		{
			if(well[l.x + 5][l.y] != Color.BLACK)
			{	
				//game over trigger
				aha = false;
				//make sure system only refresh leaderboard once
				if(controler)
				{
					newleaderboard(leaderboard(),score);
					controler = false;
					repaint();
				}

			}
		}

	}

	// Collision test for the dropping piece
	private boolean collidesAt(int x, int y, int rotation) 
	{
		for (Point p : Tetraminos[currentPiece][rotation]) 
		{
			//if any given location is not black(background color)
			//it means that the piece collides. so function will tell the main program you cannot take the move.
			if (well[p.x + x][p.y + y] != Color.BLACK) 
			{
				return true;
			}
		}
		return false;
	}

	// Rotate the piece clockwise or counterclockwise
	public void rotate(int i) 
	{
		//take current rotation + the user desire direction(+1 or -1) and mod it by 4.
		//we only need the remainder
		int newRotation = (rotation + i) % 4;
		//if the location reach -1, then it automatically resent to 3(highest possible int after mod 4)
		if (newRotation < 0) 
		{
			newRotation = 3;
		}
		//check if the rotation would collide with wall or other piece
		if (!collidesAt(pieceOrigin.x, pieceOrigin.y, newRotation)) 
		{
			//if not, set location into the new rotation
			rotation = newRotation;
		}
		//refresh
		repaint();
	}

	// Move the piece left or right
	public void move(int i) 
	{
		//if the piece don't collide with wall or other piece
		if (!collidesAt(pieceOrigin.x + i, pieceOrigin.y, rotation)) 
		{
			//move the x -1 or +1 base on user input
			pieceOrigin.x += i;	
		}
		//refresh
		repaint();
	}

	// Drops the piece one line or fixes it to the well if it can't drop
	public void dropDown() 
	{
		//if dropping down doesn't collides with wall or other pieces
		if (!collidesAt(pieceOrigin.x, pieceOrigin.y + 1, rotation)) 
		{
			//move the piece down 1
			pieceOrigin.y += 1;
		} 
		else 
		{
			//if not, then fix to well(become one of the pieces at the bottom)
			fixToWell();
			//choose a color for new piece
			ramdoncolor = (int)(Math.random()*7);
		}	
		//refresh
		repaint();

	}

	// Make the dropping piece part of the well, so it is available for new calculation
	// collision detection.
	public void fixToWell() 
	{
		//run through the current piece's current position
		for (Point p : Tetraminos[currentPiece][rotation]) 
		{
			//add the color of the piece to the background
			well[pieceOrigin.x + p.x][pieceOrigin.y + p.y] = tetraminoColors[ramdoncolor];
		}
		//check if the road is clear(can be trigger or not trigger) 
		clearRows();
		//add a new piece
		newPiece();
	}
	//if clearRows is triggered, it will delete the row by moving the block in the back ground down
	public void deleteRow(int row) 
	{
		//row - 1 because we ignore the one at the bottom which is the level we need to remove
		for (int j = row-1; j > 0; j--) 
		{
			for (int i = 1; i < 11; i++) 
			{
				//the line below becomes the line above
				well[i][j+1] = well[i][j];
			}
		}
	}

	// Clear completed rows from the field and award score according to
	// the number of simultaneously cleared rows.
	public void clearRows() 
	{
		//boolean to see if the it makes the full line.
		boolean gap;
		//the integer that keeps in track of the score
		int numClears = 0;
		//run through all the vertical lines	
		for (int j = 21; j > 0; j--) 
		{
			//original set to false
			gap = false;
			for (int i = 1; i < 11; i++) 
			{
				//if a line of block is not formed(contain no back ground color)
				if (well[i][j] == Color.BLACK)
				{
					//set gap to true and break the current for loop through each line's x position
					gap = true;
					break;
				}
			}
			//if there is no gap(which means the line is full of block
			if (!gap) 
			{
				//delete the current roll
				deleteRow(j);
				//reduce the line since everything has refreshed(everything moved down by 1)
				j += 1;
				//telling the computer that user has complete one line of block.
				numClears += 1;
			}
		}

		//base on user's total clear(maximum of 4)
		switch (numClears) 
		{
		case 1:
			//base on the sleep time(game difficulty), user is rewarded with different amount of score.(shorter sleep the better)
			switch(sleep)
			{
			case 900: score += 100;
			break;
			case 400: score += 200;
			break;
			case 100: score += 500;
			break;
			case 0: score += 2000;
			break;
			}
			break;
		case 2:
			switch(sleep)
			{
			case 900: score += 300;
			break;
			case 400: score += 600;
			break;
			case 100: score += 1500;
			break;
			case 0: score += 6000;
			break;
			}
			break;
		case 3:
			switch(sleep)
			{
			case 900: score += 600;
			break;
			case 400: score += 2000;
			break;
			case 100: score += 5000;
			break;
			case 0: score += 20000;
			break;
			}
			break;
		case 4:
			switch(sleep)
			{
			case 900: score += 1000;
			break;
			case 400: score += 5000;
			break;
			case 100: score += 20000;
			break;
			case 0: score += 100000;
			break;
			}
			break;
		}
	}

	//try to get the leaderboard score from the txt file
	public static long[] leaderboard()
	{
		//declare scanner
		Scanner input = null;
		try
		{
			input = new Scanner(new File("Leaderboard.txt"));
		}catch(Exception e){}
		//declare a long array
		long[] a = new long[5];
		//set the all index to 0
		for(int i =0; i < 5; i ++)
		{
			a[i] = 0;
		}
		//set up counter
		int counter = 0;
		//read all the long number in the txt file and put it into the array
		while(input.hasNextLine())
		{
			String b = input.nextLine();
			a[counter]= Long.parseLong(b);
			counter = counter + 1;
		}
		//return the long array
		return a;
	}
	//record new score
	public void newleaderboard(long[] x,long y)
	{
		//declare printwriter
		PrintWriter writer = null;
		try {
			writer = new PrintWriter("Leaderboard.txt", "UTF-8");
		} catch (Exception e) 
		{
			e.printStackTrace();
		}
		//Basically a boolean in case the user didn't beat the leader board.
		int java = 0;
		//if user beat the leader board, refresh the leader board
		for(int i = 0; i< x.length; i++)
		{
			if(y >= x[i])
			{
				java = 1;
				for(int j = 4; j >i; j--)
					x[j] = x[j-1]; 
				x[i] = y;
				for(int j = 0; j< 5; j++)
				{
					writer.println(x[j]);
				}
				break;
			}
		}
		//if java is not triggered, then output the old list back
		if(java == 0)
			for(int j = 0; j< 5; j++)
			{
				writer.println(x[j]);	
			}
		writer.close();


	}

	// drawPiece and paintComponent for the repaint() method. without those the repaint method would not do anything
	// also most of the painting setting is here
	// Draw the falling piece
	private void drawPiece(Graphics g) 
	{	
		g.setColor(tetraminoColors[ramdoncolor]);
		for (Point p : Tetraminos[currentPiece][rotation]) 
		{
			g.fillRect((p.x + pieceOrigin.x) * 26, 
					(p.y + pieceOrigin.y) * 26, 
					25, 25);
		}
	}
	//draw the leaderboard once at the beginning and everytime after the game is over
	private void paintleaderboard(Graphics g)
	{
		g.setColor(Color.black);
		g.fillRect(317, 87, 200, 70);
		g.setColor(Color.white);
		int counter = 1;
		final long[] token = leaderboard();
		g.drawString("Leader Board:", 317, 97);
		for(int i = 107; i <= 147; i += 10)
		{
			g.drawString(counter+". " + token[counter-1], 317, i);
			counter = counter +1;
		}

	}
	//draw out how to use the difficulty scrollbar(only trigger once)
	private void drawdifficulty(Graphics g)
	{
		g.setColor(Color.black);
		g.drawString("Difficulty(click restart to set new difficulty)", 317, 167);
	}
	@Override 
	//where repaint() method reach.
	public void paintComponent(Graphics g)
	{
		// Paint the well
		g.fillRect(0, 0, 26*12, 26*23);
		for (int i = 0; i < 12; i++) 
		{
			for (int j = 0; j < 23; j++) 
			{
				g.setColor(well[i][j]);
				g.fillRect(26*i, 26*j, 25, 25);
			}
		}
		//draw the pausing in the game
		if(!pauser)
		{
			g.setColor(Color.WHITE);
			g.drawString("Game Paused", 26*5-1, 20);
		}

		// Draw the currently falling piece
		drawPiece(g);
		//draw difficulty instruction of the game
		if(PaintLeaderBoardAtTheStartOfTheGame)
			drawdifficulty(g);
		//draw leader board of the game
		if(PaintLeaderBoardAtTheStartOfTheGame || controler == false)
		{
			paintleaderboard(g);
			PaintLeaderBoardAtTheStartOfTheGame = false;
		}


		// if game is over, clean the board and set everything to game over.
		if(!aha)
		{
			for (int i = 0; i < 12; i++) 
			{
				for (int j = 0; j < 23; j++) 
				{

					if(!(i == 0 || i == 11|| j == 22))
					{
						g.setColor(Color.BLACK);
						g.fillRect(26*i, 26*j, 25, 25);
					}
				}
			}
			g.setColor(Color.WHITE);
			g.drawString("GAMEOVER", 5*26, 5*26);
		}
		//draw the score
		g.setColor(Color.WHITE);
		g.drawString("" + score, 26*9, 25);
	}
	//method that turns on the continuously looping BGM
	public static void musicon()
	{
		try{
			AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File("music.wav"));
			clip = AudioSystem.getClip();
			clip.open(audioInputStream);
		}catch(Exception e)
		{}
		clip.loop(Clip.LOOP_CONTINUOUSLY);
		clip.start();
	}
	//method that turns it off and refresh it to the beginning(BGM).
	public static void musicoff()
	{
		try
		{
			clip.stop();
		}catch(Exception e)
		{

		}
	}
	//main
	public static void main(String[] args) 
	{
		//declare JFrame
		JFrame f = new JFrame("Tetris");
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		//set size to correct position
		f.setSize(12*26+360, 26*23+25);
		Dimension a = new Dimension(12*26+310, 26*23+25);
		f.setPreferredSize(a);
		f.setVisible(true);
		//turn off resizable
		f.setResizable(false);
		//declare JPanel, and use Absolute layout which is suppose to be 1000000000000000000....00000 times better than gridbaglayout
		final Tetris game = new Tetris();
		game.setLayout(null);
		//run through the basic setting of the game
		game.init();
		//add panel to the frame
		f.add(game);

		// Keyboard controls


		KeyListener a1 = new KeyListener() 
		{
			public void keyTyped(KeyEvent e) 
			{}


			public void keyPressed(KeyEvent e) 
			{
				if(aha)
				{
					switch (e.getKeyCode()) 
					{
					//up key
					case KeyEvent.VK_UP:
						game.rotate(-1);
						break;
						//down key
					case KeyEvent.VK_DOWN:
						game.rotate(+1);
						break;
						//left key
					case KeyEvent.VK_LEFT:
						game.move(-1);
						break;
						//right key
					case KeyEvent.VK_RIGHT:
						game.move(+1);
						break;
						//space bar
					case KeyEvent.VK_SPACE:
						game.dropDown();
						switch(sleep)
						{
						case 900: score += 1;
						break;
						case 400: score += 2;
						break;
						case 100: score += 5;
						break;
						case 0: score += 20;
						break;
						}
						break;
					} 
				}
			}

			public void keyReleased(KeyEvent e) 
			{}
		};
		f.addKeyListener(a1);

		//declare all the Jelements
		JButton pause = new JButton("Pause");
		JButton StartGame = new JButton("restart the game");
		JButton Mute = new JButton("BGM on");
		JScrollBar difficulty = new JScrollBar(JScrollBar.HORIZONTAL,0,50,0,200);

		//the mute button
		Mute.addActionListener(new ActionListener()
		{
			boolean playing = false;
			@Override
			public void actionPerformed(ActionEvent e)
			{
				if(playing)
				{
					//turn on music
					playing = false;
					Mute.setText("BGM on");
					musicoff();
				}
				else
				{
					//turn off music
					playing = true;
					Mute.setText("BGM off");
					musicon();
				}
			}
		});
		//ignore problem with space bar and focusing problem
		Mute.setFocusable(false);
		Mute.getInputMap().put(KeyStroke.getKeyStroke("SPACE"), "none");
		//set position
		Mute.setBounds(520, 5, 100, 40);
		//add it to panel
		game.add(Mute);

		//difficulty scroll bar
		//set the back ground of the scroll bar
		difficulty.setBackground(new Color(105,0,0));
		//mentioned before
		difficulty.setFocusable(false);
		difficulty.addAdjustmentListener(new AdjustmentListener()
		{
			public void adjustmentValueChanged(AdjustmentEvent e)
			{
				//using rgb to set color
				difficulty.setBackground(new Color(e.getValue()+105, 0, 0));
				difficulty_changes = e.getValue()/50;
			}
		});
		//set position
		difficulty.setBounds(317, 179, 200, 20);
		difficulty.setVisible(true);
		//add it to the panel
		game.add(difficulty);

		//restart game button
		StartGame.setFocusable(false);
		StartGame.getInputMap().put(KeyStroke.getKeyStroke("SPACE"), "none");
		ActionListener start = new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				//when restarted, program take in new difficulty input by user
				//and change the game to new difficulty
				//with different difficulty the scoring system is also different
				switch(difficulty_changes)
				{
				case 0: sleep = 900;
				break;
				case 1: sleep = 400;
				break;
				case 2: sleep = 100;
				break;
				case 3: sleep = 0;
				}
				aha = true;
				//run through the basic setting again.
				game.init();   
				//refresh score
				score = 0;
				controler = true;
				//if the game is paused refresh the pause button
				if(!pauser)
				{
					pauser = true;
					f.addKeyListener(a1);
					pause.setText("Pause");
				}

			}
		};
		StartGame.addActionListener(start);
		//set bound
		StartGame.setBounds(317, 5, 200, 40);
		StartGame.setVisible(true);
		//add it to the panel
		game.add(StartGame);


		//pause button
		pause.setFocusable(false);
		pause.getInputMap().put(KeyStroke.getKeyStroke("SPACE"), "none");
		ActionListener pausing = new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{				
				//if game is not paused
				if(pauser)
				{
					//remove the keylistner
					f.removeKeyListener(a1);
					//change the activation of the button
					pauser = false;
					pause.setText("Resume");
					//repaint the screen
					game.repaint();
				}
				//if game is paused
				else if(!pauser)
				{
					//add back the key listener
					f.addKeyListener(a1);
					//change the activation of the button
					pauser = true;
					pause.setText("Pause");
					pause.setBounds(317, 46, 200, 40);
					//repaint the screen
					game.repaint();
				}
			}
		};
		pause.addActionListener(pausing);
		//set the bounds
		pause.setBounds(317, 46, 200, 40);
		pause.setVisible(true);
		// add it to the panel
		game.add(pause); 




		// Make the falling piece drop every set of time base on the difficulty
		new Thread() 
		{
			@Override 
			public void run() 
			{
				//this will loop no matter what
				while(true)
				{
					//if game is not paused
					while (pauser) 
					{
						try 
						{
							//when game is not over
							if(aha == true)
								//let the thread sleep for certain amount of time
							{
								//if game is suddenly paused
								//break from this loop and enter the always running while loop
								Thread.sleep(sleep);
								if(!pauser)
									break;
								//if not paused, then game drop down by 1 block
								game.dropDown();
							}
							// basically, if nothing is running in a thread, system would turn it done to prevent crushing.
							// I need to add in a little sleep so system remember the thread is running
							Thread.sleep(100);

						} catch ( InterruptedException e ){}
					}
					try {
						//same reason as above
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
			//start the thread that would keep on going through out the game
		}.start();




	}
}
