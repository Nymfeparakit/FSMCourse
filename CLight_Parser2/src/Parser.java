import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

public class Parser {

    private HashMap <Symbol, HashSet<ArrayList<Symbol>>> grammarRules;
    private HashSet<Symbol> grammarSymbols;
    private HashMap <Symbol, HashSet<Symbol>> firstSets;
    private HashMap <Symbol, HashSet<Symbol>> followSets;
    private Symbol startSymbol;

    public void fillGrammarRules() {

        grammarRules = new HashMap<>();
        grammarSymbols = new HashSet<>();
        try {

            FileInputStream fstream = new FileInputStream("test_grammar.txt");
            BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
            String line;

            boolean isNewRule = true;
            HashSet<ArrayList<Symbol>> setOfRules = null;
            Symbol leftSymbol = null;
            boolean firstLine = true;
            while ((line = br.readLine()) != null) {

                isNewRule = line.startsWith("<");
                String rightPart = "";
                if (isNewRule) { //если анализируется новое правило
                    if (setOfRules != null) {
                        grammarRules.put(leftSymbol, setOfRules);
                        grammarSymbols.add(leftSymbol);
                    }
                    setOfRules = new HashSet<>();
                    int posOfSeparator = line.indexOf(':');//Ищем разделитель левой и правой частей
                    //получаем левую и правую часть
                    String leftPart = line.substring(0, posOfSeparator);
                    rightPart = line.substring(posOfSeparator + 1);

                    //слева - всегда нетерминал
                    leftSymbol = new Symbol(leftPart.substring(1, leftPart.length() - 1), false);

                    if (firstLine) { //определяем стартовый символ
                        startSymbol = leftSymbol;
                        firstLine = false;
                    }

                }  else {
                    rightPart = line;
                }
                rightPart = rightPart.replaceAll(" ", "");//убираем все пробелы
                    //Теперь обрабатываем правую часть
                    ArrayList<Symbol> rule = null;
                    if (rightPart.isEmpty()) {
                        rule = new ArrayList<>();
                        rule.add(null);//обозначаем null за пустой символ
                        setOfRules.add(rule);
                        continue;//идем к следующей строке
                    }
                    String[] rulesStr = rightPart.split("\\|");
                    for (int i = 0; i < rulesStr.length; ++i) {
                        if (rulesStr[i].isEmpty())
                            continue;
                        rule = new ArrayList<>();
                        String currRule = rulesStr[i];//постепенно собираем правила
                        do {
                            char firstCh = currRule.charAt(0);
                            int indexOfLastCh = -1;
                            boolean isTerminal = false;
                            if (firstCh == '<') {
                                indexOfLastCh = currRule.indexOf('>');
                            } else if (firstCh == '"') {
                                indexOfLastCh = currRule.indexOf('"', 1);
                                isTerminal = true;
                            }
                            Symbol smbl = new Symbol(currRule.substring(1, indexOfLastCh), isTerminal);
                            grammarSymbols.add(smbl);
                            rule.add(smbl);
                            currRule = currRule.substring(indexOfLastCh + 1);
                        } while (!currRule.isEmpty());
                        setOfRules.add(rule);
                    }

            }
            grammarRules.put(leftSymbol, setOfRules);
            grammarSymbols.add(leftSymbol);

            fstream.close();

        } catch (IOException e) {

        }
    }

    public void fillFirstSets() {

        firstSets = new HashMap<>();
        //проходимся по всем символам
        Iterator<Symbol> i = grammarSymbols.iterator();
        while(i.hasNext()) {
            Symbol smbl = i.next();
            if (smbl.isTerminal) {
                HashSet<Symbol> currSymbolSet = new HashSet<>();
                currSymbolSet.add(smbl);
                firstSets.put(smbl, currSymbolSet);
            } else { //если это нетерминал
                HashSet<Symbol> currSymbolSet = findFirst(smbl);
                firstSets.put(smbl, currSymbolSet);
            }
        }
        int a = 0;

    }

    private HashSet<Symbol> findFirst(Symbol symbol) {

        HashSet<Symbol> firstSet = new HashSet<>();
        HashSet<ArrayList<Symbol>> rules = grammarRules.get(symbol);
        //Проходимся по всем правилам
        for (ArrayList<Symbol> rule : rules) {
            int i = 0;
            HashSet<Symbol> tmpFirstSet = new HashSet<>();
            for (i = 0; i < rule.size(); i++) {
                //tmpFirstSet = new HashSet<>();
                Symbol smbl = rule.get(i);
                if (smbl == null || (smbl.isTerminal && i == 0)) {
                    tmpFirstSet.add(smbl);
                    break;
                } else if (!smbl.isTerminal) {
                    HashSet<Symbol> smblFirstSet = findFirst(smbl);
                    boolean containsNull = false;
                    Iterator<Symbol> it = smblFirstSet.iterator();
                    while (it.hasNext()) {
                        Symbol s = it.next();
                        if (s == null) {
                            containsNull = true;
                            continue;
                        }
                        tmpFirstSet.add(s);
                    }
                    if (!containsNull)
                        break;
                } else { //если это не первый терминальный символ
                    tmpFirstSet.clear();
                    break;//прерываем цикл, чтобы перейти к след правилу
                }
            }
            //после того, как прошлись по всему правилу
            firstSet.addAll(tmpFirstSet);
            //если при этом прошли все нетерминальные символы
            if (i == rule.size() && !rule.get(rule.size() - 1).isTerminal) {
                firstSet.add(null);
            }
        }

        return firstSet;
    }

    public void fillFollowSets() {

        followSets = new HashMap<>();
        //проходимся по всем символам
        Iterator<Symbol> i = grammarSymbols.iterator();
        while (i.hasNext()) {
            Symbol smbl = i.next();
            if (smbl.isTerminal) { //пропускаем все терминалы
                continue;
            }
            HashSet<Symbol> currSymbolSet = findFollow(smbl);
            followSets.put(smbl, currSymbolSet);
        }

    }

    private HashSet<Symbol> findFollow(Symbol symbol) {

        HashSet<Symbol> followSet = new HashSet<>();
        if (symbol.equals(startSymbol)) followSet.add(null);
        //Ищем в каждой продукции данный символ
        for (Map.Entry<Symbol, HashSet<ArrayList<Symbol>>> entry : grammarRules.entrySet()) {
            HashSet<ArrayList<Symbol>> setOfRules = entry.getValue();
            Symbol leftSmbl = entry.getKey();
            for (ArrayList<Symbol> rule : setOfRules) {
                boolean leftSymbolFollowSetWasAdded = false;
                int indexOfSymbol = 0;
                if ((indexOfSymbol = rule.indexOf(symbol)) == -1) //если правиле нет этого символа
                    continue;
                //смотрим на следующий символ
                if (indexOfSymbol == rule.size() - 1 && !leftSmbl.equals(symbol)) { //если символ стоит последним
                    HashSet<Symbol> leftSmblFollowSet;
                    if (followSets.containsKey(leftSmbl)) { //если follow set для этого символа уже вычислялся
                        leftSmblFollowSet = followSets.get(leftSmbl);
                    } else { //иначе вычисляем его
                        leftSmblFollowSet = findFollow(leftSmbl);
                        followSets.put(leftSmbl, leftSmblFollowSet);//добавляем сразу в список всех follow
                        //чтобы затем не вычислять еще раз
                    }
                    followSet.addAll(leftSmblFollowSet);
                    leftSymbolFollowSetWasAdded = true;
                    continue; //переходим к следующему правилу
                }
                //иначе символ должен быть предпоследним
                if (indexOfSymbol != rule.size() - 2) continue;
                Symbol nextSymbol = rule.get(indexOfSymbol + 1);
                if (nextSymbol.isTerminal) {
                    followSet.add(nextSymbol);
                } else {
                    HashSet<Symbol> firstSet = firstSets.get(nextSymbol);//получаем firstSet для этого символа
                    for (Symbol smb : firstSet) {
                        if (smb == null)
                            continue;
                        followSet.add(smb);
                    }
                }
                if (indexOfSymbol + 1 == rule.size() - 1 //если следующий символ последний
                        && firstSets.get(nextSymbol).contains(null)
                        && !leftSymbolFollowSetWasAdded
                        && !leftSmbl.equals(symbol)) { //и он содержит null
                    HashSet<Symbol> leftSmblFollowSet;
                    if (followSets.containsKey(leftSmbl)) { //если follow set для этого символа уже вычислялся
                        leftSmblFollowSet = followSets.get(leftSmbl);
                    } else { //иначе вычисляем его
                        leftSmblFollowSet = findFollow(leftSmbl);
                        followSets.put(leftSmbl, leftSmblFollowSet);
                    }
                    followSet.addAll(leftSmblFollowSet);
                }
            }
        }
        if (symbol.value.equals('T')) {
            int a = 0;
        }
        return followSet;

    }

}
