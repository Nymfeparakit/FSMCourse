import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        FSM fsm = new FSM();
        Scanner in = new Scanner(System.in);
        boolean newFile = false;
        while (true) {
            if (newFile) { //если нужно ввести новый файл
                System.out.println("File: ");
                String fileName = in.nextLine();
                System.out.println("");
                fsm = new FSM();
                fsm.createFSMFromFile(fileName);
                newFile = false;
            }
            System.out.println("Line: ");
            String line = in.nextLine();
            if (line.equals("0")) {
                newFile = true;
                continue;
            }
            int errorPos;
            if ((errorPos = fsm.parseInput(line)) != line.length()) {
                System.out.println("Строка не является корректной");
                if (errorPos != -1) {
                    System.out.println("Позиция: " + errorPos);
                }
            } else {
                System.out.println("Строка является корректной");
            }
        }

    }

}
