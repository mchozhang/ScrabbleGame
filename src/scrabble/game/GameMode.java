package scrabble.game;

public interface GameMode {
    /**
     * initialize game board, set the score of all the cells
     */
    void initBoard(Cell[][] cells);

    /**
     * assign letters to a player
     * @param player player
     */
    void assignLetters(Player player);

    /**
     * player has just placed a letter, assign him a new one
     * @param letterIndex the index of letter should be assigned
     */
    void assignOneLetter(Player player, int letterIndex);

}
