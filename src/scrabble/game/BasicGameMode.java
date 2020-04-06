package scrabble.game;

import java.io.Serializable;

public class BasicGameMode implements GameMode, Serializable {
    @Override
    public void initBoard(Cell[][] cells) {
        for (int i = 0; i < 20; i++) {
            for (int j = 0; j < 20; j++) {
                cells[i][j] = new Cell();
                cells[i][j].row = i;
                cells[i][j].column = j;
                cells[i][j].score = 1;
                cells[i][j].letter = "";
                cells[i][j].available = true;
            }
        }
    }

    @Override
    public void assignOneLetter(Player player, int letterIndex) {

    }

    @Override
    public void assignLetters(Player player) {
        for (int i = 0;i < 26; i++) {
            char c = 'A';
            c += i;
            String letter = Character.toString(c);
            player.getLettersInHand().add(letter);
        }
    }
}
