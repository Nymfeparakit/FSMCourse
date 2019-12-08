public class ForNode extends ASTNode{

    ASTNode identifier;
    ASTNode exprFrom;
    ASTNode exprTo;

    public ForNode(ASTNode identifier, ASTNode exprFrom, ASTNode exprTo) {
        this.identifier = identifier;
        this.exprFrom = exprFrom;
        this.exprTo = exprTo;
    }
}
