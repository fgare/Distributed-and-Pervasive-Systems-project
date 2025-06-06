package AdministrationServer;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ThermalPowerPlants")
public class TPPController {
    private TPPController tppController;

    public TPPController(TPPController tppController) {
        this.tppController = tppController;
    }

    /**
     * Gestisce la registrazione di nuove centrali termiche.
     * @param plant
     * @return
     */
    @PostMapping("/add")
    public ResponseEntity<Void> addPowerPlant(@RequestBody ThermalPowerPlant plant) {
        try {
            tppController.addPowerPlant(plant);
        } catch (IdAlreadyExistsException ex) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .build();
        }

        return ResponseEntity.ok().build();
    }
}
