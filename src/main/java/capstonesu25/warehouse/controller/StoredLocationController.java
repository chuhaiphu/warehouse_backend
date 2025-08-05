package capstonesu25.warehouse.controller;

import capstonesu25.warehouse.model.storedlocation.StoredLocationRequest;
import capstonesu25.warehouse.model.storedlocation.StoredLocationResponse;
import capstonesu25.warehouse.model.responsedto.MetaDataDTO;
import capstonesu25.warehouse.service.StoredLocationService;
import capstonesu25.warehouse.utils.ResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/stored-location")
@RequiredArgsConstructor
@Validated
public class StoredLocationController {
    private final StoredLocationService storedLocationService;
    private static final Logger LOGGER = LoggerFactory.getLogger(StoredLocationController.class);

    @Operation(summary = "Get all stored locations with pagination", description = "Returns a list of all stored locations")
    @GetMapping
    
    public ResponseEntity<?> getAll() {
        LOGGER.info("Getting all stored locations");
        List<StoredLocationResponse> result = storedLocationService.getAllStoredLocations();
        return ResponseUtil.getCollection(
                result,
                HttpStatus.OK,
                "Successfully get all stored locations with pagination",
                null);
    }

    @Operation(summary = "Get stored location by ID", description = "Returns a stored location by its ID")
    @GetMapping("/{storedLocationId}")
    
    public ResponseEntity<?> getById(@PathVariable Long storedLocationId) {
        LOGGER.info("Getting stored location by id: {}", storedLocationId);
        StoredLocationResponse result = storedLocationService.getStoredLocationById(storedLocationId);
        return ResponseUtil.getObject(
                result,
                HttpStatus.OK,
                "Successfully retrieved stored location");
    }

    @Operation(summary = "Create a new stored location", description = "Creates a new stored location in the system")
    @PostMapping
    public ResponseEntity<?> createStoredLocation(@RequestBody List<StoredLocationRequest> request) {
        LOGGER.info("Creating stored location");
        storedLocationService.create(request);
        return ResponseUtil.getObject(
                null,
                HttpStatus.CREATED,
                "Successfully created stored location");
    }

    @Operation(summary = "Get available stored locations", description = "Returns a list of all available stored locations")
    @GetMapping("/available")
    public ResponseEntity<?> getAvailableLocations(@RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit) {
        LOGGER.info("Getting available stored locations");
        Page<StoredLocationResponse> result = storedLocationService.getAvailableStoredLocations(page, limit);
        return ResponseUtil.getCollection(
                result.getContent(),
                HttpStatus.OK,
                "Successfully get available stored locations",
                new MetaDataDTO(
                        result.hasNext(),
                        result.hasPrevious(),
                        limit,
                        (int) result.getTotalElements(),
                        page));
    }

    @Operation(summary = "Get stored locations by zone", description = "Returns a list of stored locations filtered by zone")
    @GetMapping("/zone/{zone}")
    public ResponseEntity<?> getByZone(@PathVariable String zone,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit) {
        LOGGER.info("Getting stored locations by zone: {}", zone);
        Page<StoredLocationResponse> result = storedLocationService.getByZone(zone, page, limit);
        return ResponseUtil.getCollection(
                result.getContent(),
                HttpStatus.OK,
                "Successfully get stored locations by zone",
                new MetaDataDTO(
                        result.hasNext(),
                        result.hasPrevious(),
                        limit,
                        (int) result.getTotalElements(),
                        page));
    }

    @Operation(summary = "Get stored locations by floor", description = "Returns a list of stored locations filtered by floor")
    @GetMapping("/floor/{floor}")
    public ResponseEntity<?> getByFloor(@PathVariable String floor,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit) {
        LOGGER.info("Getting stored locations by floor: {}", floor);
        Page<StoredLocationResponse> result = storedLocationService.getByFloor(floor, page, limit);
        return ResponseUtil.getCollection(
                result.getContent(),
                HttpStatus.OK,
                "Successfully get stored locations by floor",
                new MetaDataDTO(
                        result.hasNext(),
                        result.hasPrevious(),
                        limit,
                        (int) result.getTotalElements(),
                        page));
    }

//    @Operation(summary = "Update an existing stored location", description = "Updates an existing stored location's information")
//    @PutMapping
//    public ResponseEntity<?> updateStoredLocation(@RequestBody StoredLocationRequest request) {
//        LOGGER.info("Updating stored location");
//        return ResponseUtil.getObject(
//                storedLocationService.update(request),
//                HttpStatus.OK,
//                "Successfully updated stored location");
//    }

    @GetMapping("/suggest-locations")
    @Operation(summary = "Suggest stored locations", description = "Returns a list of stored locations that are not full and not used")
    public ResponseEntity<?> suggestLocations(@RequestParam String itemId, @RequestParam Long locationId) {
        LOGGER.info("Suggesting stored locations");
        List<StoredLocationResponse> result = storedLocationService.suggestNearestStoredLocations(itemId, locationId);
        return ResponseUtil.getCollection(
                result,
                HttpStatus.OK,
                "Successfully suggested stored locations",
                null);
    }

    @Operation(summary = "Delete a stored location by ID", description = "Removes a stored location from the system")
    @DeleteMapping("/{storedLocationId}")
    public ResponseEntity<?> deleteStoredLocation(@PathVariable Long storedLocationId) {
        LOGGER.info("Deleting stored location");
        storedLocationService.delete(storedLocationId);
        return ResponseUtil.getObject(
                null,
                HttpStatus.OK,
                "Successfully deleted stored location");
    }

}
