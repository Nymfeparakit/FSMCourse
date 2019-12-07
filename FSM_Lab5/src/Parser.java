public class Parser {

    Tokenizer tokenizer;
    Token currToken;

    public Parser(Tokenizer tokenizer) {
        this.tokenizer = tokenizer;
        currToken = tokenizer.getNextToken();
    }

    private void eatToken(Token.TokenType expType) {
        if (currToken.type == expType)
            currToken = tokenizer.getNextToken();
        else  //если не получили ожидаемый тип токена
            error();
    }

    private ASTNode factor() {

        Token token = currToken;
        //int result = 0;
        ASTNode node = null;//узел, кот мы впоследствии вернем
        //<factor>: <identifier> | <number> | (<expression>)
        if (currToken.type == Token.TokenType.NUMBER) { //если это просто число, то возвр его
            eatToken(Token.TokenType.NUMBER);
            node = new NumNode(token);
        } else if (currToken.type == Token.TokenType.LEFT_PAR) { //если это левая скобка
            eatToken(Token.TokenType.LEFT_PAR);
            node = expr(); //вычисляем выражение в скобках рекурсивно
            eatToken(Token.TokenType.RIGHT_PAR); //съедаем правую скобку
        } else if (currToken.type == Token.TokenType.ID) {
            return identifier();
        }

        return node;
    }

    private ASTNode term() {

        ASTNode node = factor();// получаем первый фактор

        while (currToken.type == Token.TokenType.MUL || currToken.type == Token.TokenType.DIV) {
            Token tmp = currToken;
            if (tmp.type == Token.TokenType.MUL) {
                eatToken(Token.TokenType.MUL);
            } else if (tmp.type == Token.TokenType.DIV) {
                eatToken(Token.TokenType.DIV);
            }
            ASTNode right = factor();
            node = new OpNode(node, right, tmp);
        }

        return node;

    }

    public ASTNode expr() {

        ASTNode node = term();

        while (currToken.type == Token.TokenType.PLUS || currToken.type == Token.TokenType.MINUS) {
            Token tmp = currToken;
            if (tmp.type == Token.TokenType.PLUS) {
                eatToken(Token.TokenType.PLUS);
            } else if (tmp.type == Token.TokenType.MINUS) {
                eatToken(Token.TokenType.MINUS);
            }
            ASTNode right = term();
            node = new OpNode(node, right, tmp);
        }

        return node;

    }

    private ASTNode statement() {

        ASTNode leftNode = null;
        ASTNode rightNode = null;

        if (currToken.type == Token.TokenType.ID) {
            leftNode = assign();
        } else {
            return null;
        }
        rightNode = statement();

        return new StatementNode(leftNode, rightNode);

    }

    private ASTNode assign() {

        //<assign>: <identifier> ‘=’ <expression>
        ASTNode left = identifier(); //слева ожидается идентификатор
        Token token = currToken; //это должно быть "="
        eatToken(Token.TokenType.ASSIGN);
        ASTNode right = expr(); //справа ожидается выражение
        return new AssignNode(left, right, token);

    }

    private ASTNode identifier() {
        ASTNode node = new IDNode(currToken);
        eatToken(Token.TokenType.ID);
        return node;
    }

    private ASTNode program() {
        //<program>: <statement>
        return statement();
    }

    public ASTNode parse() { return program(); }

    private void error() {

    }



}
