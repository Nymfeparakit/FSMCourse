public class AssignNode extends ASTNode {

    ASTNode left;
    ASTNode right;
    Token token;

    public AssignNode(ASTNode left, ASTNode right, Token token) {
        this.left = left;
        this.right = right;
        this.token = token;
    }
}
