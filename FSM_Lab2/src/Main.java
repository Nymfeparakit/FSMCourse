import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        FSM fsm = new FSM();

        Scanner in = new Scanner(System.in);
        while (true) {
            System.out.println("File: ");
            String fileName = in.nextLine();
            System.out.println("");
            fsm.createFSMFromFile("test.txt");
        }

    }

}
