import java.util.ArrayList;

public class StatementNode extends ASTNode {

    /*ASTNode left;
    ASTNode right;*/
    ArrayList<ASTNode> children;

    /*public StatementNode(ASTNode left, ASTNode right) {
        this.left = left;
        this.right = right;
    }*/

    public StatementNode(ArrayList<ASTNode> children) {
        this.children = children;
    }
}
