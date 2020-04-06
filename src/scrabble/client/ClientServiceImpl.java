package scrabble.client;

import javafx.application.Platform;
import scrabble.game.Game;
import scrabble.game.Player;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

/**
 * implementation of ClientService, remote object that invocated by server
 */
public class ClientServiceImpl extends UnicastRemoteObject implements ClientService {
    // the player himself of this client
    private Player player;
    private GameListener gameListener;
    private LoginListener loginListener;
    private LobbyListener lobbyListener;

    public ClientServiceImpl() throws RemoteException {
        super();
    }

    @Override
    public void onClientConnected(List<Player> playerList) {
        Platform.runLater(() -> {
            loginListener.onLogin(playerList);
        });
    }

    @Override
    public void onPlayerListChanged(List<Player> playerList) {
        Platform.runLater(() -> {
            if (player.getStatus() == Player.PlayerStatus.GAMING) {
                if (gameListener != null) {
                    gameListener.onPlayerListChanged(playerList);
                }
            } else {
                if (lobbyListener != null) {
                    lobbyListener.onPlayerListChanged(playerList);
                }
            }
        });
    }

    @Override
    public void onInvited(Player inviter, List<Player> playerList, Game.Mode mode) {
        Platform.runLater(() -> {
            if (lobbyListener != null) {
                lobbyListener.onInvited(inviter, playerList, mode);
            }
        });
    }

    @Override
    public void onPlayerAccepted(List<Player> playerList) {
        Platform.runLater(() -> {
            if (lobbyListener != null) {
                lobbyListener.onPlayerAccepted(playerList);
            }
        });
    }

    @Override
    public void onPlayerDecline(List<Player> playerList) {
        Platform.runLater(() -> {
            if (lobbyListener != null) {
                lobbyListener.onPlayerDecline(playerList);
            }
        });
    }

    @Override
    public void onGameStarted(Game game, List<Player> playerList) {
        Platform.runLater(() -> {
            if (lobbyListener != null) {
                lobbyListener.onGameStarted(game, playerList);
            }
        });
    }

    @Override
    public void onGameStatusChanged(Game game) {
        Platform.runLater(() -> {
            gameListener.onGameStatusChanged(game);
        });
    }

    @Override
    public void onLettersInHandChanged(Player player) {
        Platform.runLater(() -> {
            gameListener.onLettersInHandChanged(player);
        });
    }

    @Override
    public void onPollStarted(List<String> words, List<Player> playerList) {
        Platform.runLater(() -> {
            gameListener.onPollStarted(words, playerList);
        });
    }

    @Override
    public void onPlayerApprovedVote(List<Player> playerList) {
        Platform.runLater(() -> {
            gameListener.onPlayerApprovedVote(playerList);
        });
    }

    @Override
    public void onPollFinished(String message) {
        Platform.runLater(() -> {
            gameListener.onPollFinished(message);
        });
    }

    @Override
    public void onGameFinished(List<Player> playerList, String winner, int score, String reason) {
        Platform.runLater(() -> {
            gameListener.onGameFinished(playerList, winner, score, reason);
        });
    }

    @Override
    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public void setLoginListener(LoginListener loginListener) {
        this.loginListener = loginListener;
    }

    public void setLobbyListener(LobbyListener lobbyListener) {
        this.lobbyListener = lobbyListener;
    }

    public void setGameListener(GameListener gameListener) {
        this.gameListener = gameListener;
    }
}
