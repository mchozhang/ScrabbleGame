package scrabble.server;

import scrabble.client.ClientService;
import scrabble.game.Cell;
import scrabble.game.Game;
import scrabble.game.Player;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of the ScrabbleManager interface, remote object that invoked by client.
 */
public class ScrabbleManagerImpl extends UnicastRemoteObject implements ScrabbleManager {
    private List<ClientService> clientList;
    private List<Player> playerList;
    private List<Game> gameList;

    public ScrabbleManagerImpl() throws RemoteException {
        super();
        clientList = new ArrayList<>();
        playerList = new ArrayList<>();
        gameList = new ArrayList<>();
    }

    @Override
    public boolean isPlayerExists(String name) {
        for (Player player : playerList) {
            if (name.equalsIgnoreCase(player.getUsername()))
                return true;
        }
        return false;
    }

    @Override
    public void clientConnect(ClientService client) {
        addClient(client);
        updatePlayerListInLobby();
    }

    @Override
    public void clientDisconnect(ClientService client) {
        int index = clientList.indexOf(client);
        Player player = playerList.get(index);
        if (player.getStatus() == Player.PlayerStatus.GAMING) {
            Game game = findGameById(player.getGameId());
            removeClient(client);
            gameOver(game, String.format("player %s disconnected.", player.getUsername()));
        } else {
            removeClient(client);
        }

        updatePlayerListInLobby();
    }

    @Override
    public void invitePlayers(String inviterId, List<String> invitedPlayerIdList, Game.Mode mode) {
        List<Player> invitedPlayerList = getPlayerListByIdList(invitedPlayerIdList);
        Player inviter = findPlayerFromListById(inviterId);

        for (Player player : invitedPlayerList) {
            try {
                int index = this.playerList.indexOf(player);

                if (player.getId().equals(inviterId)) {
                    // the inviter should be ready
                    player.setStatus(Player.PlayerStatus.READY);
                    clientList.get(index).onPlayerAccepted(invitedPlayerList);
                } else {
                    // others should receive invitation
                    clientList.get(index).onInvited(inviter, invitedPlayerList, mode);
                }
            } catch (RemoteException e) {
                invitedPlayerList.remove(player);
                removePlayer(player);
                e.printStackTrace();

                for (Player player1 : invitedPlayerList) {
                    player1.setStatus(Player.PlayerStatus.IDLE);
                }
                break;
            }
        }

        updatePlayerListInLobby();
    }

    @Override
    public void acceptInvitation(String accepterId, List<String> playerIdList, Game.Mode mode) {
        List<Player> invitedPlayerList = getPlayerListByIdList(playerIdList);

        // check if the invitation is still valid
        boolean isValid = false;
        for (Player player : invitedPlayerList) {
            if (player.getStatus() == Player.PlayerStatus.READY) {
                isValid = true;
                break;
            }
        }
        if (!isValid) {
            return;
        }

        int readyCount = 0;
        for (Player player : invitedPlayerList) {
            if (player.getId().equals(accepterId))
                player.setStatus(Player.PlayerStatus.READY);

            if (player.getStatus() == Player.PlayerStatus.READY)
                readyCount++;
        }

        // start a game if all players are ready
        if (readyCount == invitedPlayerList.size()) {
            startNewGame(invitedPlayerList, mode);
        } else {
            // notify other players
            for (Player player : invitedPlayerList) {
                try {
                    int index = this.playerList.indexOf(player);
                    clientList.get(index).onPlayerAccepted(invitedPlayerList);
                } catch (RemoteException e) {
                    invitedPlayerList.remove(player);
                    removePlayer(player);

                    for (Player player1 : invitedPlayerList) {
                        player1.setStatus(Player.PlayerStatus.IDLE);
                    }
                    e.printStackTrace();
                    break;
                }
            }
        }

        updatePlayerListInLobby();
    }

    /**
     * start a new game
     *
     * @param playerList players in the game
     */
    private void startNewGame(List<Player> playerList, Game.Mode mode) {
        int size = playerList.size();
        Game game = new Game(size, mode);

        // set every player's status
        for (int i = 0; i < size; i++) {
            Player player = playerList.get(i);
            player.setStatus(Player.PlayerStatus.GAMING);
            player.setGameId(game.getId());
            player.setTurn(i);
            game.assignLetters(player);
            setPlayerGamingStatus(player, game);
        }

        boolean started = true;
        for (Player player : playerList) {
            try {
                int index = this.playerList.indexOf(player);
                clientList.get(index).onGameStarted(game, playerList);
            } catch (RemoteException e) {
                removePlayer(player);
                gameOver(game, String.format("player %s disconnected.", player.getUsername()));
                e.printStackTrace();
                started = false;
                break;
            }
        }

        if (started) {
            gameList.add(game);
        }
    }

    private void setPlayerGamingStatus(Player player, Game game) {
        if (player.getTurn() == game.getTurn()) {
            player.setGamingStatus(Player.GamingStatus.PLACING_LETTER);
        } else {
            player.setGamingStatus(Player.GamingStatus.WAITING);
        }
    }

    @Override
    public void declineInvitation(String declinerId, List<String> playerIdList) {
        List<Player> invitedPlayerList = getPlayerListByIdList(playerIdList);

        for (Player player : invitedPlayerList) {
            player.setStatus(Player.PlayerStatus.IDLE);
        }

        for (Player player : invitedPlayerList) {
            try {
                int index = this.playerList.indexOf(player);
                clientList.get(index).onPlayerDecline(invitedPlayerList);
            } catch (RemoteException e) {
                removePlayer(player);
                e.printStackTrace();
            }
        }
        updatePlayerListInLobby();
    }

    @Override
    public void placeLetter(String gameId, String playerId, int letterIndex, int row, int col) {
        Game game = findGameById(gameId);
        Player player = findPlayerFromListById(playerId);

        if (!isCorrectGameMove(game, player, Player.GamingStatus.PLACING_LETTER)) {
            return;
        }

        // the player passes the turn
        if (letterIndex == -1) {
            game.setPassInARow(game.getPassInARow() + 1);

            // game is over if all players pass their turns
            if (game.getPassInARow() == game.getPlayerCount()) {
                gameOver(game, "all player pass");
            } else {
                updateGameTurn(game);
                updatePlayerListInGame(game);
            }
            return;
        }

        if (!game.isCellAvailable(row, col)) {
            return;
        }

        // player places a letter
        game.setPassInARow(0);
        game.placeLetter(player, letterIndex, row, col);
        game.assignOneLetter(player, letterIndex);
        game.setLastMove(row, col);

        // update letters in hand of the player
        try {
            int playerIndex = playerList.indexOf(player);
            clientList.get(playerIndex).onLettersInHandChanged(player);
        } catch (RemoteException e) {
            gameOver(game, String.format("player %s disconnected.", player.getUsername()));
            removePlayer(player);
            e.printStackTrace();
            return;
        }

        // update player status to PICKING WORD
        player.setGamingStatus(Player.GamingStatus.PICKING_WORD);

        updateGame(game);
        updatePlayerListInGame(game);
    }

    /**
     * finish the game
     *
     * @param game   game
     * @param reason reason why finish
     */
    private void gameOver(Game game, String reason) {
        List<Player> gamePlayers = findPlayersByGameId(game.getId());

        for (Player player : gamePlayers) {
            player.initPlayerData();
        }

        for (Player player : gamePlayers) {
            try {
                int index = playerList.indexOf(player);
                clientList.get(index).onGameFinished(this.playerList, game.getWinnerName(), game.getWinnerScore(), reason);
            } catch (RemoteException e) {
                removePlayer(player);
                e.printStackTrace();
            }
        }
        gameList.remove(game);
    }

    @Override
    public void pollForWord(String gameId, String playerId, Game.PollType pollType) {
        Game game = findGameById(gameId);
        Player player = findPlayerFromListById(playerId);

        if (!isCorrectGameMove(game, player, Player.GamingStatus.PICKING_WORD)) {
            return;
        }

        // pass, give up starting a poll
        if (pollType == Game.PollType.PASS) {
            updateGameTurn(game);
            updatePlayerListInGame(game);
            return;
        }

        game.setPollType(pollType);
        List<String> words = new ArrayList<>();
        String horizontal = game.getHorizontalString();
        String vertical = game.getVerticalString();

        switch (pollType) {
            case CROSS:
                if (horizontal.equals(vertical) && horizontal.length() == 1) {
                    words.add(horizontal);
                } else if(horizontal.length() > 1 && vertical.length() > 1) {
                    words.add(horizontal);
                    words.add(vertical);
                } else if (horizontal.length() > 1) {
                    words.add(horizontal);
                } else if (vertical.length() > 1) {
                    words.add(vertical);
                }
                break;
            case HORIZONTAL:
                words.add(horizontal);
                break;
            case VERTICAL:
                words.add(vertical);
                break;
        }

        List<Player> gamePlayers = findPlayersByGameId(gameId);
        player.setVoteStatus(Player.VoteStatus.APPROVED);
        player.setGamingStatus(Player.GamingStatus.POLLING);
        for (Player gamePlayer : gamePlayers) {
            try {
                int index = playerList.indexOf(gamePlayer);
                if (gamePlayer.getId().equals(playerId)) {
                    clientList.get(index).onPlayerApprovedVote(gamePlayers);
                } else {
                    clientList.get(index).onPollStarted(words, gamePlayers);
                }
            } catch (RemoteException e) {
                gameOver(game, String.format("player %s disconnected.", gamePlayer.getUsername()));
                removePlayer(gamePlayer);
                e.printStackTrace();
                return;
            }
        }

        updatePlayerListInGame(game);
    }

    @Override
    public void voteForWord(String gameId, String voterId, boolean result) {
        Game game = findGameById(gameId);
        Player voter = findPlayerFromListById(voterId);

        if (!isCorrectGameMove(game, voter, Player.GamingStatus.WAITING)) {
            return;
        }

        List<Player> gamePlayers = findPlayersByGameId(gameId);
        if (!result) {
            // voter opposes, poll is over
            for (Player player : gamePlayers) {
                // reset vote status
                player.setVoteStatus(Player.VoteStatus.EMPTY);
                try {
                    int index = playerList.indexOf(player);
                    String msg = String.format("Player %s voted down the word.", voter.getUsername());
                    clientList.get(index).onPollFinished(msg);
                } catch (RemoteException e) {
                    gameOver(game, String.format("player %s disconnected.", player.getUsername()));
                    removePlayer(player);
                    e.printStackTrace();
                    return;
                }
            }
            updateGameTurn(game);
            updatePlayerListInGame(game);
        } else {
            // voter approved
            voter.setVoteStatus(Player.VoteStatus.APPROVED);

            int voteCount = 0;
            for (Player player : gamePlayers) {
                if (player.getVoteStatus() == Player.VoteStatus.APPROVED) {
                    voteCount++;
                }
            }

            if (voteCount == game.getPlayerCount()) {
                //all voters have approved
                Player scorer = null;
                int points = 0;
                for (Player player : gamePlayers) {
                    if (player.getGamingStatus() == Player.GamingStatus.POLLING) {
                        scorer = player;
                        points = pointsFromWord(game);
                        playerScores(scorer, points, game);
                    }
                }

                for (Player player : gamePlayers) {
                    // reset vote status
                    player.setVoteStatus(Player.VoteStatus.EMPTY);
                    try {
                        int index = playerList.indexOf(player);
                        String msg = String.format("All players approved the word, %s got point of points %d.",
                                scorer.getUsername(), points);
                        clientList.get(index).onPollFinished(msg);
                    } catch (RemoteException e) {
                        gameOver(game, String.format("player %s disconnected.", player.getUsername()));
                        removePlayer(player);
                        e.printStackTrace();
                        return;
                    }
                }

                updateGameTurn(game);
                updatePlayerListInGame(game);
            } else {
                for (Player player : gamePlayers) {
                    try {
                        int index = playerList.indexOf(player);
                        clientList.get(index).onPlayerApprovedVote(gamePlayers);
                    } catch (RemoteException e) {
                        gameOver(game, String.format("player %s disconnected.", player.getUsername()));
                        removePlayer(player);
                        e.printStackTrace();
                        return;
                    }
                }
            }
        }
    }

    /**
     * point the player could get from the word he picked
     *
     * @param game game
     */
    private int pointsFromWord(Game game) {
        switch (game.getPollType()) {
            case HORIZONTAL:
                return game.scoresForHorizontalWord();
            case VERTICAL:
                return game.scoresForVerticalWord();
            case CROSS:
                return game.scoresForCrossWord();
        }
        return 0;
    }

    /**
     * player scores
     *
     * @param player player
     * @param point  points add to his scores
     * @param game   game
     */
    private void playerScores(Player player, int point, Game game) {
        int score = player.getScore();
        player.setScore(score + point);
        game.updateWinner(player);
    }

    /**
     * update a game turn and player status
     */
    private void updateGameTurn(Game game) {
        game.updateTurn();
        for (Player player : findPlayersByGameId(game.getId())) {
            if (player.getGameId().equals(game.getId()) && player.getTurn() == game.getTurn()) {
                player.setGamingStatus(Player.GamingStatus.PLACING_LETTER);
            } else {
                player.setGamingStatus(Player.GamingStatus.WAITING);
            }
        }
    }

    /**
     * update the player list of client in lobby
     */
    private void updatePlayerListInLobby() {
        for (int i = 0; i < playerList.size(); i++) {
            Player player = playerList.get(i);
            if (player.getStatus() != Player.PlayerStatus.GAMING) {
                ClientService client = clientList.get(i);
                try {
                    client.onPlayerListChanged(playerList);
                } catch (RemoteException e) {
                    removeClient(client);
                    e.printStackTrace();
                    updatePlayerListInLobby();
                }
            }
        }
    }

    /**
     * update the player list of all the players in the game
     *
     * @param game game
     */
    private void updatePlayerListInGame(Game game) {
        List<Player> playerList = findPlayersByGameId(game.getId());

        for (int i = 0; i < playerList.size(); i++) {
            ClientService client = clientList.get(i);
            try {
                client.onPlayerListChanged(playerList);
            } catch (RemoteException e) {
                removeClient(client);
                e.printStackTrace();
            }
        }
    }

    /**
     * update the game status of players in the game
     *
     * @param game
     */
    private void updateGame(Game game) {
        List<Player> gamePlayers = findPlayersByGameId(game.getId());
        for (Player player : gamePlayers) {
            int index = playerList.indexOf(player);
            ClientService client = clientList.get(index);
            try {
                client.onGameStatusChanged(game);
            } catch (RemoteException e) {
                removeClient(client);
                e.printStackTrace();
            }
        }
    }

    /**
     * add a client service and its objects,
     * assign an ID to the player.
     *
     * @param client client service instance
     */
    private void addClient(ClientService client) {
        try {
            clientList.add(client);
            Player player = client.getPlayer();
            playerList.add(player);
            player.setStatus(Player.PlayerStatus.IDLE);
            client.onClientConnected(playerList);
        } catch (RemoteException e) {
            removeClient(client);
            e.printStackTrace();
        }
    }

    /**
     * remove player from list and its client object
     *
     * @param player player
     */
    private void removePlayer(Player player) {
        clientList.remove(playerList.indexOf(player));
        playerList.remove(player);
    }

    /**
     * remove an client service and its corresponding objects
     */
    private void removeClient(ClientService client) {
        playerList.remove(clientList.indexOf(client));
        clientList.remove(client);
    }

    /**
     * get players object list by list of id
     *
     * @param idList id list
     * @return player list
     */
    private List<Player> getPlayerListByIdList(List<String> idList) {
        List<Player> playerList = new ArrayList<>();
        for (String id : idList) {
            Player player = findPlayerFromListById(id);
            if (player != null) {
                playerList.add(player);
            }
        }
        return playerList;
    }

    /**
     * find player from player list by id
     */
    private Player findPlayerFromListById(String id) {
        for (Player player : this.playerList) {
            if (player.getId().equals(id)) {
                return player;
            }
        }
        return null;
    }

    /**
     * find game from game list by id
     *
     * @param id game id
     * @return game object
     */
    private Game findGameById(String id) {
        for (Game game : this.gameList) {
            if (game.getId().equals(id)) {
                return game;
            }
        }
        return null;
    }

    /**
     * find all the players in the same game
     *
     * @param gameId game id
     * @return player list
     */
    private List<Player> findPlayersByGameId(String gameId) {
        List<Player> playerList = new ArrayList<>();
        for (Player player : this.playerList) {
            if (player.getGameId().equals(gameId)) {
                playerList.add(player);
            }
        }
        return playerList;
    }

    /**
     * check if the player's invocation is valid
     *
     * @param game   game
     * @param player player
     * @param status correct status
     * @return result
     */
    private boolean isCorrectGameMove(Game game, Player player, Player.GamingStatus status) {
        if (game == null || player == null) {
            return false;
        }

        if (!player.getGameId().equals(game.getId())) {
            return false;
        }

        if (status == Player.GamingStatus.WAITING) {
            List<Player> players = findPlayersByGameId(game.getId());
            for (Player gamePlayer : players) {
                if (gamePlayer.getGamingStatus() == Player.GamingStatus.POLLING)
                    return true;
            }
            return false;
        }

        if (game.getTurn() != player.getTurn()) {
            return false;
        }

        if (player.getGamingStatus() != status) {
            return false;
        }

        return true;
    }
}
