package edu.ucsb.cs56.projects.games.snake;

/**
 * A class that paint the snake and modify its attribute depending
 * on user input
 * @author Sam Dowell (original)
 * @author Sam Min, Eric Huang (CS56, Spring 2013)
 */

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.Timer;
import javax.swing.*;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.*;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;


//Note to self: SnakeFrame is similar to Game.java

public class SnakeFrame extends JFrame implements KeyListener,MouseListener {
    // This variable determines the speed of the snake, and corresponds to the difficulty
    private int speed= 80;
    
    //create private variables for the snakeFrame size
    private static int frameWidth;
    private static int frameHeight;

    // RNG for deciding the fruit to place
    private Random rng = new Random();

    // PowerUp variables for each player
    private String p1PowerUp = "NONE";
    private String p2PowerUp = "NONE";

    // PowerUp timer count for each player
    private int p1SpeedPowerUpStart = 0;
    private int p1WidthPowerUpStart = 0;
    private int p1GhostPowerUpStart = 0;
    private boolean p1GhostFlag = false;
    private boolean p1WidthFlag = false;
    
    private final int BOX_HEIGHT = 20;
    private final int BOX_WIDTH = 20;
    private  int GRID_WIDTH ;
    private  int GRID_HEIGHT;
 
    
    private int p2SpeedPowerUpStart = 0;
    private int p2WidthPowerUpStart = 0;
    private int p2GhostPowerUpStart = 0;
    private boolean p2GhostFlag = false;
    private boolean p2WidthFlag = false;
    
    
    //variables for determining whether or not to grow the snake
    boolean growsnake = false;
    boolean growsnake2 = false;

    HighScore hScore = HighScore.loadHighScore();
    HighScore hScore2 = HighScore.loadHighScore2();
    HighScore hScore3 = HighScore.loadHighScore3();
    ArrayList<Integer> scores = new ArrayList<>();

    // Score of fruit eaten, Head color counter, win/loss variable
    private int score = 0, score1 = 0, highScore = hScore.getScore();
    private int score2 = 0, loser = 0, menu = 0, players = 1;
    private int fruits = 50, screenSize = 0, playagain = 0;
    private int highScore2 = hScore2.getScore();
    private int highScore3 = hScore3.getScore();

    //create a boolean that gets switched to false when you press an arrow key
    //and turns to true when updateSnakes() is called to avoid the snake turning
    //into itself and losing the game
    public static boolean waitFlag = true;

    // Create boolean values for when to play again
    private boolean pause = false;
    private boolean frameresized = false, controls = false, hasIntersected;

    // Create offscreen image for double buffering
    private Image offscreen;

    // Create graphics
    Graphics g;


    
    // Create stopwatch
    private Stopwatch watch = new Stopwatch();
    private Stopwatch fruittimer = new Stopwatch();
    private boolean puddles = false;
    private boolean walls = false;
    public static boolean reversed = false;
    
    //define getters
    public int getPlayers(){ return players;}
    public int getScreenSize(){ return screenSize;}
    public boolean getPuddles(){ return puddles;}
    public boolean getWalls(){ return walls;}
    public static int getFrameWidth(){ return frameWidth; }
    public static int getFrameHeight(){ return frameHeight; }
    
    private JPanel panel2;
    private JLabel backgroundImage = new JLabel();
    private JButton onePlayerButton = new JButton("1 player");


    /*
     * THIS IMPLEMENTATION WILL HAVE TO BE REFACTORED ELSEWHERE
     * AT A FUTURE TIME.
     * 
     * Created an inner listener class that passes keyevents to the corresponding event listener classes.
     * This allows for us to remove key-press implementation from the SnakeFrame object, and narrow its
     * purpose to be more in line with a GUI component.
     */
    public GameObjectHandler GOH;
    class GameObjectHandlerListener extends Container implements KeyListener{
    	public GameObjectHandlerListener(){
    		addKeyListener(this);
    		//this.setFocusable(true);
    	}
    	public void keyPressed(KeyEvent e){	
	    if (waitFlag) {
		GOH.keyPressed(e);
		SnakeFrame.this.keyPressed(e);
		
	    }
	}
	
    	public void keyTyped(KeyEvent e){}
    	public void keyReleased(KeyEvent e){}
    	
    }
    /*************************************************************/
    
    class task implements ActionListener{		
	@Override
	public void actionPerformed(ActionEvent e) {
	    doaction();
	}
    };
    
    private task atask = new task();
    private Timer atime = new Timer(speed,atask);
    //JLabel label, m;
    //JButton button;
    
    File f;
    AudioInputStream ais;
    Clip clip;

    /** Creates new form SnakeFrame */
    public SnakeFrame() {
	// Call JFrame constructor and give it the title "Snake"
        super("Snake");
        //this.setBackground(Color.BLUE);

        // Initialize components
        initComponents();

        setScreenBoundaries();
        setLocationRelativeTo(null);
        this.setResizable(false);


		try{	    		
			f = new File("snake/sound/eat.wav");	    		
			ais = AudioSystem.getAudioInputStream(f);
		    clip = AudioSystem.getClip();
		 	clip.open(ais);
		}
		catch(Exception e)
		{
		  	e.printStackTrace();
		}


        // Add a KeyListener
        addKeyListener(this);
        GameObjectHandlerListener GOHL = new GameObjectHandlerListener();
        this.add(GOHL);
        addKeyListener(GOHL);
        GOHL.setFocusable(true);
        GOHL.requestFocus();
        
        

        //mouse listener...........this is mouse listener
        addMouseListener(this);

        // Create random starting X and Y coordinate for fruit
        BasicFruit fruit_0 = new BasicFruit();
        
        // Create the snake objects
        GOH = new GameObjectHandler();
        
        // Add the fruit to the GameObjectHandler
        GOH.addBasicFruit(fruit_0);

        // Create offscreen image
        offscreen = this.createImage(this.getWidth(), this.getHeight());

        // Create Timer
        atime.start();
    }

  
    
    public void save() {
    	
	scores.removeAll(scores);
	scores.add(highScore);
	scores.add(highScore2);
	scores.add(highScore3);
	
	if (loser > 0) {
	    scores.add(score);
	    Collections.sort(scores);
	    Collections.reverse(scores);
	    
	    hScore.setScore(scores.get(0));
	    hScore2.setScore(scores.get(1));
	    hScore3.setScore(scores.get(2));
	    try {
		hScore.saveHighScore();
		hScore2.saveHighScore2();
		hScore3.saveHighScore3();
	    }
	    catch(Exception exc) {
		exc.printStackTrace(); 
	    }
	}
    }
    
    public void doaction() {

	save();

	// If user chooses yes, restart the program
	if (playagain == 1) {
	    resetGame();
	} else if (playagain == 2){
	    System.exit(0);
	}

	if (menu != 0) {
	    if (frameresized == false) {
		//Set window size
		setScreenBoundaries();
		setLocationRelativeTo(null);
		this.setVisible(true);
		frameresized = true;
		offscreen = this.createImage(this.getWidth(), this.getHeight());

		// Create random starting X and Y coordinate for fruit
		FruitGenerator.generateNewFruit(GOH); 
		GOH.restartPlayer(1);

		// If game is in progress and is not paused,
		// send one block from the back to the front
		GOH.player_1.shiftSnake();
		GOH.player_1.removeGameObject(GOH.player_1.size() - 1);
		if (players == 2){
		    // If the game starts as 2 player, initialize two player settings
		    GOH.restartPlayer(2);
		}
		fruittimer.start();
	    }
	    if (pause == false) {
		GOH.player_1.shiftSnake();
		GOH.player_1.removeGameObject(GOH.player_1.size() - 1);
		if (players == 2){
		    GOH.player_2.shiftSnake();
		    GOH.player_2.removeGameObject(GOH.player_2.size() - 1);
		}
	    }
	}

	// End game when the fruits run out in two player game
	if (fruits <= 0) {
	    loser = 1;
	}
	// If in two player, and a fruit has not been eaten for 30 seconds
	// create new random location for fruit
	if (players == 2 && loser == 0 && fruittimer.getSeconds() >= 30 && menu != 0) {
	    FruitGenerator.MoveFruitRandomly(GOH); 
	    fruittimer.start();
	}

	repaint();
    }


    public void resetGame(){
	//Set window size
	setScreenBoundaries();

	if(score > highScore){
	    highScore = score;
	}

	setLocationRelativeTo(null);
	frameresized = false;
	controls = false;
	score = 0;
	fruits = 50;
	score1 = 0;
	score2 = 0;

	// Reset powerup variables
	Snake.WIDTH = 15;
	p1PowerUp = "NONE";
	p2PowerUp = "NONE";
	p1SpeedPowerUpStart = 0;
	p1WidthPowerUpStart = 0;
	p1GhostPowerUpStart = 0;
	p1GhostFlag = false;
	p1WidthFlag = false;
	p2SpeedPowerUpStart = 0;
	p2WidthPowerUpStart = 0;
	p2GhostPowerUpStart = 0;
	p2GhostFlag = false;
	p2WidthFlag = false;
	
	// Create random starting X and Y coordinate for fruit
	FruitGenerator.generateNewFruit(GOH); 

	// Restart the state of the new players.
	GOH.newPlayers();

	// Start stopwatch
	watch.start();

	// Reset playagain variable
	playagain = 0;

	// Reset loser variable
	loser = 0;
	
    }
    
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
	
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
	
        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 400, Short.MAX_VALUE));
        layout.setVerticalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 300, Short.MAX_VALUE));
	
        pack();
    }// </editor-fold>//GEN-END:initComponents

    
    Font font0 = new Font("Times New Roman", Font.PLAIN, 12);
    Font font1 = new Font("Helvetica", Font.PLAIN, 15);
    Font font2 = new Font("Comic Sans", Font.BOLD, 20);
    FontMetrics fm = this.getFontMetrics(font1);
    
    public Font getFont0(){ return font0; }
    public Font getFont1(){ return font1; }
    public Font getFont2(){ return font2; }
    public FontMetrics getFm(){ return fm;}


    public void paint(Graphics graph) {
	// Create font
	// Begin painting
	// Get the offscreen graphics for double buffer
   
	g = offscreen.getGraphics();
	g.setFont(font1);
	
	// Create a menu screen for the initial launch, show controls screen if indicated
	if (menu == 0) {
	    if (controls == false) {
		showMainMenu();
	    }
	    else {
		showGameControls();
	    }
	}

	// If the user doesn't pause the game, update the snakes position
	else if (pause == false) {
	    updateSnakes();
	}
	
	// If the player pauses, display that the game is paused and the resume option
	if (pause == true && players == 1) {
	    pauseGame(250);
	} else if (pause == true && players== 2) {
	    pauseGame(375);
	}
	
	// Prepare next image for double buffer
	graph.drawImage(offscreen, 0, 0, this);
    }
    
    public void pauseGame(int pauseSize) {
	g.setColor(Color.WHITE);
	g.drawString("Paused", this.getWidth() / 2 - fm.stringWidth("Paused") / 2, pauseSize);
	g.drawString("[P] to Resume", this.getWidth() / 2 - fm.stringWidth("[P] to Resume") / 2, pauseSize + 20);
	g.drawString("[M] Return to Menu", this.getWidth() / 2 - fm.stringWidth("[M] Return to Menu") / 2, pauseSize + 40);
    }

    public void updateSnakes() {

	// If the player has not yet lost, paint the next image of snake movement
	if (loser == 0) {

	    // Set the color to green to paint the background
	    Color c = new Color(2,115,153);
	    Color b = new Color(61,226,235);
	    g.setColor(c);

	    //Set window size
	    setScreenBoundaries();
	    g.fillRect(0,0,SnakeFrame.frameWidth,SnakeFrame.frameHeight);
	    if (screenSize==0){
		GRID_WIDTH=500;
		GRID_HEIGHT=500;
	    }
	    else if (screenSize==1){
		GRID_WIDTH=600;
		GRID_HEIGHT=600;
	    }
	    else {
		GRID_WIDTH=700;
		GRID_HEIGHT=700;
	    }


	    
            g.clearRect(0, 0, BOX_WIDTH * GRID_WIDTH, BOX_HEIGHT * GRID_HEIGHT);
	    
	    DrawGrid(g);
	    // Set color to red and paint the fruit
	    g.setColor(BasicFruit.getColorObject());
	    g.fillRect(GOH.getBasicFruit(0).getX(), GOH.getBasicFruit(0).getY(), BasicFruit.WIDTH, BasicFruit.WIDTH);
	    if(puddles){
		g.setColor(b);
		g.fillOval(50,90,100,50);
		g.fillOval(this.getWidth()-120,200, 60, 90);
		g.fillOval(this.getWidth()/3,this.getHeight()-300,80,30);
		g.fillOval(80,this.getHeight()-100,110,60);
		g.fillOval(this.getWidth()-200,this.getHeight()-100,70,70);
	    }

	  

	    
	 
		headToWallCollision();
	    
	    //draw the snake and fruit
	    
	    drawSnakesAndFruit(players);

	    // If a Snake ate a fruit, make the snake grow one block
	    if (growsnake) {
		GOH.player_1.shiftSnake();
	    }
	    if (players == 2) {
		if (growsnake2) {
		    GOH.player_2.shiftSnake();
		}
	    }

	    // Check for if fruit relocated on top of tail
	    fruitRespawnCollisionDetect(players);

	    checkPowerUp();

	    
	    if (players == 1) {
		if (!p1GhostFlag){
		    headToTailCollisionSinglePlayer();
		}
	    } else {
		checkSnakeCollision();
	    }

	    g.setColor(Color.WHITE);
	    g.fillRect(0, 0, this.getWidth(), 100);
	    // Display the score and time
	    g.setColor(Color.BLACK);
	    if (players == 1){
		g.drawString("Score: " + score, 50, 50);
		g.drawString("High Score: " + highScore,50,70);
		g.drawString("Time: " + watch.toString(), this.getWidth() - 50 - fm.stringWidth("Time: " + watch.toString()),50);
	    } else{
		g.drawString("Orange Score: " + score2, 50, 50);
		g.drawString("Time: " + watch.toString(), this.getWidth()/2-fm.stringWidth("Time: "+watch.toString())/2,50);
		g.drawString("New Fruit in: " + (30 - fruittimer.getSeconds()),50, 75);
		g.drawString("Green Score: " + score1, this.getWidth() - 50 - fm.stringWidth("Green score: " + 1), 50);
	    }

	    // Display Pause option if game is in progress
	    if (loser == 0) {
		int buffer = 70;
		if (players == 2){
		    buffer = 75;
		}
		g.drawString("[P] to Pause", this.getWidth() - 50 - fm.stringWidth("[P] to Pause"), buffer);
	    }
	} //end if loser == 0


	if (loser > 0) {
	    // If the player has lost, display message
	    g.setColor(Color.RED);
	    if (players == 1){
		g.drawString("Game Over!", this.getWidth() / 2 - fm.stringWidth("Game Over!") / 2, 240);
		g.setColor(Color.WHITE);
	   /* } else {
		g.drawString("Game Over!", this.getWidth() / 2 - fm.stringWidth("Game Over!") / 2, 280);
		g.setColor(Color.WHITE);
		if (GOH.player_1.size() == GOH.player_2.size()) {
		    g.drawString("Tie Game!", this.getWidth() / 2 - fm.stringWidth("Tie Game!") / 2, 300);
		} else if (GOH.player_1.size() > GOH.player_2.size()) {
		    g.drawString("Green Wins!", this.getWidth() / 2 - fm.stringWidth("Green Wins!") / 2, 300);
		} else {
		    g.drawString("Orange Wins!", this.getWidth() / 2 - fm.stringWidth("Orange Wins!") / 2, 300);
		}*/
		g.drawString("Do you want to play again?", this.getWidth()/2-fm.stringWidth("Do you want to play again?") / 2, 260);
	    g.drawString("Yes [Y] or No [N]", this.getWidth() / 2 - fm.stringWidth("Yes [Y] or No [N]") / 2, 280);
	    g.drawString("Return to Menu [M]", this.getWidth() / 2 - fm.stringWidth("Return to Menu [M]") / 2, 300);
	    }
	    
	}
	
	waitFlag = true;
    }


    public void checkSnakeCollision(){

	// Check for collisions between snakes if the game has not yet ended
	if (fruits > 0) {

	    //check to see if the snakes underwent a head to head collision
	    Rectangle p1head = new Rectangle(GOH.player_1.getGameObjectXPos(0), GOH.player_1.getGameObjectYPos(0), Snake.WIDTH, Snake.WIDTH);
	    Rectangle p2head = new Rectangle(GOH.player_2.getGameObjectXPos(0), GOH.player_2.getGameObjectYPos(0), Snake.WIDTH, Snake.WIDTH);
	    if (p1head.intersects(p2head)) {
		if(!p1GhostFlag)
		    GOH.restartPlayer(1);
		if(!p2GhostFlag)
		    GOH.restartPlayer(2);
	    }

	    // Create loop to check if the head has intersected the tail (Game Over)
	    if (!p2GhostFlag)
		hasIntersected = headToTailCollision(GOH.player_1, 1);

	    // Check for if the new random location is on top of the other snake
	    // In this case create a new random location
	    respawnCollisionPlayer_1();

	    // Check for collision between head of one snake to tail of the other
	    for (int q = GOH.player_2.size() - 1; q > 0; q--) {
		Rectangle p2tail = new Rectangle(GOH.player_2.getGameObjectXPos(q), GOH.player_2.getGameObjectYPos(q), Snake.WIDTH, Snake.WIDTH);
		p1head = new Rectangle(GOH.player_1.getGameObjectXPos(0), GOH.player_1.getGameObjectYPos(0), Snake.WIDTH, Snake.WIDTH);
		//if the head of player one intersects player two's tail, we must now check to see if player two's
		//head intersects player one's tail. if not, only player one is reset
		if (p1head.intersects(p2tail) && !p1GhostFlag) {
		    hasIntersected = true;
		    GOH.respawnPlayer(GOH.player_1);
		    score1 = 0;
		    loser++;

		    g.setColor(Color.RED);
		    g.drawString("Game Over!", this.getWidth() / 2 - fm.stringWidth("Game Over!") / 2, 240);
		    g.setColor(Color.ORANGE);
		    g.drawString("Orange Wins!", this.getWidth() / 2 - fm.stringWidth("Orange Wins!") / 2, 400);
		    g.drawString("Do you want to play again?", this.getWidth()/2-fm.stringWidth("Do you want to play again?") / 2, 260);
	   		g.drawString("Yes [Y] or No [N]", this.getWidth() / 2 - fm.stringWidth("Yes [Y] or No [N]") / 2, 280);
	    	g.drawString("Return to Menu [M]", this.getWidth() / 2 - fm.stringWidth("Return to Menu [M]") / 2, 300);

		}
		waitFlag = true; 
	    }

	    // If the new random location for one snake is on top of the others tail, create new random location
	    respawnCollisionPlayer_1();

	    // Check for collision between the second player and his own tail
	    if (!p2GhostFlag)
		hasIntersected = headToTailCollision(GOH.player_2, 2);

	    // If the new location for player 2 is on player 1's tail, create new random location for player 2's tail
	    respawnCollisionPlayer_2();

	    // Check for intersection from player 2's head to player 1's tail
	    for (int w = GOH.player_1.size() - 1; w > 0; w--) {
		Rectangle p1tail = new Rectangle(GOH.player_1.getGameObjectXPos(w), GOH.player_1.getGameObjectYPos(w), Snake.WIDTH, Snake.WIDTH);
		p2head = new Rectangle(GOH.player_2.getGameObjectXPos(0), GOH.player_2.getGameObjectYPos(0), Snake.WIDTH, Snake.WIDTH);
		if (p2head.intersects(p1tail) && !p2GhostFlag) {
		    hasIntersected = true;
		    GOH.respawnPlayer(GOH.player_2);
		    score2 = 0;
		    loser++;
		    
		    g.setColor(Color.RED);
		    g.drawString("Game Over!", this.getWidth() / 2 - fm.stringWidth("Game Over!") / 2, 240);
		    g.setColor(Color.GREEN);
		    g.drawString("Green Wins!", this.getWidth() / 2 - fm.stringWidth("Green Wins!") / 2, 400);
		    g.drawString("Do you want to play again?", this.getWidth()/2-fm.stringWidth("Do you want to play again?") / 2, 260);
	   		g.drawString("Yes [Y] or No [N]", this.getWidth() / 2 - fm.stringWidth("Yes [Y] or No [N]") / 2, 280);
	    	g.drawString("Return to Menu [M]", this.getWidth() / 2 - fm.stringWidth("Return to Menu [M]") / 2, 300);
		}
		waitFlag = true; 
	    }

	    //make sure the player 2 respawn location is valid
	    respawnCollisionPlayer_2();
	} //end if fruits > 0
    }

    
public void DrawGrid(Graphics g){
        //drawing in outside rect
    /* if (screenSize==0){
	GRID_WIDTH=500;
	GRID_HEIGHT=500;
    }
    else if (screenSize==1){
	GRID_WIDTH=600;
	GRID_HEIGHT=600;
    }
    else {
	GRID_WIDTH=700;
	GRID_HEIGHT=700;
	}*/
        g.drawRect(0, 0, GRID_WIDTH * BOX_WIDTH, GRID_HEIGHT * BOX_HEIGHT);
        //drawing the vertical lines
        for(int x = BOX_WIDTH; x< GRID_WIDTH * BOX_WIDTH; x+=BOX_WIDTH){
            g.drawLine(x, 0, x, BOX_HEIGHT * GRID_HEIGHT);
        }
        //drawing the horizontal lines
        for (int y = BOX_HEIGHT; y < GRID_HEIGHT * BOX_HEIGHT; y+=BOX_HEIGHT){
            g.drawLine(0, y, GRID_WIDTH* BOX_WIDTH, y);
        }
    }
    


    public void showMainMenu(){ 
/*
		panel2 = new JPanel();
    	panel2.setOpaque(false);
    	panel2.setLayout(new FlowLayout());

    	backgroundImage.setLayout(new FlowLayout());

    	backgroundImage.setIcon(new ImageIcon(""));
    	backgroundImage.setLayout(new BorderLayout());

    	panel2.add(onePlayerButton);

    	backgroundImage.add(panel2);

    	this.add(backgroundImage);


    	JPanel panel = new JPanel();
    	
    	JButton onePlayerButton = new JButton("1 Player");
		onePlayerButton.addActionListener(e -> players = 1);
		//this.getContentPane().add(onePlayerButton);
	
	    panel.setLayout(new FlowLayout(FlowLayout.LEFT));
	    panel.setSize(300,300);
	    panel.setBackground(Color.RED);
	    panel.add(onePlayerButton);

	   
		this.setSize(600,600);
		this.setBackground(Color.BLACK);
		this.add(panel);
		this.setVisible(true); 
*/

	
	//Set the height and width of the main menu
	this.setSize(500,500);
	//Set the color of the main menu and fill it
	g.setColor(Color.BLACK);
	g.fillRect(0, 0, this.getWidth(), this.getHeight());
	g.setColor(Color.GREEN);

	//print the authors and title of game in green
	g.drawString("Snake", this.getWidth()/2-fm.stringWidth("Snake")/2, 80);
	String authorTitle="By: Sam Dowell, with Eric Huang, Sam Min";
	g.drawString(authorTitle, this.getWidth()/2-fm.stringWidth(authorTitle)/2, 110);

			


	// Paint box displaying whether 1 or 2 players is selected
	// Set the color of the graphics object to white
	g.setColor(Color.WHITE);
	// Depending on the number of players, fill in a white rectangle to highlight the number of players desired
	// The parameters of the fillRect method correspond to the pixel locations of the white highlight.
	if (players == 1) {
	    g.fillRect(211, 217, 22, 22);
	} else if (players == 2) {
	    g.fillRect(257, 217, 22, 22);
	}

	// Paint box for window size preference
	if (screenSize == 0){g.fillRect(131,263,60,30);}
	else if (screenSize == 1){g.fillRect(225,263,77,30);}
	else if (screenSize == 2){g.fillRect(341,263,60,30);}

	// These methods change the size of the oval used to select whether puddles are enabled or disabled in the
	// main menu.
	if (puddles == false && walls == false&reversed==false){g.fillRect(77, 325, 70, 40);}
	else if(puddles == true){g.fillRect(206,325, 80,40);}
	else if(walls == true){g.fillRect(300, 325, 70, 40);}
        else if (reversed==true) {g.fillRect(410,325,90,40);}
	// The methods below highlight the difficulty of the snake game at the bottom of the main menu.
	if (speed == 80) {
	    g.fillRect(115, 410, 50, 30);
	} else if (speed == 40) {
	    g.fillRect(205, 410, 75, 30);
	} else if (speed ==20) {
	    g.fillRect(320, 410, 75, 30);
	}

	// Draw the menu options that will be highlighted
	String numPlayer = new String("Number of Players ");
	String keyPress = new String("(Press keys 1 or 2)");
	String numPlayerOptions = new String("1      2");
	String modeOptions = new String("6)Normal  7)Puddles  Z)Walls  X)Reversed");
	String difficultyOptions = new String("8)Easy    9)Medium    0)Difficult");
	String modeSelect = new String("Select mode: ");
	String difficultySelect = new String("Select difficulty: ");
	String begin_and_tutorial = new String("Press Spacebar to Begin   |   How to Play [H]");
	String HighScore1 = new String("High Score 1) ");
	String HighScore2 = new String("High Score 2) ");
	String HighScore3 = new String("High Score 3) ");
	String gameAreaSize = new String("Game Area Size:");
	String areaSizes = new String("3) Small   4) Medium   5) Large");
	
	
	g.setColor(Color.RED);
	g.setFont(font1);
       
	g.drawString(HighScore1 + hScore.getScore(), this.getWidth() / 2 -fm.stringWidth(HighScore1) /2, 135);
	g.drawString(HighScore2 + hScore2.getScore(), this.getWidth() / 2 -fm.stringWidth(HighScore2) /2, 155);
	g.drawString(HighScore3 + hScore3.getScore(), this.getWidth() / 2 -fm.stringWidth(HighScore3) /2, 175);
	g.drawString(gameAreaSize, this.getWidth() / 2 - fm.stringWidth(gameAreaSize)/2, 260);
	g.setFont(font2);
	g.drawString(areaSizes, this.getWidth() / 2 - fm.stringWidth(areaSizes)/2 - 50, 285);
	g.setFont(font1);
	g.drawString(numPlayer + keyPress, this.getWidth() / 2 - fm.stringWidth(numPlayer+keyPress) / 2, 205);
	g.setFont(font2);
	g.drawString(numPlayerOptions, this.getWidth()/2 - fm.stringWidth(numPlayerOptions)/2-15, 235);
	g.drawString(modeOptions, this.getWidth() /2 - fm.stringWidth(modeOptions)/2 -40, 350);
	g.drawString(difficultyOptions, this.getWidth() /2 - fm.stringWidth(difficultyOptions)/2-50, 430);
	g.setFont(font1);
	g.drawString(modeSelect, this.getWidth() /2 - fm.stringWidth(modeSelect)/2, 320);
	g.drawString(difficultySelect, this.getWidth() / 2 - fm.stringWidth(difficultySelect) / 2, 400);
	g.drawString(begin_and_tutorial, this.getWidth() / 2 - fm.stringWidth(begin_and_tutorial) / 2 - 20, 490);
	g.setFont(font1);

    }


    public void showGameControls(){
	// Display Control screen
	g.setColor(Color.BLACK);
	g.fillRect(0, 0, this.getWidth(), this.getHeight());
	g.setColor(Color.GREEN);
	g.setFont(font0);
	g.drawString("Single Player",85, 50);
	g.drawString("\u2191 ", 91, 67);
	g.drawString("Controls: \u2190   \u2192", 20, 75); // Arrows.  See; http://en.wikipedia.org/wiki/Arrow_(symbol)
	g.drawString("\u2193", 91, 79);
	g.drawString("How to Play:", 20, 115);
	g.drawString("Use the arrow keys to control the", 20, 135);
	g.drawString("snake and eat as many apples as", 20, 150);
	g.drawString("possible without running into the", 20, 165);
	g.drawString("tail. There are three special fruits", 20, 180);
	g.drawString("that give temporary powers:", 20, 195);
	g.drawString("- Green fruit speeds up the snake.",20,210);
	g.drawString("- Purple fruit widens the snake.",20, 225); 
	g.drawString("- Black fruit allows you to pass",20,240); 
	g.drawString("through objects such as yourself,",20,255);
	g.drawString("other snakes, and walls.",20,270);
	


	g.drawString("Two Player", 325, 50);
	g.drawString("Controls:   Green Snake: \u2190   \u2192", 250, 75);
	g.drawString(" \u2191 ", 414, 67);
	g.drawString(" \u2193 ", 414, 79);
	g.drawString("  Orange Snake: WASD", 302, 93);
	g.drawString("How to Play:", 250, 115);
	g.drawString("Each player must try to eat as many", 250, 135);
	g.drawString("apples as possible. If you collide with", 250, 150);
	g.drawString("any part of a tail, you will revert back", 250, 165);
	g.drawString("to your original size. If your heads", 250, 180);
	g.drawString("collide, you will both revert back to", 250, 195);
	g.drawString("your original sizes. A new fruit will", 250, 210);
	g.drawString("appear every 30 seconds if not eaten.", 250, 225);
	g.drawString("Act quickly! There are only 50 apples!", 250, 240);
	g.drawString("To win, have the bigger size when the", 250, 255);
	g.drawString("the apples run out", 250, 270);


	g.setColor(Color.WHITE);
	g.drawString("Puddle Mode:",200,310);
	g.drawString("In this mode, there will be several puddles placed around the map", 40, 330);
	g.drawString("which will hide some fruits as they spawn. The objective remains", 45,345);
	g.drawString("the same but are you up for the challenge?", 100,360);

	g.setColor(Color.WHITE);
	g.drawString("Walls Mode:",200,390);
	g.drawString("In this mode, you won't be able to pass through walls, they will be", 40, 405);
	g.drawString("a barrier. If you collide with the wall you will respawn. The", 60,420);
	g.drawString("objective remains the same but are you up for the challenge?", 45,435);

	g.drawString("Reverse Mode:",200, 465);
	g.drawString("Your controls are reversed!", 150, 480);


	g.setColor(Color.ORANGE);
	g.drawString("Return to Menu [M]", this.getWidth() - 135, this.getHeight() - 15);
    }
    
    //this method is used to set the boundaries of the SnakeFrame
    private void setScreenBoundaries() {
	// Set boundaries for playing field
        if (screenSize == 0){
	    this.setSize(500,500);
	    SnakeFrame.frameWidth = 500;
	    SnakeFrame.frameHeight = 500;
        }
        else if (screenSize == 1){
	    this.setSize(600, 600);
	    SnakeFrame.frameWidth = 600;
	    SnakeFrame.frameHeight = 600;
        }
        else{
	    this.setSize(700, 700);
	    SnakeFrame.frameWidth = 700;
	    SnakeFrame.frameHeight = 700;
        }
    }
    
    //this method ensures that player two does not spawn on top of player one
    private void respawnCollisionPlayer_2() {
		CollisionDetector.respawnCollisionPlayer(hasIntersected, GOH, 2); 
	}

    //this method ensures that player one does not spawn on top of player two
	private void respawnCollisionPlayer_1() {
		CollisionDetector.respawnCollisionPlayer(hasIntersected, GOH, 1); 
	}
	
    
    //this method returns true if the snake's head intersected its tail
    //the playerNumber parameter represents player one or player two, 1 = player_1, 2 = player_2
    private boolean headToTailCollision(Snake player, int playerNumber) {
    	return CollisionDetector.headToTailCollision(player, playerNumber, Snake.WIDTH, GOH); 
	}

    //checks to make sure you haven't run into a wall, and if it has,
    //respawn the player with it's direction facing up
    public void headToWallCollision() {
	if (players==1){
	    if (GOH.player_1.getGameObjectXPos(1) - Snake.WIDTH/2<= 0 ||
	   GOH.player_1.getGameObjectYPos(0) - Snake.WIDTH <= 0 ||
	   GOH.player_1.getGameObjectXPos(0) + Snake.WIDTH/2 >= SnakeFrame.frameWidth ||
	   GOH.player_1.getGameObjectYPos(0) + Snake.WIDTH >= SnakeFrame.frameHeight) {
	    GOH.respawnPlayer(GOH.player_1);
		score = 0;
		score1 = 0;
	      	loser++;
	    GOH.player_1.setDirection("UP");
	}
	}

	else if (players==2){
           if (GOH.player_1.getGameObjectXPos(1) - Snake.WIDTH/2<= 0 ||
	   GOH.player_1.getGameObjectYPos(0) - Snake.WIDTH <= 0 ||
	   GOH.player_1.getGameObjectXPos(0) + Snake.WIDTH/2 >= SnakeFrame.frameWidth ||
	   GOH.player_1.getGameObjectYPos(0) + Snake.WIDTH >= SnakeFrame.frameHeight) {
	    GOH.respawnPlayer(GOH.player_1);
		score = 0;
		score1 = 0;
	       
	    GOH.player_1.setDirection("UP");
	}	    
	   else if(GOH.player_2.getGameObjectXPos(1) - Snake.WIDTH/2 <= 0 ||
	   GOH.player_2.getGameObjectYPos(0) - Snake.WIDTH <= 0 ||
	   GOH.player_2.getGameObjectXPos(0) + Snake.WIDTH/2 >= SnakeFrame.frameWidth ||
	   GOH.player_2.getGameObjectYPos(0) + Snake.WIDTH >= SnakeFrame.frameHeight) {
	    GOH.respawnPlayer(GOH.player_2);
		score2 = 0;
	    GOH.player_2.setDirection("UP");
	}
	}
    }
    
    
    //this method causes the user to lose when colliding with their own tail in single player through the loser variable.
    private void headToTailCollisionSinglePlayer() {
    	loser += CollisionDetector.headToTailCollisionSinglePlayer(GOH, Snake.WIDTH, g); 
    }
    
    private void fruitRespawnCollisionDetect(int numPlayers) {
		CollisionDetector.fruitRespawnCollisionDetect(numPlayers, GOH, Snake.WIDTH, BasicFruit.WIDTH); 
    }

    private void drawSnakesAndFruit(int numPlayers) {


	// Set the growsnake value to false
	growsnake = false;
	growsnake2 = false;

	//set the colors of the snake head and tail for player 1
	GOH.player_1.setSnakeHeadColor("black");
	GOH.player_1.setSnakeTailColor("green");

	if(p1WidthFlag)
	    Snake.WIDTH = 40;
	generateNewSnake(GOH.player_1, 1);
	Snake.WIDTH = 20;
	
	//if multiplayer, make sure player 2 has the correct color
	if (numPlayers == 2){
	    GOH.player_2.setSnakeHeadColor("black");
	    GOH.player_2.setSnakeTailColor("orange");

	    if(p2WidthFlag)
		Snake.WIDTH = 40;
	    generateNewSnake(GOH.player_2, 2);
	    Snake.WIDTH = 20;
	}


    }

    public void generateNewSnake(Snake player, int playerNumber){
	//begin drawing the snake

		for (int i = 0; i < player.size(); i++) {
		    // Make color of first block (head) black
		    if (player.getSnakeColorAtIndex(i) == "black") {
			g.setColor(Color.BLACK);
			// Make the rest of the snake green
		    } else if (player.getSnakeColorAtIndex(i) == "green"){
			g.setColor(Color.GREEN);
		    } else {
			g.setColor(Color.ORANGE);
		    }


		    // Set turn back to 0 to indicate that the turn has been displayed
		    player.turn();


		    // Create rectangles for the snake head and the fruit in order to check for intersection
		    Rectangle r = new Rectangle(player.getGameObjectXPos(i), player.getGameObjectYPos(i), Snake.WIDTH, Snake.WIDTH);
		    Rectangle fruit = new Rectangle(GOH.getBasicFruit(0).getX(), GOH.getBasicFruit(0).getY(), BasicFruit.WIDTH, BasicFruit.WIDTH);
		    Rectangle head = new Rectangle(player.getGameObjectXPos(0), player.getGameObjectYPos(0), Snake.WIDTH, Snake.WIDTH);
		    
			Rectangle head2 = new Rectangle(GOH.player_2.getGameObjectXPos(0), GOH.player_2.getGameObjectYPos(0), Snake.WIDTH, Snake.WIDTH);
		    // Paint the snake
		    g.fillRect(player.getGameObjectXPos(i), player.getGameObjectYPos(i), Snake.WIDTH, Snake.WIDTH);


		    // If the Snake head intersects with fruit, randomly generate new location for fruit away from the snake
		    if (head.intersects(fruit)) {

				try {
					AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File("eact.wav").getAbsoluteFile( ));
		   			Clip clip = AudioSystem.getClip( );
		    		clip.open(audioInputStream);
		    		clip.start( );
		   		}
		   		catch(Exception ex)
		   		{
		     		System.out.println("Error with playing sound.");
		     		ex.printStackTrace( );
		   		}
				    	
			    while (head.intersects(GOH.getBasicFruit(0).getX(), GOH.getBasicFruit(0).getY(), BasicFruit.WIDTH, BasicFruit.WIDTH)) {

				//find out what powerup was in the fruit
				p1PowerUp = GOH.getBasicFruit(0).getPowerUp();
				// then generate a new one
				FruitGenerator.generateNewFruit(GOH); 
			    }

			    // Set growsnake to true to increase size of snake by 1
			    growsnake = true;
			    score1++;

			    // Add to score
			    if (players == 1){
				score++;
			    }
			    fruits--;
			    fruittimer.start();
			} else if(head2.intersects(fruit)){
				
				try{
		    		clip.start();
		    		clip.setFramePosition(0);
		    	}
		    	catch(Exception e)
		    	{
		    		e.printStackTrace();
		    	}

			    while (head2.intersects(GOH.getBasicFruit(0).getX(), GOH.getBasicFruit(0).getY(), BasicFruit.WIDTH, BasicFruit.WIDTH)) {
			    	//find out what powerup was in the fruit
			    	p2PowerUp = GOH.getBasicFruit(0).getPowerUp();
			    	// then generate a new one
			    	FruitGenerator.generateNewFruit(GOH); 

			    	// Set growsnake to true to increase size of snake by 1
			   		growsnake2 = true;
			    	// Add to score
			    	score2++;
			    }
			
			}
		}


    }

    // Function to handle powerups
    public void checkPowerUp() {
	int currentSeconds = watch.getMinutes() * 60 + watch.getSeconds();


	//speed power up
	if (p1SpeedPowerUpStart != 0 && (currentSeconds - p1SpeedPowerUpStart) > 5) {
	    p1SpeedPowerUpStart = 0;
	    atime.setDelay(speed);
	}
	if(p1PowerUp.equals("SPEED")) {
	    int tempSpeed = speed;

	    if (p1SpeedPowerUpStart != 0) {
		p1SpeedPowerUpStart = currentSeconds;
		p1PowerUp = "NONE";
	    } else {
		p1SpeedPowerUpStart = currentSeconds;
		tempSpeed /= 5;
		atime.setDelay(tempSpeed);
		p1PowerUp = "NONE";
	    }
	}

	//width power up
	if (p1WidthPowerUpStart != 0 && (currentSeconds - p1WidthPowerUpStart) > 5) {
	    p1WidthPowerUpStart = 0;
	    p1WidthFlag = false;
	}
	if(p1PowerUp.equals("WIDTH")) {
	    if (p1WidthPowerUpStart != 0) {
		p1WidthPowerUpStart = currentSeconds;
		p1PowerUp = "NONE";
	    } else {
		p1WidthPowerUpStart = currentSeconds;
		p1WidthFlag = true;
		p1PowerUp = "NONE";
	    }
	}
	//ghost power up
	if (p1GhostPowerUpStart != 0 && (currentSeconds - p1GhostPowerUpStart) > 5) {
	    p1GhostPowerUpStart = 0;
	    p1GhostFlag = false;
	}
	if(p1PowerUp.equals("GHOST")) {
	    if (p1GhostPowerUpStart != 0) {
		p1GhostPowerUpStart = currentSeconds;
		p1PowerUp = "NONE";
	    } else {
		p1GhostPowerUpStart = currentSeconds;
		p1GhostFlag = true;
		p1PowerUp = "NONE";
	    }
	}

	if(players == 2) {
	    //speed power up
	    if (p2SpeedPowerUpStart != 0 && (currentSeconds - p2SpeedPowerUpStart) > 5) {
		p2SpeedPowerUpStart = 0;
		atime.setDelay(speed);
	    }
	    if(p2PowerUp.equals("SPEED")) {
		int tempSpeed = speed;
		
		if (p2SpeedPowerUpStart != 0) {
		    p2SpeedPowerUpStart = currentSeconds;
		    p2PowerUp = "NONE";
		} else {
		    p2SpeedPowerUpStart = currentSeconds;
		    tempSpeed /= 5;
		    atime.setDelay(tempSpeed);
		    p2PowerUp = "NONE";
		}
	    }

	    //width power up
	    if (p2WidthPowerUpStart != 0 && (currentSeconds - p2WidthPowerUpStart) > 5) {
		p2WidthPowerUpStart = 0;
		p2WidthFlag = false;
	    }
	    if(p2PowerUp.equals("WIDTH")) {
		if (p2WidthPowerUpStart != 0) {
		    p2WidthPowerUpStart = currentSeconds;
		    p2PowerUp = "NONE";
		} else {
		    p2WidthPowerUpStart = currentSeconds;
		    p2WidthFlag = true;
		    p2PowerUp = "NONE";
		}
	    }
	    //ghost power up
	    if (p2GhostPowerUpStart != 0 && (currentSeconds - p2GhostPowerUpStart) > 5) {
		p2GhostPowerUpStart = 0;
		p2GhostFlag = false;
	    }
	    if(p2PowerUp.equals("GHOST")) {
		if (p2GhostPowerUpStart != 0) {
		    p2GhostPowerUpStart = currentSeconds;
		    p2PowerUp = "NONE";
		} else {
		    p2GhostPowerUpStart = currentSeconds;
		    p2GhostFlag = true;
		    p2PowerUp = "NONE";
		}
	    }   
	}
    }

    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
    @Override
    public void keyTyped(KeyEvent ke) {
    }
    
    @Override
    public void keyPressed(KeyEvent ke) {
	waitFlag = true;
    	GOH.keyPressed(ke);
        // Track key using KeyListener
	int key = ke.getKeyCode();
	// If the user presses an arrow key, change the direction values
	// Don't allow the direction to directly reverse; and dont allow direction to change near the edges
        
	// Listen for the "y" and "n" key for when the game has ended (replay)
	if (key == KeyEvent.VK_Y) {
	    if (loser > 0) {
		playagain = 1;
	    }
	} else if (key == KeyEvent.VK_N) {
	    if (loser > 0) {
		playagain = 2;
	    }
	} // If the spacebar is pressed, add one to menu so that it doesnt return
	else if (key == KeyEvent.VK_SPACE) {
	    if (menu == 0 && controls == false) {
		menu++;
		// Start stopwatch
		watch.start();
	    }
	    // If "m" key is pressed when the game has ended, restart game and return to menu screen
	} else if (key == KeyEvent.VK_M) {
	    if (((loser > 0) || (pause))&&(menu != 0)) {
		playagain = 1;
		menu = 0;
		players = 1;
		screenSize = 0;
		pause = false;
		loser = 1;
	    } else if (controls == true) {
		controls = !(controls);
	    }
	    // If "p" key is pressed while game is in progress, pause/unpause the game and stopwatch
	} else if (key == KeyEvent.VK_P) {
	    if ((menu != 0) && (loser == 0)) {
		pause = !(pause);
		if (pause == true) {
		    watch.stop();
		} else if (pause == false) {
		    watch.unpause();
		}
	    }
	    // Key listener for menu screen 1/2 players
	} else if (key == KeyEvent.VK_1) {
	    if (menu == 0 && controls == false) {
		players = 1;
		
	    }
	} else if (key == KeyEvent.VK_2) {
	    if (menu == 0 && controls == false) {
		players = 2;
	    }
	    // Key listener for How to Play in main menu
	} else if (key == KeyEvent.VK_H) {
	    if (menu == 0 && controls == false) {
		controls = !(controls);
	    }
	}
	//key listeners for window sizes
	else if (key == KeyEvent.VK_3){
	    if (menu == 0 && controls == false){
		screenSize = 0;
	    }
	}
	else if (key == KeyEvent.VK_4){
	    if (menu == 0 && controls == false){
		screenSize = 1;
	    }
	}
	else if (key == KeyEvent.VK_5){
	    if (menu == 0 && controls == false){
		screenSize = 2;
	    }
	}
	else if (key == KeyEvent.VK_6){
	    if (menu == 0 && controls == false){
		puddles = false;
		walls = false;
		reversed = false;
	    }
	}
	else if (key == KeyEvent.VK_7){
	    if (menu == 0 && controls == false){
		puddles = true;
		walls = false;
		reversed = false;
	    }
	}
	else if (key == KeyEvent.VK_Z){
	    if (menu == 0 && controls == false){
		puddles = false;
		walls = true;
		reversed = false;
	    }
	    
	}
		else if (key == KeyEvent.VK_X){
	    if (menu == 0 && controls == false){
		puddles = false;
		walls = false;
		reversed = true;
	    }
	    
	}
	else if (key == KeyEvent.VK_8){
	    if (menu == 0 ){
		speed=80;
		atime.stop();
		atime.setDelay(speed);
		atime.restart();
	    }
	}
	else if (key == KeyEvent.VK_9){
	    if (menu == 0 ){
		speed=40;
		atime.stop();
		atime.setDelay(speed);
		atime.restart();
	    }
	}
	else if (key == KeyEvent.VK_0){
	    if (menu == 0 ){
		speed=20;
		atime.stop();
		atime.setDelay(speed);
		atime.restart();
		
	    }
	}

    }
    
    
    //mouselistener...........listener
    public void mouseClicked(MouseEvent e) {
	if ((menu != 0) && (loser == 0)) {
	    pause = !(pause);
	    if (pause == true) {
		watch.stop();
	    } else if (pause == false) {
		watch.unpause();
	    }
	}
    }
    
    @Override
    public void mousePressed(MouseEvent e) {}
    @Override
    public void mouseEntered(MouseEvent e) {}
    @Override
    public void mouseExited(MouseEvent e) {}
    @Override
    public void mouseReleased(MouseEvent e) {}
    
    

    @Override
    public void keyReleased(KeyEvent key) {

    }
}
