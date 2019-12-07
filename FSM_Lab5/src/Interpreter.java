

public class Interpreter {

    Parser parser;



    public Interpreter(Parser parser) {
        this.parser = parser;
    }

    //здесь описывается алгоритм обхода дерева
    public int visit(ASTNode node) {

        if (node instanceof OpNode) {
            return visitOp((OpNode)node); //двигаемся дальше вниз по дереву
        } else if (node instanceof NumNode) {
            return ((NumNode) node).value; //возвращаем значение листа дерева
        }

        return 0;

    }

    private int visitOp(OpNode node) {

        if (node.token.type == Token.TokenType.PLUS) {
            return visit(node.left) + visit(node.right);
        } else if (node.token.type == Token.TokenType.MINUS) {
            return visit(node.left) - visit(node.right);
        } else if (node.token.type == Token.TokenType.MUL) {
            return visit(node.left) * visit(node.right);
        } else if (node.token.type == Token.TokenType.DIV) {
            return visit(node.left) / visit(node.right);
        }

        return 0;

    }

    public int interpret() {
        ASTNode root = parser.parse();
        return visit(root);
    }

}
