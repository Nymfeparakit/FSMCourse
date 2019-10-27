public class Main {

    public static void main(String[] args) {
        Parser parser = new Parser();
        parser.fillGrammarRules();
        parser.fillFirstSets();
        parser.fillFollowSets();
    }

}
