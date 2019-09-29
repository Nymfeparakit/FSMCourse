import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FSM {

    //здесь описаны переходы автомата
    //private HashMap<String, HashMap<String, ArrayList<String>>> switches;
    private HashMap<String, HashMap<String, HashSet<String>>> switches;
    private boolean isDeterministic;

    //читает входной текствый файл и создает из него FSM
    public void createFSMFromFile(String fileName) {

        switches = new HashMap<>();
        isDeterministic = true;
        //читаем файл построчно
        try {

            File file = new File(fileName);
            FileReader fileReader = new FileReader(file);
            BufferedReader reader = new BufferedReader(fileReader);
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isEmpty()) //пропускаем пустые строки
                    continue;
                //сначала проверяем строку на соответствие шаблону
                int errorPos;
                if ((errorPos = checkIfSwitchLineIsCorrect(line)) < line.length()) {
                    System.out.print("Line is not correct: ");
                    System.out.println(line);//печатаем неверную строку
                    //System.out.println((char)27 + "[33mYELLOW"); escape sequence для цветв
                    return;
                }
                addSwitch(line);
            }
            printSwitches();
            System.out.println("Deterministic: " + isDeterministic);

        } catch (IOException ex) {
            System.err.println(ex.getMessage());
        }

    }

    //проверяет, что в строке был корректно записан переход
    //если нет, то возвращает некорректную позицию, где найдено нессответствие
    private int checkIfSwitchLineIsCorrect(String line)
    {
        Pattern switchLinePat = Pattern.compile("q\\d+,.=[qf]\\d+");//шаблон записи перехода
        Matcher matcher = switchLinePat.matcher(line);
        for (int i = line.length(); i >= 0; --i) { //находим некорректную позицию
            Matcher region = matcher.region(0, i);
            if (region.matches() || region.hitEnd()) {
                return i;
            }
        }
        return line.length();//если вся строка соответствует шаблону, то возращаем длину
    }

    //анализирует строку и добавляет соответствующий переход в switches
    private void addSwitch(String line) {

        //Анализируем строку
        Pattern statePat = Pattern.compile("[qf]\\d+");//состояние
        //Pattern symbolPat = Pattern.compile("(?<=,).(?==)");
        Matcher matcher = statePat.matcher(line);
        matcher.find();//находим начальное состояние
        int posOfSymbol = matcher.end() + 1;//символ находится за состоянием и запятой
        String initialState = line.substring(0, matcher.end());
        matcher.find();//находим следующее состояние
        String nextState = line.substring(matcher.start(), matcher.end());
        //matcher = symbolPat.matcher(line);
        //matcher.find();
        String symbol = line.substring(posOfSymbol, posOfSymbol + 1);

        //Заполняем HashMap
        //HashMap<String, ArrayList<String>> switchesForCurrState;//переходы для текущего состояния
        HashMap<String, HashSet<String>> switchesForCurrState;//переходы для текущего состояния
        if ((switchesForCurrState = switches.get(initialState)) == null) { //если для такого состояния еще не записывались переходы
            switchesForCurrState = new HashMap<>();
        }
        //ArrayList<String> nextStates;
        HashSet<String> nextStates;
        if ((nextStates = switchesForCurrState.get(symbol)) == null) {
            //nextStates = new ArrayList<>();
            nextStates = new HashSet<>();
        } else { //если уже были переходы для этого же состояния и этого же символа
            isDeterministic = false;
        }
        nextStates.add(nextState);
        switchesForCurrState.put(symbol, nextStates);
        switches.put(initialState, switchesForCurrState);

    }

    //преобразует недетерминированный автомат в детерминированный
    private void convertToDeterministic() {

        HashMap<String, HashMap<String, String>> newSwitches = new HashMap<>();//здесь будут переходы для нового детерминированного автомата
        ArrayList<HashSet<String>> newStatesList = new ArrayList<>();//очередь из новых состояний, которые нужно разобрать
        int indexOfNewStatesList = 0;//индекс чтобы постепенно проходится по элементам
        //используется отдельно, так как список будет пополняться постепенно
        HashSet<String> firstNewState = new HashSet<>();
        //сюда будут записываться все переходя для
        HashMap<String, HashSet<String>> switchesForState = new HashMap<>();
        firstNewState.add("q0");//начальное состояние - всегда qo?
        newStatesList.add(firstNewState);
        HashMap<HashSet<String>, String> newStatesNames = new HashMap<>();
        int statesCounter = 0;//счетчик новых состояний
        int finalStatesCounter = 0;//счетчик новых финальных состояний
        boolean isNewStateHasFinishState = false;//находится ли в новом состоянии финальное
        do {
            HashSet<String> newState = newStatesList.get(indexOfNewStatesList);

            //сюда запишем переходы для нового состояния
            HashMap<String, HashSet<String>> newStateSwitches = new HashMap<>();
            for (String s : newState) { //проходимся по всем состояниям из нового
                //если переходы для данного элемента еще не были записаны
                HashMap<String, HashSet<String>> switchesForS = switches.get(s);//получаем все переходы для данного состояния
                for (HashMap.Entry<String, HashSet<String>> item : switchesForS.entrySet()) { //Проходимся по всем символам
                    String key = item.getKey();
                    HashSet partOfValue = item.getValue();
                    HashSet<String> partOfValue2;
                    if ((partOfValue2 = newStateSwitches.get(key)) != null) { //объединяем с теми состояниями, которые уже записаны
                        partOfValue.addAll(partOfValue2);
                        newStateSwitches.put(key, partOfValue);
                    }
                }

            }
            //Сюда будут записаны переходы с уже новыми именами
            HashMap<String, String> newStateSwitches2 = new HashMap<>();
            //Теперь проходимся по всем символам и находим те состояния, которых еще нет в newStateList
            for (HashMap.Entry<String, HashSet<String>> item : newStateSwitches.entrySet()) {
                HashSet<String> value = item.getValue();
                if (!newStatesList.contains(value)) { //если такого еще нет в списке
                    newStatesList.add(value);
                    //определим, будет ли оно финальным
                    isNewStateHasFinishState = false;
                    Iterator<String> it = value.iterator();
                    while (it.hasNext()) {
                        if (it.next().contains("f")) {
                            isNewStateHasFinishState = true;
                            break;
                        }
                    }
                    //определяем для него новое имя и запоминаем его
                    newStatesNames.put(value, createNameForNewState(isNewStateHasFinishState, statesCounter, finalStatesCounter));
                }
                newStateSwitches2.put(item.getKey(), newStatesNames.get(value));//записываем с новым именем
            }
            newSwitches.put(newStatesNames.get(newState), newStateSwitches2);//дополняем новыми переходами
            ++indexOfNewStatesList;//переходим к следующему элементу
        } while (indexOfNewStatesList < newStatesList.size()); //Пока есть неразобранные состояния

    }

    //statesCounter, finalStatesCounter используются, чтобы определить какой номер состоянию задать
    private String createNameForNewState(boolean isNewStateHasFinishState, int statesCounter, int finalStatesCounter) {

        //Составляем новое имя для состояния
        String newName = "";
        if (isNewStateHasFinishState) { //если в новом состоянии есть финальное
            newName = "f" + finalStatesCounter;
        } else {
            newName = "q" + statesCounter;
        }
        return  newName;
    }

    //вывод переходов в консоль
    private void printSwitches() {
        for (HashMap.Entry<String, HashMap<String, HashSet<String>>> item : switches.entrySet()) {
            String currState = item.getKey();
            HashMap<String, HashSet<String>> switchesForCurrState = item.getValue();
            for (HashMap.Entry<String, HashSet<String>> item2 : switchesForCurrState.entrySet()) {
                String symbol = item2.getKey();
                HashSet<String> nextStates = item2.getValue();
                for (String nextState : nextStates) {
                    System.out.println("IniState = " + currState + ", Symbol = \"" + symbol +
                            "\", NextState = " + nextState + ".");
                }
            }
        }
    }


}

