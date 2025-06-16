package AdministrationServerPackage;

public class IdAlreadyExistsException extends RuntimeException {

    public IdAlreadyExistsException(String message) {
        super(message);
    }

    public IdAlreadyExistsException(Integer plantId) {
        super("There is another ThermalPowerPlant with ID " +  plantId);
    }

}
