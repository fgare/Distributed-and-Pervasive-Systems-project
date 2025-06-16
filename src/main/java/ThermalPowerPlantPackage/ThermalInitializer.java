package ThermalPowerPlantPackage;

import java.io.IOException;

public class ThermalInitializer {

    public static void main(String[] args) {
        try {
            ThermalPowerPlant p = ThermalPowerPlant.newThermalPowerPlant();
        } catch (IOException e) {
            e.printStackTrace();
            //throw new RuntimeException(e);
        }
    }
}
