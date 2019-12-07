
public class OpNode extends ASTNode {

    ASTNode left;
    ASTNode right;
    Token token;

    public OpNode(ASTNode left, ASTNode right, Token token) {
        this.left = left;
        this.right = right;
        this.token = token;
    }

}
