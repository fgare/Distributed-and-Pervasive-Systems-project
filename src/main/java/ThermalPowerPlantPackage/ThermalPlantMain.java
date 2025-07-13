package ThermalPowerPlantPackage;

import AdministrationServerPackage.IdAlreadyExistsException;
import org.springframework.web.client.HttpStatusCodeException;

import java.io.IOException;
import java.util.Scanner;

public class ThermalPlantMain {

    public static void main(String[] args) {
        Scanner scan = new Scanner(System.in);

        // ID
        System.out.print("Plant Id >  ");
        int plant;
        while (!scan.hasNextInt()) {
            System.err.println("Id must be an integer");
            scan.next();
        }
        plant = scan.nextInt();

        // PORT
//        System.out.print("Client connection port >  ");
        int clientPort = 5000 + plant*2 - 1;
//        while (!scan.hasNextInt()) {
//            System.err.println("Id must be an integer");
//            scan.next();
//        }
//        clientPort = scan.nextInt();

        // ADMINISTRATION SERVER
        System.out.print("Server connection port >  ");
        int serverPort;
        while (!scan.hasNextInt()) {
            System.err.println("Id must be an integer");
            scan.next();
        }
        serverPort = scan.nextInt();

        try {
            new ThermalPowerPlant(plant, clientPort);
        } catch (IdAlreadyExistsException e) {
            System.err.println("Plant ID already exists");
        } catch (HttpStatusCodeException statusExc) {
            System.err.println("Unable to register the plant - HttpStatusCodeException: " + statusExc.getMessage());
        } catch (IOException | InterruptedException e) {
            System.err.println("Unable to start gRPC server - IOException: " + e.getMessage());
        }

    }
}
