package AdministrationServerPackage;

import SimulatorsPackage.Measurement;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.*;


public class VirtualThermalPowerPlant {
    private final Integer id;
    private final String ipAddress;
    private final Integer port;
    private final TreeSet<Measurement> pollutionMeasurements;

    @JsonCreator
    public VirtualThermalPowerPlant(
            @JsonProperty("id") Integer id,
            @JsonProperty("ip") String ipAddress,
            @JsonProperty("port") Integer port) {
        this.id = id;
        this.ipAddress = ipAddress;
        this.port = port;
        pollutionMeasurements = new TreeSet<>();
        new Thread(new DataReceiver(pollutionMeasurements, id)).start();
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
    public SortedSet<Measurement> getAllPollutionMeasurements() {
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
        Measurement fromDP = new Measurement("0", "from", 0, from);
        Measurement toDP = new Measurement("0", "to", 0, to);
        return getAverageMeasurementBetween(fromDP, toDP);
    }

    public Float getAverageMeasurementBetween(Measurement fromMeas, Measurement toMeas) throws IllegalArgumentException {
        synchronized (pollutionMeasurements) {
            NavigableSet<Measurement> subSet = pollutionMeasurements.subSet(fromMeas, true, toMeas, true);
            return computeAveragePollutionValue(subSet);
        }
    }

    private Float computeAveragePollutionValue(NavigableSet<Measurement> set) {
        double sum = 0;
        for (Measurement m : set) {
            sum += m.getValue();
        }
        return (float) sum/set.size();
    }

}
