
public class ExprNode extends ASTNode {

    ASTNode left;
    ASTNode right;
    Token token;

    public ExprNode(ASTNode left, ASTNode right, Token token) {
        this.left = left;
        this.right = right;
        this.token = token;
    }

}
