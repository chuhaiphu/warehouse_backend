package capstonesu25.warehouse.service;

import capstonesu25.warehouse.annotation.transactionLog.TransactionLoggable;
import capstonesu25.warehouse.entity.*;
import capstonesu25.warehouse.enums.ImportType;
import capstonesu25.warehouse.enums.RequestStatus;
import capstonesu25.warehouse.model.importrequest.OverviewImport;
import capstonesu25.warehouse.model.importrequest.ImportRequestCreateRequest;
import capstonesu25.warehouse.model.importrequest.ImportRequestResponse;
import capstonesu25.warehouse.model.importrequest.ImportRequestUpdateRequest;
import capstonesu25.warehouse.repository.*;
import capstonesu25.warehouse.utils.Mapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class ImportRequestService {
    private final ImportRequestRepository importRequestRepository;
    private static final Logger LOGGER = LoggerFactory.getLogger(ImportRequestService.class);
    private final ConfigurationRepository configurationRepository;
    private final DepartmentRepository departmentRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final ItemProviderRepository itemProviderRepository;

    public List<ImportRequestResponse> getAllImportRequests() {
        LOGGER.info("Get all import requests");
        return importRequestRepository.findAll().stream()
                .map(d -> Mapper.mapToImportRequestResponse(d, itemProviderRepository))
                .toList();
    }

    public Page<ImportRequestResponse> getAllImportRequestsByPage(int page, int limit) {
        LOGGER.info("Get all import requests by page");
        Pageable pageable = PageRequest.of(page - 1, limit);
        Page<ImportRequest> importRequests = importRequestRepository.findAll(pageable);
        return importRequests.map(d -> Mapper.mapToImportRequestResponse(d,itemProviderRepository));
    }

    public List<ImportRequestResponse> getImportRequestsByStatus(RequestStatus status, LocalDate fromDate, LocalDate toDate) {
        LOGGER.info("Get import requests by status: " + status);
        List<ImportRequest> importRequests = importRequestRepository.findAllByStatus(status);
        return importRequests.stream()
                .filter(er -> {
                    LocalDate created = er.getCreatedDate().toLocalDate();
                    return (created.isEqual(fromDate) || created.isAfter(fromDate))
                            && (created.isEqual(toDate) || created.isBefore(toDate));
                })
                .map(d-> Mapper.mapToImportRequestResponse(d,itemProviderRepository))
                .toList();
    }

    public OverviewImport getNumberFromDate(LocalDate fromDate, LocalDate toDate) {
        LOGGER.info("get number import requests");
        List<RequestStatus> ongoingStatuses = List.of(
                RequestStatus.IN_PROGRESS,
                RequestStatus.EXTENDED,
                RequestStatus.COUNTED,
                RequestStatus.COUNT_AGAIN_REQUESTED,
                RequestStatus.COUNT_CONFIRMED,
                RequestStatus.WAITING_EXPORT,
                RequestStatus.CONFIRMED
        );

        List<RequestStatus> finishStatues = List.of(
                RequestStatus.COMPLETED,
                RequestStatus.CANCELLED
        );

        List<ImportRequest> ongoing = importRequestRepository.findAllByStatusIn(ongoingStatuses).stream().filter(er -> {
            LocalDate created = er.getCreatedDate().toLocalDate();
            return (created.isEqual(fromDate) || created.isAfter(fromDate))
                    && (created.isEqual(toDate) || created.isBefore(toDate));
        }).toList();
        List<ImportRequest> finish = importRequestRepository.findAllByStatusIn(finishStatues).stream().filter(er -> {
            LocalDate created = er.getCreatedDate().toLocalDate();
            return (created.isEqual(fromDate) || created.isAfter(fromDate))
                    && (created.isEqual(toDate) || created.isBefore(toDate));
        }).toList();

        return OverviewImport.builder()
                .numberOfOngoingImport(ongoing.size())
                .numberOfFinishImport(finish.size())
                .build();
    }

    public ImportRequestResponse getImportRequestById(String id) {
        LOGGER.info("Get import request by id: " + id);
        ImportRequest importRequest = importRequestRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("ImportRequest not found with ID: " + id));
        return Mapper.mapToImportRequestResponse(importRequest,itemProviderRepository);
    }

    @Transactional
    @TransactionLoggable(type = "IMPORT_REQUEST", action = "CREATE", objectIdSource = "importRequestId")
    public ImportRequestResponse createReturnImport(ImportRequestCreateRequest request) {
        LOGGER.info("Create new return import request");
        ImportRequest importRequest = new ImportRequest();
        if (!request.getImportType().equals(ImportType.RETURN)) {
            throw new RuntimeException("The type of import request must be RETURN");
        }
        Department department = departmentRepository.findById(request.getDepartmentId()).orElseThrow(
                () -> new NoSuchElementException("Department not found with ID: " + request.getDepartmentId()));
        Configuration config = configurationRepository.findAll().getFirst();

        importRequest.setId(createImportRequestId());
        importRequest.setImportReason(request.getImportReason());
        importRequest.setType(request.getImportType());
        importRequest.setStatus(RequestStatus.NOT_STARTED);
        importRequest.setDepartmentId(department.getId());
        LocalDate startDate = LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        LocalDate endDate = startDate.plusDays(config.getMaxAllowedDaysForImportRequestProcess());

        if (request.getEndDate() != null) {
            if (request.getEndDate().isBefore(request.getStartDate())) {
                throw new IllegalArgumentException("End date cannot be before start date.");
            }
            long daysBetween = ChronoUnit.DAYS.between(request.getStartDate(), request.getEndDate());

            if (daysBetween > config.getMaxAllowedDaysForImportRequestProcess()) {
                throw new IllegalArgumentException(
                        "End date cannot be after the maximum allowed date for import request processing.");
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
        importRequest = importRequestRepository.save(importRequest);
        List<ImportRequestDetail> detailList = new ArrayList<>();
        for (ImportRequestCreateRequest.ReturnImportRequestDetail detail : request.getReturnImportRequestDetails()) {
            ImportRequestDetail importRequestDetail = new ImportRequestDetail();
            InventoryItem inventoryItem = inventoryItemRepository.findById(detail.getInventoryItemId())
                    .orElseThrow(() -> new NoSuchElementException(
                            "InventoryItem not found with ID: " + detail.getInventoryItemId()));
            importRequestDetail.setInventoryItemId(detail.getInventoryItemId());
            importRequestDetail.setExpectMeasurementValue(detail.getMeasurementValue());
            importRequestDetail.setItem(inventoryItem.getItem());
            importRequestDetail.setExpectQuantity(1);
            importRequestDetail.setActualQuantity(0);
            importRequestDetail.setActualMeasurementValue(0.0);
            importRequestDetail.setOrderedMeasurementValue(0.0);
            importRequestDetail.setOrderedQuantity(0);
            importRequestDetail.setImportRequest(importRequest);
            detailList.add(importRequestDetail);
        }
        importRequest.setDetails(detailList);
        return Mapper.mapToImportRequestResponse(importRequest,itemProviderRepository);
    }

    public ImportRequestResponse updateImportRequest(ImportRequestUpdateRequest request) {
        LOGGER.info("Update import request");

        ImportRequest importRequest = importRequestRepository.findById(request.getImportRequestId())
                .orElseThrow(() -> new NoSuchElementException(
                        "ImportRequest not found with ID: " + request.getImportRequestId()));

        importRequest.setImportReason(request.getImportReason());

        return Mapper.mapToImportRequestResponse(importRequestRepository.save(importRequest),itemProviderRepository);
    }

    private String createImportRequestId() {
        String prefix = "PN";
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        String datePart = today.format(DateTimeFormatter.BASIC_ISO_DATE);
        
        String todayPrefix = prefix + "-" + datePart + "-";
        List<ImportRequest> existingRequests = importRequestRepository.findByIdStartingWith(todayPrefix);
        int todayCount = existingRequests.size();

        String sequence = String.format("%03d", todayCount + 1);

        return String.format("%s-%s-%s", prefix, datePart, sequence);
    }

    // private OptionalInt findLatestBatchSuffixForToday() {
    // LOGGER.info("Finding latest batch suffix for today");
    // List<ImportRequest> requests =
    // importRequestRepository.findByBatchCodeStartingWith(getTodayPrefix());

    // return requests.stream()
    // .map(ImportRequest::getBatchCode)
    // .map(code -> code.substring(getTodayPrefix().length()))
    // .mapToInt(Integer::parseInt)
    // .max(); // returns OptionalInt
    // }
    // private String getTodayPrefix() {
    // return LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh")) + "_";
    // }
    // private String createInventoryItemId(ImportOrderDetail importOrderDetail, int
    // index) {
    // return "ITM-" + importOrderDetail.getItem().getId() + "-" +
    // importOrderDetail.getImportOrder().getId() + "-" + (index + 1);
    // }

    // private String createImportOrderId(ImportRequest importRequest) {
    // int size = importRequest.getImportOrders().size();
    // return "DN-" + importRequest.getId() + "-" + (size + 1);
    // }

}
