package scrabble.client;

import com.jfoenix.controls.JFXButton;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;

public class BasicGameControllerMode implements GameControllerMode {
    @Override
    public Button[] createLetterButtons() {
        return new Button[26];
    }

    @Override
    public void setupLetterButton(Button[] buttons, GridPane letterPane) {

        for (int i = 0; i < 9; i++) {
            Button button = new JFXButton();
            Utils.initLetterButton(button);
            letterPane.add(button, i, 0);
            buttons[i] = button;
        }

        for (int i = 0; i < 9; i++) {
            Button button = new JFXButton();
            Utils.initLetterButton(button);
            letterPane.add(button, i, 1);
            buttons[i  + 9] = button;
        }

        for (int i = 0; i < 8; i++) {
            Button button = new JFXButton();
            Utils.initLetterButton(button);
            letterPane.add(button, i, 2);
            buttons[i  + 18] = button;
        }
    }
}
