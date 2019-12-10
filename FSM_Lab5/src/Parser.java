import java.util.ArrayList;

public class Parser {

    Tokenizer tokenizer;
    Token currToken;
    boolean parsingSuccess;

    private class ParseError extends RuntimeException {}

    public Parser(Tokenizer tokenizer) {
        this.tokenizer = tokenizer;
        currToken = tokenizer.getNextToken();
        parsingSuccess = true;
    }

    private ParseError error(Token.TokenType expType, Token.TokenType actualType) {
        System.out.println("Error at line " + (tokenizer.strNum + 1) +
                ", expected \"" + expType.getReg() +
                "\", but was \"" + actualType.getReg() + "\"");
        parsingSuccess = false;
        return new ParseError();
    }

    private ParseError error(String msg) {
        System.out.println(msg);
        parsingSuccess = false;
        return new ParseError();
    }

    private void eatToken(Token.TokenType expType) {
        if (currToken == null) throw new ParseError();
        if (currToken.type == expType) {
            currToken = tokenizer.getNextToken();
            if (currToken == null) throw new ParseError();
        }
        else  //если не получили ожидаемый тип токена
            throw error(expType, currToken.type);
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
            node = new ExprNode(node, right, tmp);
        }

        return node;

    }

    private ASTNode statement() {

        ASTNode currNode = null;
        ArrayList<ASTNode> stmntChildren = new ArrayList<>();
        do {
            try {
                currNode = null;
                if (currToken == null) throw new ParseError();
                if (currToken.type == Token.TokenType.ID) {
                    currNode = assign();
                    stmntChildren.add(currNode);
                } else if (currToken.type == Token.TokenType.IF) { //  | <if>  ‘{ <statement> ‘}’ <else>
                    currNode = ifStatement();
                    eatToken(Token.TokenType.LEFT_BRACE);
                    ASTNode thenNode = statement();
                    eatToken(Token.TokenType.RIGHT_BRACE);
                    ASTNode elseNode = elseStatement();
                    stmntChildren.add(currNode);
                    stmntChildren.add(thenNode);
                    stmntChildren.add(elseNode); //else node всегда есть, но он может быть null
                } else if (currToken.type == Token.TokenType.FOR) {
                    currNode = forStatement();
                    eatToken(Token.TokenType.LEFT_BRACE);
                    ASTNode body = statement();
                    eatToken(Token.TokenType.RIGHT_BRACE);
                    stmntChildren.add(currNode);
                    stmntChildren.add(body);
                } else if (currToken.type == Token.TokenType.PRINT) {
                    currNode = printStatement();
                    stmntChildren.add(currNode);
                } else if (currToken.type == Token.TokenType.SCAN) {
                    currNode = scanStatement();
                    stmntChildren.add(currNode);
                } else if (currToken.type == Token.TokenType.EOF) {
                    break;
                } else if (currToken.type != Token.TokenType.RIGHT_BRACE){
                    throw error("Error at line " + tokenizer.strNum +
                            ", expected beginning of statement, but was \"" + currToken.type.getReg() + "\"");
                }
            } catch (ParseError e) {
                synchronize();
                currNode = new ASTNode();
                parsingSuccess = false;
                //continue; //пытаемся пе
            }

            } while (currNode != null);
        //} while (currToken.type != Token.TokenType.EOF);

        return new StatementNode(stmntChildren);

    }

    private ASTNode scanStatement() {

       // try {
            //<scan>: ‘scan’ <identifier> ‘;’
            eatToken(Token.TokenType.SCAN);
            ASTNode idNode = identifier();
            eatToken(Token.TokenType.SEMICOLON);
            return new ScanNode(idNode);
       /* } catch (ParseError e) {
            synchronize();
            return null;
        }*/


    }

    private ASTNode printStatement() {

        //try {
            eatToken(Token.TokenType.PRINT);
            ArrayList<ASTNode> childrenToPrint = new ArrayList<>();
            boolean firsIter = true;
            do {
                if (!firsIter) eatToken(Token.TokenType.COMMA); //аргументы print отделяются запятой
                ASTNode child = null;
                Token token = currToken;
                //это должна быть строка или выражение
                if (currToken.type == Token.TokenType.STRING) {
                    eatToken(Token.TokenType.STRING);
                    child = new StringNode(token);
                } else {
                    child = expr();
                }
                childrenToPrint.add(child);
                firsIter = false;
            } while (currToken.type != Token.TokenType.SEMICOLON && currToken.type != Token.TokenType.EOF);
            eatToken(Token.TokenType.SEMICOLON);

            return new PrintNode(childrenToPrint);
       /* } catch (ParseError e) {
            synchronize();
            return null;
        } */


    }

    private ASTNode forStatement() {

        //try {
            //<for>: ‘for’ <identifier> ‘=’ <expression> ‘to’ <expression>
            eatToken(Token.TokenType.FOR);
            ASTNode idNode = identifier();
            eatToken(Token.TokenType.ASSIGN);
            ASTNode exprFrom = expr();
            eatToken(Token.TokenType.TO);
            ASTNode exprTo = expr();
            return new ForNode(idNode, exprFrom, exprTo);
       /* } catch (ParseError e) {
            synchronize();
            return null;
        } */


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
            node = new ExprNode(node, right, tmp);
        }

        return node;

    }

    private ASTNode ifStatement() {

      //  try {
            //<if>: ‘if’ <bool_expression>
            eatToken(Token.TokenType.IF);
            //ASTNode boolExprNode = boolOp();
            ASTNode boolExprNode = boolExpr();
            return new IfNode(boolExprNode);
      /*  } catch (ParseError e) {
            synchronize();
            return null;
        } */


    }

    private ASTNode boolExpr() {

        ASTNode leftNode = boolTerm();
        ASTNode exprNode = null;
        while (currToken.type == Token.TokenType.OR) {
            Token tmp = currToken;
            eatToken(Token.TokenType.OR);
            ASTNode right = boolTerm();
            exprNode = new LogicExprNode(leftNode, right, tmp);
            return exprNode;
        }
        return leftNode;

    }

    private ASTNode boolTerm() {

        ASTNode leftNode = boolOp();
        ASTNode exprNode = null;
        while (currToken.type == Token.TokenType.AND) {
            Token tmp = currToken;
            eatToken(Token.TokenType.AND);
            ASTNode right = boolOp();
            exprNode = new LogicExprNode(leftNode, right, tmp);
            return exprNode;
        }
        return leftNode;

    }

    private ASTNode boolOp() {

        ASTNode leftNode = expr();
        Token tmp = currToken;
        if (tmp.type == Token.TokenType.GREATER) {
            eatToken(Token.TokenType.GREATER);
        } else if (tmp.type == Token.TokenType.LESS) {
            eatToken(Token.TokenType.LESS);
        } else if (tmp.type == Token.TokenType.EQUAL) {
            eatToken(Token.TokenType.EQUAL);
        } else if (tmp.type == Token.TokenType.NOT_EQUAL) {
            eatToken(Token.TokenType.NOT_EQUAL);
        }
        ASTNode rightNode = expr();

        return new LogicOpNode(leftNode, rightNode, tmp);

    }

    private ASTNode elseStatement() {

   //     try {
            //<else>:
            //| ‘else’ ‘{‘ <statement> ‘}’
            if (currToken.type == Token.TokenType.ELSE) {
                eatToken(Token.TokenType.ELSE);
                eatToken(Token.TokenType.LEFT_BRACE);
                ASTNode statementNode = statement();
                eatToken(Token.TokenType.RIGHT_BRACE);
                return new ElseNode(statementNode);
            } else {
                return null;
            }
     /*   } catch (ParseError e) {
            synchronize();
            return null;
        }
        */

    }

    private ASTNode assign() {

     //   try {
            //<assign>: <identifier> ‘=’ <expression>
            ASTNode left = identifier(); //слева ожидается идентификатор
            Token token = currToken; //это должно быть "="
            eatToken(Token.TokenType.ASSIGN);
            ASTNode right = expr(); //справа ожидается выражение
            return new AssignNode(left, right, token);

      /*  } catch (ParseError e) {
            synchronize();
            return null;
        } */

    }

    private void synchronize() {

        currToken = tokenizer.getNextToken();

        while(!isAtEnd()) {

            if (currToken.type == Token.TokenType.SEMICOLON) {
                eatToken(Token.TokenType.SEMICOLON);
                return;
            }

            switch (currToken.type) {
                case PRINT:
                case SCAN:
                case FOR:
                case IF:
                    return;
            }

           currToken = tokenizer.getNextToken();

        }

    }

    private boolean isAtEnd() {
        return currToken.type == Token.TokenType.EOF;
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

    public ASTNode parse() {
        ASTNode prog = program();
        if (parsingSuccess)
            return prog;
        else
            return null;
    }

    private void error() {

    }



}
