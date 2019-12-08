public class IDNode extends ASTNode {

    Token token;
    String value; //имя идентификатора

    public IDNode(Token token) {
        this.token = token;
        this.value = token.value;
    }
}
