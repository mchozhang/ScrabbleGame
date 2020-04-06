package scrabble.client;

import scrabble.game.Game;
import scrabble.game.Player;

import java.util.List;

public interface GameListener {
    void onPlayerListChanged(List<Player> playerList);

    void onGameStatusChanged(Game game);

    void onLettersInHandChanged(Player player);

    void onPollStarted(List<String> words, List<Player> playerList);

    void onPlayerApprovedVote(List<Player> playerList);

    void onPollFinished(String string);

    void onGameFinished(List<Player> playerList, String winner, int score, String reason);
}
