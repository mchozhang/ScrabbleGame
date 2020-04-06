package scrabble.client;

import scrabble.game.Game;
import scrabble.game.Player;

import java.util.List;

public interface LobbyListener {
    /**
     * player data has changed
     * @param playerList player list
     */
    void onPlayerListChanged(List<Player> playerList);

    /**
     * being invited to a game
     * @param inviter inviter
     * @param playerList players got invited
     * @param mode game mode
     */
    void onInvited(Player inviter, List<Player> playerList, Game.Mode mode);

    /**
     * player invited in the same game has accepted invitation
     * @param playerList players got invited
     */
    void onPlayerAccepted(List<Player> playerList);

    /**
     * player invited in the same game has declined invitation
     * @param pLayerList players got invited
     */
    void onPlayerDecline(List<Player> pLayerList);

    /**
     * all the players have accepted the invitation, a new game started
     * @param game game object
     * @param playerList players in the game
     */
    void onGameStarted(Game game, List<Player> playerList);
}
