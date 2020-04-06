package scrabble.game;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AdvancedGameMode implements GameMode, Serializable {
    private List<String> letterPool;

    public AdvancedGameMode() {
        // initialize letter pool
        letterPool = new ArrayList<>();
        for (int i = 0; i < 26; i++) {
            char c = 'A';
            c += i;
            String letter = Character.toString(c);
            for (int j = 0; j < 15; j++) {
                letterPool.add(letter);
            }
        }
    }

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
    public void assignLetters(Player player) {
        for (int i = 0; i < 12 && letterPool.size() > 0; i++) {
            player.getLettersInHand().add(getOneLetterFromPool());
        }
    }

    @Override
    public void assignOneLetter(Player player, int letterIndex) {
        player.getLettersInHand().set(letterIndex, getOneLetterFromPool());
    }

    /**
     * pop a random letter from pool
     *
     * @return letter
     */
    private String getOneLetterFromPool() {
        Random random = new Random();
        int index = random.nextInt(letterPool.size());
        String letter = letterPool.get(index);
        letterPool.remove(index);
        return letter;
    }
}
