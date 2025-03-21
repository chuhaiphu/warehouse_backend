package capstonesu25.warehouse.service;

import capstonesu25.warehouse.entity.InventoryItem;
import capstonesu25.warehouse.entity.StoredLocation;
import capstonesu25.warehouse.model.storedlocation.StoredLocationRequest;
import capstonesu25.warehouse.model.storedlocation.StoredLocationResponse;
import capstonesu25.warehouse.repository.InventoryItemRepository;
import capstonesu25.warehouse.repository.StoredLocationRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StoredLocationService {
    private final StoredLocationRepository storedLocationRepository;
    private final InventoryItemRepository inventoryItemRepository;

    private static final Logger LOGGER = LoggerFactory.getLogger(StoredLocationService.class);

    public List<StoredLocationResponse> getAllStoredLocations(int page, int limit) {
        LOGGER.info("Getting all stored locations with page: {} and limit: {}", page, limit);
        Pageable pageable = PageRequest.of(page - 1, limit);
        return storedLocationRepository.findAll(pageable).getContent()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public StoredLocationResponse getStoredLocationById(Long id) {
        LOGGER.info("Getting stored location by id: {}", id);
        StoredLocation storedLocation = storedLocationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Stored location not found with id: " + id));
        return mapToResponse(storedLocation);
    }

    public List<StoredLocationResponse> getAvailableStoredLocations(int page, int limit) {
        LOGGER.info("Getting available stored locations with page: {} and limit: {}", page, limit);
        Pageable pageable = PageRequest.of(page - 1, limit);
        return storedLocationRepository.findByIsUsedFalseAndIsFulledFalse(pageable).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<StoredLocationResponse> getByZone(String zone, int page, int limit) {
        LOGGER.info("Getting stored locations by zone: {} with page: {} and limit: {}", zone, page, limit);
        Pageable pageable = PageRequest.of(page - 1, limit);
        return storedLocationRepository.findByZone(zone, pageable).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<StoredLocationResponse> getByFloor(String floor, int page, int limit) {
        LOGGER.info("Getting stored locations by floor: {} with page: {} and limit: {}", floor, page, limit);
        Pageable pageable = PageRequest.of(page - 1, limit);
        return storedLocationRepository.findByFloor(floor, pageable).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public StoredLocationResponse create(StoredLocationRequest request) {
        LOGGER.info("Creating stored location");
        StoredLocation storedLocation = mapToEntity(request);
        return mapToResponse(storedLocationRepository.save(storedLocation));
    }

    @Transactional
    public StoredLocationResponse update(StoredLocationRequest request) {
        LOGGER.info("Updating stored location with id: {}", request.getId());
        if (request.getId() == null) {
            throw new IllegalArgumentException("Stored location ID cannot be null for update operation");
        }

        StoredLocation existingLocation = storedLocationRepository.findById(request.getId())
                .orElseThrow(() -> new EntityNotFoundException("Stored location not found with id: " + request.getId()));

        updateEntityFromRequest(existingLocation, request);
        return mapToResponse(storedLocationRepository.save(existingLocation));
    }

    @Transactional
    public void delete(Long id) {
        LOGGER.info("Deleting stored location with id: {}", id);
        if (!storedLocationRepository.existsById(id)) {
            throw new EntityNotFoundException("Stored location not found with id: " + id);
        }
        storedLocationRepository.deleteById(id);
    }

    private StoredLocationResponse mapToResponse(StoredLocation storedLocation) {
        StoredLocationResponse response = new StoredLocationResponse();
        response.setId(storedLocation.getId());
        response.setZone(storedLocation.getZone());
        response.setFloor(storedLocation.getFloor());
        response.setRow(storedLocation.getRow());
        response.setBatch(storedLocation.getBatch());
        response.setUsed(storedLocation.isUsed());
        response.setFulled(storedLocation.isFulled());

        // Set inventory item references
        if (storedLocation.getInventoryItems() != null && !storedLocation.getInventoryItems().isEmpty()) {
            response.setInventoryItemIds(storedLocation.getInventoryItems().stream()
                    .map(InventoryItem::getId)
                    .collect(Collectors.toList()));
        } else {
            response.setInventoryItemIds(new ArrayList<>());
        }

        return response;
    }

    private StoredLocation mapToEntity(StoredLocationRequest request) {
        StoredLocation storedLocation = new StoredLocation();
        updateEntityFromRequest(storedLocation, request);
        return storedLocation;
    }

    private void updateEntityFromRequest(StoredLocation storedLocation, StoredLocationRequest request) {
        storedLocation.setZone(request.getZone());
        storedLocation.setFloor(request.getFloor());
        storedLocation.setRow(request.getRow());
        storedLocation.setBatch(request.getBatch());
        storedLocation.setUsed(request.isUsed());
        storedLocation.setFulled(request.isFulled());

        // Update inventory items if provided
        if (request.getInventoryItemIds() != null && !request.getInventoryItemIds().isEmpty()) {
            List<InventoryItem> inventoryItems = request.getInventoryItemIds().stream()
                    .map(id -> inventoryItemRepository.findById(id)
                            .orElseThrow(() -> new EntityNotFoundException("Inventory item not found with id: " + id)))
                    .collect(Collectors.toList());
            storedLocation.setInventoryItems(inventoryItems);
        } else {
            storedLocation.setInventoryItems(new ArrayList<>());
        }
    }
}
