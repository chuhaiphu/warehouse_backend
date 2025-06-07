package capstonesu25.warehouse.service;

import capstonesu25.warehouse.entity.Configuration;
import capstonesu25.warehouse.entity.ImportOrder;
import capstonesu25.warehouse.entity.ImportRequest;
import capstonesu25.warehouse.enums.RequestStatus;
import capstonesu25.warehouse.model.importrequest.ImportRequestCreateRequest;
import capstonesu25.warehouse.model.importrequest.ImportRequestResponse;
import capstonesu25.warehouse.model.importrequest.ImportRequestUpdateRequest;
import capstonesu25.warehouse.model.importrequest.importrequestdetail.ImportRequestDetailResponse;
import capstonesu25.warehouse.repository.ConfigurationRepository;
import capstonesu25.warehouse.repository.ExportRequestRepository;
import capstonesu25.warehouse.repository.ImportRequestRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ImportRequestService {
    private final ImportRequestRepository importRequestRepository;
    private final ExportRequestRepository exportRequestRepository;
    private static final Logger LOGGER = LoggerFactory.getLogger(ImportRequestService.class);
    private final ConfigurationRepository configurationRepository;
    private final ImportRequestDetailService importRequestDetailService;

    public List<ImportRequestResponse> getAllImportRequests() {
        LOGGER.info("Get all import requests");
        return importRequestRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    public Page<ImportRequestResponse> getAllImportRequestsByPage(int page, int limit) {
        LOGGER.info("Get all import requests by page");
        Pageable pageable = PageRequest.of(page - 1, limit);
        Page<ImportRequest> importRequests = importRequestRepository.findAll(pageable);
        return importRequests.map(this::mapToResponse);
    }

    public ImportRequestResponse getImportRequestById(String id) {
        LOGGER.info("Get import request by id: " + id);
        ImportRequest importRequest = importRequestRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("ImportRequest not found with ID: " + id));
        return mapToResponse(importRequest);
    }

    public ImportRequestResponse createImportRequest(ImportRequestCreateRequest request) {
        LOGGER.info("Create new import request");


        ImportRequest importRequest = new ImportRequest();
        importRequest.setId(createImportRequestId());
        importRequest.setImportReason(request.getImportReason());
        importRequest.setType(request.getImportType());
        importRequest.setStatus(RequestStatus.NOT_STARTED);

        Configuration configuration = configurationRepository.findAll()
                .stream()
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("Configuration not found"));

        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(configuration.getMaxAllowedDaysForImportRequestProcess());

        if(request.getEndDate() != null) {

            if(request.getEndDate().isBefore(request.getStartDate())) {
                throw new IllegalArgumentException("End date cannot be before start date.");
            }

            long daysBetween = ChronoUnit.DAYS.between(request.getStartDate(), request.getEndDate());

            if (daysBetween > configuration.getMaxAllowedDaysForImportRequestProcess()) {
                throw new IllegalArgumentException("End date cannot be after the maximum allowed date for import request processing.");
            }

            endDate = request.getEndDate();
        }

        if(request.getStartDate() != null) {
            if(request.getStartDate().isAfter(endDate)) {
                throw new IllegalArgumentException("Start date cannot be after end date.");
            }

            if(request.getStartDate().isBefore(LocalDate.now())) {
                throw new IllegalArgumentException("Start date cannot be in the past.");
            }

            startDate = request.getStartDate();
        }

        importRequest.setStartDate(startDate);
        importRequest.setEndDate(endDate);

        if (request.getExportRequestId() != null) {
            importRequest.setExportRequest(exportRequestRepository.findById(request.getExportRequestId())
                    .orElseThrow(() -> new NoSuchElementException("ExportRequest not found with ID: " + request.getExportRequestId())));
        }
        
        return mapToResponse(importRequestRepository.save(importRequest));
    }

    public ImportRequestResponse updateImportRequest(ImportRequestUpdateRequest request) {
        LOGGER.info("Update import request");
        
        ImportRequest importRequest = importRequestRepository.findById(request.getImportRequestId())
                .orElseThrow(() -> new NoSuchElementException("ImportRequest not found with ID: " + request.getImportRequestId()));
        
        importRequest.setImportReason(request.getImportReason());
        
        return mapToResponse(importRequestRepository.save(importRequest));
    }

    private ImportRequestResponse mapToResponse(ImportRequest importRequest) {
        List<ImportRequestDetailResponse> details = importRequest.getDetails() != null ?
                importRequest.getDetails().stream()
                        .map(importRequestDetailService::mapToResponse)
                        .collect(Collectors.toList()) :
                List.of();

        return new ImportRequestResponse(
                importRequest.getId(),
                importRequest.getImportReason(),
                importRequest.getType(),
                importRequest.getStatus(),
                importRequest.getProvider() != null ? importRequest.getProvider().getId() : null,
                importRequest.getExportRequest() != null ? importRequest.getExportRequest().getId() : null,
                details,
                importRequest.getImportOrders() != null ?
                        importRequest.getImportOrders().stream().map(ImportOrder::getId).toList() :
                        List.of(),
                importRequest.getCreatedBy(),
                importRequest.getUpdatedBy(),
                importRequest.getCreatedDate(),
                importRequest.getUpdatedDate(),
                importRequest.getBatchCode(),
                importRequest.getStartDate(),
                importRequest.getEndDate()
        );
    }

    private String createImportRequestId() {
        String prefix = "PN";
        LocalDate today = LocalDate.now();

        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);

        int todayCount = importRequestRepository.countByCreatedAtBetween(startOfDay, endOfDay);

        String datePart = today.format(DateTimeFormatter.BASIC_ISO_DATE);
        String sequence = String.format("%03d", todayCount + 1);

        return String.format("%s-%s-%s", prefix, datePart, sequence);
    }

}
