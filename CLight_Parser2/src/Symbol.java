public class Symbol {

    String value;
    boolean isTerminal;

    public Symbol(String value, boolean isTerminal) {
        this.value = value;
        this.isTerminal = isTerminal;
    }

    @Override
    public boolean equals(Object o) {
        // self check
        if (this == o)
            return true;
        // null check
        if (o == null)
            return false;
        // type check and cast
        if (getClass() != o.getClass())
            return false;
        Symbol symbol = (Symbol) o;
        // field comparison
        return (this.value.equals(symbol.value) && this.isTerminal == symbol.isTerminal);
    }

    @Override public int hashCode() {
        //simple one-line implementation
        int v2 = 0;
        if (isTerminal) v2 = 1;
        return value.hashCode() + v2;
    }

}
