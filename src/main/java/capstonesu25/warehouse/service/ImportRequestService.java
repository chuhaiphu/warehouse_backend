package capstonesu25.warehouse.service;

import capstonesu25.warehouse.entity.Configuration;
import capstonesu25.warehouse.entity.ImportRequest;
import capstonesu25.warehouse.enums.RequestStatus;
import capstonesu25.warehouse.model.importrequest.ImportRequestCreateRequest;
import capstonesu25.warehouse.model.importrequest.ImportRequestResponse;
import capstonesu25.warehouse.model.importrequest.ImportRequestUpdateRequest;
import capstonesu25.warehouse.repository.ConfigurationRepository;
import capstonesu25.warehouse.repository.ImportRequestRepository;
import capstonesu25.warehouse.utils.Mapper;
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

        LocalDate startDate = LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"));
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

            if(request.getStartDate().isBefore(LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh")))) {
                throw new IllegalArgumentException("Start date cannot be in the past.");
            }

            startDate = request.getStartDate();
        }

        importRequest.setStartDate(startDate);
        importRequest.setEndDate(endDate);

        return Mapper.mapToImportRequestResponse(importRequestRepository.save(importRequest));
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

}
