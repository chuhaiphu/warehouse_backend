package capstonesu25.warehouse.service;

import capstonesu25.warehouse.entity.InventoryItem;
import capstonesu25.warehouse.entity.Item;
import capstonesu25.warehouse.entity.StoredLocation;
import capstonesu25.warehouse.model.storedlocation.StoredLocationRequest;
import capstonesu25.warehouse.model.storedlocation.StoredLocationResponse;
import capstonesu25.warehouse.repository.ItemRepository;
import capstonesu25.warehouse.repository.StoredLocationRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
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
    private final ItemRepository itemRepository;

    private static final Logger LOGGER = LoggerFactory.getLogger(StoredLocationService.class);

    public List<StoredLocationResponse> getAllStoredLocations() {
        LOGGER.info("Getting all stored locations");
        return storedLocationRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public StoredLocationResponse getStoredLocationById(Long id) {
        LOGGER.info("Getting stored location by id: {}", id);
        StoredLocation storedLocation = storedLocationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Stored location not found with id: " + id));
        return mapToResponse(storedLocation);
    }

    public Page<StoredLocationResponse> getAvailableStoredLocations(int page, int limit) {
        LOGGER.info("Getting available stored locations with page: {} and limit: {}", page, limit);
        Pageable pageable = PageRequest.of(page - 1, limit);
        return storedLocationRepository.findByIsUsedFalseAndIsFulledFalse(pageable)
                .map(this::mapToResponse);
    }

    public Page<StoredLocationResponse> getByZone(String zone, int page, int limit) {
        LOGGER.info("Getting stored locations by zone: {} with page: {} and limit: {}", zone, page, limit);
        Pageable pageable = PageRequest.of(page - 1, limit);
        return storedLocationRepository.findByZone(zone, pageable).map(this::mapToResponse);
    }

    public Page<StoredLocationResponse> getByFloor(String floor, int page, int limit) {
        LOGGER.info("Getting stored locations by floor: {} with page: {} and limit: {}", floor, page, limit);
        Pageable pageable = PageRequest.of(page - 1, limit);
        return storedLocationRepository.findByFloor(floor, pageable).map(this::mapToResponse);
    }

    public Boolean hasExistingData() {
        long count = storedLocationRepository.count();
        LOGGER.info("Found {} stored locations in database", count);
        return count > 0;
    }

    @Transactional
    public void create(List<StoredLocationRequest> requestList) {
        LOGGER.info("Creating {} stored locations", requestList.size());
        List<StoredLocation> storedLocations = new ArrayList<>();
        for(StoredLocationRequest request : requestList) {
            StoredLocation storedLocation = new StoredLocation();
            if(request.getIsDoor() == true && request.getIsRoad() == true) {
                throw new IllegalArgumentException("One location cannot being a road and door in same time");
            }
            storedLocation.setZone(request.getZone());
            storedLocation.setFloor(request.getFloor());
            storedLocation.setLine(request.getLine());
            storedLocation.setRow(request.getRow());
            storedLocation.setRoad(request.getIsRoad());
            storedLocation.setDoor(request.getIsDoor());
            storedLocation.setMaximumCapacityForItem(request.getMaximumCapacityForItem());
            if( request.getIsDoor() == false && request.getIsRoad() == false) {
                Item item = itemRepository.findById(request.getItemId()).orElseThrow(
                        () -> new IllegalArgumentException("This item with ID: " + request.getItemId() + " is not presented")
                );
                storedLocation.setItem(item);
            }
            storedLocations.add(storedLocation);
        }
        storedLocationRepository.saveAll(storedLocations);
        LOGGER.info("Successfully created {} stored locations", storedLocations.size());
    }

//    @Transactional
//    public StoredLocationResponse update(StoredLocationRequest request) {
//        LOGGER.info("Updating stored location with id: {}", request.getId());
//        if (request.getId() == null) {
//            throw new IllegalArgumentException("Stored location ID cannot be null for update operation");
//        }
//
//        StoredLocation existingLocation = storedLocationRepository.findById(request.getId())
//                .orElseThrow(() -> new EntityNotFoundException("Stored location not found with id: " + request.getId()));
//
//        updateEntityFromRequest(existingLocation, request);
//        return mapToResponse(storedLocationRepository.save(existingLocation));
//    }

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
        response.setLine(storedLocation.getLine());
        response.setRoad(storedLocation.isRoad());
        response.setDoor(storedLocation.isDoor());
        response.setUsed(storedLocation.isUsed());
        response.setFulled(storedLocation.isFulled());
        response.setMaximumCapacityForItem(storedLocation.getMaximumCapacityForItem());
        response.setCurrentCapacity(storedLocation.getCurrentCapacity());

        // Set inventory item references
        if (storedLocation.getInventoryItems() != null && !storedLocation.getInventoryItems().isEmpty()) {
            response.setInventoryItemIds(storedLocation.getInventoryItems().stream()
                    .map(InventoryItem::getId)
                    .collect(Collectors.toList()));
        } else {
            response.setInventoryItemIds(new ArrayList<>());
        }

        if(storedLocation.getItem() != null) {
            response.setItemId(storedLocation.getItem().getId());
        }

        return response;
    }

//    private StoredLocation mapToEntity(StoredLocationRequest request) {
//        StoredLocation storedLocation = new StoredLocation();
//        updateEntityFromRequest(storedLocation, request);
//        return storedLocation;
//    }

//    private void updateEntityFromRequest(StoredLocation storedLocation, StoredLocationRequest request) {
//        storedLocation.setZone(request.getZone());
//        storedLocation.setFloor(request.getFloor());
//        storedLocation.setRow(request.getRow());
//        storedLocation.setLine(request.getLine());
//        storedLocation.setUsed(request.isUsed());
//        storedLocation.setFulled(request.isFulled());
//        storedLocation.setMaximumCapacityForItem(request.getMaximumCapacityForItem());
//        storedLocation.setCurrentCapacity(request.getCurrentCapacity());
//
//        if (request.getInventoryItemIds() != null && !request.getInventoryItemIds().isEmpty()) {
//            List<InventoryItem> inventoryItems = request.getInventoryItemIds().stream()
//                    .map(id -> inventoryItemRepository.findById(id)
//                            .orElseThrow(() -> new EntityNotFoundException("Inventory item not found with id: " + id)))
//                    .collect(Collectors.toList());
//            storedLocation.setInventoryItems(inventoryItems);
//        } else {
//            storedLocation.setInventoryItems(new ArrayList<>());
//        }
//        if(request.getItemId() != null) {
//            storedLocation.setItem(itemRepository.findById(request.getItemId())
//                    .orElseThrow(() -> new EntityNotFoundException("Item not found with id: " + request.getItemId())));
//        }
//    }
}
