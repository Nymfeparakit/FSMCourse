import java.util.ArrayList;

public class Rule {

    ArrayList<Symbol> symbols;

    Rule(ArrayList<Symbol> symbols) {
        this.symbols = symbols;
    }

    @Override public String toString() {
        String str = "";
        for (Symbol smbl : symbols) {
            if (smbl == null) {
                str += "eps ";
                continue;
            }
            str += smbl.toString() + " ";
        }
        return str;
    }

}
