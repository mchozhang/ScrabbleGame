package scrabble.client;

import com.jfoenix.controls.JFXButton;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import scrabble.game.*;
import scrabble.game.Cell;
import scrabble.server.ScrabbleManager;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Optional;

import static scrabble.game.Player.GamingStatus.*;

/**
 * the interface controller of the scrabble game window
 */
public class GameController implements GameListener {
    public static final int WIDTH = 850;
    public static final int HEIGHT = 650;

    private MainApp mainApp;
    private ScrabbleManager scrabbleManager;
    private ClientService clientService;

    @FXML
    private TableView<PlayerListItem> playerTableView;
    @FXML
    private TableView<PlayerListItem> playerTable;
    @FXML
    private TableColumn<PlayerListItem, String> turnCol;
    @FXML
    private TableColumn<PlayerListItem, String> nameCol;
    @FXML
    private TableColumn<PlayerListItem, String> scoreCol;
    @FXML
    private TextArea consoleText;
    @FXML
    private GridPane boardPane;
    @FXML
    private GridPane letterPane;
    @FXML
    private VBox placeLetterBox;
    @FXML
    private JFXButton confirmBtn;
    @FXML
    private JFXButton letterPassBtn;
    @FXML
    private VBox pickWordBox;
    @FXML
    private JFXButton horizontalWordBtn;
    @FXML
    private JFXButton verticalWordBtn;
    @FXML
    private JFXButton crossWordBtn;
    @FXML
    private JFXButton wordPassBtn;
    @FXML
    private VBox pollBox;
    @FXML
    private Text buttonText;
    @FXML
    private HBox voteBox;
    @FXML
    private JFXButton approveBtn;
    @FXML
    private JFXButton opposeBtn;

    private ObservableList<PlayerListItem> playerItemList;
    private Button[][] cellButtons;
    private Button[] letterButtons;

    // game entities
    private Player player;
    private Game game;
    private GameControllerMode controllerMode;

    // the position of the selected cell
    private int selectedRow;
    private int selectedCol;

    // the position of the letter placed
    private int placedLetterIndex;
    private int placedLetterRow;
    private int placedLetterCol;

    public void setup(MainApp mainApp, Game game, List<Player> playerList) {
        this.mainApp = mainApp;
        scrabbleManager = mainApp.getScrabbleManager();
        clientService = mainApp.getClientService();
        ((ClientServiceImpl) clientService).setGameListener(this);
        this.game = game;

        // update player's data
        player = mainApp.getPlayer();
        for (Player player : playerList) {
            if (this.player.equals(player)) {
                this.player.updatePlayerData(player);
            }
        }

        // set game controller mode
        if (game.getMode() instanceof BasicGameMode) {
            controllerMode = new BasicGameControllerMode();
        } else {
            controllerMode = new AdvancedGameControllerMode();
        }

        initUI(playerList);
        initGame(playerList);
    }

    /**
     * setup UI elements
     */
    private void initUI(List<Player> playerList) {
        // player list view
        playerItemList = FXCollections.observableArrayList();
        for (int i = 0; i < playerList.size(); i++) {
            Player player = playerList.get(i);
            boolean isMe = this.player.equals(player);
            playerItemList.add(new PlayerListItem(i + 1, player, isMe));
        }
        playerTable.setItems(playerItemList);
        turnCol.setCellValueFactory(cellData -> cellData.getValue().turnProperty());
        nameCol.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        scoreCol.setCellValueFactory(cellData -> cellData.getValue().scoreProperty());

        // board pane
        Utils.initBoard(boardPane);

        // cell buttons in board: 20 * 20 grid
        cellButtons = new Button[20][20];
        for (int i = 0; i < 20; i++) {
            for (int j = 0; j < 20; j++) {
                JFXButton button = new JFXButton();
                Utils.initCellButton(button);
                boardPane.add(button, j, i);
                cellButtons[i][j] = button;
                button.setOnAction(event -> {
                    if (player.getGamingStatus() != PLACING_LETTER) {
                        return;
                    }
                    int row = GridPane.getRowIndex(button);
                    int col = GridPane.getColumnIndex(button);

                    if (!game.getCell(row, col).available) {
                        return;
                    }

                    // unselect the button if it's selected
                    if (selectedRow == row && selectedCol == col) {
                        unselectCell();
                        return;
                    }

                    // select the button if it's empty
                    if (game.isCellEmpty(row, col)) {
                        unselectCell();
                        selectCell(row, col);
                    } else {
                        unselectCell();
                    }
                });
            }
        }

        // letter pane
        letterButtons = controllerMode.createLetterButtons();
        controllerMode.setupLetterButton(letterButtons, letterPane);
        for (int i = 0; i < letterButtons.length; i++) {
            final int letterIndex = i;
            Button button = letterButtons[i];
            button.setOnAction(event -> {
                if (player.getGamingStatus() != PLACING_LETTER || !hasSelectedCell()) {
                    return;
                }

                // withdraw the letter placed
                if (hasPlacedLetter()) {
                    cellButtons[placedLetterRow][placedLetterCol].setText("");
                    if (placedLetterIndex == letterIndex) {
                        recoverPlacedLetter();
                        return;
                    }
                }

                // place letter
                placedLetterIndex = letterIndex;
                placedLetterRow = selectedRow;
                placedLetterCol = selectedCol;
                cellButtons[selectedRow][selectedCol].setText(player.getLettersInHand().get(placedLetterIndex));
            });
        }

        // confirm button
        confirmBtn.setOnAction(event -> {
            if (player.getGamingStatus() != PLACING_LETTER || !hasPlacedLetter()) {
                return;
            }

            try {
                scrabbleManager.placeLetter(game.getId(), player.getId(), placedLetterIndex, placedLetterRow, placedLetterCol);
                unselectCell();
                recoverPlacedLetter();
            } catch (RemoteException e) {
                if (Utils.showConnectFailWindow().get() == ButtonType.OK) {
                    mainApp.showLoginScene();
                }
                e.printStackTrace();
            }
        });

        // letter pass button
        letterPassBtn.setOnAction(event -> {
            if (player.getGamingStatus() != PLACING_LETTER) {
                return;
            }

            try {
                scrabbleManager.placeLetter(game.getId(), player.getId(), -1, -1, -1);
                unselectCell();
                recoverPlacedLetter();
            } catch (RemoteException e) {
                if (Utils.showConnectFailWindow().get() == ButtonType.OK) {
                    mainApp.showLoginScene();
                }
                e.printStackTrace();
            }
        });

        // picking word buttons
        horizontalWordBtn.setOnAction(event -> {
            pollForWord(Game.PollType.HORIZONTAL);
        });

        verticalWordBtn.setOnAction(event -> {
            pollForWord(Game.PollType.VERTICAL);
        });

        crossWordBtn.setOnAction(event -> {
            pollForWord(Game.PollType.CROSS);
        });

        wordPassBtn.setOnAction(event -> {
            pollForWord(Game.PollType.PASS);
        });

        // vote buttons
        approveBtn.setOnAction(event -> {
            voteForWord(true);
        });

        opposeBtn.setOnAction(event -> {
            voteForWord(false);
        });
    }

    /**
     * initialize game
     */
    private void initGame(List<Player> playerList) {
        selectedCol = -1;
        selectedRow = -1;
        recoverPlacedLetter();

        updatePlayerList(playerList);
        updateLetterPane();
        updateBoardPane();
    }

    /**
     * select a cell on the game board
     */
    private void selectCell(int row, int col) {
        selectedRow = row;
        selectedCol = col;
        cellButtons[selectedRow][selectedCol].setId("selectedCell");
    }

    /**
     * recover the current selected button
     */
    private void unselectCell() {
        if (hasSelectedCell()) {
            cellButtons[selectedRow][selectedCol].setId("cellBtn");
            selectedRow = -1;
            selectedCol = -1;
        }
    }

    /**
     * if has selected a cell of the board
     *
     * @return result
     */
    private boolean hasSelectedCell() {
        return selectedRow != -1 && selectedCol != -1;
    }

    /**
     * recover the placed letter
     */
    private void recoverPlacedLetter() {
        placedLetterIndex = -1;
        placedLetterRow = -1;
        placedLetterCol = -1;
    }

    /**
     * if has placed a letter on the board
     *
     * @return result
     */
    private boolean hasPlacedLetter() {
        return placedLetterIndex != -1;
    }

    /**
     * start a poll for the word
     */
    private void pollForWord(Game.PollType pollType) {
        if (player.getGamingStatus() != PICKING_WORD)
            return;

        try {
            scrabbleManager.pollForWord(game.getId(), player.getId(), pollType);
            showBox(false, false, false);
        } catch (RemoteException e) {
            if (Utils.showConnectFailWindow().get() == ButtonType.OK) {
                mainApp.showLoginScene();
            }
            e.printStackTrace();
        }
    }

    /**
     * vote for the words chosen by other players
     *
     * @param result approved or opposed
     */
    private void voteForWord(boolean result) {
        if (player.getVoteStatus() != Player.VoteStatus.EMPTY) {
            return;
        }
        try {
            scrabbleManager.voteForWord(game.getId(), player.getId(), result);
            approveBtn.setVisible(false);
            opposeBtn.setVisible(false);
        } catch (RemoteException e) {
            if (Utils.showConnectFailWindow().get() == ButtonType.OK) {
                mainApp.showLoginScene();
            }
            e.printStackTrace();
        }
    }

    @Override
    public void onPlayerListChanged(List<Player> playerList) {
        this.playerItemList.clear();
        updatePlayerList(playerList);
    }

    @Override
    public void onGameStatusChanged(Game game) {
        this.game = game;
        updateBoardPane();
    }

    @Override
    public void onLettersInHandChanged(Player player) {
        this.player = player;
        updateLetterPane();
    }

    @Override
    public void onPollStarted(List<String> words, List<Player> playerList) {
        showBox(false, false, true);
        // words
        String text = "";
        if (words.size() == 2) {
            text = String.format("Are \"%s\" and \"%s\" both words?", words.get(0), words.get(1));
        } else if (words.size() == 1) {
            text = String.format("Is \"%s\" a word?", words.get(0));
        }
        buttonText.setText(text);

        // vote box
        setVoteBox(playerList);
    }

    @Override
    public void onPlayerApprovedVote(List<Player> playerList) {
        for (Player player : playerList) {
            if (player.getId().equals(this.player.getId()))
                this.player.updatePlayerData(player);
        }
        showBox(false, false, true);

        // vote box
        setVoteBox(playerList);
    }

    /**
     * show vote status, how many players has approved
     *
     * @param playerList player list
     */
    private void setVoteBox(List<Player> playerList) {
        voteBox.getChildren().clear();
        for (Player voter : playerList) {
            JFXButton button = new JFXButton();
            button.setId("voteBtn");
            if (voter.getVoteStatus() == Player.VoteStatus.APPROVED) {
                button.setId("greenVoteBtn");
            }
            voteBox.getChildren().add(button);
        }
    }

    @Override
    public void onPollFinished(String message) {
        voteBox.getChildren().clear();
        consoleText.appendText(message + "\n");
        showBox(false, false, false);
    }

    @Override
    public void onGameFinished(List<Player> playerList, String winner, int score, String reason) {
        // show game over popup window
        String msg = String.format("The game is over, %s", reason);

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Game Over");
        alert.setHeaderText(msg);
        if (winner != null) {
            alert.setContentText(String.format("The winner is %s with scores of %d", winner, score));
        } else {
            alert.setContentText("Nobody won.");
        }
        ButtonType exit = new ButtonType("Exit");
        alert.getButtonTypes().setAll(exit);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == exit) {
            for (Player player : playerList) {
                if (player.equals(this.player)) {
                    this.player.updatePlayerData(player);
                }
            }
            mainApp.showLobbyScene(playerList);
        }
    }

    /**
     * refresh player list, update players data
     */
    private void updatePlayerList(List<Player> playerList) {
        playerItemList.clear();
        for (Player player : playerList) {
            boolean isMe = false;
            if (player.equals(this.player)) {
                isMe = true;
                this.player.updatePlayerData(player);

                if (this.player.getGamingStatus() == WAITING) {
                    boolean poll = false;
                    for (Player player1 : playerList) {
                        if (player1.getGamingStatus() == POLLING) {

                            poll = true;
                            break;
                        }
                    }
                    showBox(false, false, poll);
                }

                if (this.player.getGamingStatus() == PLACING_LETTER) {
                                        showBox(true, false, false);
                }

                if (this.player.getGamingStatus() == PICKING_WORD) {
                                        showBox(false, true, false);

                    String horizontalWord = game.getHorizontalString();
                    String verticalWord = game.getVerticalString();
                    if (horizontalWord.length() == 1 && horizontalWord.equals(verticalWord)) {
                        // single letter
                        horizontalWordBtn.setVisible(false);
                        verticalWordBtn.setVisible(false);
                        crossWordBtn.setText(horizontalWord);
                    } else if (horizontalWord.length() == 1 || verticalWord.length() == 1) {
                        horizontalWordBtn.setVisible(false);
                        verticalWordBtn.setVisible(false);
                        String word = horizontalWord.length() == 1 ? verticalWord : horizontalWord;
                        crossWordBtn.setText(word);
                    } else {
                        horizontalWordBtn.setVisible(true);
                        verticalWordBtn.setVisible(true);
                        horizontalWordBtn.setText(horizontalWord);
                        verticalWordBtn.setText(verticalWord);
                        crossWordBtn.setText(horizontalWord + " & " + verticalWord);
                    }
                }
            }

            playerItemList.add(new PlayerListItem(0, player, isMe));
        }
    }

    /**
     * refresh letter pane with letters in hand
     */
    private void updateLetterPane() {
        for (int i = 0; i < letterButtons.length; i++) {
            Button button = letterButtons[i];
            if (i < player.getLettersInHand().size()) {
                button.setText(player.getLettersInHand().get(i));
            } else {
                button.setText("");
            }
        }
    }

    /**
     * refresh board with cell letters
     */
    private void updateBoardPane() {
        for (int i = 0; i < 20; i++) {
            for (int j = 0; j < 20; j++) {
                Cell cell = game.getCell(i, j);
                cellButtons[i][j].setText(cell.letter);
            }
        }
    }

    /**
     * show button box by player's status
     *
     * @param placeLetter if show place letter button
     * @param pickWord    if show pick word button
     * @param poll        if show poll word button
     */
    private void showBox(boolean placeLetter, boolean pickWord, boolean poll) {
        placeLetterBox.setVisible(placeLetter);
        pickWordBox.setVisible(pickWord);
        pollBox.setVisible(poll);

        if (placeLetter) {
            buttonText.setText("Place a letter:");
        }

        if (pickWord) {
            buttonText.setText("Choose the word to score:");
        }

        if (poll) {
            if (player.getGamingStatus() == POLLING) {
                buttonText.setText("Waiting for the vote result: ");
                approveBtn.setVisible(false);
                opposeBtn.setVisible(false);
            } else {
                if (player.getVoteStatus() == Player.VoteStatus.EMPTY) {
                    approveBtn.setVisible(true);
                    opposeBtn.setVisible(true);
                } else {
                    approveBtn.setVisible(false);
                    opposeBtn.setVisible(false);
                }
            }
        }

        if (!placeLetter && !pickWord && !poll) {
            buttonText.setText("");
        }
    }
}
