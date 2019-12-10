public class Main {

    //https://craftinginterpreters.com/

    public static void main(String[] args) {

        Tokenizer tokenizer = new Tokenizer();
        tokenizer.openFileToRead("test3.txt");
        Parser parser = new Parser(tokenizer);
        //ASTNode root = parser.parse();
        int a = 0;
        Interpreter interpreter = new Interpreter(parser);
        interpreter.interpret();
        //interpreter.printGlobalScope();
    }

}
