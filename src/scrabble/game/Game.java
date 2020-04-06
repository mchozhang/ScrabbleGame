package scrabble.game;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class Game implements Serializable {
    public enum Mode {
        BASIC,
        ADVANCED,
    }

    public enum PollType {
        HORIZONTAL,
        VERTICAL,
        CROSS,
        PASS,
    }

    private String id;

    private GameMode mode;

    private int playerCount;

    // the number of player passes the turn in a row
    private int passInARow;

    // cells in the boards
    private Cell[][] cells;

    private Cell lastMove;

    // the player turn id of whom holds the turn
    private int turn;

    private int horizontalWordScore;
    private int verticalWordScore;

    private PollType pollType;

    private boolean isStarted;

    private String winnerName;

    private int winnerScore;

    public Game(int playerCount, Mode mode) {
        turn = 0;
        passInARow = 0;
        this.playerCount = playerCount;
        id = UUID.randomUUID().toString();
        horizontalWordScore = 0;
        verticalWordScore = 0;
        isStarted = false;

        switch (mode) {
            case BASIC:
                this.mode = new BasicGameMode();
                break;
            case ADVANCED:
                this.mode = new AdvancedGameMode();
                break;
        }

        // initialize cells
        cells = new Cell[20][20];
        this.mode.initBoard(cells);
    }

    /**
     * a player place a letter on the board
     *
     * @param player      player
     * @param letterIndex index of letters in hand
     * @param row         row
     * @param col         column
     */
    public void placeLetter(Player player, int letterIndex, int row, int col) {
        if (!isStarted) {
            for (int i = 0; i < 20; i++) {
                for (int j = 0; j < 20; j++) {
                    cells[i][j].available = false;
                }
            }
        }
        isStarted = true;
        Cell cell = getCell(row, col);
        cell.letter = player.getLettersInHand().get(letterIndex);
        cell.turnId = player.getTurn();
        cell.available = false;
        lastMove = cell;

        List<Cell> neighbors = getCellNeighbors(row, col);
        for (Cell neighbor:neighbors) {
            if (neighbor != null && neighbor.letter.isEmpty()) {
                neighbor.available = true;
            }
        }
    }

    /**
     * assign letters to new player from letter pool
     *
     * @param player
     */
    public void assignLetters(Player player) {
        mode.assignLetters(player);
    }

    /**
     * assign a new letter to the player
     *
     * @param letterIndex index of the letter in hand
     */
    public void assignOneLetter(Player player, int letterIndex) {
        mode.assignOneLetter(player, letterIndex);
    }

    public int scoresForHorizontalWord() {
        int score = horizontalWordScore;
        horizontalWordScore = 0;
        verticalWordScore = 0;
        System.out.println("score for hor word " + score);

        return score;
    }

    public int scoresForVerticalWord() {
        int score = verticalWordScore;
        horizontalWordScore = 0;
        verticalWordScore = 0;
        System.out.println("score for ver word " + score);
        return score;
    }

    public int scoresForCrossWord() {
        int score;
        if (verticalWordScore == 1 && horizontalWordScore == 1) {
            score = 1;
        } else if (verticalWordScore == 1 && horizontalWordScore > 1) {
            score = horizontalWordScore;
        } else if (verticalWordScore > 1 && horizontalWordScore == 1) {
            score = verticalWordScore;
        } else {
            score = horizontalWordScore + verticalWordScore;
        }

        horizontalWordScore = 0;
        verticalWordScore = 0;
        return score;
    }

    /**
     * get the horizontal string from last move
     *
     * @return string
     */
    public String getHorizontalString() {
        final int row = lastMove.row;
        final int col = lastMove.column;
        horizontalWordScore = lastMove.score;
        if (isCellEmpty(row, col - 1) && isCellEmpty(row, col + 1)) {
            return lastMove.letter;
        } else if (!isCellEmpty(row, col - 1) && !isCellEmpty(row, col + 1)) {
            return getLeftString(row, col) + lastMove.letter + getRightString(row, col);
        } else if (isCellEmpty(row, col - 1)) {
            return lastMove.letter + getRightString(row, col);
        } else {
            return getLeftString(row, col) + lastMove.letter;
        }
    }

    /**
     * get the vertical string from last move
     *
     * @return string
     */
    public String getVerticalString() {
        final int row = lastMove.row;
        final int col = lastMove.column;
        verticalWordScore = lastMove.score;
        if (isCellEmpty(row - 1, col) && isCellEmpty(row + 1, col)) {
            return lastMove.letter;
        } else if (!isCellEmpty(row + 1, col) && !isCellEmpty(row - 1, col)) {
            return getTopString(row, col) + lastMove.letter + getBottomString(row, col);
        } else if (isCellEmpty(row - 1, col)) {
            return lastMove.letter + getBottomString(row, col);
        } else {
            return getTopString(row, col) + lastMove.letter;
        }
    }

    private String getLeftString(int row, int col) {
        StringBuilder left = new StringBuilder();
        for (int i = col - 1; !isCellEmpty(row, i); i--) {
            Cell cell = getCell(row, i);
            left.append(cell.letter);
            horizontalWordScore += cell.score;
        }
        return left.reverse().toString();
    }

    private String getRightString(int row, int col) {
        StringBuilder right = new StringBuilder();
        for (int i = col + 1; !isCellEmpty(row, i); i++) {
            Cell cell = getCell(row, i);
            right.append(cell.letter);
            horizontalWordScore += cell.score;
        }
        return right.toString();
    }

    private String getTopString(int row, int col) {
        StringBuilder top = new StringBuilder();
        for (int i = row - 1; !isCellEmpty(i, col); i--) {
            Cell cell = getCell(i, col);
            top.append(cell.letter);
            verticalWordScore += cell.score;
        }
        return top.reverse().toString();
    }

    private String getBottomString(int row, int col) {
        StringBuilder bottom = new StringBuilder();
        for (int i = row + 1; !isCellEmpty(i, col); i++) {
            Cell cell = getCell(i, col);
            bottom.append(cell.letter);
            verticalWordScore += cell.score;
        }
        return bottom.toString();
    }

    /**
     * update winner's data
     */
    public void updateWinner(Player player) {
        if (player.getScore() >= winnerScore) {
            winnerName = player.getUsername();
            winnerScore = player.getScore();
        }
    }

    /**
     * get winner name
     * @return winner's name
     */
    public String getWinnerName() {
        return winnerName;
    }

    /**
     * get winner's score
     * @return winner score
     */
    public int getWinnerScore() {
        return winnerScore;
    }

    /**
     * update the turn of the game
     */
    public void updateTurn() {
        if (turn == playerCount - 1) {
            turn = 0;
        } else {
            turn++;
        }
    }

    /**
     * check whether the cell could place a letter
     *
     * @param row row
     * @param col column
     * @return result
     */
    public boolean isCellAvailable(int row, int col) {
        return getCell(row, col).available;
    }

    /**
     * is the cell empty or invalid
     */
    public boolean isCellEmpty(int row, int col) {
        if (getCell(row, col) == null)
            return true;
        return getCell(row, col).letter.isEmpty();
    }

    /**
     * get cell at a position
     * @param row row
     * @param col column
     * @return cell object
     */
    public Cell getCell(int row, int col) {
        if (row < 0 || row > 19 || col < 0 || col > 19) {
            return null;
        }
        return cells[row][col];
    }

    public int getPlayerCount() {
        return playerCount;
    }

    public void setPlayerCount(int playerCount) {
        this.playerCount = playerCount;
    }

    public int getTurn() {
        return turn;
    }

    /**
     * get neighbors of the cell in order of left, top, right, bottom,
     * return null if the neighbor doesn't exist
     *
     * @param row row of cell
     * @param col column of cell
     * @return neighbors cell
     */
    private List<Cell> getCellNeighbors(int row, int col) {
        List<Cell> neighbors = new ArrayList<>();
        neighbors.add(getCell(row, col - 1));
        neighbors.add(getCell(row - 1, col));
        neighbors.add(getCell(row + 1, col));
        neighbors.add(getCell(row, col + 1));
        return neighbors;
    }

    public void setLastMove(int row, int col) {
        this.lastMove = cells[row][col];
    }

    public int getLastMoveRow() {
        return lastMove.row;
    }

    public int getLastMoveColumn() {
        return lastMove.column;
    }

    public Cell getLastMove() {
        return lastMove;
    }

    public String getId() {
        return id;
    }

    public int getPassInARow() {
        return passInARow;
    }

    public void setPassInARow(int passInARow) {
        this.passInARow = passInARow;
    }

    public GameMode getMode() {
        return mode;
    }

    public PollType getPollType() {
        return pollType;
    }

    public void setPollType(PollType pollType) {
        this.pollType = pollType;
    }
}
