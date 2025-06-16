package AdministrationServerPackage;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.*;


public class VirtualThermalPowerPlant {
    private final Integer id;
    private final String ipAddress;
    private final Integer port;
    private final TreeSet<DataPoint> pollutionMeasurements;
    private final DataReceiver dataReceiver;

    @JsonCreator
    public VirtualThermalPowerPlant(
            @JsonProperty("id") Integer id,
            @JsonProperty("ip") String ipAddress,
            @JsonProperty("port") Integer port) {
        this.id = id;
        this.ipAddress = ipAddress;
        this.port = port;
        pollutionMeasurements = new TreeSet<>();
        dataReceiver = new DataReceiver(pollutionMeasurements, id);
        new Thread(dataReceiver).start();
    }

    public Integer getId() {
        return id;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public Integer getPort() {
        return port;
    }

    /**
     * Ritorna tutte le misure presenti
     * @return lista di misure
     */
    public SortedSet<DataPoint> getAllPollutionMeasurements() {
        synchronized (pollutionMeasurements) {
            return pollutionMeasurements;
        }
    }

    /**
     * Calcola il valore medio delle misure comprese tra due istanti di tempo.
     * @param from inizio intervallo (compreso)
     * @param to fine intervallo (compreso)
     * @return valore medio di inquinamento
     */
    public Float getAverageMeasurementBetween(Long from, Long to) throws IllegalArgumentException {
        DataPoint fromDP = new DataPoint(from);
        DataPoint toDP = new DataPoint(to);
        return getAverageMeasurementBetween(fromDP, toDP);
    }

    public Float getAverageMeasurementBetween(DataPoint fromDP, DataPoint toDP) throws IllegalArgumentException {
        synchronized (pollutionMeasurements) {
            NavigableSet<DataPoint> subSet = pollutionMeasurements.subSet(fromDP, true, toDP, true);
            return computeAveragePollutionValue(subSet);
        }
    }

    private Float computeAveragePollutionValue(NavigableSet<DataPoint> set) {
        Integer sum = 0;
        for (DataPoint dataPoint : set) {
            sum += dataPoint.getValue();
        }
        return (float) sum/set.size();
    }

}
