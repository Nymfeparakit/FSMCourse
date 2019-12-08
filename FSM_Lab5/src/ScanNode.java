public class ScanNode extends ASTNode{

    ASTNode idNode; //переменная, в которую считывается значение

    public ScanNode(ASTNode idNode) {
        this.idNode = idNode;
    }
}
