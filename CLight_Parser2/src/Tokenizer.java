import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Tokenizer {

    BufferedReader reader;
    String currentLine = "";//текущая обрабатываемя строка
    boolean currentTokenIsCharOrNmbr = false;
    String fullLine = "";

    public void openFileToRead(String fileName) {

        try {
            FileReader fileReader = new FileReader(fileName);
            reader = new BufferedReader(fileReader);
            //currentLine = reader.readLine().trim();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public String getNextToken() {

        try {
            if (currentLine.isEmpty()) {//если в строке больше ничего нет
                currentLine = reader.readLine().trim(); //читаем по строке
                fullLine = currentLine;
            }
            if (currentLine == null) { //если достигнут конец файла
                return "eof";
            }
            if (currentTokenIsCharOrNmbr) {
                if (String.valueOf(currentLine.charAt(0)).matches("\\s")) { //если уже встретили разделитьель лексем
                    currentTokenIsCharOrNmbr = false;
                    currentLine = currentLine.trim();
                } else {
                    String nextToken = currentLine.substring(0, 1);//забираем по одному символу
                    currentLine = currentLine.substring(1);
                    return nextToken;
                }
            }
            //пытаемся определить первый токен в строке
            for (TokenType type : TokenType.values()) { //последовательное применяем все типы
                String reg = "^" + type.getReg(); //TODO пропускать пробелы в начале строки
                Pattern pattern = Pattern.compile(reg);
                Matcher matcher = pattern.matcher(currentLine);
                if (matcher.find()) { //если совпадение найдено
                    String nextToken = "";
                    if (type == TokenType.ID || type == TokenType.NUMBER) {
                        currentTokenIsCharOrNmbr = true;
                        nextToken = currentLine.substring(0, 1);//забираем по одному символу
                        currentLine = currentLine.substring(1);
                    } else {
                        nextToken = currentLine.substring(matcher.start(), matcher.end());
                        currentLine = currentLine.substring(matcher.end()).trim();//стираем обработанную часть строки
                    }
                    return nextToken;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";

    }

    public String getCurrentLine() {
        return currentLine;
    }

    public String getFullLine() {
        return fullLine;
    }

    public enum TokenType {

        KEYWORD("\\b(for|if|int|bool|void|return|main)\\b"),
        REL_OP("(<|>|==|!=)"),
        ASSIGN("="),//единтсвенная операция в языке - присваивание
        ID("[A-Za-z]\\w*"),//имена переменных
        NUMBER("(0|[1-9]\\d*)"),//допускаются только целые числа
        MARK("(\\(|\\)|\\{|\\}|;)");

        private String reg;

        TokenType(String reg) {
            this.reg = reg;
        }

        public String getReg() {
            return reg;
        }

    }

}
