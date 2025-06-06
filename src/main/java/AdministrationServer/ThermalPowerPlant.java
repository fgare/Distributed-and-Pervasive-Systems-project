package AdministrationServer;

import java.sql.Array;
import java.util.ArrayList;
import java.util.LinkedList;

public class ThermalPowerPlant {
    private Integer id;
    private String ipAddress;
    private Integer port;
    private DataReceiver dataReceiver;
    private ArrayList<DataPoint> pullutionMeasurements;


    public ThermalPowerPlant(Integer id, String ipAddress, Integer port) {
        pullutionMeasurements = new ArrayList<>(100);
        dataReceiver = new DataReceiver(pullutionMeasurements);
    }

    public Integer getId() {
        return id;
    }

}
