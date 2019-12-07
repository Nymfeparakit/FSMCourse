public class Main {

    public static void main(String[] args) {

        Tokenizer tokenizer = new Tokenizer();
        tokenizer.openFileToRead("Code2.txt");
        Interpreter interpreter = new Interpreter(tokenizer);
        int result = interpreter.expr();
        System.out.println(result);

    }

}
