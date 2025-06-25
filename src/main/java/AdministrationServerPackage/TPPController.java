package AdministrationServerPackage;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
    public ResponseEntity<String> getAllPowerPlants() {
        String jsonBody = tppService.getAllPlants();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new ResponseEntity<>(jsonBody, headers, HttpStatus.OK);
    }

    @GetMapping("/averagePullition")
    public ResponseEntity<Map<Integer,Float>> getAveragePollution(@RequestParam Long start, @RequestParam Long end) {
        try {
            return ResponseEntity.ok(tppService.averagePollution(start*1000, end*1000));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Gestisce la registrazione di nuove centrali termiche.
     * @param plant
     * @return
     */
    @PostMapping()
    public ResponseEntity<String> addPowerPlant(@RequestBody VirtualThermalPowerPlant plant) {
        try {
            String plantsList = tppService.addPlant(plant);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            return new ResponseEntity<>(plantsList, headers, HttpStatus.CREATED);
        } catch (IdAlreadyExistsException ex) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .build();
        }
    }
}
