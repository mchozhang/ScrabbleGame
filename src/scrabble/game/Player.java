package scrabble.game;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * player model, every client holds a player object represent the player himself
 */
public class Player implements Serializable {
    public enum GamingStatus {
        Empty,
        WAITING,
        PLACING_LETTER,
        PICKING_WORD,
        POLLING,
    }

    public enum PlayerStatus {
        IDLE,
        READY,
        GAMING,
    }

    public enum VoteStatus {
        EMPTY,
        APPROVED,
        OPPOSED,
    }

    /**
     * id of the player, generated by UUID utility
     */
    private String id;

    /**
     * the id of the game that the player is in,
     * if the user isn't in gaming status, this value would be empty
     */
    private String gameId;

    /**
     * the turn number of a player in game,
     * if the user isn't in gaming status, this value would be -1
     */
    private int turn;

    private String username;

    private int score;

    private List<String> lettersInHand;

    private GamingStatus gamingStatus;

    private PlayerStatus status;

    private VoteStatus voteStatus;

    public Player() {
        id = UUID.randomUUID().toString();
        username = "";
        initPlayerData();
    }

    /**
     * copy player data
     *
     * @param player player object
     */
    public void updatePlayerData(Player player) {
        id = player.id;
        username = player.username;
        gameId = player.gameId;
        turn = player.turn;
        score = player.score;
        lettersInHand = player.lettersInHand;
        gamingStatus = player.gamingStatus;
        voteStatus = player.voteStatus;
        status = player.status;
    }

    /**
     * initialize or reset a player's data
     */
    public void initPlayerData() {
        turn = -1;
        gameId = "";
        lettersInHand = new ArrayList<>();
        score = 0;
        gamingStatus = GamingStatus.Empty;
        status = PlayerStatus.IDLE;
        voteStatus = VoteStatus.EMPTY;
    }

    /**
     * check if they're the same user
     *
     * @param player player to compare
     * @return result
     */
    public boolean equals(Player player) {
        return id.equals(player.id);
    }

    public String getId() {
        return id;
    }

    public int getTurn() {
        return turn;
    }

    public void setTurn(int turn) {
        this.turn = turn;
    }

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }


    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public List<String> getLettersInHand() {
        return lettersInHand;
    }

    public void setLettersInHand(List<String> lettersInHand) {
        this.lettersInHand = lettersInHand;
    }

    public GamingStatus getGamingStatus() {
        return gamingStatus;
    }

    public void setGamingStatus(GamingStatus gamingStatus) {
        this.gamingStatus = gamingStatus;
    }

    public PlayerStatus getStatus() {
        return status;
    }

    public void setStatus(PlayerStatus status) {
        this.status = status;
    }

    public VoteStatus getVoteStatus() {
        return voteStatus;
    }

    public void setVoteStatus(VoteStatus status) {
        this.voteStatus = status;
    }
}
