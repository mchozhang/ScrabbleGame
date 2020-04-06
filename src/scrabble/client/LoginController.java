package scrabble.client;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import scrabble.game.Player;
import scrabble.server.ScrabbleManager;

import java.rmi.RemoteException;
import java.util.List;

public class LoginController implements LoginListener {

    @FXML private Label messageLabel;
    @FXML private TextField nameTextField;
    @FXML private TextField hostTextField;
    @FXML private Button loginBtn;
    @FXML private Button resetBtn;
    @FXML private Button connectBtn;

    private MainApp mainApp;
    private ScrabbleManager scrabbleManager;
    private ClientService clientService;
    private Player player;

    /**
     * initialize login controller, setup controls
     * @param app main app
     */
    public void setup(MainApp app) {
        this.mainApp = app;
        //scrabbleManager = mainApp.getScrabbleManager();
        clientService = mainApp.getClientService();
        ((ClientServiceImpl) clientService).setLoginListener(this);
        player = mainApp.getPlayer();

        hostTextField.setText(mainApp.getHost());
        nameTextField.setOnKeyPressed(event -> {
            hideMessage();
            if (event.getCode().getName().equals("Enter")) {
                login();
            }
        });


        connectBtn.setOnAction(event -> {
            setConnectBtn();
        });

        loginBtn.setOnAction(event -> {
            hideMessage();
            login();
        });

        resetBtn.setOnAction(event -> {
            nameTextField.clear();
        });
    }

    public void setScrabbleManager(ScrabbleManager scrabbleManager){
        this.scrabbleManager = scrabbleManager;
    }

    @Override
    public void onLogin(List<Player> playerList) {
        for (Player player : playerList) {
            if (player.getUsername().equals(this.player.getUsername())) {
                player.updatePlayerData(player);
            }
        }

        // redirect to lobby scene if login succeeded.
        mainApp.showLobbyScene(playerList);
    }

    /**
     * get input player name, check if exists,
     */
    private void login() {
        String playerName = nameTextField.getText().trim();
        if (playerName.isEmpty()) {
            showMessage("Please input your name.");
            return;
        }
        if(scrabbleManager != null) {
            try {
                if (scrabbleManager.isPlayerExists(playerName)) {
                    showMessage("This name already exists");
                } else {
                    player.setUsername(playerName);
                    scrabbleManager.clientConnect(clientService);
                }
            } catch (RemoteException e) {
                showMessage("Failed to connect to server.");
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            showMessage("Please connect to server first.");
        }
    }

    private void setConnectBtn(){
        String hostAddress = hostTextField.getText().trim();
        if (hostAddress.isEmpty()){
            hostAddress = "localhost";
        }
        mainApp.setHost(hostAddress);
        mainApp.connectServer();
        if(scrabbleManager != null) {
            showMessage("Connection successful.");
        }
        messageLabel.setTextFill(Color.web("#0076a3"));
    }

    private void showMessage(String message) {
        messageLabel.setText(message);
        messageLabel.setVisible(true);
    }

    private void hideMessage() {
        messageLabel.setVisible(false);
    }

}
