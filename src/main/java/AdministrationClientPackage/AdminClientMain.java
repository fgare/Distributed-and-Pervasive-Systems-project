package AdministrationClientPackage;

import org.springframework.web.client.RestClientException;

import java.util.Scanner;

public class AdminClientMain {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int choise = 9;

        do {
            System.out.println("======= ADMINISTRATION CLIENT =======");
            System.out.println("Functions:\n" +
                    "0.\tExit\n" +
                    "1.\tGet list of thermal power plants\n" +
                    "2.\tGet average emission level by all plants");
            while (!scanner.hasNextInt()) {
                System.err.println("Invalid input");
                scanner.next();
            }
            choise = scanner.nextInt();

            switch (choise) {
                case 0:
                    System.out.println("Quitting client...");
                    break;
                case 1:
                    try {
                        String s = new APIClient("localhost:8080").getPlantsList();
                        System.out.println(s);
                    } catch (RestClientException e) {
                        System.err.println("Unable to reach the server");
                    }
                    break;
                case 2:
                    break;
                default:
                    System.err.println("Invalid input");
                    break;
            }

        } while (choise != 0);

    }

}
