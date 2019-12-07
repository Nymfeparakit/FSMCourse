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

        }

        error();
        return new Token(Token.TokenType.EOF, "$");

    }

    private void error() {

    }

    public void moveToNextSymbol() {

        String currLine = text.get(strNum);
        ++smblPos; //переходим к след символу
        if (smblPos < currLine.length()) { //если еще не вышли за пределы строки
            if (strNum == text.size() - 1) { //если это последняя строка
                currentSmbl = '$';
            }
            currentSmbl = currLine.charAt(smblPos);
        } else {
            ++strNum; //переходим к следующей строке
            currLine = text.get(strNum);
            smblPos = 0;
            currentSmbl = currLine.charAt(0); //берем первый символ
        }

    }

}
