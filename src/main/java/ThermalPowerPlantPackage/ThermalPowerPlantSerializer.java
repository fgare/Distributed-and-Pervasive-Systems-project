package ThermalPowerPlantPackage;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

class ThermalPowerPlantSerializer implements JsonSerializer<ThermalPowerPlant> {

    @Override
    public JsonElement serialize(ThermalPowerPlant tp, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("id", tp.getId());
        jsonObject.addProperty("ip", tp.getIpAddress());
        jsonObject.addProperty("port", tp.getPort());
        return jsonObject;
    }

}
