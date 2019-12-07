public class Token {

    TokenType type;
    String value;

    public Token(TokenType type, String value) {
        this.type = type;
        this.value = value;
    }

    public enum TokenType {

        KEYWORD("\\b(for|if|int|bool|void|return|main|and|or)\\b"),
        REL_OP("(<|>|==|!=)"),
        ASSIGN("="),//единтсвенная операция в языке - присваивание
        ID("[A-Za-z]\\w*"),//имена переменных
        NUMBER("(0|[1-9]\\d*)"),//допускаются только целые числа
        //MARK("(\\(|\\)|\\{|\\}|;)");
        MARK("(\\(|\\)|\\{|\\}|;|&&|\\|\\|)"),
        MUL("*"),
        DIV("/"),
        PLUS("+"),
        MINUS("-"),
        EOF("$");

        private String reg;

        TokenType(String reg) {
            this.reg = reg;
        }

        public String getReg() {
            return reg;
        }

    }

}


