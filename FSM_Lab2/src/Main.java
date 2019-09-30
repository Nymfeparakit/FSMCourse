import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        FSM fsm = new FSM();
        fsm.createFSMFromFile("test.txt");
        Scanner in = new Scanner(System.in);
        while (true) {
            //System.out.println("File: ");
            //String fileName = in.nextLine();
            //System.out.println("");
            System.out.println("Line: ");
            String line = in.nextLine();
            int errorPos;
            if ((errorPos = fsm.parseInput(line)) != line.length()) {
                System.out.println("Строка не является корректной");
                System.out.println("Позиция: " + errorPos);
            } else {
                System.out.println("Строка является корректной");
            }
        }

    }

}
