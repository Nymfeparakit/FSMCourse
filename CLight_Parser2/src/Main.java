import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import jdk.nashorn.api.tree.Tree;

import java.awt.*;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import javafx.scene.paint.Color;

public class Main extends Application {

    static Parser parser;

    public static void main(String[] args) {

        parser = new Parser();
        parser.fillGrammarRules();
        parser.fillFirstSets();
        parser.fillFollowSets();
        parser.fillPredictTable();
        //parser.printFirstSets();
        System.out.println("\n");
        //parser.printFollowSets();
        //parser.printPredictTable();

        /*Tokenizer tokenizer = new Tokenizer();
        tokenizer.openFileToRead("test.txt");
        String nextToken = "";
        while(!(nextToken = tokenizer.getNextToken()).equals("eof")) {
            System.out.print(nextToken);
        }*/

        //parser.parse("test.txt");

        launch(args);
    }

    public void start(Stage primaryStage) {

        primaryStage.setTitle("JavaFX WebView Example");

        /*String textStyle = "-fx-font: 16 arial;";
        Text text1 = new Text("for ( int i = a ");
        Text errorText = new Text(" ) ");
        errorText.setFill(Color.FIREBRICK);
        Text text2 = new Text("; i < c ; )");
        text1.setStyle(textStyle);
        errorText.setStyle(textStyle);
        text2.setStyle(textStyle);

        ObservableList list = textFlow.getChildren();
        list.addAll(text1, errorText, text2);*/

        TextFlow textFlMistakesPlaces = new TextFlow();
        textFlMistakesPlaces.setPrefSize(600, 300);

        TextFlow textFlCode = new TextFlow();
        textFlMistakesPlaces.setPrefSize(600, 300);
        Text text = new Text("..............");
        textFlCode.getChildren().add(text);

        FlowPane root = new FlowPane();
        root.getChildren().add(textFlCode);
        root.getChildren().add(textFlMistakesPlaces);

        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.show();

        TextFlowFiller filler = new TextFlowFiller(textFlMistakesPlaces, textFlCode);
        parser.parse("test_error.txt", filler);
        filler.displayMistakePlaces();

        primaryStage.show();

    }

    private void writePredictTableToFile() {

        String tableHtml = "<table border=\"1\">\n" +
                "  <tr>\n" +
                "    <th></th>";
        StringBuilder stringBuilder = new StringBuilder(tableHtml);
        TreeSet<Symbol> terminalSymbols = (TreeSet<Symbol>)parser.terminalSymbols.clone();
        terminalSymbols.add(new Symbol("$"));
        Iterator<Symbol> it = terminalSymbols.iterator();
        while(it.hasNext()) { //печатаем горизонтальный заголовок из терминальных символов
            Symbol smbl = it.next();
            String str = "<th scope=\"col\">" + smbl + "</th>";
            stringBuilder.append(str);
        }

        HashSet<Symbol> grammarSymbols = parser.grammarSymbols;
        it = grammarSymbols.iterator();
        while (it.hasNext()) {
            Symbol symbol = it.next();
            if (symbol.isTerminal) continue;
            System.out.println(symbol);
            stringBuilder.append("</tr>\n" + //окончание предыдущей строки
                    "  <tr>"); //начало следующей строки
            String str = "<th scope=\"row\">" + symbol + "</th>";//вертикальный заголовок
            stringBuilder.append(str);
            TreeMap<Symbol, Rule> tableRow = parser.getPredictTableRow(symbol);
            for (Map.Entry<Symbol, Rule> entry : tableRow.entrySet()) {
                str = "<td>" + entry.getValue() + "</td>";
                stringBuilder.append(str);
                System.out.print(entry.getKey() + ", " + entry.getValue() + "; ");
            }
            System.out.println("\n");

        }

        stringBuilder.append("</tr>\n" + //окончание предыдущей строки
                "  </table>");

        tableHtml = stringBuilder.toString();

        /*try {
            FileOutputStream out = new FileOutputStream("table.html");
            out.write(tableHtml.getBytes());
            out.close();
        } catch (IOException e) {

        }*/

        //System.out.print(tableHtml);

    }


    /*int func ( ) {
        int a = 0;
        int c = 0;
        + for ( int i = a ) ; i < c ; )
        {
            if ( i == b )
            {
                return 1 ;
            }
        }
    }*/

    /*int func ( ) {
        int a = 0;
        int c = 0;
        for ) int i = a ; i < c ; )
        {
            if ( i == b )
            {
                return 1 ;
            }
        }
    }*/

}



/*symbolsInLine.add("id");
        symbolsInLine.add("+");
        symbolsInLine.add("id");
        symbolsInLine.add("*");
        symbolsInLine.add("id");*/

/*symbolsInLine.add("id");
        symbolsInLine.add("*");
        symbolsInLine.add("(");
        symbolsInLine.add("id");
        symbolsInLine.add("+");
        symbolsInLine.add("id");
        symbolsInLine.add(")");*/