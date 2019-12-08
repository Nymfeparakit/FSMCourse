
public class LogicOpNode extends ASTNode {

    ASTNode left;
    ASTNode right;
    Token token;

    public LogicOpNode(ASTNode left, ASTNode right, Token token) {
        this.left = left;
        this.right = right;
        this.token = token;
    }
}
