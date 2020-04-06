package scrabble.client;

import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;

/**
 * game controller mode interface, how should the game controller setup the UI elements
 */
public interface GameControllerMode {
    /**
     * declare the letter buttons
     * @return letter buttons
     */
    Button[] createLetterButtons();

    /**
     * setup the letter buttons
     * @param buttons letter buttons
     * @param letterPane letter grid pane
     */
    void setupLetterButton(Button[] buttons, GridPane letterPane);
}
