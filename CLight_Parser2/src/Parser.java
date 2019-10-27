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
    private HashMap<HashMap<Symbol, Symbol>, ArrayList<Symbol>> predictTable;

    public void fillGrammarRules() {

        grammarRules = new HashMap<>();
        grammarSymbols = new HashSet<>();
        try {

            FileInputStream fstream = new FileInputStream("test_grammar_2.txt");
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
                    tmpFirstSet.add(smbl);
                    //continue;
                    //tmpFirstSet.clear();
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

    private HashSet<Symbol> findFirstSetForList(ArrayList<Symbol> list) {

        HashSet<Symbol> tmpFirstSet = new HashSet<>();
        for (int i = 0; i < list.size(); i++) {
            //tmpFirstSet = new HashSet<>();
            Symbol smbl = list.get(i);
            if (smbl == null || (smbl.isTerminal && i == 0)) {
                tmpFirstSet.add(smbl);
                break;
            } else if (!smbl.isTerminal) {
                HashSet<Symbol> smblFirstSet;
                if (firstSets.containsKey(smbl)) {
                    smblFirstSet = firstSets.get(smbl);
                } else {
                    smblFirstSet = findFirst(smbl);
                }
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
                tmpFirstSet.add(smbl);
                break;//прерываем цикл, чтобы перейти к след правилу
            }
        }
        return tmpFirstSet;
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
        int a = 0;
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
                indexOfSymbol = rule.indexOf(symbol);//если правиле нет этого символа
                while (indexOfSymbol != -1) {
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
                        break; //переходим к следующему правилу
                    }
                    //смотрим все символы следующие за ним
                    boolean continuesNull = false;//содержит ли first строки за ним null
                    for (int i = indexOfSymbol + 1; i < rule.size(); i++) {
                        Symbol nextSymbol = rule.get(i);
                        boolean setContinuesNull = false;
                        if (nextSymbol.isTerminal) {
                            followSet.add(nextSymbol);
                            break;
                        } else {
                            HashSet<Symbol> firstSet = firstSets.get(nextSymbol);//получаем firstSet для этого символа
                            for (Symbol smb : firstSet) {
                                if (smb == null) {
                                    setContinuesNull = true;
                                    continue;
                                }
                                followSet.add(smb);
                            }
                            //если дошли до последнего нетерминала и он содержит epsilon
                            if (i == rule.size() - 1 && setContinuesNull) {
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
                        if (!setContinuesNull) break;//далее не идем
                    }
                    /*if (indexOfSymbol + 1 == rule.size() - 1 //если следующий символ последний
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
                    }*/
                    indexOfSymbol = rule.subList(indexOfSymbol + 1, rule.size()).indexOf(symbol);
                }

            }
        }
        if (symbol.value.equals('T')) {
            int a = 0;
        }
        return followSet;

    }

    public void fillPredictTable() {

        predictTable = new HashMap<>();
        //проходимся по всем продукциям
        for (Map.Entry<Symbol, HashSet<ArrayList<Symbol>>> entry : grammarRules.entrySet()) {
            HashSet<ArrayList<Symbol>> setOfRules = entry.getValue();
            Symbol nonTerminal = entry.getKey(); //символ слева
            HashSet<Symbol> followSet = followSets.get(nonTerminal); //узнаем follow для левого символа
            for (ArrayList<Symbol> rule : setOfRules) { //проходимся по всем правилам для данного нетерминала
                HashSet<Symbol> firstSet = findFirstSetForList(rule);//получаем first для правой части
                Iterator<Symbol> it = firstSet.iterator();
                while (it.hasNext()) { //проходимся по каждому терминалу
                    Symbol terminal = it.next();
                    if (terminal == null) { //если это epsilon
                        Iterator<Symbol> it2 = followSet.iterator();
                        while (it.hasNext()) { //проходимся по каждому терминалу из follow
                            Symbol terminal2 = it2.next();
                            if (terminal2 == null) {
                                HashMap<Symbol, Symbol> symbolsPair = new HashMap<>();
                                symbolsPair.put(nonTerminal, null);
                                predictTable.put(symbolsPair, rule);
                            }
                            HashMap<Symbol, Symbol> symbolsPair = new HashMap<>();
                            symbolsPair.put(nonTerminal, terminal2);
                            predictTable.put(symbolsPair, rule);
                        }
                    }
                    HashMap<Symbol, Symbol> symbolsPair = new HashMap<>();
                    symbolsPair.put(nonTerminal, terminal);
                    predictTable.put(symbolsPair, rule);
                }
            }
        }

    }

}
