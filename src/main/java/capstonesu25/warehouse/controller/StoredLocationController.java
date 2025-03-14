package capstonesu25.warehouse.controller;

import capstonesu25.warehouse.model.storedlocation.StoredLocationRequest;
import capstonesu25.warehouse.model.storedlocation.StoredLocationResponse;
import capstonesu25.warehouse.model.responsedto.MetaDataDTO;
import capstonesu25.warehouse.service.StoredLocationService;
import capstonesu25.warehouse.utils.ResponseUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/stored-locations")
@RequiredArgsConstructor
@Validated
public class StoredLocationController {
    private final StoredLocationService storedLocationService;
    private static final Logger LOGGER = LoggerFactory.getLogger(StoredLocationController.class);

    @GetMapping
    public ResponseEntity<?> getAll(@RequestParam(defaultValue = "1") int page,
                                    @RequestParam(defaultValue = "10") int limit) {
        LOGGER.info("Getting all stored locations");
        List<StoredLocationResponse> result = storedLocationService.getAllStoredLocations(page, limit);
        return ResponseUtil.getCollection(
                result,
                HttpStatus.OK,
                "Successfully retrieved all stored locations",
                new MetaDataDTO(page < result.size(), page > 1, limit, result.size(), page)
        );
    }

    @GetMapping("/{storedLocationId}")
    public ResponseEntity<?> getById(@PathVariable Long storedLocationId) {
        LOGGER.info("Getting stored location by id: {}", storedLocationId);
        StoredLocationResponse result = storedLocationService.getStoredLocationById(storedLocationId);
        return ResponseUtil.getObject(
                result,
                HttpStatus.OK,
                "Successfully retrieved stored location"
        );
    }

    @PostMapping
    public ResponseEntity<?> createStoredLocation(@RequestBody StoredLocationRequest request) {
        LOGGER.info("Creating stored location");
        storedLocationService.create(request);
        return ResponseUtil.getObject(
                null,
                HttpStatus.CREATED,
                "Successfully created stored location"
        );
    }

    @PutMapping
    public ResponseEntity<?> updateStoredLocation(@RequestBody StoredLocationRequest request) {
        LOGGER.info("Updating stored location");
        storedLocationService.update(request);
        return ResponseUtil.getObject(
                null,
                HttpStatus.OK,
                "Successfully updated stored location"
        );
    }

    @DeleteMapping("/{storedLocationId}")
    public ResponseEntity<?> deleteStoredLocation(@PathVariable Long storedLocationId) {
        LOGGER.info("Deleting stored location");
        storedLocationService.delete(storedLocationId);
        return ResponseUtil.getObject(
                null,
                HttpStatus.OK,
                "Successfully deleted stored location"
        );
    }

    @GetMapping("/available")
    public ResponseEntity<?> getAvailableLocations(@RequestParam(defaultValue = "1") int page,
                                                  @RequestParam(defaultValue = "10") int limit) {
        LOGGER.info("Getting available stored locations");
        List<StoredLocationResponse> result = storedLocationService.getAvailableStoredLocations(page, limit);
        return ResponseUtil.getCollection(
                result,
                HttpStatus.OK,
                "Successfully retrieved available stored locations",
                new MetaDataDTO(page < result.size(), page > 1, limit, result.size(), page)
        );
    }

    @GetMapping("/zone/{zone}")
    public ResponseEntity<?> getByZone(@PathVariable String zone,
                                      @RequestParam(defaultValue = "1") int page,
                                      @RequestParam(defaultValue = "10") int limit) {
        LOGGER.info("Getting stored locations by zone: {}", zone);
        List<StoredLocationResponse> result = storedLocationService.getByZone(zone, page, limit);
        return ResponseUtil.getCollection(
                result,
                HttpStatus.OK,
                "Successfully retrieved stored locations by zone",
                new MetaDataDTO(page < result.size(), page > 1, limit, result.size(), page)
        );
    }

    @GetMapping("/floor/{floor}")
    public ResponseEntity<?> getByFloor(@PathVariable String floor,
                                       @RequestParam(defaultValue = "1") int page,
                                       @RequestParam(defaultValue = "10") int limit) {
        LOGGER.info("Getting stored locations by floor: {}", floor);
        List<StoredLocationResponse> result = storedLocationService.getByFloor(floor, page, limit);
        return ResponseUtil.getCollection(
                result,
                HttpStatus.OK,
                "Successfully retrieved stored locations by floor",
                new MetaDataDTO(page < result.size(), page > 1, limit, result.size(), page)
        );
    }
}
