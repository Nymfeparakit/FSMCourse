import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

import java.util.ArrayList;

public class Main extends Application {

    public static void main(String[] args) {

        Parser parser = new Parser();
        parser.fillGrammarRules();
        parser.fillFirstSets();
        parser.fillFollowSets();
        parser.fillPredictTable();
        parser.printFirstSets();
        System.out.println("\n");
        parser.printFollowSets();
        parser.printPredictTable();

        ArrayList<String> symbolsInLine = new ArrayList<>();
        symbolsInLine.add(")");
        symbolsInLine.add("id");
        symbolsInLine.add("*");
        symbolsInLine.add("+");
        symbolsInLine.add("id");
        ArrayList<Symbol> line = new ArrayList<>();
        for (String str : symbolsInLine) {
            Symbol s = new Symbol(str, true);
            line.add(s);
        }

        launch(args);
    }

    public void start(Stage primaryStage) {

        primaryStage.setTitle("JavaFX WebView Example");

        WebView webView = new WebView();

        String tableHtml = "Hi";

        webView.getEngine().loadContent(tableHtml, "text/html");

        VBox vBox = new VBox(webView);
        Scene scene = new Scene(vBox, 960, 600);

        primaryStage.setScene(scene);
        primaryStage.show();

    }

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