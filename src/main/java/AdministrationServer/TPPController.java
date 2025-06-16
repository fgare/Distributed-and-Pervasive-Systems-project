package AdministrationServer;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/ThermalPowerPlants")
public class TPPController {
    private final TPPService tppService;

    @Autowired
    public TPPController(TPPService tppService) {
        this.tppService = tppService;
    }

    @GetMapping
    public ResponseEntity<List<VirtualThermalPowerPlant>> getAllPowerPlants() {
        return ResponseEntity.ok(tppService.getAllPlants());
    }

    @GetMapping("/averagePullition")
    public ResponseEntity<Map<Integer,Float>> getAveragePollution(@RequestParam Long start, @RequestParam Long end) {
        try {
            return ResponseEntity.ok(tppService.averagePollution(start, end));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Gestisce la registrazione di nuove centrali termiche.
     * @param plant
     * @return
     */
    @PostMapping("/add")
    public ResponseEntity<Void> addPowerPlant(@RequestBody VirtualThermalPowerPlant plant) {
        try {
            tppService.addPlant(plant);
            return ResponseEntity.ok().build();
        } catch (IdAlreadyExistsException ex) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .build();
        }
    }
}
