import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class TextFlowFiller {

    private TextFlow textFlMistakesPlaces;
    private TextFlow textFlCode;
    String textStyle = "-fx-font: 16 arial;";
    ArrayList<Mistake> mistakes;//здесь будут сохраняться места ошибок и их типы
    //HashSet<Integer> numbersOfLinesWithMistakes;

    class Mistake {

        final int row;
        final int column;
        String errorMsg;//сообщение об ошибке

        Mistake(int row, int column, String errorMsg) {
            this.row = row;
            this.column = column;
            this.errorMsg = errorMsg;
        }

    }

    public void addMistake(String errorMsg, int row, int column) {

        Mistake mst = new Mistake(row, column, errorMsg);
        mistakes.add(mst);
        //numbersOfLinesWithMistakes.add(row);

    }

    public TextFlowFiller(TextFlow textFlMistakesPlaces, TextFlow textFlCode) {
        this.textFlMistakesPlaces = textFlMistakesPlaces;
        this.textFlCode = textFlCode;
        mistakes = new ArrayList<>();
    }

    public void displayLine(String fullLine) {

        //делим строку на лексемы
        String[] tokens = fullLine.split("\\s+");

        //пишем по одной лексеме, при этом проверяем
        //какие из них принадлежат к ошибочным

    }

    public void displayText(String str) {
        Text text = new Text(str);
        text.setStyle(textStyle);
        textFlMistakesPlaces.getChildren().add(text);
    }

    public void displayErroneusText(String str) {
        Text text = new Text(str);
        text.setStyle(textStyle);
        text.setFill(Color.FIREBRICK);
        textFlMistakesPlaces.getChildren().add(text);
    }

    //отображает типы ошибок и их расположение
    public void displayMistakePlaces() {

        for (Mistake m : mistakes) {
            String str = m.errorMsg + " " + m.row + ", " + m.column + "\n";
            Text text = new Text(str);
            text.setStyle(textStyle);
            textFlMistakesPlaces.getChildren().add(text);
        }

    }

}
