package scrabble.client;

import com.jfoenix.controls.JFXButton;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import scrabble.game.Player;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * utility methods
 */
public class Utils {
    public static void setButtonColor(Button button, Color color) {
        button.setBackground(new Background(new BackgroundFill(color, null, null)));
    }

    public static List<String> getIdListByPlayerList(List<Player> playerList) {
        List<String> ids = new ArrayList<>();
        for (Player player : playerList) {
            ids.add(player.getId());
        }
        return ids;
    }

    public static void initBoard(GridPane boardPane) {
        for (int i = 0; i < 20; i++) {
            RowConstraints rowConstraints = new RowConstraints();
            rowConstraints.setPercentHeight(5);
            ColumnConstraints columnConstraints = new ColumnConstraints();
            columnConstraints.setPercentWidth(5);
            rowConstraints.setFillHeight(true);
            columnConstraints.setFillWidth(true);
            boardPane.getColumnConstraints().add(columnConstraints);
            boardPane.getRowConstraints().add(rowConstraints);
        }
    }

    public static void initCellButton(Button button) {
        button.setId("cellBtn");
        button.setPadding(new Insets(0,0,0,0));
        GridPane.setFillWidth(button, true);
        GridPane.setFillHeight(button, true);
        button.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
    }

    public static void initLetterButton(Button button) {
        button.setPrefSize(25, 25);
        button.setPadding(new Insets(0, 0, 0, 0));
        button.setId("letterBtn");
    }

    /**
     * show connection failure popup window
     */
    public static Optional<ButtonType> showConnectFailWindow() {
        String msg = String.format("Failed to connect to server, please check your network");

        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Network Failure");
        alert.setHeaderText(msg);

        return alert.showAndWait();
    }
}
