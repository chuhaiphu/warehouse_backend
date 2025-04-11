package capstonesu25.warehouse.service;

import capstonesu25.warehouse.entity.ExportRequest;
import capstonesu25.warehouse.entity.ExportRequestDetail;
import capstonesu25.warehouse.entity.InventoryItem;
import capstonesu25.warehouse.enums.DetailStatus;
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
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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
    private static final Logger LOGGER = LoggerFactory.getLogger(ExportRequestDetailService.class);

    public void createExportRequestDetail(MultipartFile file, Long exportRequestId) {
        LOGGER.info("Creating export request detail");
        LOGGER.info("Finding export request by id: {}", exportRequestId);

        ExportRequest exportRequest = exportRequestRepository.findById(exportRequestId)
                .orElseThrow(() -> new RuntimeException("Export request not found"));

        // Process Excel file
        List<ExportRequestDetailExcelRow> excelRows = ExcelUtil.processExcelFile(file, ExportRequestDetailExcelRow.class);

        // Create export request details
        for (ExportRequestDetailExcelRow row : excelRows) {
            ExportRequestDetail exportRequestDetail = new ExportRequestDetail();
            exportRequestDetail.setExportRequest(exportRequest);
            exportRequestDetail.setMeasurementValue(row.getMeasurementValue());
            exportRequestDetail.setActualMeasurementValue((double) 0);
            exportRequestDetail.setItem(itemRepository.findById(row.getItemId())
                    .orElseThrow(() -> new RuntimeException("Item not found with ID: " + row.getItemId())));
            exportRequestDetailRepository.save(exportRequestDetail);
            autoChooseInventoryItemsForExportRequestDetail(exportRequestDetail);
        }
    }

    public ExportRequestDetailResponse updateActualMeasurementValue(Long exportRequestDetailId, Double actual) {
        LOGGER.info("Updating actual quantity for export request detail with ID: {}", exportRequestDetailId);
        ExportRequestDetail exportRequestDetail = exportRequestDetailRepository.findById(exportRequestDetailId)
                .orElseThrow(() -> new RuntimeException("Export request detail not found"));
        exportRequestDetail.setActualMeasurementValue(actual);
        if(!Objects.equals(actual, exportRequestDetail.getMeasurementValue())) {
            exportRequestDetail.setStatus(DetailStatus.LACK);
        } else {
            exportRequestDetail.setStatus(DetailStatus.MATCH);
        }
        return mapToEntity(exportRequestDetailRepository.save(exportRequestDetail));
    }

    private void autoChooseInventoryItemsForExportRequestDetail(ExportRequestDetail exportRequestDetail) {
        Double totalMeasurementOfExport = exportRequestDetail.getMeasurementValue();
        if(totalMeasurementOfExport > exportRequestDetail.getItem().getMeasurementValue()) {
            throw new RuntimeException("quantity of export request detail id: " + exportRequestDetail.getItem().getId()
                    + " is greater than measurement value of item id: " + exportRequestDetail.getItem().getId());
        }
        // Fetch inventory items by item ID
        List<InventoryItem> inventoryItemList = inventoryItemRepository.findByItem_Id(exportRequestDetail.getItem().getId());

        if (inventoryItemList.isEmpty()) {
            throw new RuntimeException("Inventory items not found for item ID: " + exportRequestDetail.getItem().getId());
        }

        // Sort the inventory items by importedDate (furthest from now first)
        List<InventoryItem> sortedInventoryItems = inventoryItemList.stream()
                .sorted(Comparator.comparing(InventoryItem::getImportedDate).reversed())
                .toList();
        // Select the inventory items until the total measurement reaches the required quantity
        List<InventoryItem> selectedInventoryItems = new ArrayList<>();
       for(InventoryItem inventoryItem : sortedInventoryItems) {
            if(totalMeasurementOfExport <= 0) {
                break;
            }
            if(inventoryItem.getMeasurementValue() > totalMeasurementOfExport) {
                selectedInventoryItems.add(inventoryItem);
                totalMeasurementOfExport = (double) 0;
            } else {
                totalMeasurementOfExport -= inventoryItem.getMeasurementValue();
            }
        }
        exportRequestDetail.setInventoryItems(selectedInventoryItems);
    }


    private ExportRequestDetailResponse mapToEntity(ExportRequestDetail exportRequestDetail) {
        ExportRequestDetailResponse response = new ExportRequestDetailResponse();
        response.setId(exportRequestDetail.getId());
        response.setMeasurementValue(exportRequestDetail.getMeasurementValue());
        response.setActualMeasurementValue(exportRequestDetail.getActualMeasurementValue());
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