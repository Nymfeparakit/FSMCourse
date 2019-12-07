
public class NumNode extends ASTNode {

    Token token;
    int value;

    public NumNode(Token token) {
        this.token = token;
        this.value = Integer.parseInt(token.value);
    }
}
