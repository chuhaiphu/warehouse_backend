package capstonesu25.warehouse.service;

import capstonesu25.warehouse.entity.Configuration;
import capstonesu25.warehouse.entity.ExportRequest;
import capstonesu25.warehouse.entity.ExportRequestDetail;
import capstonesu25.warehouse.entity.ImportOrder;
import capstonesu25.warehouse.entity.ImportOrderDetail;
import capstonesu25.warehouse.entity.ImportRequest;
import capstonesu25.warehouse.entity.ImportRequestDetail;
import capstonesu25.warehouse.entity.InventoryItem;
import capstonesu25.warehouse.entity.Item;
import capstonesu25.warehouse.enums.DetailStatus;
import capstonesu25.warehouse.enums.ExportType;
import capstonesu25.warehouse.enums.ImportType;
import capstonesu25.warehouse.enums.ItemStatus;
import capstonesu25.warehouse.enums.RequestStatus;
import capstonesu25.warehouse.model.importrequest.ImportRequestCreateRequest;
import capstonesu25.warehouse.model.importrequest.ImportRequestResponse;
import capstonesu25.warehouse.model.importrequest.ImportRequestUpdateRequest;
import capstonesu25.warehouse.repository.AccountRepository;
import capstonesu25.warehouse.repository.ConfigurationRepository;
import capstonesu25.warehouse.repository.ExportRequestRepository;
import capstonesu25.warehouse.repository.ImportOrderDetailRepository;
import capstonesu25.warehouse.repository.ImportOrderRepository;
import capstonesu25.warehouse.repository.ImportRequestDetailRepository;
import capstonesu25.warehouse.repository.ImportRequestRepository;
import capstonesu25.warehouse.repository.InventoryItemRepository;
import capstonesu25.warehouse.utils.Mapper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.OptionalInt;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class ImportRequestService {
    private final ImportRequestRepository importRequestRepository;
    private static final Logger LOGGER = LoggerFactory.getLogger(ImportRequestService.class);
    private final ConfigurationRepository configurationRepository;
    private final ExportRequestRepository exportRequestRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final ImportRequestDetailRepository importRequestDetailRepository;
    private final AccountRepository accountRepository;
    private final ImportOrderRepository importOrderRepository;
    private final ImportOrderDetailRepository importOrderDetailRepository;

    public List<ImportRequestResponse> getAllImportRequests() {
        LOGGER.info("Get all import requests");
        return importRequestRepository.findAll().stream()
                .map(Mapper::mapToImportRequestResponse)
                .toList();
    }

    public Page<ImportRequestResponse> getAllImportRequestsByPage(int page, int limit) {
        LOGGER.info("Get all import requests by page");
        Pageable pageable = PageRequest.of(page - 1, limit);
        Page<ImportRequest> importRequests = importRequestRepository.findAll(pageable);
        return importRequests.map(Mapper::mapToImportRequestResponse);
    }

    public ImportRequestResponse getImportRequestById(String id) {
        LOGGER.info("Get import request by id: " + id);
        ImportRequest importRequest = importRequestRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("ImportRequest not found with ID: " + id));
        return Mapper.mapToImportRequestResponse(importRequest);
    }

    public ImportRequestResponse createReturnImport(ImportRequestCreateRequest request) {
        LOGGER.info("Create new return import request");
        ImportRequest importRequest = new ImportRequest();
        if(!request.getImportType().equals(ImportType.RETURN)) {
           throw new RuntimeException("The type of import request must be RETURN");
        }

        Configuration config = configurationRepository.findAll().getFirst();
        ExportRequest exportRequest = exportRequestRepository.findById(request.getExportRequestId())
                .orElseThrow(() -> new IllegalArgumentException("Not found export request with ID : " + request.getExportRequestId()));

        if(exportRequest.getType().equals(ExportType.INTERNAL)) {
            importRequest.setId(createImportRequestId());
            importRequest.setImportReason(request.getImportReason());
            importRequest.setType(request.getImportType());
            importRequest.setStatus(RequestStatus.NOT_STARTED);

            LocalDate startDate = LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"));
            LocalDate endDate = startDate.plusDays(config.getMaxAllowedDaysForImportRequestProcess());

            if (request.getEndDate() != null) {

                if (request.getEndDate().isBefore(request.getStartDate())) {
                    throw new IllegalArgumentException("End date cannot be before start date.");
                }

                long daysBetween = ChronoUnit.DAYS.between(request.getStartDate(), request.getEndDate());

                if (daysBetween > config.getMaxAllowedDaysForImportRequestProcess()) {
                    throw new IllegalArgumentException("End date cannot be after the maximum allowed date for import request processing.");
                }

                endDate = request.getEndDate();
            }

            if (request.getStartDate() != null) {
                if (request.getStartDate().isAfter(endDate)) {
                    throw new IllegalArgumentException("Start date cannot be after end date.");
                }

                if (request.getStartDate().isBefore(LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh")))) {
                    throw new IllegalArgumentException("Start date cannot be in the past.");
                }

                startDate = request.getStartDate();
            }

            importRequest.setStartDate(startDate);
            importRequest.setEndDate(endDate);

            List<ImportRequestDetail> importRequestDetails = new ArrayList<>();
            for(ExportRequestDetail exportRequestDetail : exportRequest.getExportRequestDetails()) {
                ImportRequestDetail detail = new ImportRequestDetail();
                detail.setItem(detail.getItem());
                detail.setExpectMeasurementValue(detail.getActualMeasurementValue());
                detail.setExpectQuantity(detail.getActualQuantity());
                detail.setOrderedMeasurementValue(0.0);
                detail.setOrderedQuantity(0);
                detail.setActualMeasurementValue(0.0);
                detail.setActualQuantity(0);
                detail.setImportRequest(importRequest);
                importRequestDetails.add(detail);
            }

            importRequestDetailRepository.saveAll(importRequestDetails);
        }

        return Mapper.mapToImportRequestResponse(importRequest);
    }

    public ImportRequestResponse updateImportRequest(ImportRequestUpdateRequest request) {
        LOGGER.info("Update import request");
        
        ImportRequest importRequest = importRequestRepository.findById(request.getImportRequestId())
                .orElseThrow(() -> new NoSuchElementException("ImportRequest not found with ID: " + request.getImportRequestId()));
        
        importRequest.setImportReason(request.getImportReason());
        
        return Mapper.mapToImportRequestResponse(importRequestRepository.save(importRequest));
    }

    private String createImportRequestId() {
        String prefix = "PN";
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"));

        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);

        int todayCount = importRequestRepository.countByCreatedAtBetween(startOfDay, endOfDay);

        String datePart = today.format(DateTimeFormatter.BASIC_ISO_DATE);
        String sequence = String.format("%03d", todayCount + 1);

        return String.format("%s-%s-%s", prefix, datePart, sequence);
    }

    private OptionalInt findLatestBatchSuffixForToday() {
        LOGGER.info("Finding latest batch suffix for today");
        List<ImportRequest> requests = importRequestRepository.findByBatchCodeStartingWith(getTodayPrefix());

        return requests.stream()
                .map(ImportRequest::getBatchCode)
                .map(code -> code.substring(getTodayPrefix().length()))
                .mapToInt(Integer::parseInt)
                .max(); // returns OptionalInt
    }
    private String getTodayPrefix() {
        return LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh")) + "_";
    }
    private String createInventoryItemId(ImportOrderDetail importOrderDetail, int index) {
        return "ITM-" + importOrderDetail.getItem().getId() + "-" + importOrderDetail.getImportOrder().getId() + "-" + (index + 1);
    }

    private String createImportOrderId(ImportRequest importRequest) {
        int size = importRequest.getImportOrders().size();
        return "DN-" + importRequest.getId() + "-" + (size + 1);
    }

}
