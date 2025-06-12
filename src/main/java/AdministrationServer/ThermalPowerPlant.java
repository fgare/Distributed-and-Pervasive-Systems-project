package AdministrationServer;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.xml.crypto.Data;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class ThermalPowerPlant {
    private final Integer id;
    private final String ipAddress;
    private final Integer port;
    private final ArrayList<DataPoint> pullutionMeasurements;
    private final DataReceiver dataReceiver;

    @JsonCreator
    public ThermalPowerPlant(
            @JsonProperty("id") Integer id,
            @JsonProperty("ip") String ipAddress,
            @JsonProperty("port") Integer port) {
        this.id = id;
        this.ipAddress = ipAddress;
        this.port = port;
        pullutionMeasurements = new ArrayList<>();
        dataReceiver = new DataReceiver(pullutionMeasurements);
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
    public List<DataPoint> getAllPullutionMeasurements() {
        synchronized (pullutionMeasurements) {
            Collections.sort(pullutionMeasurements);
            return pullutionMeasurements;
        }
    }

    /**
     * Calcola il valore medio delle misure comprese tra due istanti di tempo.
     * @param from inizio intervallo (compreso)
     * @param to fine intervallo (compreso)
     * @return valore medio di inquinamento
     * @throws IllegalArgumentException se gli estremi dell'intervallo non sono ordinati
     */
    public float getAverageMeasurementBetween(Long from, Long to) throws IllegalArgumentException{
        if (from > to) throw new IllegalArgumentException();

        synchronized (pullutionMeasurements) {
            List<DataPoint> subList = extractSubListBetween(new DataPoint(from), new DataPoint(to));
            return computeListAverageValue(subList);
        }
    }

    /**
     * Estrae una sotto-lista dalla lista delle misure di inquinamento, includendo i valori compresi tra due estremi temporali
     * @param fromDP inizio (compreso)
     * @param toDP fine (compresa)
     * @return lista di elementi con timestamp all'interno dell'intervallo
     */
    private List<DataPoint> extractSubListBetween(DataPoint fromDP, DataPoint toDP) {
        Collections.sort(pullutionMeasurements);

        int startingIndex = Collections.binarySearch(pullutionMeasurements, fromDP);
        // calcola l'indice del primo timestamp maggiore del timestamp fornito
        if (startingIndex < 0) startingIndex= Math.abs(startingIndex)+1;

        int endingIndex = Collections.binarySearch(pullutionMeasurements, toDP);
        if (endingIndex < 0) endingIndex = Math.abs(endingIndex)-1;

        return pullutionMeasurements.subList(startingIndex, endingIndex);
    }

    private float computeListAverageValue(List<DataPoint> list) {
        Integer sum = 0;
        for (DataPoint dataPoint : list) {
            sum += dataPoint.getValue();
        }
        return (float) sum/list.size();
    }

    public void publishMeasurement(DataPoint dataPoint) {
        synchronized (pullutionMeasurements) {
            pullutionMeasurements.add(dataPoint);
        }
    }
}
