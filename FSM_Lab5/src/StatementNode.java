public class StatementNode extends ASTNode {

    ASTNode left;
    ASTNode right;

    public StatementNode(ASTNode left, ASTNode right) {
        this.left = left;
        this.right = right;
    }
}
