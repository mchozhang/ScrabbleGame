package scrabble.server;

import scrabble.client.ClientService;
import scrabble.game.Game;
import scrabble.game.Player;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 * the remote interface of ScrabbleManagerImpl.
 * Scrabble manager can create, delete scrabble games.
 */
public interface ScrabbleManager extends Remote {
    /**
     * check if the username is existed
     * @param name username
     * @return result
     */
    boolean isPlayerExists(String name) throws RemoteException;

    /**
     * client connect to the server, transfer the player object
     * @param client client service rmi object
     */
    void clientConnect(ClientService client) throws RemoteException;

    /**
     * client disconnect
     * @param client client service rmi object
     */
    void clientDisconnect(ClientService client) throws RemoteException;

    /**
     * a player invite other players to start a game
     * @param inviterId id of inviter player
     * @param playerIdList id of players got invited
     * @param mode game mode
     * @throws RemoteException
     */
    void invitePlayers(String inviterId, List<String> playerIdList, Game.Mode mode) throws RemoteException;

    /**
     * accept a game invitation
     * @param playerId the id of the player who accepts invitation
     * @param playerIdList id of players got invited
     * @param mode game mode
     */
    void acceptInvitation(String playerId, List<String> playerIdList, Game.Mode mode) throws RemoteException;

    /**
     * decline a game invitation
     * @param playerId the id of the player who declines invitation
     * @param playerIdList id of players got invited
     */
    void declineInvitation(String playerId, List<String> playerIdList) throws RemoteException;

    /**
     * a player places a letter on the board
     * @param gameId game id
     * @param playerId player id
     * @param letterIndex the index of the letters in hand
     * @param row row of the board
     * @param column column of the board
     * @throws RemoteException
     */
    void placeLetter(String gameId, String playerId, int letterIndex, int row, int column) throws RemoteException;

    /**
     * start a poll for the word or pass
     * @param gameId game id
     * @param playerId player id
     * @param pollType poll type
     */
    void pollForWord(String gameId, String playerId, Game.PollType pollType) throws RemoteException;

    /**
     * vote for a word poll
     * @param gameId game id
     * @param playerId voter id
     * @param result result of the vote
     */
    void voteForWord(String gameId, String playerId, boolean result) throws RemoteException;


}