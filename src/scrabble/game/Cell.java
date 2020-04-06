package scrabble.game;

import java.io.Serializable;

/**
 * a cell of the 20 * 20 scrabble board
 */
public class Cell implements Serializable {
    // the turn of the player who placed a letter in the cell
    public int turnId;

    public int score;

    public String letter;

    public int column;

    public int row;

    public boolean available;
}
