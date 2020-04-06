package scrabble.client;

import com.jfoenix.controls.JFXCheckBox;
import javafx.beans.property.*;
import javafx.scene.control.CheckBox;
import scrabble.game.Player;

/**
 * item in player list
 */
public class PlayerListItem {

    private Player player;
    private boolean isMe;
    private int index;

    private JFXCheckBox checkBox;
    private StringProperty indexProperty;
    private StringProperty name;
    private StringProperty status;
    private StringProperty gameStatus;
    private StringProperty score;
    private StringProperty turn;

    public PlayerListItem(int index, Player player, boolean isMe) {
        indexProperty = new SimpleStringProperty();
        name = new SimpleStringProperty();
        status = new SimpleStringProperty();
        score = new SimpleStringProperty();
        gameStatus = new SimpleStringProperty();
        turn = new SimpleStringProperty();
        checkBox = new JFXCheckBox();
        checkBox.setFocusTraversable(false);

        this.index = index;
        this.isMe = isMe;
        if (player.getStatus() != Player.PlayerStatus.IDLE || isMe) {
            checkBox.setVisible(false);
        } else {
            checkBox.setVisible(true);
        }
        setPlayer(player);
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
        indexProperty.set(index + "");
        name.set(player.getUsername());
        score.set(player.getScore() + "");
        status.set(player.getStatus().toString());
        gameStatus.set(player.getGamingStatus().toString());

        if (player.getGamingStatus() == Player.GamingStatus.WAITING) {
            turn.set("");
        } else {
            turn.set("→");
        }
    }

    public String getTurn() {
        return turn.get();
    }

    public StringProperty turnProperty() {
        return turn;
    }

    public void setTurn(String turn) {
        this.turn.set(turn);
    }

    public String getIndex() {
        return indexProperty.get();
    }

    public StringProperty indexProperty() {
        return indexProperty;
    }

    public void setIndex(int index) {
        this.indexProperty.set(index + "");
    }

    public String getName() {
        return name.get();
    }

    public StringProperty nameProperty() {
        return name;
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public String getStatus() {
        return status.get();
    }

    public StringProperty statusProperty() {
        return status;
    }

    public void setStatus(String status) {
        this.status.set(status);
    }

    public String getGameStatus() {
        return gameStatus.get();
    }

    public StringProperty gameStatusProperty() {
        return gameStatus;
    }

    public void setGameStatus(String gameStatus) {
        this.gameStatus.set(gameStatus);
    }

    public String getScore() {
        return score.get();
    }

    public StringProperty scoreProperty() {
        return score;
    }

    public void setScore(int score) {
        this.score.set(score + "");
    }

    public JFXCheckBox getCheckBox() {
        return checkBox;
    }

    public void setCheckBox(JFXCheckBox checkBox) {
        this.checkBox = checkBox;
    }
}
