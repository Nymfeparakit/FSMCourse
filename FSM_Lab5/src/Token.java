public class Token {

    TokenType type;
    String value;

    public Token(TokenType type, String value) {
        this.type = type;
        this.value = value;
    }

    public String toString() {
        return value;
    }

    public enum TokenType {

        KEYWORD("\\b(for|if|int|bool|void|return|main|and|or)\\b"),
        ASSIGN("="),//единтсвенная операция в языке - присваивание
        ID("identifier"),//имена переменных
        NUMBER("number"),//допускаются только целые числа
        //MARK("(\\(|\\)|\\{|\\}|;)");
        MARK("(\\(|\\)|\\{|\\}|;|&&|\\|\\|)"),
        MUL("*"),
        DIV("/"),
        PLUS("+"),
        MINUS("-"),
        EOF("EOF"),
        LEFT_PAR("("),
        RIGHT_PAR(")"),
        IF("if"),
        ELSE("else"),
        GREATER(">"),
        GREATER_EQ(">="),
        LESS("<"),
        LESS_EQ("<="),
        EQUAL("=="),
        NOT_EQUAL("!="),
        LEFT_BRACE("{"),
        RIGHT_BRACE("}"),
        FOR("for"),
        TO("to"),
        STRING(""),
        PRINT("print"),
        SEMICOLON(";"),
        COMMA(","),
        SCAN("scan");

        private String reg;

        TokenType(String reg) {
            this.reg = reg;
        }

        public String getReg() {
            return reg;
        }

    }

}


