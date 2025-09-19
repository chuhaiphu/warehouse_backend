package capstonesu25.warehouse.service;

import capstonesu25.warehouse.entity.*;
import capstonesu25.warehouse.enums.ItemStatus;
import capstonesu25.warehouse.model.inventoryitem.*;
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

    public List<InventoryItemResponse> getAllInventoryItems() {
        LOGGER.info("Getting all inventory items ");
        return inventoryItemRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

//    @Transactional(readOnly = true)
//    public List<InventoryItemResponse> getInventoryItemHistory(String id) {
//        LOGGER.info("Getting inventory item history with id: {}", id);
//
//        // 1. Tìm item hiện tại
//        InventoryItem current = inventoryItemRepository.findById(id)
//                .orElseThrow(() -> new EntityNotFoundException("Inventory item not found with id: " + id));
//
//        // 2. Tìm node root (cha xa nhất)
//        InventoryItem root = current;
//        while (root.getParent() != null) {
//            root = root.getParent();
//        }
//
//        // 3. Duyệt preorder để gom list từ cha → con
//        List<InventoryItemResponse> result = new ArrayList<>();
//        preorderFlatten(root, result);
//        return result;
//    }

    @Transactional(readOnly = true)
    public List<InventoryItemResponse> getInventoryItemHistory(String id) {
        LOGGER.info("Getting inventory item history (child → parent) with id: {}", id);

        // 1. Lấy item hiện tại
        InventoryItem current = inventoryItemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Inventory item not found with id: " + id));

        // 2. Duyệt ngược từ con lên cha và map sang DTO
        List<InventoryItemResponse> history = new ArrayList<>();
        for (InventoryItem node = current; node != null; node = node.getParent()) {
            history.add(mapToResponse(node));
        }

        return history; // kết quả theo thứ tự: con → ... → gốc
    }

    @Transactional(readOnly = true)
    public List<InventoryItemResponse> searchByScarfId(String id) {
        LOGGER.info("Searching inventory item");
        return inventoryItemRepository.findByIdContaining(id).stream().map(this::mapToResponse).toList();
    }


    private void preorderFlatten(InventoryItem entity, List<InventoryItemResponse> acc) {
        // Map sang DTO bằng mapper có sẵn
        InventoryItemResponse response = mapToResponse(entity);
        acc.add(response);

        // Lấy danh sách con từ entity (nếu đã có quan hệ children)
        if (entity.getChildren() != null && !entity.getChildren().isEmpty()) {
            for (InventoryItem child : entity.getChildren()) {
                preorderFlatten(child, acc);
            }
        } else {
            // fallback: query repo nếu bạn không bật fetch children trong entity
            List<InventoryItem> children = inventoryItemRepository.findInventoryItemByParent_Id(entity.getId());
            for (InventoryItem child : children) {
                preorderFlatten(child, acc);
            }
        }
    }


    public Page<InventoryItemResponse> getAllInventoryItemsByItemId(String itemId,int page, int limit) {
        LOGGER.info("Getting all inventory items by item id: {}", itemId);
        Pageable pageable = PageRequest.of(page - 1, limit);
        return inventoryItemRepository.findByItem_Id(itemId, pageable)
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

    @Transactional(readOnly = true)
    public List<InventoryFigure> getInventoryItemsFigure() {
        LOGGER.info("Getting inventory items figure");

        List<Item> items = itemRepository.findAll();
        List<InventoryFigure> figures = new ArrayList<>(items.size());

        for (Item item : items) {
            List<InventoryItem> list = inventoryItemRepository.findByItem_Id(item.getId());

            long available = list.stream()
                    // available = free & AVAILABLE (tweak if your rule differs)
                    .filter(ii -> ii.getStatus() == ItemStatus.AVAILABLE )
                    .count();

            long needLiquid = list.stream()
                    // define your own rule here:
                    // e.g. statuses that imply liquidation OR any custom predicate like expiry, damaged, etc.
                    .filter(ii -> ii.getStatus() == ItemStatus.NEED_LIQUID)
                    .count();

            long unavailable = list.stream()
                    // define your own rule here:
                    // e.g. statuses that imply liquidation OR any custom predicate like expiry, damaged, etc.
                    .filter(ii -> ii.getStatus() == ItemStatus.UNAVAILABLE)
                    .count();

            long readyToStore = list.stream()
                    .filter(ii -> ii.getStatus() == ItemStatus.READY_TO_STORE)
                    .count();

            long noLongerExist = list.stream()
                    .filter(ii -> ii.getStatus() == ItemStatus.NO_LONGER_EXIST)
                    .count();

            figures.add(new InventoryFigure(
                    item.getId(),
                    (int) available,
                    (int) unavailable,
                    (int) needLiquid,
                    (int) readyToStore,
                    (int) noLongerExist
            ));
        }
        return figures;
    }

    /** Centralize your liquidation rule here */



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
    public void changeInventoryItemOfExportDetail(ChangeInventoryItemOfExportDetailRequest request) {
        // 1) Validate input
        if (request == null) {
            throw new IllegalArgumentException("Request must not be null");
        }
        if (request.getOldInventoryItemIds() == null || request.getNewInventoryItemIds() == null) {
            throw new IllegalArgumentException("oldInventoryItemIds and newInventoryItemIds must not be null");
        }

        // 2) Load items
        List<InventoryItem> oldItems = inventoryItemRepository.findAllById(request.getOldInventoryItemIds());
        List<InventoryItem> newItems = inventoryItemRepository.findAllById(request.getNewInventoryItemIds());
        if (oldItems.size() != request.getOldInventoryItemIds().size()) {
            throw new NoSuchElementException("Some oldInventoryItemIds do not exist");
        }
        if (newItems.size() != request.getNewInventoryItemIds().size()) {
            throw new NoSuchElementException("Some newInventoryItemIds do not exist");
        }

        // 3) All old items must belong to the same detail
        ExportRequestDetail exportDetail = null;
        for (InventoryItem oi : oldItems) {
            if (oi.getExportRequestDetail() == null) {
                throw new IllegalArgumentException("Old inventory item " + oi.getId() + " has no export request detail");
            }
            if (exportDetail == null) exportDetail = oi.getExportRequestDetail();
            else if (!exportDetail.getId().equals(oi.getExportRequestDetail().getId())) {
                throw new IllegalArgumentException("All old items must belong to the same ExportRequestDetail");
            }
        }
        if (exportDetail == null) {
            throw new IllegalStateException("Could not resolve ExportRequestDetail from old items");
        }

        // 4) Context + type checks
        List<InventoryItem> currentItems = new ArrayList<>(exportDetail.getInventoryItems());
        if (currentItems.isEmpty()) {
            throw new IllegalStateException("ExportRequestDetail has no current inventory items");
        }
        String requiredItemId = currentItems.get(0).getItem().getId();
        for (InventoryItem ci : currentItems) {
            if (!requiredItemId.equals(ci.getItem().getId())) {
                throw new IllegalStateException("ExportRequestDetail contains mixed item types; cannot safely swap");
            }
            if (ci.getMeasurementValue() == null) {
                throw new IllegalStateException("Current inventory item " + ci.getId() + " has null measurement value");
            }
        }

        // New items must be free, AVAILABLE, same type, valid measurement
        for (InventoryItem ni : newItems) {
            if (ni.getExportRequestDetail() != null) {
                throw new IllegalArgumentException("New inventory item " + ni.getId() + " is already assigned");
            }
            if (ni.getStatus() != ItemStatus.AVAILABLE) {
                throw new IllegalArgumentException("New inventory item " + ni.getId() + " is not AVAILABLE");
            }
            if (!requiredItemId.equals(ni.getItem().getId())) {
                throw new IllegalArgumentException("New inventory item " + ni.getId() + " does not match required Item type");
            }
            if (ni.getMeasurementValue() == null || ni.getMeasurementValue() <= 0d) {
                throw new IllegalArgumentException("New inventory item " + ni.getId() + " has invalid measurement value");
            }
        }

        // 5) Build remaining set: kept (= current - old) + new
        Set<String> oldIds = new HashSet<>(request.getOldInventoryItemIds());
        Set<String> currentIds = currentItems.stream().map(InventoryItem::getId).collect(Collectors.toSet());
        for (String oldId : oldIds) {
            if (!currentIds.contains(oldId)) {
                throw new IllegalArgumentException("Old inventory item " + oldId + " is not in the export detail");
            }
        }

        // kept items = những cái không nằm trong danh sách đổi
        List<InventoryItem> keptItems = currentItems.stream()
                .filter(ci -> !oldIds.contains(ci.getId()))
                .toList();

        // 6) Validate measurement theo rule: sum(kept) + sum(new) >= required
        double keptTotal = keptItems.stream()
                .mapToDouble(InventoryItem::getMeasurementValue)
                .sum();

        double addedTotal = newItems.stream()
                .mapToDouble(InventoryItem::getMeasurementValue)
                .sum();

        double postSwapTotal = keptTotal + addedTotal;
        double required = exportDetail.getMeasurementValue() != null ? exportDetail.getMeasurementValue() : 0d;

//        if (postSwapTotal < required) {
//            throw new IllegalStateException(
//                    String.format("Post-swap total measurement %.3f is less than required %.3f", postSwapTotal, required));
//        }

        // 7) Apply updates
        ItemStatus targetStatus = keptItems.stream()
                .map(InventoryItem::getStatus)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(ItemStatus.UNAVAILABLE);

        // unlink old
        oldItems.forEach(oi -> {
            oi.setExportRequestDetail(null);
            oi.setStatus(ItemStatus.AVAILABLE);
            oi.setNote(request.getNote());
        });
        inventoryItemRepository.saveAll(oldItems);

        // link new
        for (InventoryItem ni : newItems) {
            ni.setExportRequestDetail(exportDetail);
            ni.setStatus(targetStatus);
            ni.setNote(request.getNote());
        }
        inventoryItemRepository.saveAll(newItems);

        // 8) Update detail collection + quantity
        List<InventoryItem> remaining = new ArrayList<>(keptItems);
        // tránh trùng ID (bình thường newItems là free nên không trùng)
        Set<String> remainingIds = remaining.stream().map(InventoryItem::getId).collect(Collectors.toSet());
        for (InventoryItem ni : newItems) {
            if (!remainingIds.contains(ni.getId())) {
                remaining.add(ni);
                remainingIds.add(ni.getId());
            }
        }

        exportDetail.setInventoryItems(remaining);
        exportDetail.setQuantity(remaining.size());
        exportRequestDetailRepository.save(exportDetail);

        LOGGER.info("Swapped {} old items with {} new items on ExportRequestDetail {}. Post-swap total: {} (required: {})",
                oldItems.size(), newItems.size(), exportDetail.getId(), postSwapTotal, required);
    }


    public InventoryItemResponse autoChangeInventoryItem(String inventoryItemId, String note) {
        InventoryItem oldItem = inventoryItemRepository.findById(inventoryItemId)
                .orElseThrow(() -> new EntityNotFoundException("Old inventory item not found with id: " + inventoryItemId));

        ExportRequest exportRequest = oldItem.getExportRequestDetail().getExportRequest();
        List<ExportRequestDetail> sameItemDetails = exportRequest.getExportRequestDetails().stream()
                .filter(detail -> detail.getItem().getId().equals(oldItem.getItem().getId()))
                .toList();

        Double totalMeasurementValue = sameItemDetails.stream()
                .map(ExportRequestDetail::getMeasurementValue)
                .filter(Objects::nonNull)
                .reduce(0.0, Double::sum);

        Double remainingMeasurementValue = totalMeasurementValue - oldItem.getMeasurementValue();

        List<InventoryItem> inventoryItems = inventoryItemRepository.findByItem_Id(oldItem.getItem().getId());
        inventoryItems.sort(Comparator.comparingDouble(InventoryItem::getMeasurementValue));

        InventoryItem newItem = inventoryItems.stream()
                .filter(i -> !i.getId().equals(oldItem.getId())) // không trùng với old
                .filter(i -> Double.compare(remainingMeasurementValue + i.getMeasurementValue(), totalMeasurementValue) >= 0)
                .filter(i -> i.getExportRequestDetail() == null)
                .filter(i -> i.getStatus() == ItemStatus.AVAILABLE)
                .max(Comparator.comparing(i -> i.getImportOrderDetail().getImportOrder().getDateReceived()))
                .orElseThrow(() -> new NoSuchElementException("No matching inventory item found"));
        newItem.setExportRequestDetail(oldItem.getExportRequestDetail());
        newItem.setStatus(ItemStatus.UNAVAILABLE);
        InventoryItem savedNewItem = inventoryItemRepository.save(newItem);

        oldItem.setExportRequestDetail(null);
        if(note == null || note.isEmpty()){
            oldItem.setNote("Hàng bị lỗi, đã tự động chuyển sang hàng mới");
        } else {
            oldItem.setNote(note);
        }
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
        response.setIsTrackingForExport(inventoryItem.getIsTrackingForExport());

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
