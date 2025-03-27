package capstonesu25.warehouse.service;

import capstonesu25.warehouse.entity.*;
import capstonesu25.warehouse.enums.ItemStatus;
import capstonesu25.warehouse.model.paper.PaperRequest;
import capstonesu25.warehouse.model.paper.PaperResponse;
import capstonesu25.warehouse.repository.*;
import capstonesu25.warehouse.utils.CloudinaryUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaperService {
    private final PaperRepository paperRepository;
    private final ItemRepository itemRepository;
    private final ImportOrderRepository importOrderRepository;
    private final ExportRequestRepository exportRequestRepository;
    private final CloudinaryUtil cloudinaryUtil;
    private final StoredLocationRepository storedLocationRepository;
    private final ImportOrderDetailRepository importOrderDetailRepository;


    private static final Logger LOGGER = LoggerFactory.getLogger(PaperService.class);
    private final InventoryItemRepository inventoryItemRepository;

    public Page<PaperResponse> getListPaper(int page, int limit) {
        Pageable pageable = PageRequest.of(Math.max(0, page - 1), limit);
        Page<Paper> papers = paperRepository.findAll(pageable);
        return papers.map(this::convertToResponse);
    }

    public List<PaperResponse> getListPaperByImportOrderId(Long importOrderId,int page, int limit) {
        Pageable pageable = PageRequest.of(Math.max(0, page - 1), limit);
        Page<Paper> papers = paperRepository.findPapersByImportOrder_Id(importOrderId, pageable);
        return papers.stream().map(this::convertToResponse).toList();
    }

    public List<PaperResponse> getListPaperByExportRequestId(Long exportRequestId,int page, int limit) {
        Pageable pageable = PageRequest.of(Math.max(0, page - 1), limit);
        Page<Paper> papers = paperRepository.findPapersByExportRequest_Id(exportRequestId, pageable);
        return papers.stream().map(this::convertToResponse).toList();
    }

    public PaperResponse getPaperById(Long id) {
        LOGGER.info("Getting paper by id");
        Paper paper = paperRepository.findById(id).orElse(null);
        return paper != null ? convertToResponse(paper) : null;
    }

    public void createPaper(PaperRequest request) throws IOException {
        LOGGER.info("Creating paper");
        String signProviderUrl = cloudinaryUtil.uploadImage(request.getSignProviderUrl());
        String signWarehouseUrl = cloudinaryUtil.uploadImage(request.getSignWarehouseUrl());
        Paper paper = convertToEntity(request);
        paper.setSignProviderUrl(signProviderUrl);
        paper.setSignWarehouseUrl(signWarehouseUrl);
        try {
            paperRepository.save(paper);
        }catch (Exception e) {
            LOGGER.error("Error creating paper: {}", e.getMessage());
            throw e;
        }
        afterCreatedPaperUpdateItems(request);
        autoFillLocation(request);
    }

    private void autoFillLocation(PaperRequest request) {
        LOGGER.info("Auto fill location");
        LOGGER.info("Import Order ID: {}", request.getImportOrderId());

        if (request.getImportOrderId() == null) {
            LOGGER.warn("Import order id is null");
            return;
        }

        ImportOrder importOrder = importOrderRepository.findById(request.getImportOrderId()).orElse(null);
        if (importOrder == null) {
            LOGGER.warn("Import order not found");
            return;
        }

        LOGGER.info("Import order found with {} details", importOrder.getImportOrderDetails().size());

        for (ImportOrderDetail importOrderDetail : importOrder.getImportOrderDetails()) {
            LOGGER.info("Processing Import Order Detail ID: {}", importOrderDetail.getId());

            // Tính tổng số lượng từ danh sách InventoryItem
            int totalQuantity = importOrderDetail.getInventoryItems()
                    .stream()
                    .mapToInt(InventoryItem::getQuantity)
                    .sum();
            importOrderDetail.setActualQuantity(totalQuantity);
            importOrderDetailRepository.save(importOrderDetail);

            int remainingQuantity = importOrderDetail.getActualQuantity();
            if (remainingQuantity <= 0) {
                LOGGER.info("No remaining quantity for Import Order Detail ID: {}", importOrderDetail.getId());
                continue;
            }

            Item item = importOrderDetail.getItem();
            List<StoredLocation> availableLocations = storedLocationRepository
                    .findByItem_IdAndIsUsedFalseOrderByZoneAscFloorAscRowAscBatchAsc(item.getId());

            LOGGER.info("Found {} available locations for Item ID: {}", availableLocations.size(), item.getId());

            if (availableLocations.isEmpty()) {
                LOGGER.warn("No available storage location for Item ID: {}", item.getId());
                continue;
            }

            List<InventoryItem> itemsWithoutLocation = importOrderDetail.getInventoryItems()
                    .stream()
                    .filter(inv -> inv.getStoredLocation() == null)
                    .collect(Collectors.toList());

            LOGGER.info("Found {} Inventory Items without stored location in Import Order Detail ID: {}", itemsWithoutLocation.size(), importOrderDetail.getId());

            for (StoredLocation location : availableLocations) {
                if (remainingQuantity <= 0) break;

                int availableSpace = (int) (location.getMaximumCapacityForItem() - location.getCurrentCapacity());
                if (availableSpace <= 0) continue;

                int allocatedQuantity = Math.min(remainingQuantity, availableSpace);

                if (!itemsWithoutLocation.isEmpty()) {
                    InventoryItem inventoryItem = itemsWithoutLocation.remove(0);
                    inventoryItem.setStoredLocation(location);
                    LOGGER.info("Assigned Location ID: {} to existing Inventory Item ID: {}", location.getId(), inventoryItem.getId());
                    inventoryItemRepository.save(inventoryItem);
                } else {
                    Optional<InventoryItem> existingItemOpt = importOrderDetail.getInventoryItems()
                            .stream()
                            .filter(inv -> inv.getStoredLocation() != null && inv.getStoredLocation().getId().equals(location.getId()))
                            .findFirst();

                    InventoryItem inventoryItem;
                    if (existingItemOpt.isPresent()) {
                        inventoryItem = existingItemOpt.get();
                        inventoryItem.setQuantity(inventoryItem.getQuantity() + allocatedQuantity);
                        LOGGER.info("Updated existing Inventory Item ID: {} at Location ID: {}", inventoryItem.getId(), location.getId());
                    } else {
                        LOGGER.warn("Unexpected case: No inventory item available for assignment in Import Order Detail ID: {}", importOrderDetail.getId());
                        continue;
                    }

                    inventoryItemRepository.save(inventoryItem);
                }

                location.setCurrentCapacity(location.getCurrentCapacity() + allocatedQuantity);
                if (location.getCurrentCapacity() >= location.getMaximumCapacityForItem()) {
                    location.setUsed(true);
                }
                storedLocationRepository.save(location);

                remainingQuantity -= allocatedQuantity;
            }
        }
    }


    private void afterCreatedPaperUpdateItems(PaperRequest request) {
        LOGGER.info("Updating items after creating paper");
        if (request.getImportOrderId() != null) {
            ImportOrder importOrder = importOrderRepository.findById(request.getImportOrderId()).orElse(null);
            List<Item> itemsToUpdate = importOrder.getImportOrderDetails().stream()
                    .map(importOrderDetail -> {
                        Item item = importOrderDetail.getItem();
                        item.setTotalMeasurementValue(item.getTotalMeasurementValue() + importOrderDetail.getActualQuantity());
                        return item;
                    })
                    .toList();
            itemRepository.saveAll(itemsToUpdate);
        }

        if (request.getExportRequestId() != null) {
            ExportRequest exportRequest = exportRequestRepository.findById(request.getExportRequestId()).orElse(null);
            List<Item> itemsToUpdate = exportRequest.getExportRequestDetails().stream()
                    .map(exportRequestDetail -> {
                        Item item = exportRequestDetail.getItem();
                        item.setTotalMeasurementValue(item.getTotalMeasurementValue() - exportRequestDetail.getQuantity());
                        return item;
                    })
                    .toList();
            itemRepository.saveAll(itemsToUpdate);
        }
    }

    private Paper convertToEntity(PaperRequest request) {
        Paper paper = new Paper();
        if(request.getId() != null) {
            paper.setId(request.getId());
        }
        paper.setDescription(request.getDescription());
        if(request.getImportOrderId() != null) {
            paper.setImportOrder(importOrderRepository.findById(request.getImportOrderId()).orElse(null));
        }
        if(request.getExportRequestId() != null){
            paper.setExportRequest(exportRequestRepository.findById(request.getExportRequestId()).orElse(null));
        }
        return paper;
    }

    private PaperResponse convertToResponse(Paper paper) {
        PaperResponse response = new PaperResponse();
        response.setId(paper.getId());
        response.setDescription(paper.getDescription());
        if(paper.getImportOrder() != null) {
            response.setImportOrderId(paper.getImportOrder().getId());
        }
        if(paper.getExportRequest() != null) {
            response.setExportRequestId(paper.getExportRequest().getId());
        }
        response.setSignProviderUrl(paper.getSignProviderUrl());
        response.setSignWarehouseUrl(paper.getSignWarehouseUrl());
        return response;
    }

}
