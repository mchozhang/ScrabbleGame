package scrabble.client;

import com.jfoenix.controls.JFXButton;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;

public class AdvancedGameControllerMode implements GameControllerMode {
    @Override
    public Button[] createLetterButtons() {
        return new Button[12];
    }

    @Override
    public void setupLetterButton(Button[] buttons, GridPane letterPane) {
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 6; j++) {
                Button button = new JFXButton();
                Utils.initLetterButton(button);
                letterPane.add(button, j, i);
                buttons[i * 6 + j] = button;
            }
        }
    }
}
