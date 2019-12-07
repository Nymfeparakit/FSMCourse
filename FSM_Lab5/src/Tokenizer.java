import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class Tokenizer {

    BufferedReader reader;
    String currentLine = "";//текущая обрабатываемя строка
    boolean currentTokenIsCharOrNmbr = false;
    int numberOfCurrentLine;
    int numberOfTokenInLine;
    int smblPos; //позиция символа в строке
    int strNum; //номер строки в файле
    ArrayList<String> text; //строки прочитанного файла
    public char currentSmbl;

    public Tokenizer() {
        strNum = 0;
        smblPos = 0;
    }

    public void openFileToRead(String fileName) {

        try {
            FileReader fileReader = new FileReader(fileName);
            reader = new BufferedReader(fileReader);

            text = new ArrayList<>();
            //читаем весь файл сразу целиком
            while ((currentLine = reader.readLine()) != null) {
                if (currentLine.isEmpty()) continue; //пропускаем пустые строки
                text.add(currentLine);
            }

            //берем первый символ
            currentLine = text.get(0);
            currentSmbl = currentLine.charAt(0);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void skipWhiteSpace() {
        while (currentSmbl != '$' && Character.isWhitespace(currentSmbl))
            moveToNextSymbol();
    }

    private String number() {
        String value = "";
        while (currentSmbl != '$' && Character.isDigit(currentSmbl)) {
            value += currentSmbl;
            moveToNextSymbol();
        }
        return value;
    }

    private Token id() {

        String result = "";
        int currStrNum = strNum;//id располагается только в одной строке

        while(currentSmbl != '$' && Character.isLetter(currentSmbl) && currStrNum == strNum) {
            result += currentSmbl;
            moveToNextSymbol();
        }

        Token token = getReservedWordToken(result);
        if (token == null) { //если ключевое слово не было распознано
            token = new Token(Token.TokenType.ID, result); //значит это идентификатор
        }

        return token;

    }

    private Token getReservedWordToken(String value) {

        //проверяем на ключевые слова

        return null; //если не совпало ни с одним словом

    }

    public Token getNextToken() {

        while (currentSmbl != '$') {

            if (Character.isWhitespace(currentSmbl)) {
                skipWhiteSpace();
                continue;
            }

            if (Character.isDigit(currentSmbl)) {
                return new Token(Token.TokenType.NUMBER, number());
            }

            if (currentSmbl == '+') {
                moveToNextSymbol();
                return new Token(Token.TokenType.PLUS, "+");
            }

            if (currentSmbl == '-') {
                moveToNextSymbol();
                return new Token(Token.TokenType.MINUS, "-");
            }

            if (currentSmbl == '*') {
                moveToNextSymbol();
                return new Token(Token.TokenType.MUL, "*");
            }

            if (currentSmbl == '/') {
                moveToNextSymbol();
                return new Token(Token.TokenType.DIV, "/");
            }

            if (currentSmbl == '(') {
                moveToNextSymbol();
                return new Token(Token.TokenType.LEFT_PAR, "(");
            }

            if (currentSmbl == ')') {
                moveToNextSymbol();
                return new Token(Token.TokenType.RIGHT_PAR, ")");
            }

            if (Character.isLetter(currentSmbl)) {
                return id();
            }

            if (currentSmbl == '=' && peek() != '=') {
                moveToNextSymbol();
                return new Token(Token.TokenType.ASSIGN, "=");
            }

            error();

        }

        return new Token(Token.TokenType.EOF, "$");

    }

    private char peek() {
        char ch;
        int peekPos = smblPos + 1;
        int peekNumStr = strNum;
        if (peekPos == text.get(strNum).length()) { //если вышли за пределы строки
            if (strNum == text.size() - 1) { //если это последняя строка
                return '$';
            }
            ++peekNumStr;
            peekPos = 0;
        }
        ch = text.get(peekNumStr).charAt(peekPos);
        return ch;
    }

    private void error() {

    }

    public void moveToNextSymbol() {

        String currLine = text.get(strNum);
        ++smblPos; //переходим к след символу
        if (smblPos < currLine.length()) { //если еще не вышли за пределы строки
            currentSmbl = currLine.charAt(smblPos);
        } else {
            if (strNum == text.size() - 1) { //если это последняя строка
                currentSmbl = '$';
                return;
            }
            ++strNum; //переходим к следующей строке
            currLine = text.get(strNum);
            smblPos = 0;
            currentSmbl = currLine.charAt(0); //берем первый символ
        }

    }

}
