public class LogicExprNode extends ASTNode {

    ASTNode left;
    ASTNode right;
    Token token;

    public LogicExprNode(ASTNode left, ASTNode right, Token token) {
        this.left = left;
        this.right = right;
        this.token = token;
    }

}
