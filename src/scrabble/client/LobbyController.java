package scrabble.client;

import com.jfoenix.controls.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import scrabble.game.Game;
import scrabble.game.Player;
import scrabble.server.ScrabbleManager;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LobbyController implements LobbyListener {

    @FXML
    private Label nameLabel;
    @FXML
    private JFXCheckBox modeBox;
    @FXML
    private TableView<PlayerListItem> playerTable;
    @FXML
    private TableColumn<PlayerListItem, String> idCol;
    @FXML
    private TableColumn<PlayerListItem, String> nameCol;
    @FXML
    private TableColumn<PlayerListItem, String> statusCol;
    @FXML
    private TableColumn<PlayerListItem, CheckBox> checkBoxCol;
    @FXML
    private HBox waitingListBox;
    @FXML
    private Button inviteBtn;

    private MainApp mainApp;
    private ScrabbleManager scrabbleManager;
    private ClientService clientService;
    private Player player;

    private ObservableList<PlayerListItem> playerItemList;

    /**
     * setup method
     * switch into a scene showing potential players
     */
    public void setup(MainApp mainApp, List<Player> playerList) {
        // controller entities
        this.mainApp = mainApp;
        scrabbleManager = mainApp.getScrabbleManager();
        clientService = mainApp.getClientService();
        ((ClientServiceImpl) clientService).setLobbyListener(this);
        player = mainApp.getPlayer();

        // UI elements
        nameLabel.setText("Name: " + player.getUsername());
        playerItemList = FXCollections.observableArrayList();

        for (int i = 0; i < playerList.size(); i++) {
            Player player = playerList.get(i);
            boolean isMe = this.player.equals(player);
            playerItemList.add(new PlayerListItem(i + 1, player, isMe));
        }

        playerTable.setItems(playerItemList);
        checkBoxCol.setCellValueFactory(new PropertyValueFactory<PlayerListItem, CheckBox>("checkBox"));
        idCol.setCellValueFactory(cellData -> cellData.getValue().indexProperty());
        nameCol.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        statusCol.setCellValueFactory(cellData -> cellData.getValue().statusProperty());

        inviteBtn.setOnAction(event -> {
            invitePlayers();
        });
    }

    @Override
    public void onPlayerListChanged(List<Player> playerList) {
        playerItemList.clear();

        for (int i = 0; i < playerList.size(); i++) {
            Player player = playerList.get(i);
            boolean isMe = false;
            if (this.player.equals(player)) {
                this.player.updatePlayerData(player);
                isMe = true;
            }
            playerItemList.add(new PlayerListItem(i + 1, player, isMe));
        }
    }

    @Override
    public void onInvited(Player inviter, List<Player> playerList, Game.Mode mode) {
        // show invitation popup window
        String msg = String.format("You get invited by %s to a %d-men game.",
                inviter.getUsername(), playerList.size());

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Invitation to a game");
        alert.setHeaderText(msg);
        alert.setContentText("Would you like to accept it?");
        ButtonType accept = new ButtonType("Accept");
        ButtonType decline = new ButtonType("Decline");
        alert.getButtonTypes().setAll(decline, accept);

        try {
            Optional<ButtonType> result = alert.showAndWait();
            List<String> ids = Utils.getIdListByPlayerList(playerList);
            if (result.get() == accept) {
                scrabbleManager.acceptInvitation(player.getId(), ids, mode);
            } else {
                scrabbleManager.declineInvitation(player.getId(), ids);
            }
        } catch (RemoteException e) {
            Utils.showConnectFailWindow();
            e.printStackTrace();
        }
    }

    @Override
    public void onPlayerAccepted(List<Player> playerList) {

        inviteBtn.setVisible(false);
        waitingListBox.setVisible(true);
        waitingListBox.getChildren().clear();

        // show players in waiting list box
        for (Player player : playerList) {
            JFXButton button = new JFXButton();
            button.setId("voteBtn");
            if (player.getStatus() == Player.PlayerStatus.READY) {
                button.setId("greenVoteBtn");
            }
            waitingListBox.getChildren().add(button);
        }
    }

    @Override
    public void onPlayerDecline(List<Player> pLayerList) {
        inviteBtn.setVisible(true);
        waitingListBox.setVisible(false);
        waitingListBox.getChildren().removeAll();
    }

    @Override
    public void onGameStarted(Game game, List<Player> playerList) {
        mainApp.showGameScene(game, playerList);
    }

    /**
     * invite players that checked
     */
    private void invitePlayers() {
        List<String> invitedList = new ArrayList<>();
        invitedList.add(player.getId());
        for (PlayerListItem item : playerItemList) {
            CheckBox checkBox = item.getCheckBox();
            if (checkBox.isSelected() && item.getPlayer().getStatus() == Player.PlayerStatus.IDLE) {
                invitedList.add(item.getPlayer().getId());
            }
        }

        if (invitedList.size() > 1) {
            try {
                Game.Mode mode = modeBox.isSelected() ? Game.Mode.ADVANCED : Game.Mode.BASIC;
                scrabbleManager.invitePlayers(player.getId(), invitedList, mode);
            } catch (RemoteException e) {
                Utils.showConnectFailWindow();
                e.printStackTrace();
            }
        }
    }
}
