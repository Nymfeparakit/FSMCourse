public class IfNode extends ASTNode{

    ASTNode boolExprNode;

    public IfNode(ASTNode boolExprNode) {
        this.boolExprNode = boolExprNode;
    }
}
