import java.util.ArrayList;

public class PrintNode extends ASTNode{

    public ArrayList<ASTNode> nodesToPrint;

    public PrintNode(ArrayList<ASTNode> nodesToPrint) {
        this.nodesToPrint = nodesToPrint;
    }
}
