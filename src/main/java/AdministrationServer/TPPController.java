package AdministrationServer;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ThermalPowerPlants")
public class TPPController {
    private final TPPService tppService;

    @Autowired
    public TPPController(TPPService tppService) {
        this.tppService = tppService;
    }

    @GetMapping
    public ResponseEntity<List<ThermalPowerPlant>> getAllPowerPlants() {
        return ResponseEntity.ok(tppService.getAll());
    }

    @GetMapping("/averagePullition")
    public ResponseEntity<Float> getAveragePullition() {
        //TODO
    }

    /**
     * Gestisce la registrazione di nuove centrali termiche.
     * @param plant
     * @return
     */
    @PostMapping("/add")
    public ResponseEntity<Void> addPowerPlant(@RequestBody ThermalPowerPlant plant) {
        try {
            tppService.add(plant);
            return ResponseEntity.ok().build();
        } catch (IdAlreadyExistsException ex) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .build();
        }
    }
}
