import java.util.ArrayList;

public class Main {

    public static void main(String[] args) {
        Parser parser = new Parser();
        parser.fillGrammarRules();
        parser.fillFirstSets();
        parser.fillFollowSets();
        parser.fillPredictTable();
        parser.printFirstSets();
        parser.printFollowSets();
        parser.printPredictTable2();

        ArrayList<String> symbolsInLine = new ArrayList<>();
        //symbolsInLine.add("-");
        //symbolsInLine.add("(");
        symbolsInLine.add("id");
        symbolsInLine.add("*");
        symbolsInLine.add("(");
        symbolsInLine.add("id");
        symbolsInLine.add("+");
        symbolsInLine.add("id");
        symbolsInLine.add(")");
        ArrayList<Symbol> line = new ArrayList<>();
        for (String str : symbolsInLine) {
            Symbol s = new Symbol(str, true);
            line.add(s);
        }
        parser.parse(line);
    }

}

/*symbolsInLine.add("id");
        symbolsInLine.add("+");
        symbolsInLine.add("id");
        symbolsInLine.add("*");
        symbolsInLine.add("id");*/