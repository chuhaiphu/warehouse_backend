package capstonesu25.warehouse.service;

import capstonesu25.warehouse.entity.*;
import capstonesu25.warehouse.enums.ItemStatus;
import capstonesu25.warehouse.model.inventoryitem.ChangeInventoryItemOfExportDetailRequest;
import capstonesu25.warehouse.model.inventoryitem.InventoryItemRequest;
import capstonesu25.warehouse.model.inventoryitem.InventoryItemResponse;
import capstonesu25.warehouse.model.inventoryitem.UpdateInventoryLocationRequest;
import capstonesu25.warehouse.repository.InventoryItemRepository;
import capstonesu25.warehouse.repository.ItemRepository;
import capstonesu25.warehouse.repository.ExportRequestDetailRepository;
import capstonesu25.warehouse.repository.ImportOrderDetailRepository;
import capstonesu25.warehouse.repository.StoredLocationRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InventoryItemService {
    private final InventoryItemRepository inventoryItemRepository;
    private final ItemRepository itemRepository;
    private final ExportRequestDetailRepository exportRequestDetailRepository;
    private final ImportOrderDetailRepository importOrderDetailRepository;
    private final StoredLocationRepository storedLocationRepository;

    private static final Logger LOGGER = LoggerFactory.getLogger(InventoryItemService.class);

    public Page<InventoryItemResponse> getAllInventoryItems(int page, int limit) {
        LOGGER.info("Getting all inventory items with page: {} and limit: {}", page, limit);
        Pageable pageable = PageRequest.of(page - 1, limit);
        return inventoryItemRepository.findAll(pageable)
                .map(this::mapToResponse);
    }

    public InventoryItemResponse getInventoryItemById(String id) {
        LOGGER.info("Getting inventory item by id: {}", id);
        InventoryItem inventoryItem = inventoryItemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Inventory item not found with id: " + id));
        return mapToResponse(inventoryItem);
    }

    public Page<InventoryItemResponse> getAllNeedToReturnByItemId(String itemId, int page, int limit) {
        LOGGER.info("Getting all inventory items that need to return by item id: {}", itemId);
        Pageable pageable = PageRequest.of(page - 1, limit);

        Page<InventoryItem> inventoryItemsPage = inventoryItemRepository
                .findByItem_IdAndParentNullAndStatusAndNeedReturnToProvider(itemId, ItemStatus.AVAILABLE, true, pageable);

        List<InventoryItemResponse> filteredList = inventoryItemsPage
                .stream()
                .filter(inventoryItem -> inventoryItem.getExportRequestDetail() == null)
                .map(this::mapToResponse)
                .toList();

        return new PageImpl<>(filteredList, pageable, filteredList.size());
    }

    @Transactional
    public InventoryItemResponse update(InventoryItemRequest request) {
        LOGGER.info("Updating inventory item with id: {}", request.getId());
        if (request.getId() == null) {
            throw new IllegalArgumentException("Inventory item ID cannot be null for update operation");
        }

        InventoryItem existingItem = inventoryItemRepository.findById(request.getId())
                .orElseThrow(() -> new EntityNotFoundException("Inventory item not found with id: " + request.getId()));

        updateEntityFromRequest(existingItem, request);
        existingItem.setUpdatedDate(LocalDateTime.now());
        return mapToResponse(inventoryItemRepository.save(existingItem));
    }

    public Page<InventoryItemResponse> getByImportOrderDetailId(Long importOrderDetailId, int page, int limit) {
        LOGGER.info("Getting inventory items by import order detail id: {}", importOrderDetailId);
        Pageable pageable = PageRequest.of(page - 1, limit);
        return inventoryItemRepository.findByImportOrderDetailId(importOrderDetailId, pageable)
                .map(this::mapToResponse);
    }

    public List<InventoryItemResponse> getByListImportOrderDetailIds(List<Long> importOrderDetailIds) {
        LOGGER.info("Getting inventory items by list import order detail ids: {}", importOrderDetailIds);
        List<InventoryItem> inventoryItems = inventoryItemRepository.findByImportOrderDetailIdIn(importOrderDetailIds);
        return inventoryItems.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }


    public Page<InventoryItemResponse> getByExportRequestDetailId(Long exportRequestDetailId, int page, int limit) {
        LOGGER.info("Getting inventory items by export request detail id: {}", exportRequestDetailId);
        Pageable pageable = PageRequest.of(page - 1, limit);
        Page<InventoryItem> inventoryItems = inventoryItemRepository.findByExportRequestDetailId(exportRequestDetailId, pageable);
        return inventoryItems.map(this::mapToResponse);
    }

    public Page<InventoryItemResponse> getByStoredLocationId(Long storedLocationId, int page, int limit) {
        LOGGER.info("Getting inventory items by stored location id: {}", storedLocationId);
        Pageable pageable = PageRequest.of(page - 1, limit);
        return inventoryItemRepository.findByStoredLocationId(storedLocationId, pageable)
                .map(this::mapToResponse);
    }

    public List<InventoryItemResponse> getListQrCodes(List<String> inventoryItemIds) {
        LOGGER.info("Getting QR codes by inventory item IDs: {}", inventoryItemIds);
        List<InventoryItem> inventoryItems = inventoryItemRepository.findAllById(inventoryItemIds);
        
        return inventoryItems.stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    @Transactional
    public List<InventoryItemResponse> updateStoredLocation(List<UpdateInventoryLocationRequest> requests) {
        LOGGER.info("Updating stored location of inventory items");

        Map<String, InventoryItem> inventoryItemMap = new HashMap<>();
        Map<Long, StoredLocation> storedLocationMap = new HashMap<>();
        Map<Long, Integer> locationDelta = new HashMap<>(); // Track capacity changes

        // Validation phase
        for (UpdateInventoryLocationRequest request : requests) {
            InventoryItem inventoryItem = inventoryItemRepository.findById(request.getInventoryItemId())
                    .orElseThrow(() -> new EntityNotFoundException("Inventory item not found with id: " + request.getInventoryItemId()));
            inventoryItemMap.put(request.getInventoryItemId(), inventoryItem);

            StoredLocation storedLocation = storedLocationRepository.findById(request.getStoredLocationId())
                    .orElseThrow(() -> new EntityNotFoundException("Stored location not found with id: " + request.getStoredLocationId()));
            storedLocationMap.put(request.getStoredLocationId(), storedLocation);

            locationDelta.put(request.getStoredLocationId(), locationDelta.getOrDefault(request.getStoredLocationId(), 0) + 1);
        }

        //validate that new locations won’t exceed max capacity
        for (Map.Entry<Long, Integer> entry : locationDelta.entrySet()) {
            StoredLocation loc = storedLocationMap.get(entry.getKey());
            int newCapacity = loc.getCurrentCapacity() + entry.getValue();
            if (loc.getMaximumCapacityForItem() != null && newCapacity > loc.getMaximumCapacityForItem()) {
                throw new IllegalStateException("Stored location ID " + loc.getId() + " exceeds its max capacity");
            }
        }

        List<InventoryItemResponse> updatedItems = new ArrayList<>();

        for (UpdateInventoryLocationRequest request : requests) {
            InventoryItem inventoryItem = inventoryItemMap.get(request.getInventoryItemId());
            StoredLocation newLocation = storedLocationMap.get(request.getStoredLocationId());

            StoredLocation oldLocation = inventoryItem.getStoredLocation();
            if (oldLocation != null && !oldLocation.getId().equals(newLocation.getId())) {
                oldLocation.setCurrentCapacity(oldLocation.getCurrentCapacity() - 1);
                storedLocationRepository.save(oldLocation);
            }

            inventoryItem.setStoredLocation(newLocation);
            newLocation.setCurrentCapacity(newLocation.getCurrentCapacity() + 1);

            InventoryItem savedItem = inventoryItemRepository.save(inventoryItem);
            updatedItems.add(mapToResponse(savedItem));
        }

        storedLocationRepository.saveAll(storedLocationMap.values());

        return updatedItems;
    }
    @Transactional
    public InventoryItemResponse changeInventoryItemOfExportDetail(ChangeInventoryItemOfExportDetailRequest request) {
        InventoryItem oldItem = inventoryItemRepository.findById(request.getOldInventoryItemId())
                .orElseThrow(() -> new EntityNotFoundException("Old inventory item not found with id: " + request.getOldInventoryItemId()));
        InventoryItem newItem = inventoryItemRepository.findById(request.getNewInventoryItemId())
                .orElseThrow(() -> new EntityNotFoundException("New inventory item not found with id: " + request.getNewInventoryItemId()));

        if(oldItem.getExportRequestDetail() == null) {
            throw new IllegalArgumentException("Old inventory item does not have an export request detail");
        }
        if (newItem.getExportRequestDetail() != null) {
            throw new IllegalArgumentException("New inventory item already has an export request detail");
        }

        if (!oldItem.getItem().getId().equals(newItem.getItem().getId())) {
            throw new IllegalArgumentException("Old and new inventory items are not of the same item type");
        }


        newItem.setExportRequestDetail(oldItem.getExportRequestDetail());
        newItem.setStatus(oldItem.getStatus());
        InventoryItem savedNewItem = inventoryItemRepository.save(newItem);
        LOGGER.info("check step");
        oldItem.setExportRequestDetail(null);
        oldItem.setStatus(ItemStatus.AVAILABLE);

        inventoryItemRepository.save(oldItem);

        return mapToResponse(savedNewItem);
    }

    public InventoryItemResponse autoChangeInventoryItem(String inventoryItemId) {
        InventoryItem oldItem = inventoryItemRepository.findById(inventoryItemId)
                .orElseThrow(() -> new EntityNotFoundException("Old inventory item not found with id: " + inventoryItemId));
        List<InventoryItem> inventoryItems = inventoryItemRepository.findByItem_Id(oldItem.getItem().getId());
        InventoryItem newItem = inventoryItems.stream()
                .filter(i -> !i.getId().equals(oldItem.getId())) // không trùng với old
                .filter(i -> Double.compare(i.getMeasurementValue(), oldItem.getMeasurementValue()) == 0)
                .filter(i -> i.getExportRequestDetail() == null)
                .filter(i -> i.getStatus() == ItemStatus.AVAILABLE)
                .max(Comparator.comparing(i -> i.getImportOrderDetail().getImportOrder().getDateReceived()))
                .orElseThrow(() -> new NoSuchElementException("No matching inventory item found"));
        newItem.setExportRequestDetail(oldItem.getExportRequestDetail());
        newItem.setStatus(ItemStatus.UNAVAILABLE);
        InventoryItem savedNewItem = inventoryItemRepository.save(newItem);

        oldItem.setExportRequestDetail(null);
        oldItem.setStatus(ItemStatus.NEED_LIQUID);

        inventoryItemRepository.save(oldItem);

        return mapToResponse(savedNewItem);
    }

    private InventoryItemResponse mapToResponse(InventoryItem inventoryItem) {
        InventoryItemResponse response = new InventoryItemResponse();
        response.setId(inventoryItem.getId());
        response.setReasonForDisposal(inventoryItem.getReasonForDisposal());
        response.setMeasurementValue(inventoryItem.getMeasurementValue());
        response.setStatus(inventoryItem.getStatus());
        response.setExpiredDate(inventoryItem.getExpiredDate());
        response.setImportedDate(inventoryItem.getImportedDate());
        response.setUpdatedDate(inventoryItem.getUpdatedDate());

        // Parent reference
        if (inventoryItem.getParent() != null) {
            response.setParentId(inventoryItem.getParent().getId());
        }

        // Children references
        if (inventoryItem.getChildren() != null && !inventoryItem.getChildren().isEmpty()) {
            response.setChildrenIds(inventoryItem.getChildren().stream()
                    .map(InventoryItem::getId)
                    .collect(Collectors.toList()));
        } else {
            response.setChildrenIds(new ArrayList<>());
        }

        // Item information
        if (inventoryItem.getItem() != null) {
            response.setItemId(inventoryItem.getItem().getId());
            response.setItemName(inventoryItem.getItem().getName());
            // Item doesn't have code field based on the provided entity
        }

        // Export request detail reference
        if (inventoryItem.getExportRequestDetail() != null) {
            response.setExportRequestDetailId(inventoryItem.getExportRequestDetail().getId());
        }

        // Import order detail reference
        if (inventoryItem.getImportOrderDetail() != null) {
            response.setImportOrderDetailId(inventoryItem.getImportOrderDetail().getId());
        }

        // Stored location information
        if (inventoryItem.getStoredLocation() != null) {
            response.setStoredLocationId(inventoryItem.getStoredLocation().getId());
            // Create a formatted location string since StoredLocation doesn't have a name field
            String locationName = String.format("Zone: %s, Floor: %s, Row: %s, Line: %s",
                    inventoryItem.getStoredLocation().getZone(),
                    inventoryItem.getStoredLocation().getFloor(),
                    inventoryItem.getStoredLocation().getRow(),
                    inventoryItem.getStoredLocation().getLine());
            response.setStoredLocationName(locationName);
        }

        return response;
    }

    private InventoryItem updateEntityFromRequest(InventoryItem inventoryItem, InventoryItemRequest request) {
        LOGGER.info("convert to inventory item entity from request: {}", request);
        inventoryItem.setReasonForDisposal(request.getReasonForDisposal() != null ? request.getReasonForDisposal() : null);
        if (request.getMeasurementValue() != null) {
            inventoryItem.setMeasurementValue(Double.valueOf(request.getMeasurementValue()));
        } else {
            inventoryItem.setMeasurementValue(null);
        }
        if(request.getStatus() != null){
            inventoryItem.setStatus(request.getStatus());
        }

        // Set parent reference if provided
        if (request.getParentId() != null) {
            inventoryItem.setParent(inventoryItemRepository.findById(request.getParentId())
                    .orElseThrow(() -> new EntityNotFoundException("Parent inventory item not found with id: " + request.getParentId())));
        } else {
            inventoryItem.setParent(null);
        }

        // Set item reference
        if (request.getItemId() != null) {
            Item item = itemRepository.findById(request.getItemId())
                    .orElseThrow(() -> new EntityNotFoundException("Item not found with id: " + request.getItemId()));
            inventoryItem.setItem(item);
            if (item.getDaysUntilDue() != null) {
                inventoryItem.setExpiredDate(inventoryItem.getImportedDate().plusDays(item.getDaysUntilDue()));
            }
        }

        // Set export request detail reference
        if (request.getExportRequestDetailId() != null) {
            ExportRequestDetail exportRequestDetail = exportRequestDetailRepository.findById(request.getExportRequestDetailId())
                    .orElseThrow(() -> new EntityNotFoundException("Export request detail not found with id: " + request.getExportRequestDetailId()));

            inventoryItem.setExportRequestDetail(exportRequestDetail);
            inventoryItemRepository.save(inventoryItem);
        }


        // Set import order detail reference
        if (request.getImportOrderDetailId() != null) {
            ImportOrderDetail importOrderDetail = importOrderDetailRepository.findById(request.getImportOrderDetailId())
                    .orElseThrow(() -> new EntityNotFoundException("Import order detail not found with id: " + request.getImportOrderDetailId()));

            inventoryItem.setImportedDate(LocalDateTime.of(importOrderDetail.getImportOrder().getDateReceived(),
                    importOrderDetail.getImportOrder().getTimeReceived()));
            inventoryItem.setImportOrderDetail(importOrderDetail);
            inventoryItem.setUpdatedDate(LocalDateTime.now());
        } else {
            inventoryItem.setImportOrderDetail(null);
        }

        if (request.getStoredLocationId() != null) {
            StoredLocation storedLocation = storedLocationRepository.findById(request.getStoredLocationId())
                    .orElseThrow(() -> new EntityNotFoundException("Stored location not found with id: " + request.getStoredLocationId()));
            inventoryItem.setStoredLocation(storedLocation);
        } else {
            inventoryItem.setStoredLocation(null);
        }
        LOGGER.info("Finish convert to inventory item entity from request: {}", request);
        return inventoryItem;
    }
}
