package scrabble.client;

import scrabble.game.Game;
import scrabble.game.Player;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface ClientService extends Remote {
    /**
     * client has logged in
     *
     * @param playerList player list of the lobby
     */
    void onClientConnected(List<Player> playerList) throws RemoteException;

    /**
     * got invited to a game
     *
     * @param inviter    inviter of the game
     * @param playerList all the players got invited
     * @param mode       game mode
     */
    void onInvited(Player inviter, List<Player> playerList, Game.Mode mode) throws RemoteException;

    /**
     * a player accept invitation
     *
     * @param playerList players got invited
     */
    void onPlayerAccepted(List<Player> playerList) throws RemoteException;

    /**
     * a player decline the invitation
     *
     * @param playerList players got invited
     */
    void onPlayerDecline(List<Player> playerList) throws RemoteException;

    /**
     * a player data has changed
     *
     * @param playerList player list
     */
    void onPlayerListChanged(List<Player> playerList) throws RemoteException;

    /**
     * the game has began
     *
     * @param game       game
     * @param playerList players of the game
     */
    void onGameStarted(Game game, List<Player> playerList) throws RemoteException;

    /**
     * a letter has placed
     *
     * @param game game
     * @throws RemoteException
     */
    void onGameStatusChanged(Game game) throws RemoteException;

    /**
     * letter in hand changed
     *
     * @param player player
     * @throws RemoteException
     */
    void onLettersInHandChanged(Player player) throws RemoteException;

    /**
     * a player put up a poll for the words he chosen
     *
     * @param words      1 or 2 words
     * @param playerList voters
     */
    void onPollStarted(List<String> words, List<Player> playerList) throws RemoteException;

    /**
     * a player approved the word to score
     *
     * @param playerList voters
     */
    void onPlayerApprovedVote(List<Player> playerList) throws RemoteException;

    /**
     * poll finished
     *
     * @param  message result of the poll
     */
    void onPollFinished(String message) throws RemoteException;

    /**
     * game is finish
     *
     * @param playerList lobby player list
     * @param winner     winner's name
     * @param score      score of the winner
     * @param reason     reason why game finishes
     */
    void onGameFinished(List<Player> playerList, String winner, int score, String reason) throws RemoteException;

    /**
     * get the player object
     *
     * @return player object
     */
    Player getPlayer() throws RemoteException;
}
