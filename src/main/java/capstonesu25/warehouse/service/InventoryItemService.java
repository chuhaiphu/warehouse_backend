package capstonesu25.warehouse.service;

import capstonesu25.warehouse.entity.InventoryItem;
import capstonesu25.warehouse.entity.Item;
import capstonesu25.warehouse.entity.StoredLocation;
import capstonesu25.warehouse.model.inventoryitem.InventoryItemRequest;
import capstonesu25.warehouse.model.inventoryitem.InventoryItemResponse;
import capstonesu25.warehouse.model.inventoryitem.QrCodeResponse;
import capstonesu25.warehouse.repository.InventoryItemRepository;
import capstonesu25.warehouse.repository.ItemRepository;
import capstonesu25.warehouse.repository.ExportRequestDetailRepository;
import capstonesu25.warehouse.repository.ImportOrderDetailRepository;
import capstonesu25.warehouse.repository.StoredLocationRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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

    public List<InventoryItemResponse> getAllInventoryItems(int page, int limit) {
        LOGGER.info("Getting all inventory items with page: {} and limit: {}", page, limit);
        Pageable pageable = PageRequest.of(page - 1, limit);
        return inventoryItemRepository.findAll(pageable).getContent()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public InventoryItemResponse getInventoryItemById(Long id) {
        LOGGER.info("Getting inventory item by id: {}", id);
        InventoryItem inventoryItem = inventoryItemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Inventory item not found with id: " + id));
        return mapToResponse(inventoryItem);
    }

    @Transactional
    public List<QrCodeResponse>  create(InventoryItemRequest request) {
        LOGGER.info("Creating inventory item");

        List<QrCodeResponse> qrCodes = new ArrayList<>();

        for (int i = 0; i < request.getNumberOfItems(); i++) {
        InventoryItem inventoryItem = mapToEntity(request);
        inventoryItem.setImportedDate(LocalDateTime.now());
        inventoryItem.setUpdatedDate(LocalDateTime.now());
        InventoryItem savedItem = inventoryItemRepository.save(inventoryItem);
            QrCodeResponse qrCode = new QrCodeResponse(
                    savedItem.getId(),
                    savedItem.getItem().getId(),
                    savedItem.getImportOrderDetail().getId(),
                    savedItem.getExportRequestDetail().getId(),
                    savedItem.getStoredLocation().getId()
            );
            qrCodes.add(qrCode);

        }

        return qrCodes;
    }

    @Transactional
    public void update(InventoryItemRequest request) {
        LOGGER.info("Updating inventory item with id: {}", request.getId());
        if (request.getId() == null) {
            throw new IllegalArgumentException("Inventory item ID cannot be null for update operation");
        }

        InventoryItem existingItem = inventoryItemRepository.findById(request.getId())
                .orElseThrow(() -> new EntityNotFoundException("Inventory item not found with id: " + request.getId()));

        existingItem = updateEntityFromRequest(existingItem, request);
        existingItem.setUpdatedDate(LocalDateTime.now());
        inventoryItemRepository.save(existingItem);
    }

    @Transactional
    public void delete(Long id) {
        LOGGER.info("Deleting inventory item with id: {}", id);
        if (!inventoryItemRepository.existsById(id)) {
            throw new EntityNotFoundException("Inventory item not found with id: " + id);
        }
        inventoryItemRepository.deleteById(id);
    }

    public List<InventoryItemResponse> getByImportOrderDetailId(Long importOrderDetailId, int page, int limit) {
        LOGGER.info("Getting inventory items by import order detail id: {}", importOrderDetailId);
        Pageable pageable = PageRequest.of(page - 1, limit);
        return inventoryItemRepository.findByImportOrderDetailId(importOrderDetailId, pageable)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<InventoryItemResponse> getByExportRequestDetailId(Long exportRequestDetailId, int page, int limit) {
        LOGGER.info("Getting inventory items by export request detail id: {}", exportRequestDetailId);
        Pageable pageable = PageRequest.of(page - 1, limit);
        return inventoryItemRepository.findByExportRequestDetailId(exportRequestDetailId, pageable)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<InventoryItemResponse> getByStoredLocationId(Long storedLocationId, int page, int limit) {
        LOGGER.info("Getting inventory items by stored location id: {}", storedLocationId);
        Pageable pageable = PageRequest.of(page - 1, limit);
        return inventoryItemRepository.findByStoredLocationId(storedLocationId, pageable)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private InventoryItemResponse mapToResponse(InventoryItem inventoryItem) {
        InventoryItemResponse response = new InventoryItemResponse();
        response.setId(inventoryItem.getId());
        response.setReasonForDisposal(inventoryItem.getReasonForDisposal());
        response.setQuantity(inventoryItem.getQuantity());
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
            String locationName = String.format("Zone: %s, Floor: %s, Row: %s, Batch: %s",
                    inventoryItem.getStoredLocation().getZone(),
                    inventoryItem.getStoredLocation().getFloor(),
                    inventoryItem.getStoredLocation().getRow(),
                    inventoryItem.getStoredLocation().getBatch());
            response.setStoredLocationName(locationName);
        }

        return response;
    }

    private InventoryItem mapToEntity(InventoryItemRequest request) {
        InventoryItem inventoryItem = new InventoryItem();
        return   updateEntityFromRequest(inventoryItem, request);
    }

    private InventoryItem updateEntityFromRequest(InventoryItem inventoryItem, InventoryItemRequest request) {
        inventoryItem.setReasonForDisposal(request.getReasonForDisposal());
        inventoryItem.setQuantity(request.getQuantity());
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
            inventoryItem.setExportRequestDetail(exportRequestDetailRepository.findById(request.getExportRequestDetailId())
                    .orElseThrow(() -> new EntityNotFoundException("Export request detail not found with id: " + request.getExportRequestDetailId())));
        } else {
            inventoryItem.setExportRequestDetail(null);
        }

        // Set import order detail reference
        if (request.getImportOrderDetailId() != null) {
            inventoryItem.setImportOrderDetail(importOrderDetailRepository.findById(request.getImportOrderDetailId())
                    .orElseThrow(() -> new EntityNotFoundException("Import order detail not found with id: " + request.getImportOrderDetailId())));
        } else {
            inventoryItem.setImportOrderDetail(null);
        }

        // Set stored location reference
        if (request.getStoredLocationId() != null) {
            StoredLocation storedLocation = storedLocationRepository.findById(request.getStoredLocationId())
                    .orElseThrow(() -> new EntityNotFoundException("Stored location not found with id: " + request.getStoredLocationId()));
            inventoryItem.setStoredLocation(storedLocation);
        } else {
            inventoryItem.setStoredLocation(null);
        }
        return inventoryItem;
    }
}
