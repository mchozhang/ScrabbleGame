package scrabble.client;

import scrabble.game.Player;

import java.util.List;

/**
 * login controller listener interface
 */
public interface LoginListener {
    void onLogin(List<Player> player);
}
