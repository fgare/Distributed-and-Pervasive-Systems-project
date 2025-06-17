package ThermalPowerPlantPackage;

import AdministrationServerPackage.IdAlreadyExistsException;
import SimulatorsPackage.PollutionSensor;
import com.google.gson.*;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.ServerSocket;
import java.util.Random;

public class ThermalPowerPlant {
    private Integer id;
    private final String ipAddress;
    private final Integer port;

    private final MeasureManager measureManager;

    /**
     * Si occupa della generazione di una nuova centrale termica con un ID univoco nel sistema
     * @return la centrale termica creata
     * @throws IOException se tutte le porte del sistema sono già in uso
     */
    public static ThermalPowerPlant newThermalPowerPlant() throws IOException {
        Random rnd = new Random();
        while (true) {
            try {
                return new ThermalPowerPlant(rnd.nextInt());
            } catch (IdAlreadyExistsException e) {}
        }
    }

    private ThermalPowerPlant(Integer id, String ipAddress) throws IOException, IdAlreadyExistsException {
        this.id = id;
        this.ipAddress = ipAddress;
        this.port = findAvailablePort();
        new ThermalInsertionHandler(this).publishPlant(); // prova a registrare la nuova centrale
        measureManager = new MeasureManager(this, 8, (float) 0.5);
        (new PollutionSensor(measureManager)).start(); // avvia il simulatore di inquinamento
    }

    public ThermalPowerPlant(Integer id) throws IOException, IdAlreadyExistsException {
        this(id, "localhost");
    }

    public synchronized Integer getPort() {
        return port;
    }

    public synchronized Integer getId() {
        return id;
    }

    public synchronized String getIpAddress() {
        return ipAddress;
    }

    private Integer findAvailablePort() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(0)) {
            return serverSocket.getLocalPort();
        } catch (IOException ioe) {
            System.err.println("Unable to find available TCP port");
            throw ioe;
        }
    }

    public String toJsonString() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(ThermalPowerPlant.class, new ThermalPowerPlantSerializer());
        gsonBuilder.setPrettyPrinting();

        Gson gson = gsonBuilder.create();
        return gson.toJson(this);
    }


    private static class ThermalPowerPlantSerializer implements JsonSerializer<ThermalPowerPlant> {
        @Override
        public JsonElement serialize(ThermalPowerPlant tp, Type type, JsonSerializationContext jsonSerializationContext) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("id", tp.getId());
            jsonObject.addProperty("ip", tp.getIpAddress());
            jsonObject.addProperty("port", tp.getPort());
            return jsonObject;
        }
    }


    public static void main (String[] args) throws IOException, IdAlreadyExistsException {
        ThermalPowerPlant tpp = newThermalPowerPlant();
    }

}
