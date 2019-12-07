import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Tokenizer {

    BufferedReader reader;
    String currentLine = "";//текущая обрабатываемя строка
    boolean currentTokenIsCharOrNmbr = false;
    String fullLine = "";
    String currentToken = "";
    Parser parser;
    int numberOfCurrentLine;
    int numberOfTokenInLine;
    TextFlowFiller filler;
    boolean lineContainsMistakes;

    public Tokenizer(Parser parser, TextFlowFiller filler) {
        this.parser = parser;
        this.filler = filler;
    }

    public void openFileToRead(String fileName) {

        try {
            FileReader fileReader = new FileReader(fileName);
            reader = new BufferedReader(fileReader);
            numberOfCurrentLine = 0;//номер текущей строки
            numberOfTokenInLine = 0;//номер токена в строке
            //currentLine = reader.readLine().trim();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void addMistake(String errorMsg) {
        filler.addMistake(errorMsg, numberOfCurrentLine, numberOfTokenInLine);
        lineContainsMistakes = true;
    }

    public String getNextToken() {

        //try {
            if (currentLine.isEmpty()) {//если в строке больше ничего нет
                getNextLine();
                lineContainsMistakes = false;
                /*
                currentLine = reader.readLine(); //читаем по строке
                fullLine = currentLine; //запоминаем строку целиком
                currentLine = currentLine.trim();
                ++numberOfCurrentLine;
                numberOfTokenInLine = 0;//пока еще не прочитали ни одного токена
                */
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
                    //currentLine = currentLine.substring(1);
                    return (currentToken = nextToken);
                }
            }
            ++numberOfTokenInLine;//увеличиваем номер токена в строке
            //пытаемся определить первый токен в строке
            for (TokenType type : TokenType.values()) { //последовательное применяем все типы
                String reg = "^" + type.getReg();
                Pattern pattern = Pattern.compile(reg);
                Matcher matcher = pattern.matcher(currentLine);
                if (matcher.find()) { //если совпадение найдено
                    String nextToken = "";
                    if (type == TokenType.ID || type == TokenType.NUMBER) {
                        currentTokenIsCharOrNmbr = true;
                        nextToken = currentLine.substring(0, 1);//забираем по одному символу
                        //currentLine = currentLine.substring(1);
                    } else {
                        nextToken = currentLine.substring(matcher.start(), matcher.end());
                        //currentLine = currentLine.substring(matcher.end()).trim();//стираем обработанную часть строки
                    }
                    return (currentToken = nextToken);
                }
            }
            //если дошли до этого места, значит токен не распознался лексером
            //тогда пишем об ошибке в таблице
            StringBuilder tableStrBuilder = parser.addRowToParsingTable(parser.getCurrentStackState(parser.stack), currentLine,
                    "Невозможно распознать символ", parser.tableStrBuilder);
            parser.tableStrBuilder = tableStrBuilder;
            addMistake("Невозможно распознать символ");//запоминаем ошибку

            int posOfSeparator = currentLine.indexOf(" ");//находим пробел
            currentLine = currentLine.substring(posOfSeparator).trim();//и обрезаем строку до следующей лексемы
       // } catch (IOException e) {
       //     e.printStackTrace();
       // }
        return "#";//индикатор неопознанного символа

    }

    public String getNextLine() {
        try {
            //выводим текущую линию в textFlow
            filler.displayLine(fullLine, numberOfCurrentLine);
            currentLine = reader.readLine(); //читаем по строке
            if (currentLine == null) //если дошли до конца файла
                return null;
            currentTokenIsCharOrNmbr = false;//чтобы токены идентификаторов и цифр разделялись переносами строк
            fullLine = currentLine; //запоминаем строку целиком
            ++numberOfCurrentLine;
            numberOfTokenInLine = 0;//пока еще не прочитали ни одного токена
            return (currentLine = currentLine.trim()); //читаем по строке
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    //"выталкивает" из строки первый токен
    public void popToken() {
        int length = currentToken.length();
        if (currentTokenIsCharOrNmbr) { //если сейчас идет число или id, не обрезаем пробелы слева
            currentLine = currentLine.substring(length); //чтобы словить момент, когда он закончится
        } else {
            currentLine = currentLine.substring(length).trim();
        }

    }

    public String getCurrentLine() {
        return currentLine;
    }

    public String getFullLine() {
        return fullLine;
    }

    public enum TokenType {

        KEYWORD("\\b(for|if|int|bool|void|return|main|and|or)\\b"),
        REL_OP("(<|>|==|!=)"),
        ASSIGN("="),//единтсвенная операция в языке - присваивание
        ID("[A-Za-z]\\w*"),//имена переменных
        NUMBER("(0|[1-9]\\d*)"),//допускаются только целые числа
        //MARK("(\\(|\\)|\\{|\\}|;)");
        MARK("(\\(|\\)|\\{|\\}|;|&&|\\|\\|)");

        private String reg;

        TokenType(String reg) {
            this.reg = reg;
        }

        public String getReg() {
            return reg;
        }

    }

}
