package uno;

// Uno.java
/**
 *  This program contains the layout of the Uno game and a main program. It initialize a Game object and keeps the game
 *  going until one of the players wins.
 *
 * @author  Yixuan Huang
 * @version Last modified 5/12/2019
 *
 */

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;


class Uno extends JFrame {

    // fields for the user interface
    private GridBagConstraints c;
    private JPanel main;
    private JPanel hidden;
    private JPanel userPanel;
    private JPanel radioPanel;
    private JPanel computerPanel;
    private ArrayList<JButton> cardButtons;
    private JButton drawButton;
    private JButton janetsCard;
    private JButton bendersCard;
    private JButton doloressCard;
    private JLabel[] lNames;
    private JLabel nameLabel;
    private JButton [] comCards;
    private JRadioButton [] wildColors;
    private ButtonGroup buttonGroup;
    private JLabel gameStatus;

    // fields for the game
    private Game game;
    private Card lastCardPlayed;
    private String usersName;
    private Hand usersHand;
    private ArrayList<Card> usersCards;
    private boolean userActed;
    private JCheckBox usersTurn;
    private JCheckBox over;
    private String winner;
    private Card userPlayed;
    private Timer timer;

    // constructor
    public Uno () {

        setTitle("Uno");
        setSize(1000, 800);
        setLocationRelativeTo(null);
        // setLocation(200, 50);

        // use nested JPanels
        main = new JPanel();
        main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane (main);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        // initialize the four nested JPanels
        initializeHiddenPanel ();
        initializeUserPanel ();
        initializeRadioPanel ();
        initializeComputerPanel ();

        // show the current game color and value at the bottom
        gameStatus = new JLabel ("current color: none; current value: none.");
        c.insets = new Insets(20, 0, 0, 20);
        c.weightx = 0.15;
        addToComputerPanel(gameStatus, 4, 0, 2, 1);

        // add all the child panels to the main panel
        userPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        radioPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        computerPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        main.add(hidden);
        main.add(userPanel);
        main.add(radioPanel);
        main.add(computerPanel);

        setLayout(new BorderLayout());
        add(scrollPane, BorderLayout.CENTER);
        pack();
        setVisible(true);
    }


     /** Initialize the panel with the hidden checkboxes.
     **/
    void initializeHiddenPanel () {
        // the hidden panel is used to hold two checkboxes use for listening to item changes
        hidden = new JPanel();
        // check box for the whether it's the user's turn to play
        userActed = false;
        usersTurn = new JCheckBox("user's turn");
        usersTurn.setSelected(true);
        usersTurn.addItemListener(new UsersTurnChange());
        usersTurn.setVisible(false);
        hidden.add(usersTurn);
        // check box for whether the game is over
        over = new JCheckBox("game over");
        over.setSelected(false);
        over.addItemListener(new GameStatusChange());
        over.setVisible(false);
        hidden.add(over);
    }


     /** Initialize the panel with user's cards.
     **/
    void initializeUserPanel () {
        // userPanel has the user's label and card buttons
        userPanel = new JPanel();
        userPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        usersName = "";
        usersName = JOptionPane.showInputDialog (this, "Enter your name and start the game.");
        if ( usersName == null ) System.exit(0);
        if ( usersName.equals("") ) usersName = "User";
        nameLabel = new JLabel (usersName + " has 7 cards");
        setLabelFormat(nameLabel, 150, 100);
        userPanel.add(nameLabel);

        // add a button for drawing; button is always disabled unless no legal cards exist
        drawButton = new JButton ("Draw");
        drawButton.setEnabled(false);
        drawButton.addActionListener (new DrawClicker());
        setButtonFormat (drawButton);
        userPanel.add (drawButton);

        // initialize the game; each player gets 7 cards
        game = new Game(this);
        usersHand = game.getUsersHand();
        usersCards = usersHand.getCards();
        cardButtons = new ArrayList<JButton> (usersCards.size());
        // add cards to the GUI and add actionListener to the cards
        for (int i=0; i<usersCards.size(); i++) {
            JButton b = new JButton();
        	b.setOpaque(true);
        	b.setBorderPainted(false);
            b.setText (usersCards.get(i).getValue());
            setButtonColor (usersCards.get(i).getColor(), b);
            setButtonFormat (b);

            b.addActionListener (new CardClicker());
            userPanel.add (b);
            cardButtons.add (i, b);
        }
    }


     /** Initialize the panel with the radio buttons.
     **/
    void initializeRadioPanel () {
        // this panel is for JRadioButtons for the user to choose a color for their wild cards
        radioPanel = new JPanel();
        radioPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        JLabel yourWildCard = new JLabel ("Your wild card color:");
        radioPanel.add(yourWildCard);
        wildColors = new JRadioButton[4];
        buttonGroup = new ButtonGroup();
        String [] wildLabels = {"Red", "Blue", "Yellow", "Green"};
        for (int i=0; i<4; i++) {
            wildColors[i] = new JRadioButton (wildLabels[i]);
            wildColors[i].setActionCommand (wildLabels[i].substring(0, 1));
            buttonGroup.add(wildColors[i]);
            radioPanel.add(wildColors[i]);
        }
        wildColors[0].setSelected(true);
    }


     /** Initialize the panel with the computers' cards.
     **/
    void initializeComputerPanel () {
        // this panel is for the labels and cards of the computers
        computerPanel = new JPanel();
        computerPanel.setLayout(new GridBagLayout());
        c = new GridBagConstraints();
        c.fill = GridBagConstraints.NONE;
        c.anchor=GridBagConstraints.WEST;
        c.weightx = 0.10;
        c.weighty = 0.0;
        c.insets = new Insets(40, 0, 0, 40);

        lNames = new JLabel[4];
        lNames[0] = nameLabel;
        JLabel janet = new JLabel ("Janet has 7 cards");
        setLabelFormat(janet, 150, 100);
        addToComputerPanel(janet, 1, 0, 1, 1);
        lNames[1] = janet;
        JLabel bender = new JLabel ("Bender has 7 cards");
        setLabelFormat(bender, 150, 100);
        addToComputerPanel(bender, 2, 0, 1, 1);
        lNames[2] = bender;
        JLabel dolores = new JLabel ("Dolores has 7 cards");
        setLabelFormat(dolores, 150, 100);
        addToComputerPanel(dolores, 3, 0, 1, 1);
        lNames[3] = dolores;

        // the cards that the computer players play are disabled JButtons
        janetsCard = new JButton();
        janetsCard.setOpaque(true);
        janetsCard.setBorderPainted(false);
        bendersCard = new JButton();
        bendersCard.setOpaque(true);
        bendersCard.setBorderPainted(false);
        doloressCard = new JButton();
        doloressCard.setOpaque(true);
        doloressCard.setBorderPainted(false);

        comCards = new JButton[4];
        comCards[0] = new JButton();
        comCards[1] = janetsCard;
        comCards[2] = bendersCard;
        comCards[3] = doloressCard;
    }


    // getter and setters for private fields

    /** Return the usersName field.
     **/
    public String getusersName () {
        return usersName;
    }


    /** Return a Hand object.
     **/
    public Hand getUsersHand() {
        return usersHand;
    }


    /** Return an ArrayList of Cards that are the user's cards
     **/
    public ArrayList<Card> getUsersCards() {
        return usersCards;
    }


    /** Return the game object.
     **/
    public Game getGame() {
        return game;
    }


    /** Return the winner field.
     **/
    public String getWinner() {
        return winner;
    }

    /** Set the winner field.
     **/
    public void setWinner(String winner) {
        this.winner = winner;
    }


    /** Return an array of JLabels representing all the names of the players.
     **/
    public JLabel [] getLNames() {
        return lNames;
    }


    /** Get the buttons representing the cards of the users.
     **/
    public ArrayList<JButton> getCardButtons () {
        return cardButtons;
    }

    /** Add a button to the ArrayList of buttons representing the user's cards.
     **/
    public void addCardButtons(JButton b) {
        this.cardButtons.add(b);
    }


    /** Select or de-select the usersTurn checkbox.
     **/
    public void setUsersTurn (boolean usersTurn) {
        this.usersTurn.setSelected(usersTurn);
    }


    /** Set the lastCardPlayed field.
     **/
    public void setLastCardPlayed (Card lastCardPlayed) {
        this.lastCardPlayed = lastCardPlayed;
    }


    /** Set the gameStatus label.
     *
     * @param color     the current color of the game
     * @param value     the current value of the game
     *
     **/
    public void setGameStatus (String color, String value) {
       String s = "current color: " + color + "; current value: " + value;
       gameStatus.setText(s);
    }


    /** Select or de-select the game over checkbox.
     **/
    public void setOver (boolean over) {
        this.over.setSelected(over);
    }


    /** Set the text of the labels indicating which player has how many cards.
     **/
    public void setLabelText (JLabel l, String name, int number) {
        if (number != 1) {
            l.setText (name + " has " + number + " cards");
        } else {
            l.setText (name + " has 1 card");
        }
    }


    /** A helper method to revalidate and repaint all the panels with the card buttons.
     **/
    public void resetPanel () {
        computerPanel.revalidate();
        computerPanel.repaint();
        userPanel.revalidate();
        userPanel.repaint();
        main.revalidate();
        main.repaint();
    }


    /** Change the game status based on the card played by the user.
     *
     * @param       index, index of the card played in the hand
     **/
    public void playedCard(int index) {
        userPanel.remove(cardButtons.get(index));
        resetPanel();
        cardButtons.remove(index);
        userPlayed = usersHand.removeCard(index);
        game.getDiscarded().addCard(userPlayed);

        // if the user has no cards left they win
        if (usersHand.getCards().size()==0 || usersHand.getNumberOfCards()<=0) {
            setWinner("You");
            setOver(true);
        }

        // if the user plays a wild card, set the current color according to the radio button selected
        if (userPlayed.getColor().equals("W")) {
            game.setCurrentColor (buttonGroup.getSelection().getActionCommand());
        } else {
            game.setCurrentColor (userPlayed.getColor());
        }
        // reset what the next legal cards are
        game.setCurrentValue (userPlayed.getValue());
        game.setCurrentDirection (game.getCurrentDirection() * userPlayed.getDirection());
        game.setCurrentDraw (userPlayed.getDraw() + game.getCurrentDraw());
        game.setCurrentSkip (userPlayed.getSkip());

        // update the game status label at the bottm
        String c = "";
        if (game.getCurrentColor().equals("R")) c = "red";
        else if (game.getCurrentColor().equals("B")) c = "blue";
        else if (game.getCurrentColor().equals("Y")) c = "yellow";
        else if (game.getCurrentColor().equals("G")) c = "green";
        setGameStatus(c, game.getCurrentValue());
    }


    /** Reset the card displayed next to a computer player label, representing the last action they took.
     *
     * @param       t, the turn indicating which computer played
     **/
    public void setComCard (int t) {
        // remove the current button
        if (comCards[t].getParent() == computerPanel) {
            computerPanel.remove( comCards[t]);
        }
        comCards[t].setText(lastCardPlayed.getValue());
        setButtonColor(lastCardPlayed.getColor(), comCards[t]);
        // if a wild card is played, set the color to the current color (which is set by the computer player)
        if (lastCardPlayed.getColor().equals("W")) {
            setButtonColor(game.getCurrentColor(), comCards[t]);
        }
        setButtonFormat (comCards[t]);
        comCards[t].setEnabled (false);
        c.weightx = 0.90;
        addToComputerPanel (comCards[t], t, 1, 1, 1);
    }


    /** Invoke actions of the computer players after the user has acted. This will keep going until it's the user's
     *  turn again.
     *
     * @param       index, index of the card played in the user's hand
     **/
    public void userActed (int index) {

        // if the user plays a card instead of drawing
        if (index != -1) {
            playedCard(index);
        }
        setLabelText(lNames[0], usersName, usersHand.getNumberOfCards());

        // deselect the checkbox that indicates whether it's the user's turn to trigger an item event
        setUsersTurn(false);

        // determine which computer to play next based on the direction and current skip of the game
        if (game.getCurrentDirection() == 1 ) {
            game.setTurn (1 + game.getCurrentSkip());
        } else {
            game.setTurn (3 - game.getCurrentSkip());
        }

        // use a timer to create a delay between each player; this also serves as a while loop;
        // it will make each computer play a turn until it's the user's turn again, or until someone wins
        timer = new Timer ( 1000,
            new ActionListener() {
                public void actionPerformed (ActionEvent ae) {
                    int t = game.getTurn();
                    if ( game.getGameOver() ) {
                        timer.stop();
                        // select the checkbox that indicates whether the game is over to trigger an item event
                        setOver(true);
                    } else if (t == 0) {
                        timer.stop();
                        // select the checkbox that indicates whether it's the user's turn to trigger an item event
                        setUsersTurn(true);
                    } else {
                        // call the play method in the game object that gets the next player to play
                        game.play(Uno.this);
                        // reset the card being displayed
                        setComCard(t);

                        // reset the text at the bottom about the current game color and value
                        String [] names = {usersName, "Janet", "Bender", "Dolores"};
                        setLabelText (lNames[t], names[t], game.getHands()[t].getNumberOfCards());
                        String c = "";
                        if (game.getCurrentColor().equals("R")) c = "red";
                        else if (game.getCurrentColor().equals("B")) c = "blue";
                        else if (game.getCurrentColor().equals("Y")) c = "yellow";
                        else if (game.getCurrentColor().equals("G")) c = "green";
                        setGameStatus (c, game.getCurrentValue());

                        resetPanel();
                    }
                }
            });
        timer.setRepeats ( true );
        timer.start ();
    }


     /** Enable all the buttons corresponding to the cards legal to play in the user's hand.
     *
     **/
    public void resetButtons () {
        for (int i=0; i<usersCards.size(); i++) {
            cardButtons.get(i).setEnabled(false);
        }
        drawButton.setEnabled(false);
    }


     /** Enable all the buttons corresponding to the cards legal to play in the user's hand.
     *
     **/
    public void enableLegalButtons () {
        boolean hasLegalCards = false;
        for (int i=0; i<usersHand.getCards().size();i++) {
            if (usersHand.isLegal(game, usersHand.getCards().get(i))) {
                cardButtons.get(i).setEnabled(true);
                hasLegalCards = true;
            }
        }
        // if no card is legal, enable the draw button
        if (!hasLegalCards) drawButton.setEnabled(true);
    }


     /** Add component to the computerPanel.
     *
     * @param comp
     * @param row row index of the gridbag layout
     * @param column column index of the layout
     * @param width
     * @param height
     *
     **/
    public void addToComputerPanel (Component comp, int row, int column, int width, int height ) {
        // set gridx and gridy
        c.gridx = column;
        c.gridy = row;

        // set gridwidth and gridheight
        c.gridwidth = width;
        c.gridheight = height;

        // add component
        computerPanel.add (comp, c);
    }


     /** Add component to the userPanel.
     *
     * @param comp
     **/
    public void addToUserPanel (Component comp) {
        userPanel.add(comp);
    }


     /** Set the background color of a JButton based on the color of the corresponding card.
     *
     * @param       c, String indicating the color of the card
     * @param       b, the button
     **/
    public void setButtonColor (String c, JButton b) {
        if (c.equals("R")) {
            b.setBackground(new Color(242,95,92));
        } else if (c.equals("B")) {
            b.setBackground(new Color(36,123,160));
        } else if (c.equals("Y")) {
            b.setBackground(new Color(255,224,102));
        } else if (c.equals("G")) {
            b.setBackground(new Color(136,212,152));
        } else if (c.equals("W")) {
            b.setBackground(Color.LIGHT_GRAY);
        } else if (c.equals("")) {
            b.setBackground(Color.WHITE);
        }
    }


     /** Set the size, margin and font of a button.
     *
     * @param       b, the button
     **/
    public void setButtonFormat (JButton b) {
        b.setMargin(new Insets(1, 1, 1, 1));
        b.setMinimumSize(new Dimension (80, 100));
        b.setMaximumSize(new Dimension (80, 100));
        b.setPreferredSize(new Dimension (80, 100));
        b.setFont(new Font("Tahoma", Font.BOLD, 10));
    }


     /** Set the size of a label.
     *
     * @param       l, a JLabel object
     * @param       width
     * @param       height
     **/
    public void setLabelFormat (JLabel l, int width, int height) {
        l.setMinimumSize(new Dimension (width, height));
        l.setMaximumSize(new Dimension (width, height));
        l.setPreferredSize(new Dimension (width, height));
    }


    /** Helper method to generate integers betwee a range, inclusive,
     *
     * @param       a, the lower bound
     * @param       b, the upper bound
     *
     * @return      a random integer
     **/
    public static int rint (int a, int b)  {
       return ((int) (Math.random() * (b-a+1)) )  + a;
    }


     /** ItemListener to monitor the change of the user's turn checkbox.
     **/
    class UsersTurnChange implements ItemListener {

        public void itemStateChanged(ItemEvent e) {
            JCheckBox box = (JCheckBox) e.getSource();
            Uno f = (Uno) SwingUtilities.getRoot(box);

            if (e.getStateChange() == ItemEvent.DESELECTED) {
                // when the checkbox is deselected, disabled all the buttons for the user
                f.resetButtons();
            } else {
                // when the checkbox is selected, enable the buttons representing the legal cards
                f.enableLegalButtons();
            }
        }
    }


     /** ItemListener to monitor the change of the game status checkbox.
     **/
    class GameStatusChange implements ItemListener {

        public void itemStateChanged(ItemEvent e) {
            JCheckBox box = (JCheckBox) e.getSource();
            Uno f = (Uno) SwingUtilities.getRoot(box);

            if (e.getStateChange() == ItemEvent.SELECTED) {
                JOptionPane.showMessageDialog(f, f.getWinner()+" won!");
                System.exit(0);
            }
        }
    }


     /** ActionListener for when the user click to play a card.
     **/
    class CardClicker implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            JButton button = (JButton) e.getSource();
            Uno u = (Uno) SwingUtilities.getRoot(button);
            int index = u.getCardButtons().indexOf(button);
            u.setUsersTurn(false);
            u.userActed (index);
        }
    }


     /** ActionListener for when the user click the draw button.
     **/
    class DrawClicker implements ActionListener {

        public void actionPerformed(ActionEvent e) {

            JButton b = (JButton) e.getSource();
            Uno u =  (Uno) SwingUtilities.getRoot(b);

            int num = u.getGame().getCurrentDraw();
            int current = u.getUsersCards().size();

            // an array for all the cards being drawn
            Card[] newCards;
            JButton[] added;

            // if drawing is due to no legal cards present but current game draw is 0, only draw one,
            // otherwise draw the corresponding amount
            if (num == 0) {
                u.getUsersHand().draw(u.getGame().getDeck(), 1, u.getGame().getDiscarded());
                added = new JButton[1];
                newCards = new Card[1];
            } else {
                u.getUsersHand().draw(u.getGame().getDeck(), num, u.getGame().getDiscarded());
                added = new JButton[num];
                newCards = new Card[num];
            }

            // iterate through all the cards drawn and add buttons
            for (int i=0; i<newCards.length; i++) {
                newCards[i] = u.getUsersCards().get(current+i);
                added[i] = new JButton(newCards[i].getValue());
                setButtonColor(newCards[i].getColor(), added[i]);
                setButtonFormat(added[i]);
                added[i].addActionListener(new CardClicker());
                added[i].setEnabled(false);
                u.addCardButtons(added[i]);
                u.addToUserPanel(added[i]);
            }

            u.getGame().setCurrentDraw(0);
            u.resetPanel();
            // pass -1 to userActed() to indicate no card was played
            u.userActed (-1);
        }
    }


    public static void main (String [] args) {
    	try {
    		UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
    	}catch(Exception e){
    		e.printStackTrace(); 
    	}
        Uno app = new Uno();
        app.setDefaultCloseOperation (JFrame.EXIT_ON_CLOSE);
    }
}