package capstonesu25.warehouse.service;

import capstonesu25.warehouse.entity.*;
import capstonesu25.warehouse.enums.AccountStatus;
import capstonesu25.warehouse.enums.DetailStatus;
import capstonesu25.warehouse.enums.ImportStatus;
import capstonesu25.warehouse.enums.ItemStatus;
import capstonesu25.warehouse.model.paper.PaperRequest;
import capstonesu25.warehouse.model.paper.PaperResponse;
import capstonesu25.warehouse.repository.*;
import capstonesu25.warehouse.utils.CloudinaryUtil;
import capstonesu25.warehouse.utils.NotificationUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

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
    private final ImportRequestDetailRepository importRequestDetailRepository;
    private final ImportRequestRepository importRequestRepository;
    private final AccountRepository accountRepository;
    private final NotificationUtil notificationUtil;

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

        if(request.getImportOrderId() != null) {
            //update status
            LOGGER.info("Updating import order status at creating paper");
            ImportOrder importOrder = importOrderRepository.findById(request.getImportOrderId()).orElseThrow();
            importOrder.setStatus(ImportStatus.CONFIRMED);
            importOrderRepository.save(importOrder);
            paper.setImportOrder(importOrder);
//            if (importOrder != null && importOrder.getAssignedStaff() != null) {
//                LOGGER.info("Setting import order staff status back to ACTIVE");
//                Account staff = importOrder.getAssignedStaff();
//                staff.setStatus(AccountStatus.ACTIVE);
//                accountRepository.save(staff);
//            }
//            updateImportRequest(request.getImportOrderId());
//            updateImportOrder(request.getImportOrderId());
//            autoFillLocationForImport(request);
        }
        if(request.getExportRequestId() != null) {
            //update status
            LOGGER.info("Updating export request status at creating paper");
            ExportRequest exportRequest = exportRequestRepository.findById(request.getExportRequestId()).orElseThrow();
            exportRequest.setStatus(ImportStatus.CONFIRMED);
            exportRequestRepository.save(exportRequest);
            paper.setExportRequest(exportRequest);
        }
        paperRepository.save(paper);
        // * Notification
        Map<String, Object> notificationPayload = new HashMap<>();
        notificationPayload.put("id", request.getImportOrderId());
        notificationPayload.put("message", "An import order has been counted.");
        notificationUtil.notify(NotificationUtil.WAREHOUSE_MANAGER_CHANNEL, NotificationUtil.IMPORT_ORDER_COUNTED_EVENT, notificationPayload);
        // *
//        afterCreatedPaperUpdateItems(request);
    }

    //update inventory item and location
    private void autoFillLocationForImport(PaperRequest request) {
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

        List<ImportOrderDetail> importOrderDetails = importOrder.getImportOrderDetails();
        for(ImportOrderDetail importOrderDetail : importOrderDetails) {
           List<InventoryItem> inventoryItemList = importOrderDetail.getItem().getInventoryItems();
            //get the stored location
            List<StoredLocation> storedLocationList = storedLocationRepository
                    .findByItem_IdAndIsFulledFalseOrderByZoneAscFloorAscRowAscBatchAsc(importOrderDetail.getItem().getId());
            for (InventoryItem inventoryItem : inventoryItemList) {
                for (StoredLocation storedLocation : storedLocationList) {
                    if (storedLocation.getCurrentCapacity() + inventoryItem.getMeasurementValue() <= storedLocation.getMaximumCapacityForItem()) {
                        inventoryItem.setStoredLocation(storedLocation);
                        double newCapacity = storedLocation.getCurrentCapacity() + inventoryItem.getMeasurementValue();
                        storedLocation.setCurrentCapacity(newCapacity);

                        storedLocation.setUsed(true);

                        boolean isNowFull = (storedLocation.getMaximumCapacityForItem() - newCapacity) < inventoryItem.getMeasurementValue();
                        storedLocation.setFulled(isNowFull);

                        storedLocationRepository.save(storedLocation);
                        break;
                    }
                }

                inventoryItem.setStatus(ItemStatus.AVAILABLE);
                inventoryItemRepository.save(inventoryItem);
            }

        }

    }

    //Update Item
    private void afterCreatedPaperUpdateItems(PaperRequest request) {
        LOGGER.info("Updating items after creating paper");

        if (request.getImportOrderId() != null) {
            handleImportItems(request.getImportOrderId());
        }

        if (request.getExportRequestId() != null) {
            handleExportItems(request.getExportRequestId());
        }
    }
    //update import order
    private void handleImportItems(Long importOrderId) {
        ImportOrder importOrder = importOrderRepository.findById(importOrderId).orElse(null);
        if (importOrder == null) {
            LOGGER.warn("Import order not found: {}", importOrderId);
            return;
        }

        Map<Long, Item> updatedItems = new HashMap<>();

        for (ImportOrderDetail detail : importOrder.getImportOrderDetails()) {
            for (InventoryItem inventoryItem : detail.getInventoryItems()) {
                Item item = inventoryItem.getItem();
                if (item != null) {
                    item.setTotalMeasurementValue(item.getTotalMeasurementValue() + inventoryItem.getMeasurementValue());
                    item.setQuantity(item.getQuantity() + 1);
                    updatedItems.put(item.getId(), item);
                }
            }
        }

        itemRepository.saveAll(updatedItems.values());
        LOGGER.info("Updated {} imported items", updatedItems.size());
    }
    //update export request
    private void handleExportItems(Long exportRequestId) {
        ExportRequest exportRequest = exportRequestRepository.findById(exportRequestId).orElse(null);
        if (exportRequest == null) {
            LOGGER.warn("Export request not found: {}", exportRequestId);
            return;
        }

        Map<Long, Item> updatedItems = new HashMap<>();

        for (ExportRequestDetail detail : exportRequest.getExportRequestDetails()) {
            for (InventoryItem inventoryItem : detail.getInventoryItems()) {
                Item item = inventoryItem.getItem();
                if (item != null) {
                    item.setTotalMeasurementValue(item.getTotalMeasurementValue() - inventoryItem.getMeasurementValue());
                    item.setQuantity(item.getQuantity() - 1);
                    updatedItems.put(item.getId(), item);
                }
            }
        }

        itemRepository.saveAll(updatedItems.values());
        LOGGER.info("Updated {} exported items", updatedItems.size());
    }

    //update import request
    private void updateImportRequest(Long importOrderId) {
        LOGGER.info("Updating import request after paper creation");
        ImportOrder importOrder = importOrderRepository.findById(importOrderId).orElse(null);
        if (importOrder == null) {
           LOGGER.warn("Import order not found");
            return;
        }
        importOrder.setStatus(ImportStatus.COMPLETED);
        importOrderRepository.save(importOrder);

        ImportRequest importRequest = importOrder.getImportRequest();
        List<ImportRequestDetail> importRequestDetails = importRequest.getDetails();
        for (ImportRequestDetail detail : importRequestDetails) {
            for(ImportOrderDetail importOrderDetail : importOrder.getImportOrderDetails()) {
                if (detail.getItem().getId().equals(importOrderDetail.getItem().getId())) {
                    detail.setActualQuantity(detail.getActualQuantity() + importOrderDetail.getActualQuantity());
                    if(detail.getActualQuantity() == detail.getExpectQuantity()) {
                        detail.setStatus(DetailStatus.MATCH);
                    } else if(detail.getActualQuantity() > detail.getExpectQuantity()) {
                        detail.setStatus(DetailStatus.EXCESS);
                    } else {
                        detail.setStatus(DetailStatus.LACK);
                    }
                    importRequestDetailRepository.save(detail);
                }
            }
        }

        boolean allCompleted = true;
        for (ImportRequestDetail detail : importRequestDetails) {
            if (detail.getActualQuantity() < detail.getExpectQuantity()) {
                allCompleted = false;
                break;
            }
        }

        if (allCompleted) {
            importRequest.setStatus(ImportStatus.COMPLETED);
            importRequestRepository.save(importRequest);
        }
    }

    //Update import Order
    private void updateImportOrder (Long importerId) {
        LOGGER.info("Updating import order after paper creation");
        ImportOrder importOrder = importOrderRepository.findById(importerId).orElse(null);
        if (importOrder == null) {
            LOGGER.warn("Import order not found");
            return;
        }
        List<ImportOrderDetail> importOrderDetails = importOrder.getImportOrderDetails();
        for(ImportOrderDetail importOrderDetail : importOrderDetails) {
            LOGGER.info("Create inventory item for import order detail id: {}", importOrderDetail.getId());
            createInventoryItem(importOrderDetail);
            LOGGER.info("Update status for import order detail id: {}", importOrderDetail.getId());
            if(importOrderDetail.getActualQuantity() == importOrderDetail.getExpectQuantity()) {
                importOrderDetail.setStatus(DetailStatus.MATCH);
            } else if(importOrderDetail.getActualQuantity() > importOrderDetail.getExpectQuantity()) {
                importOrderDetail.setStatus(DetailStatus.EXCESS);
            } else {
                importOrderDetail.setStatus(DetailStatus.LACK);
            }
            importOrderDetailRepository.save(importOrderDetail);
        }

        importOrder.setStatus(ImportStatus.COMPLETED);
        importOrder.setUpdatedDate(LocalDateTime.now());
        importOrderRepository.save(importOrder);
    }

    private void updateExportRequest(Long exportRequestId) {
        LOGGER.info("Updating export request after paper creation");
        ExportRequest exportRequest = exportRequestRepository.findById(exportRequestId).orElse(null);
        if (exportRequest == null) {
            LOGGER.warn("Export request not found");
            return;
        }
        exportRequest.setStatus(ImportStatus.COMPLETED);
        exportRequestRepository.save(exportRequest);
    }

    private void updateInventoryItemAndLocationAfterExport(Long exportRequestId) {
        LOGGER.info("Updating inventory item after export request");
        ExportRequest exportRequest = exportRequestRepository.findById(exportRequestId).orElse(null);
        if (exportRequest == null) {
            LOGGER.warn("Export request not found");
            return;
        }
        List<ExportRequestDetail> exportRequestDetails = exportRequest.getExportRequestDetails();
        for(ExportRequestDetail exportRequestDetail : exportRequestDetails) {
            for(InventoryItem inventoryItem : exportRequestDetail.getInventoryItems()) {
                LOGGER.info("Updating inventory item id: {}", inventoryItem.getId());
                inventoryItem.setStatus(ItemStatus.UNAVAILABLE);
                inventoryItemRepository.save(inventoryItem);

                StoredLocation location = inventoryItem.getStoredLocation();
                if (location != null) {
                    LOGGER.info("Updating stored location id: {}", location.getId());
                    location.setCurrentCapacity(location.getCurrentCapacity() - inventoryItem.getMeasurementValue());
                    location.setFulled(false);
                    if(location.getCurrentCapacity() == 0) {
                        location.setUsed(false);
                    }
                    storedLocationRepository.save(location);
                }
            }
        }
    }

    private void createInventoryItem(ImportOrderDetail importOrderDetail) {
        for(int i = 0; i < importOrderDetail.getActualQuantity(); i++) {
            InventoryItem inventoryItem = new InventoryItem();
            inventoryItem.setImportOrderDetail(importOrderDetail);
            inventoryItem.setItem(importOrderDetail.getItem());
            inventoryItem.setImportedDate(LocalDateTime.of(importOrderDetail.getImportOrder().getDateReceived(),
                    importOrderDetail.getImportOrder().getTimeReceived()));
            inventoryItem.setMeasurementValue(importOrderDetail.getItem().getMeasurementValue());
            inventoryItem.setStatus(ItemStatus.AVAILABLE);
            inventoryItem.setUpdatedDate(LocalDateTime.now());
            if(importOrderDetail.getItem().getDaysUntilDue() != null) {
                inventoryItem.setExpiredDate(inventoryItem.getImportedDate().plusDays(importOrderDetail.getItem().getDaysUntilDue()));
            }
            inventoryItemRepository.save(inventoryItem);
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
