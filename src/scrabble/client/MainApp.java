package scrabble.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import scrabble.game.Game;
import scrabble.game.Player;
import javafx.scene.Parent;
import scrabble.server.ScrabbleManager;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.io.IOException;

/**
 * MainApp window entrance,
 * javafx application object
 */
public class MainApp extends Application {
    private static final String TITLE = "Scrabble Game";

    private static String host;

    private Stage primaryStage;
    private ClientService clientService;
    private ScrabbleManager scrabbleManager;
    private Player player;
    private boolean isConnected;
    private  LoginController loginController;

    private Thread shutdownHook;

    public static void main(String[] args) {
        host = args.length == 1 ? args[0] : "localhost";
        launch(args);
    }

    public void setHost(String host) {
        this.host = host;
    }


    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;
        primaryStage.setTitle(TITLE);
        primaryStage.setOnCloseRequest(event -> {
            primaryStage.close();
            System.exit(0);
        });

        initClientService();
        showLoginScene();
    }

    /**
     * initialize and display login scene
     */
    public void showLoginScene() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("views/login.fxml"));
            Parent root = loader.load();
            // Parent root = loader.load(getClass().getResourceAsStream("views/login.fxml"));
            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            primaryStage.show();
            this.loginController = loader.getController();
            loginController.setup(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * initialize and display lobby scene
     *
     * @param playerList player list
     */
    public void showLobbyScene(List<Player> playerList) {
        try {
            isConnected = true;
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("views/lobby.fxml"));
            Parent root = loader.load(getClass().getResourceAsStream("views/lobby.fxml"));
            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            LobbyController lobbyController = loader.getController();
            lobbyController.setup(this, playerList);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * initialize and display game scene
     */
    public void showGameScene(Game game, List<Player> playerList) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("views/game.fxml"));
            Parent root = loader.load(getClass().getResourceAsStream("views/game.fxml"));
            Scene scene = new Scene(root, GameController.WIDTH, GameController.HEIGHT);
            primaryStage.setScene(scene);
            GameController gameController = loader.getController();
            gameController.setup(this, game, playerList);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * initialize client service
     */
    private void initClientService() {
        try {
            player = new Player();
            clientService = new ClientServiceImpl();
            ((ClientServiceImpl) clientService).setPlayer(player);
            isConnected = false;
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * RMI get ScrabbleManager from remote server
     */
    public void connectServer() {
        try {
            // set up registry and 2 second timeout
            Registry registry = LocateRegistry.getRegistry(host, 1099, new RMIClientSocketFactory() {
                @Override
                public Socket createSocket(String host, int port) throws IOException {
                    Socket socket = new Socket();
                    socket.connect(new InetSocketAddress(host, port), 2000);
                    return socket;
                }
            });

            //Retrieve the stub for the remote ScrabbleManager from the registry
            scrabbleManager = (ScrabbleManager) registry.lookup("scrabble");

            // add disconnection handler
            loginController.setScrabbleManager(scrabbleManager);
            if (shutdownHook != null) {
                Runtime.getRuntime().removeShutdownHook(shutdownHook);
            }
            shutdownHook = new Thread(this::clientDisconnect);
            Runtime.getRuntime().addShutdownHook(shutdownHook);
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error Dialog");
            alert.setHeaderText(null);
            alert.setContentText("Ooops, connection failed!");
            alert.showAndWait();
        }
    }

    /**
     * client disconnect handler, notify server of player disconnection
     */
    private void clientDisconnect() {
        if (scrabbleManager != null && isConnected) {
            try {
                scrabbleManager.clientDisconnect(clientService);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public ClientService getClientService() {
        return clientService;
    }

    public ScrabbleManager getScrabbleManager() {
        return scrabbleManager;
    }

    public Player getPlayer() {
        return player;
    }

    public String getHost() {
        return host;
    }

}
