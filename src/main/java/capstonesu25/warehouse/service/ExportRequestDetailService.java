package capstonesu25.warehouse.service;

import capstonesu25.warehouse.entity.ExportRequest;
import capstonesu25.warehouse.entity.ExportRequestDetail;
import capstonesu25.warehouse.entity.InventoryItem;
import capstonesu25.warehouse.entity.Item;
import capstonesu25.warehouse.enums.DetailStatus;
import capstonesu25.warehouse.enums.ExportType;
import capstonesu25.warehouse.model.exportrequest.exportrequestdetail.ExportRequestDetailExcelRow;
import capstonesu25.warehouse.model.exportrequest.exportrequestdetail.ExportRequestDetailResponse;
import capstonesu25.warehouse.repository.ExportRequestDetailRepository;
import capstonesu25.warehouse.repository.ExportRequestRepository;
import capstonesu25.warehouse.repository.InventoryItemRepository;
import capstonesu25.warehouse.repository.ItemRepository;
import capstonesu25.warehouse.utils.ExcelUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExportRequestDetailService {
    private final ExportRequestRepository exportRequestRepository;
    private final ExportRequestDetailRepository exportRequestDetailRepository;
    private final ItemRepository itemRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private static final Integer LIQUIDATION = 30;
    private static final Logger LOGGER = LoggerFactory.getLogger(ExportRequestDetailService.class);

    public Page<ExportRequestDetailResponse> getAllByExportRequestId(Long exportRequestId, int page, int limit) {
        LOGGER.info("Getting all export request detail by export request id: {}", exportRequestId);
        Pageable pageable = PageRequest.of(Math.max(0, page - 1), limit);
        Page<ExportRequestDetail> exportRequestDetailPage = exportRequestDetailRepository
                .findExportRequestDetailByExportRequest_Id(exportRequestId, pageable);
        return exportRequestDetailPage.map(this::mapToResponse);
    }

    public ExportRequestDetailResponse getById(Long exportRequestDetailId) {
        LOGGER.info("Getting export request detail by id: {}", exportRequestDetailId);
        ExportRequestDetail exportRequestDetail = exportRequestDetailRepository.findById(exportRequestDetailId)
                .orElseThrow(() -> new RuntimeException("Export request detail not found"));
        return mapToResponse(exportRequestDetail);
    }

    public void createExportRequestDetail(MultipartFile file, Long exportRequestId) {
        LOGGER.info("Creating export request detail");
        LOGGER.info("Finding export request by id: {}", exportRequestId);

        ExportRequest exportRequest = exportRequestRepository.findById(exportRequestId)
                .orElseThrow(() -> new RuntimeException("Export request not found"));

        // Process Excel file
        List<ExportRequestDetailExcelRow> excelRows = ExcelUtil.processExcelFile(file, ExportRequestDetailExcelRow.class);
        List<ExportRequestDetail> exportRequestDetails = new ArrayList<>();
        // Create export request details
        for (ExportRequestDetailExcelRow row : excelRows) {
            ExportRequestDetail exportRequestDetail = new ExportRequestDetail();
            exportRequestDetail.setExportRequest(exportRequest);
            if(row.getMeasurementValue() != null) {
                exportRequestDetail.setMeasurementValue(row.getMeasurementValue());
            }
            exportRequestDetail.setQuantity(row.getQuantity());
            exportRequestDetail.setActualQuantity(0);
            Item item = itemRepository.findById(row.getItemId())
                    .orElseThrow(() -> new RuntimeException("Item not found with ID: " + row.getItemId()));
            if(exportRequest.getType().equals(ExportType.RETURN)) {
                if(!Objects.equals(item.getProvider().getId(), exportRequest.getProviderId())) {
                    throw new RuntimeException("Item provider does not match export request provider");
                }
            }
            if(exportRequest.getType().equals(ExportType.PARTIAL)
                    || exportRequest.getType().equals(ExportType.BORROWING)) {
              exportRequestDetail.setMeasurementValue(row.getMeasurementValue());
            }
            exportRequestDetail.setItem(item);
            exportRequestDetails.add(exportRequestDetail);
        }
        List<ExportRequestDetail> list = exportRequestDetailRepository.saveAll(exportRequestDetails);
        LOGGER.info("Export request details created successfully");
        for(ExportRequestDetail detail : list) {
            LOGGER.info("choose inventory items for export request detail ID: {}", detail.getId());
            chooseInventoryItemsForThoseCases(detail);
        }
    }

    public ExportRequestDetailResponse updateActualQuantity(Long exportRequestDetailId, Integer actual) {
        LOGGER.info("Updating actual quantity for export request detail with ID: {}", exportRequestDetailId);
        ExportRequestDetail exportRequestDetail = exportRequestDetailRepository.findById(exportRequestDetailId)
                .orElseThrow(() -> new RuntimeException("Export request detail not found"));
        exportRequestDetail.setActualQuantity(actual);
        if(!Objects.equals(actual, exportRequestDetail.getQuantity())) {
            exportRequestDetail.setStatus(DetailStatus.LACK);
        } else {
            exportRequestDetail.setStatus(DetailStatus.MATCH);
        }
        return mapToResponse(exportRequestDetailRepository.save(exportRequestDetail));
    }

    private void chooseInventoryItemsForThoseCases(ExportRequestDetail exportRequestDetail) {
        switch (exportRequestDetail.getExportRequest().getType()) {
            case RETURN -> {

            }
            case BORROWING, PARTIAL -> autoChooseInventoryItemsForPartialAndBorrowing(exportRequestDetail);
            case LIQUIDATION -> autoChooseInventoryItemsForLiquidation(exportRequestDetail);
            default -> // case PRODUCTION
                    autoChooseInventoryItemsForProduction(exportRequestDetail);
        }
    }

    private void autoChooseInventoryItemsForProduction(ExportRequestDetail exportRequestDetail) {
        Integer actualQuantity = exportRequestDetail.getActualQuantity();
        if((actualQuantity*exportRequestDetail.getItem().getMeasurementValue()) >
                exportRequestDetail.getItem().getTotalMeasurementValue()) {
            throw new RuntimeException("quantity of export request detail id: " + exportRequestDetail.getItem().getId()
                    + " is greater than total measurement value of item id: " + exportRequestDetail.getItem().getId());
        }
        // Fetch inventory items by item ID
        List<InventoryItem> inventoryItemList = inventoryItemRepository.findByItem_IdAndParentNull(exportRequestDetail.getItem().getId());

        if (inventoryItemList.isEmpty()) {
            throw new RuntimeException("Inventory items not found for item ID: " + exportRequestDetail.getItem().getId());
        }

        // Sort the inventory items by importedDate (furthest from now first)
        List<InventoryItem> sortedInventoryItems = inventoryItemList.stream()
                .sorted(Comparator.comparing(InventoryItem::getImportedDate).reversed())
                .limit(actualQuantity)
                .toList();

        exportRequestDetail.setInventoryItems(sortedInventoryItems);
        exportRequestDetailRepository.save(exportRequestDetail);
    }

    private void autoChooseInventoryItemsForPartialAndBorrowing(ExportRequestDetail exportRequestDetail) {
        List<InventoryItem> inventoryItemList = inventoryItemRepository.findByItem_Id(exportRequestDetail.getItem().getId());

        // FIFO - sort by imported date (oldest first)
        List<InventoryItem> sortedInventoryItems = inventoryItemList.stream()
                .sorted(Comparator.comparing(InventoryItem::getImportedDate))
                .toList();

        int quantityToSelect = exportRequestDetail.getQuantity();
        double requestedMeasurement = exportRequestDetail.getMeasurementValue();

        // Collect all "good fit" items (with parent + >= requested measurement)
        List<InventoryItem> goodFitItems = sortedInventoryItems.stream()
                .filter(i -> i.getParent() != null && i.getMeasurementValue() >= requestedMeasurement)
                .sorted(Comparator.comparingDouble(InventoryItem::getMeasurementValue)) // Closest fitting first
                .toList();

        // Pick the best N good-fit items
        List<InventoryItem> selectedItems = new ArrayList<>();
        for (InventoryItem item : goodFitItems) {
            selectedItems.add(item);
            if (selectedItems.size() == quantityToSelect) {
                break;
            }
        }

        // Fallback — fill remaining with other FIFO items (avoiding duplicates)
        List<InventoryItem> noParentList = inventoryItemRepository.findByItem_Id(exportRequestDetail.getItem().getId());
        List<InventoryItem> noParentItems = noParentList.stream()
                .filter(i -> i.getParent() == null)
                .sorted(Comparator.comparing(InventoryItem::getImportedDate)) // FIFO
                .toList();

        if (selectedItems.size() < quantityToSelect) {
            for (InventoryItem item : noParentItems) {
                if (!selectedItems.contains(item)) {
                    selectedItems.add(item);
                    if (selectedItems.size() == quantityToSelect) {
                        break;
                    }
                }
            }
        }

        // Set result or log error
        if (selectedItems.size() == quantityToSelect) {
            exportRequestDetail.setInventoryItems(selectedItems); // assumes list field
            exportRequestDetailRepository.save(exportRequestDetail);
            LOGGER.info("Selected inventory items for export request detail ID: {}", exportRequestDetail.getId());
        } else {
            LOGGER.error("Not enough inventory items found for export request detail ID: {}", exportRequestDetail.getId());
        }
    }

    private void autoChooseInventoryItemsForLiquidation(ExportRequestDetail exportRequestDetail) {
        Integer actualQuantity = exportRequestDetail.getActualQuantity();
        if((actualQuantity*exportRequestDetail.getItem().getMeasurementValue()) >
                exportRequestDetail.getItem().getTotalMeasurementValue()) {
            throw new RuntimeException("quantity of export request detail id: " + exportRequestDetail.getItem().getId()
                    + " is greater than total measurement value of item id: " + exportRequestDetail.getItem().getId());
        }
        // Fetch inventory items by item ID
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime maxExpireDate = now.plusDays(LIQUIDATION);
        List<InventoryItem> inventoryItemList = inventoryItemRepository
                .findByItem_IdAndExpiredDateLessThanEqual(exportRequestDetail.getItem().getId(), maxExpireDate);

        if (inventoryItemList.isEmpty()) {
            throw new RuntimeException("Inventory items not found for item ID: " + exportRequestDetail.getItem().getId());
        }

        // Sort the inventory items by importedDate (furthest from now first)
        List<InventoryItem> sortedInventoryItems = inventoryItemList.stream()
                .sorted(Comparator.comparing(InventoryItem::getImportedDate).reversed())
                .limit(actualQuantity)
                .toList();

        exportRequestDetail.setInventoryItems(sortedInventoryItems);
        exportRequestDetailRepository.save(exportRequestDetail);
    }

    private ExportRequestDetailResponse mapToResponse(ExportRequestDetail exportRequestDetail) {
        ExportRequestDetailResponse response = new ExportRequestDetailResponse();
        response.setId(exportRequestDetail.getId());
        response.setMeasurementValue(exportRequestDetail.getMeasurementValue());
        response.setActualQuantity(exportRequestDetail.getActualQuantity());
        response.setQuantity(exportRequestDetail.getQuantity());
        response.setStatus(exportRequestDetail.getStatus());
        response.setExportRequestId(exportRequestDetail.getExportRequest().getId());
        response.setItemId(exportRequestDetail.getItem().getId());
        if(exportRequestDetail.getInventoryItems() != null) {
            List<Long> inventoryItemIds = exportRequestDetail.getInventoryItems().stream()
                    .map(InventoryItem::getId)
                    .collect(Collectors.toList());
            response.setInventoryItemIds(inventoryItemIds);
        } else {
            response.setInventoryItemIds(new ArrayList<>());
        }
        return response;
    }

} 