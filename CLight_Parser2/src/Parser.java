import java.io.*;
import java.util.*;

public class Parser {

    private HashMap <Symbol, HashSet<ArrayList<Symbol>>> grammarRules;
    public HashSet<Symbol> grammarSymbols;
    public TreeSet<Symbol> terminalSymbols;
    private HashMap <Symbol, HashSet<Symbol>> firstSets;
    private HashMap <Symbol, HashSet<Symbol>> followSets;
    private Symbol startSymbol;
    private HashMap<HashMap<Symbol, Symbol>, Rule> predictTable;
    private HashMap<HashMap<Symbol, Symbol>, HashSet<Rule>> predictTable2;

    public void fillGrammarRules() {

        grammarRules = new HashMap<>();
        grammarSymbols = new HashSet<>();
        terminalSymbols = new TreeSet<>();
        try {

            FileInputStream fstream = new FileInputStream("grammar.txt");
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
                            if (isTerminal) terminalSymbols.add(smbl);
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
        int i = 0;
        for (i = 0; i < list.size(); i++) {
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
        //если при этом прошли все нетерминальные символы
        if (i == list.size() && !list.get(list.size() - 1).isTerminal) {
            tmpFirstSet.add(null);
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
                    int prevIndexOfSymbol = indexOfSymbol;
                    indexOfSymbol = rule.subList(prevIndexOfSymbol + 1, rule.size()).indexOf(symbol);
                    if (indexOfSymbol != -1) indexOfSymbol += prevIndexOfSymbol + 1;
                    //indexOfSymbol = rule.indexOf(symbol, indexOfSymbol + 1);
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
        predictTable2 = new HashMap<>();
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
                        while (it2.hasNext()) { //проходимся по каждому терминалу из follow
                            Symbol terminal2 = it2.next();
                            if (terminal2 == null) {
                                HashMap<Symbol, Symbol> symbolsPair = new HashMap<>();
                                symbolsPair.put(nonTerminal, null);
                                Rule rule2 = new Rule(rule);
                                HashSet<Rule> rules = predictTable2.get(symbolsPair);
                                if (rules == null) {
                                    rules = new HashSet<>();
                                }
                                rules.add(rule2);
                                predictTable2.put(symbolsPair, rules);
                                predictTable.put(symbolsPair, rule2);
                                //printPredictTableCell(symbolsPair, rule);
                                continue;
                            }
                            HashMap<Symbol, Symbol> symbolsPair = new HashMap<>();
                            symbolsPair.put(nonTerminal, terminal2);
                            Rule rule2 = new Rule(rule);
                            HashSet<Rule> rules = predictTable2.get(symbolsPair);
                            if (rules == null) {
                                rules = new HashSet<>();
                            }
                            rules.add(rule2);
                            predictTable2.put(symbolsPair, rules);
                            predictTable.put(symbolsPair, rule2);
                            //printPredictTableCell(symbolsPair, rule);
                        }
                        continue;
                    }
                    HashMap<Symbol, Symbol> symbolsPair = new HashMap<>();
                    symbolsPair.put(nonTerminal, terminal);
                    Rule rule2 = new Rule(rule);
                    HashSet<Rule> rules = predictTable2.get(symbolsPair);
                    if (rules == null) {
                        rules = new HashSet<>();
                    }
                    rules.add(rule2);
                    predictTable2.put(symbolsPair, rules);
                    predictTable.put(symbolsPair, rule2);
                }
            }
        }

        addSynchToPredictTable();//добавляем символы synch

    }

    private void addSynchToPredictTable() {

        //проходимся по всем нетерминалам
        for (Symbol symbol : grammarSymbols) {
            if (symbol.isTerminal) continue;
            HashSet<Symbol> followSet = followSets.get(symbol);//Берем множество follow для текущего символа
            for (Symbol smblFromFllwSet : followSet) {
                ArrayList<Symbol> rightPartOfRule = new ArrayList<>();
                Symbol synchSymbol = new Symbol("Synch");
                rightPartOfRule.add(synchSymbol);
                Rule rule = new Rule(rightPartOfRule);
                HashMap<Symbol, Symbol> index = new HashMap<>();
                index.put(symbol, smblFromFllwSet);
                HashSet<Rule> rulesFromTable = predictTable2.get(index);
                predictTable.putIfAbsent(index, rule);
                /*if (rulesFromTable == null) {
                    HashSet<Rule> rules = new HashSet<>();
                    rules.add(rule);
                    predictTable2.put(index, rules);
                }*/
            }
        }

    }

    public void printPredictTableCell(HashMap<Symbol, Symbol> indexes, ArrayList<Symbol> rule) {

        for (Map.Entry<Symbol, Symbol> entry2 : indexes.entrySet()) {
            System.out.print("[" + entry2.getKey() + ", " + entry2.getValue() + "]\t");
        }
        for (Symbol smbl : rule) {
            System.out.print(smbl);
        }
        System.out.println("");

    }

    public void printFirstSets()  {

        System.out.println("First sets: ");
        for (Map.Entry<Symbol, HashSet<Symbol>> entry : firstSets.entrySet()) {
            Symbol key = entry.getKey();
            if (key.isTerminal) continue;//не печатаем для терминальных символов
            HashSet value = entry.getValue();
            System.out.print(key + ": ");
            Iterator<Symbol> it = value.iterator();
            while (it.hasNext()) {
                Symbol smbl = it.next();
                System.out.print(smbl + " ");
            }
            System.out.print("\n");
        }

    }

    public void printFollowSets() {

        System.out.println("Follow sets: ");
        for (Map.Entry<Symbol, HashSet<Symbol>> entry : followSets.entrySet()) {
            Symbol key = entry.getKey();
            HashSet value = entry.getValue();
            System.out.print(key + ":\t");
            Iterator<Symbol> it = value.iterator();
            while (it.hasNext()) {
                Symbol smbl = it.next();
                System.out.print(smbl + " ");
            }
            System.out.print("\n");
        }

    }

    public void printPredictTable() {

        System.out.println("Predict table: ");
        //HashMap<HashMap<Symbol, Symbol>, ArrayList<Symbol>> predictTable;
        for (Map.Entry<HashMap<Symbol, Symbol>, Rule> entry : predictTable.entrySet()){
            HashMap<Symbol, Symbol> indexes = entry.getKey();
            Rule rule = entry.getValue();
            for (Map.Entry<Symbol, Symbol> entry2 : indexes.entrySet()) {
                System.out.print("[" + entry2.getKey() + ", " + entry2.getValue() + "]\t");
            }
            System.out.println(rule);
        }

    }

    public TreeMap<Symbol, Rule> getPredictTableRow(Symbol leftHeaderSymbol) {

        TreeMap<Symbol, Rule> tableRow = new TreeMap<>();
        Iterator<Symbol> it = terminalSymbols.iterator();

        HashMap<Symbol, Symbol> index = new HashMap<>();
        while(it.hasNext()) { //проходимся по всем терминалам для данного символа
            Symbol terminalSmbl = it.next();
            index.put(leftHeaderSymbol, terminalSmbl);
            Rule rule = predictTable.get(index);//получаем соответствующее правило
            tableRow.put(terminalSmbl, rule);//кладем в строку терминал и правило
        }
        index = new HashMap<>();
        index.put(leftHeaderSymbol, null);
        Rule rule = predictTable.get(index);//получаем соответствующее правило
        tableRow.put(new Symbol("$"), rule);//кладем в строку терминал и правило

        return tableRow;
    }

    public void printPredictTable2() {

        System.out.println("Predict table2: ");
        for (Map.Entry<HashMap<Symbol, Symbol>, HashSet<Rule>> entry : predictTable2.entrySet()){
            HashMap<Symbol, Symbol> indexes = entry.getKey();
            HashSet<Rule> rules = entry.getValue();
            for (Map.Entry<Symbol, Symbol> entry2 : indexes.entrySet()) {
                System.out.print("[" + entry2.getKey() + ", " + entry2.getValue() + "]\t");
            }
            Iterator<Rule> it = rules.iterator();
            while (it.hasNext()) {
                Rule rule = it.next();
                System.out.print(rule + "; ");
            }
            System.out.println("");
        }

    }

    public void parse(String fileName) {

        Tokenizer tokenizer = new Tokenizer();//tokenizer будет подавать токены для обработки
        tokenizer.openFileToRead(fileName);//читает первую строку
        StringBuilder tableStrBuilder = new StringBuilder();//для печати таблицы разбора в html
        tableStrBuilder.append("<table border=\"1\">\n" +
                "  <tr>\n" +
                "    <th scope=\"col\">Стек</th>\n" +
                "    <th scope=\"col\">Вход</th>\n" +
                "    <th scope=\"col\">Примечание</th>\n" +
                "  </tr>");

        Stack<Symbol> stack = new Stack<>();
        stack.push(startSymbol);
        //Печатаем таблицу разбора
        Formatter formatter = new Formatter();
        System.out.println(formatter.format("%-30s |%-20s |%-20s", "Стек", "Вход", "Примечание"));
        //печатаем первое состояние
        formatter = new Formatter();
        //System.out.println(formatter.format("%-20s %-20s %-20s"
              //  ,getCurrentStackState(stack), getCurrentLineState(line), ""));

        boolean moveToNextToken = true;
        Symbol lineFirstSymbol = null;
        String currentLine = "";
        while (!stack.isEmpty()) {
            String errorMsg = "";
            Symbol stackTopSmbl = stack.peek();//берем символ с вершины стека
            //берем первый символ из строки
            if (moveToNextToken) { //если нужно перейти к следующему токену
                boolean moveToNextLine = tokenizer.getCurrentLine().isEmpty();
                String nextToken = tokenizer.getNextToken();
                if (moveToNextLine) {
                    currentLine = tokenizer.getFullLine();
                } else {
                    currentLine = tokenizer.getCurrentLine();
                }
                if (!nextToken.equals("eof")) {
                    lineFirstSymbol = new Symbol(nextToken, true);
                } else {
                    lineFirstSymbol = null;
                }
                moveToNextToken = false;
            }
            //Symbol lineFirstSymbol = new Symbol(String.valueOf(line.charAt(0)), true);
            if (!stackTopSmbl.isTerminal) { //если он не является терминальным
                //смотрим в таблицу
                HashMap<Symbol, Symbol> indexes = new HashMap<>();
                indexes.put(stackTopSmbl, lineFirstSymbol);//символы являются индексами ячейки таблицы
                Rule rule = predictTable.get(indexes);
                //HashSet<Rule> rules = predictTable2.get(indexes);
                if (rule == null) { //если нет подходящего правила
                    errorMsg = "Нет правила для [" + stackTopSmbl + ", " + lineFirstSymbol + "]";
                    //Печатаем шаг
                    formatter = new Formatter();
                    //System.out.println(formatter.format("%-20s %-20s %-20s"
                      //      ,getCurrentStackState(stack), getCurrentLineState(line), errorMsg));
                    System.out.println(formatter.format("%-30s |%-20s |%-20s"
                                  ,getCurrentStackState(stack), currentLine, errorMsg));
                    tableStrBuilder = addRowToParsingTable(getCurrentStackState(stack), currentLine, errorMsg, tableStrBuilder);
                    //просто стираем символ (пропускаем)
                    //line.remove(0);
                    moveToNextToken = true;
                    continue;
                } else if (rule.symbols.get(0) != null && rule.symbols.get(0).value.equals("Synch")) {
                    formatter = new Formatter();
                    if (stack.size() > 1) {
                        errorMsg = "[" + stackTopSmbl + ", " + lineFirstSymbol + "] = Synch";
                        System.out.println(formatter.format("%-30s |%-20s |%-20s"
                                ,getCurrentStackState(stack), currentLine, errorMsg));
                        tableStrBuilder = addRowToParsingTable(getCurrentStackState(stack), currentLine, errorMsg, tableStrBuilder);
                        stack.pop();//снимаем нетерминал для стека
                    } else { //если в стеке только один нетерминал
                        errorMsg = "[" + stackTopSmbl + ", " + lineFirstSymbol + "] = Synch, в стеке 1 символ";
                        System.out.println(formatter.format("%-30s |%-20s |%-20s"
                                ,getCurrentStackState(stack), currentLine, errorMsg));
                        //line.remove(0);//пропускаем символ строки
                        moveToNextToken = true;
                        continue;
                    }
                    continue;
                } else {
                    formatter = new Formatter();
                    //Iterator<Rule> it = rules.iterator();
                    //Rule rule = it.next();//пока для каждого индекса только одно правило
                    stack.pop();
                    ArrayList<Symbol> symbolsInRule = rule.symbols;//получаем символы правила
                    //Если в правой части правила только epsilon
                    if (!(symbolsInRule.size() == 1 && symbolsInRule.get(0) == null)) {
                        //stack.pop();//Тогда мы просто удаляем верхний символ стека
                    //} else {
                        //добавляем их в стек
                        for (int i = symbolsInRule.size() - 1; i >= 0; --i) {
                            Symbol smbl = symbolsInRule.get(i);
                            stack.push(smbl);
                        }
                    }
                }

            } else { //иначе символ в стеке терминальный
                if (!stackTopSmbl.equals(lineFirstSymbol)) { //сравниваем верхний символ стека и первый символ строки
                    errorMsg = "Символы " + stackTopSmbl + " " + lineFirstSymbol + "не совпали";
                    System.out.println(formatter.format("%-30s |%-20s |%-20s"
                            ,getCurrentStackState(stack), currentLine, errorMsg));
                    tableStrBuilder = addRowToParsingTable(getCurrentStackState(stack), currentLine, errorMsg, tableStrBuilder);
                    stack.pop();//снимаем терминальный символ со стека
                    continue;
                } else {
                    //стираем символ из стека и из входной сроки
                    stack.pop();
                    //line.remove(0);
                    moveToNextToken = true;
                    currentLine = tokenizer.getCurrentLine();
                    //System.out.println();

                }
            }
            formatter = new Formatter();
            //Печатаем шаг
            System.out.println(formatter.format("%-30s |%-20s |%-20s"
                    ,getCurrentStackState(stack), currentLine, errorMsg));
            tableStrBuilder = addRowToParsingTable(getCurrentStackState(stack), currentLine, errorMsg, tableStrBuilder);

        }

        tableStrBuilder.append("</table>");
        writeParsingTableToFile(tableStrBuilder.toString());

    }

    private StringBuilder addRowToParsingTable(String currentStackState, String currentLine, String errorMsg, StringBuilder tableStrBuilder) {

        tableStrBuilder.append("<tr>\n" +
                "    <td>" + currentStackState + "</td>\n" +
                "    <td>" + currentLine + "</td>\n" +
                "    <td>" + errorMsg + "</td>\n" +
                "  </tr>");

        return tableStrBuilder;

    }

    private void writeParsingTableToFile(String tableStr) {

        try {
            FileOutputStream out = new FileOutputStream("parsing_table.html");
            out.write(tableStr.getBytes());
            out.close();
        } catch (IOException e) {

        }

    }

    private String getCurrentStackState(Stack<Symbol> stack) {

        //версия стека для вывода в консоль
        Stack<Symbol> reverseStackCopy = new Stack<>();
        Stack<Symbol> stackCopy = (Stack<Symbol>) stack.clone();
        while (!stackCopy.isEmpty()) {
            reverseStackCopy.push(stackCopy.pop());
        }
        String stackState = "";
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append("$ ");
        while (!reverseStackCopy.isEmpty()) {
            strBuilder.append(reverseStackCopy.pop().toString() + " ");
            //stackState += stackCopy.pop().toString();
        }
        return strBuilder.toString();
    }

    private String getCurrentLineState(ArrayList<Symbol> line) {
        String lineState = "";
        for (Symbol smbl : line) {
            lineState += smbl.toString();
        }
        lineState += "$";
        return lineState;
    }

}
