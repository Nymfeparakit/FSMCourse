public class Main {

    public static void main(String[] args) {

        Tokenizer tokenizer = new Tokenizer();
        tokenizer.openFileToRead("test.txt");
        Parser parser = new Parser(tokenizer);
        ASTNode root = parser.parse();
        int a = 0;
        //Interpreter interpreter = new Interpreter(parser);
        //int result = interpreter.interpret();
        //System.out.println(result);

    }

}
