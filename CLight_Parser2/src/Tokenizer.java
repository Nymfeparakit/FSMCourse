import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Tokenizer {

    BufferedReader reader;
    String currentLine = "";//текущая обрабатываемя строка

    public void openFileToRead(String fileName) {

        try {
            FileReader fileReader = new FileReader(fileName);
            reader = new BufferedReader(fileReader);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    public String getNextToken() {

        try {
            if (currentLine.isEmpty()) {//если в строке больше ничего нет
                currentLine = reader.readLine(); //читаем по строке
            }
            if (currentLine == null) { //если достигнут конец файла
                return "eof";
            }
            //пытаемся определить первый токен в строке
            for (TokenType type : TokenType.values()) { //последовательное применяем все типы
                String reg = "^" + type.getReg(); //TODO пропускать пробелы в начале строки
                Pattern pattern = Pattern.compile(reg);
                Matcher matcher = pattern.matcher(currentLine);
                if (matcher.find()) { //если совпадение найдено
                    currentLine = currentLine.substring(matcher.start(), matcher.end());//стираем обработанную часть строки
                    return currentLine.substring(matcher.start(), matcher.end());
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

    public enum TokenType {

        KEYWORD("\\b(for|if|int|bool|void|return)\\b"),
        ASSIGN("="),//единтсвенная операция в языке - присваивание
        REL_OP("<|>|==|!="),
        ID("[A-Za-z]\\w*"),//имена переменных
        NUMBER("(0|[1-9]\\d*)"),//допускаются только целые числа
        MARK("\\(|\\)|\\{|\\}|;");

        private String reg;

        TokenType(String reg) {
            this.reg = reg;
        }

        public String getReg() {
            return reg;
        }

    }

}
