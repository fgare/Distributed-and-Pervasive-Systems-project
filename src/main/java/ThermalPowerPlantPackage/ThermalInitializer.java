package ThermalPowerPlantPackage;

import AdministrationServerPackage.IdAlreadyExistsException;

import java.io.IOException;
import java.util.Scanner;

public class ThermalInitializer {

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
        System.out.print("Client connection port >  ");
        int clientPort;
        while (!scan.hasNextInt()) {
            System.err.println("Id must be an integer");
            scan.next();
        }
        clientPort = scan.nextInt();

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
        }

    }
}
