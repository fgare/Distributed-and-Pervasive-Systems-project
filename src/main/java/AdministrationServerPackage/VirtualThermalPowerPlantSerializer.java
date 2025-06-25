package AdministrationServerPackage;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

class VirtualThermalPowerPlantSerializer implements JsonSerializer<VirtualThermalPowerPlant> {

    @Override
    public JsonElement serialize(VirtualThermalPowerPlant powerPlant, Type
    type, JsonSerializationContext jsonSerializationContext) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("id", powerPlant.getId());
        jsonObject.addProperty("ip", powerPlant.getIpAddress());
        jsonObject.addProperty("port", powerPlant.getPort());
        return jsonObject;
    }

}
