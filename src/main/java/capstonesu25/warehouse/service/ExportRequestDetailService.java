package capstonesu25.warehouse.service;

import capstonesu25.warehouse.entity.ExportRequest;
import capstonesu25.warehouse.entity.ExportRequestDetail;
import capstonesu25.warehouse.model.exportrequest.exportrequestdetail.ExportRequestDetailExcelRow;
import capstonesu25.warehouse.repository.ExportRequestDetailRepository;
import capstonesu25.warehouse.repository.ExportRequestRepository;
import capstonesu25.warehouse.repository.ItemRepository;
import capstonesu25.warehouse.utils.ExcelUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ExportRequestDetailService {
    private final ExportRequestRepository exportRequestRepository;
    private final ExportRequestDetailRepository exportRequestDetailRepository;
    private final ItemRepository itemRepository;
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
            exportRequestDetail.setQuantity(row.getQuantity());
            exportRequestDetail.setActualQuantity(0);
            exportRequestDetail.setItem(itemRepository.findById(row.getItemId())
                    .orElseThrow(() -> new RuntimeException("Item not found with ID: " + row.getItemId())));
            exportRequestDetailRepository.save(exportRequestDetail);
        }
    }

    public void updateActualQuantity(Long exportRequestDetailId, Integer actualQuantity) {
        LOGGER.info("Updating actual quantity for export request detail with ID: {}", exportRequestDetailId);
        ExportRequestDetail exportRequestDetail = exportRequestDetailRepository.findById(exportRequestDetailId)
                .orElseThrow(() -> new RuntimeException("Export request detail not found"));
        exportRequestDetail.setActualQuantity(actualQuantity);
        exportRequestDetailRepository.save(exportRequestDetail);
    }
} 