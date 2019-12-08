
public class StringNode extends ASTNode {

    Token token;
    String value;

    public StringNode(Token token) {
        this.token = token;
        this.value = token.value;
    }
}
