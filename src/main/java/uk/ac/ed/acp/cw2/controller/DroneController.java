package uk.ac.ed.acp.cw2.controller;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.ac.ed.acp.cw2.Service.DroneService;
import uk.ac.ed.acp.cw2.Service.IlpClient;
import uk.ac.ed.acp.cw2.dto.Drone;
import uk.ac.ed.acp.cw2.dto.MedDispatchRec;
import uk.ac.ed.acp.cw2.dto.QueryAttribute;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class DroneController {

    private final DroneService droneService;

    @GetMapping("/dronesWithCooling/{state}")
    public List<Integer> dronesWithCooling(@PathVariable boolean state) {
        return droneService.getDronesWithCooling(state);
    }

    @GetMapping("/droneDetails/{id}")
    public ResponseEntity<Drone> droneDetails(@PathVariable int id) {
        return ResponseEntity.ok(droneService.getDroneDetails(id));
    }

    @GetMapping("/queryAsPath/{attribute}/{value}")
    public List<Integer> queryAsPath(
            @PathVariable("attribute") String attribute,
            @PathVariable("value") String value) {
        return droneService.queryAsPath(attribute, value);
    }

    @PostMapping("/query")
    public List<Integer> query(@RequestBody List<QueryAttribute> request) {
        return droneService.query(request);
    }

    @PostMapping("/queryAvailableDrones")
    public List<Integer> queryAvailableDrones(@RequestBody List<MedDispatchRec> dispatches) {
        return droneService.queryAvailableDrones(dispatches);
    }
}
