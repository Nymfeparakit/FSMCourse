public class Interpreter {

    Tokenizer tokenizer;
    Token currToken;

    public Interpreter(Tokenizer tokenizer) {
        this.tokenizer = tokenizer;
        currToken = tokenizer.getNextToken();
    }

    private void eatToken(Token.TokenType expType) {
        if (currToken.type == expType)
            currToken = tokenizer.getNextToken();
        else  //если не получили ожидаемый тип токена
            error();
    }

    private int factor() {
        Token token = currToken;
        eatToken(Token.TokenType.NUMBER);
        return Integer.parseInt(token.value);
    }

    private int term() {

        int result = factor();// получаем первый фактор

        while (currToken.type == Token.TokenType.MUL || currToken.type == Token.TokenType.DIV) {
            Token tmp = currToken;
            if (tmp.type == Token.TokenType.MUL) {
                eatToken(Token.TokenType.MUL);
                result = result * factor();
            } else if (tmp.type == Token.TokenType.DIV) {
                eatToken(Token.TokenType.DIV);
                result = result / factor();
            }
        }

        return result;

    }

    public int expr() {

        int result = term();

        while (currToken.type == Token.TokenType.PLUS || currToken.type == Token.TokenType.MINUS) {
            Token tmp = currToken;
            if (tmp.type == Token.TokenType.PLUS) {
                eatToken(Token.TokenType.PLUS);
                result = result + term();
            } else if (tmp.type == Token.TokenType.MINUS) {
                eatToken(Token.TokenType.MINUS);
                result = result - term();
            }
        }

        return result;

    }

    private void error() {

    }



}
