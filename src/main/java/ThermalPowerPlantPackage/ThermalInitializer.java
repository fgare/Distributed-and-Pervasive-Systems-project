package ThermalPowerPlantPackage;

import java.io.IOException;

public class ThermalInitializer {

    public static void main(String[] args) {
        System.out.println("Creo classe");

        try {
            new ThermalPowerPlant(5);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
