package capstonesu25.warehouse.service;

import capstonesu25.warehouse.entity.*;
import capstonesu25.warehouse.enums.*;
import capstonesu25.warehouse.model.account.AccountResponse;
import capstonesu25.warehouse.model.account.ActiveAccountRequest;
import capstonesu25.warehouse.model.exportrequest.RenewExportRequestRequest;
import capstonesu25.warehouse.model.exportrequest.exportborrowing.ExportBorrowingRequest;
import capstonesu25.warehouse.model.exportrequest.exportliquidation.ExportLiquidationRequest;
import capstonesu25.warehouse.model.exportrequest.exportpartial.ExportSellingRequest;
import capstonesu25.warehouse.model.exportrequest.exportproduction.ExportRequestRequest;
import capstonesu25.warehouse.model.exportrequest.ExportRequestResponse;
import capstonesu25.warehouse.model.exportrequest.exportreturn.ExportReturnRequest;
import capstonesu25.warehouse.model.importrequest.AssignStaffExportRequest;
import capstonesu25.warehouse.repository.*;
import capstonesu25.warehouse.utils.NotificationUtil;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class ExportRequestService {
    private final ExportRequestRepository exportRequestRepository;
    private final AccountRepository accountRepository;
    private final ImportOrderRepository importOrderRepository;
    private final StaffPerformanceRepository staffPerformanceRepository;
    private final ConfigurationRepository configurationRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final StoredLocationRepository storedLocationRepository;
    private final ItemRepository itemRepository;
    private final AccountService accountService;
    private final DepartmentRepository departmentRepository;
    private final ExportRequestDetailRepository exportRequestDetailRepository;
    private final NotificationService notificationService;
    private final ImportRequestRepository importRequestRepository;
    private final ImportRequestDetailRepository importRequestDetailRepository;
    private final ImportOrderDetailRepository importOrderDetailRepository;


    private static final Logger LOGGER = LoggerFactory.getLogger(ExportRequestService.class);

    public List<ExportRequestResponse> getAllExportRequests() {
        return exportRequestRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    public Page<ExportRequestResponse> getAllExportRequestsByPage(int page, int limit) {
        Pageable pageable = PageRequest.of(page - 1, limit);
        Page<ExportRequest> exportRequests = exportRequestRepository.findAll(pageable);
        return exportRequests.map(this::mapToResponse);
    }

    public Page<ExportRequestResponse> getAllExportRequestByAssignStaff(Long staffId, int page, int limit) {
        Pageable pageable = PageRequest.of(page - 1, limit, Sort.by(Sort.Direction.DESC, "createdDate"));

        // Fetch both sets
        Page<ExportRequest> exportRequestsConfirmStaff = exportRequestRepository.findAllByAssignedStaff_Id(staffId, Pageable.unpaged());
        Page<ExportRequest> exportRequestsCountingStaff = exportRequestRepository.findAllByCountingStaffId(staffId, Pageable.unpaged());

        // Merge, remove duplicates by ID
        Map<String, ExportRequest> uniqueRequests = new HashMap<>();
        Stream.concat(exportRequestsConfirmStaff.getContent().stream(), exportRequestsCountingStaff.getContent().stream())
                .forEach(req -> uniqueRequests.putIfAbsent(req.getId(), req));

        // Sort by createdDate DESC
        List<ExportRequest> sortedMergedList = uniqueRequests.values().stream()
                .sorted(Comparator.comparing(ExportRequest::getCreatedDate).reversed())
                .collect(Collectors.toList());

        // Manual pagination
        int start = Math.min((page - 1) * limit, sortedMergedList.size());
        int end = Math.min(start + limit, sortedMergedList.size());
        List<ExportRequestResponse> pagedResponses = sortedMergedList.subList(start, end).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return new PageImpl<>(pagedResponses, pageable, sortedMergedList.size());
    }



    public ExportRequestResponse getExportRequestById(String id) {
        ExportRequest exportRequest = exportRequestRepository.findById(id).orElseThrow();
        return mapToResponse(exportRequest);
    }

    public ExportRequestResponse createExportSellingRequest(ExportSellingRequest request) {
        LOGGER.info("Creating export selling request");
        if(!checkType(ExportType.SELLING, request.getType())) {
            LOGGER.error("Invalid export type: " + request.getType());
            throw new IllegalArgumentException("Invalid export type: " + request.getType());
        }
        if(request.getReceiverName() == null
                && request.getReceiverPhone() == null && request.getReceiverAddress() == null) {
            LOGGER.error("receiver name, phone, and address cannot be null");
            throw new IllegalArgumentException("receiver name, phone, and address cannot be null");
        }

        ExportRequest exportRequest = new ExportRequest();
        exportRequest.setId(createExportRequestId());

        exportRequest.setReceiverName(request.getReceiverName());
        exportRequest.setReceiverPhone(request.getReceiverPhone());
        exportRequest.setReceiverAddress(request.getReceiverAddress());
        exportRequest.setExportReason(request.getExportReason());
        exportRequest.setType(request.getType());

        LOGGER.info("Check counting date and counting time is valid?");
        validateForTimeDate(request.getCountingDate(), request.getCountingTime());
        exportRequest.setCountingDate(request.getCountingDate());
        exportRequest.setCountingTime(request.getCountingTime());
        LOGGER.info("Check export date and export time is valid?");
        validateForTimeDate(request.getExportDate(), null);
        exportRequest.setExportDate(request.getExportDate());
        exportRequest.setStatus(RequestStatus.IN_PROGRESS);
        exportRequest.setExportRequestDetails(new ArrayList<>());
        ExportRequest export = exportRequestRepository.save(exportRequest);
        notificationService.handleNotification(
            NotificationUtil.WAREHOUSE_MANAGER_CHANNEL,
            NotificationUtil.EXPORT_REQUEST_CREATED_EVENT,
            export.getId(),
            "Đơn xuất mã #" + export.getId() + " đã được tạo",
            accountRepository.findByRole(AccountRole.WAREHOUSE_MANAGER)
        );
        return mapToResponse(export);
    }

    public ExportRequestResponse createExportLiquidationRequest(ExportLiquidationRequest request) {
        LOGGER.info("Creating export liquidation request");
        if(!checkType(ExportType.LIQUIDATION, request.getType())) {
            LOGGER.error("Invalid export type: " + request.getType());
            throw new IllegalArgumentException("Invalid export type: " + request.getType());
        }

        ExportRequest exportRequest = new ExportRequest();
        exportRequest.setId(createExportRequestId());
        exportRequest.setReceiverName(request.getReceiverName());
        exportRequest.setReceiverPhone(request.getReceiverPhone());
        exportRequest.setReceiverAddress(request.getReceiverAddress());
        exportRequest.setExportReason(request.getExportReason());
        exportRequest.setType(request.getType());

        LOGGER.info("Check counting date and counting time is valid?");
        validateForTimeDate(request.getCountingDate(), request.getCountingTime());
        exportRequest.setCountingDate(request.getCountingDate());
        exportRequest.setCountingTime(request.getCountingTime());
        LOGGER.info("Check export date and export time is valid?");
        validateForTimeDate(request.getExportDate(), request.getExportTime());
        exportRequest.setExportDate(request.getExportDate());
        exportRequest.setStatus(RequestStatus.NOT_STARTED);

        ExportRequest export = exportRequestRepository.save(exportRequest);
        export = autoAssignCountingStaff(exportRequest);
        notificationService.handleNotification(
            NotificationUtil.WAREHOUSE_MANAGER_CHANNEL,
            NotificationUtil.EXPORT_REQUEST_CREATED_EVENT,
            export.getId(),
            "Đơn xuất mã #" + export.getId() + " đã được tạo",
            accountRepository.findByRole(AccountRole.WAREHOUSE_MANAGER)
        );
        return mapToResponse(export);
    }

    public ExportRequestResponse createExportBorrowingRequest(ExportBorrowingRequest request) {
        LOGGER.info("Creating export borrowing request");
        if(!checkType(ExportType.BORROWING, request.getType())) {
            LOGGER.error("Invalid export type: " + request.getType());
            throw new IllegalArgumentException("Invalid export type: " + request.getType());
        }

        if(request.getDepartmentId() == null && request.getReceiverName() == null
                && request.getReceiverPhone() == null && request.getReceiverAddress() == null) {
            LOGGER.error("Department ID, receiver name, phone, and address cannot be null");
            throw new IllegalArgumentException("Department ID, receiver name, phone, and address cannot be null");
        }

        ExportRequest exportRequest = new ExportRequest();
        exportRequest.setId(createExportRequestId());
        if(request.getDepartmentId() != null) {
            exportRequest.setDepartmentId(request.getDepartmentId());
        }
        exportRequest.setReceiverName(request.getReceiverName());
        exportRequest.setReceiverPhone(request.getReceiverPhone());
        exportRequest.setReceiverAddress(request.getReceiverAddress());
        exportRequest.setExportReason(request.getExportReason());
        exportRequest.setType(request.getType());

        LOGGER.info("Check counting date and counting time is valid?");
        validateForTimeDate(request.getCountingDate(), request.getCountingTime());
        exportRequest.setCountingDate(request.getCountingDate());
        exportRequest.setCountingTime(request.getCountingTime());
        LOGGER.info("Check export date and export time is valid?");
        validateForTimeDate(request.getExportDate(), request.getExportTime());
        exportRequest.setExportDate(request.getExportDate());
        exportRequest.setExpectedReturnDate(request.getExpectedReturnDate());
        exportRequest.setStatus(RequestStatus.NOT_STARTED);

        ExportRequest export = exportRequestRepository.save(exportRequest);
        export = autoAssignCountingStaff(exportRequest);
        notificationService.handleNotification(
            NotificationUtil.WAREHOUSE_MANAGER_CHANNEL,
            NotificationUtil.EXPORT_REQUEST_CREATED_EVENT,
            export.getId(),
            "Đơn xuất mã #" + export.getId() + " đã được tạo",
            accountRepository.findByRole(AccountRole.WAREHOUSE_MANAGER)
        );
        return mapToResponse(export);
    }

    public ExportRequestResponse createExportReturnRequest(ExportReturnRequest request) {
        LOGGER.info("Creating export return request");
        if(!checkType(ExportType.RETURN, request.getType())) {
            LOGGER.error("Invalid export type: " + request.getType());
            throw new IllegalArgumentException("Invalid export type: " + request.getType());
        }
        ImportOrder importOrder = importOrderRepository.findById(request.getImportOrderId())
                .orElseThrow(() ->
                        new IllegalArgumentException("The import order with ID: " + request.getImportOrderId() + " is not presented")
                );

        if(importOrder.getStatus() != RequestStatus.COMPLETED) {
            throw new IllegalArgumentException("The import order is not valid for return");
        }

        ExportRequest exportRequest = new ExportRequest();
        exportRequest.setId(createExportRequestId());
        exportRequest.setExportReason(request.getExportReason());
        exportRequest.setProviderId(importOrder.getImportRequest().getProvider().getId());
        exportRequest.setType(request.getType());
        exportRequest.setImportOrder(importOrder);

        LOGGER.info("Check counting date and counting time is valid?");
        validateForTimeDate(request.getCountingDate(), request.getCountingTime());
        exportRequest.setCountingDate(request.getCountingDate());
        exportRequest.setCountingTime(request.getCountingTime());
        LOGGER.info("Check export date and export time is valid?");
        validateForTimeDate(request.getExportDate(),null);
        exportRequest.setExportDate(request.getExportDate());

        exportRequest.setStatus(RequestStatus.WAITING_EXPORT);
        exportRequest.setCountingStaffId(importOrder.getAssignedStaff().getId());
        if(exportRequest.getExportDate().equals(importOrder.getActualDateReceived())) {
            exportRequest.setAssignedStaff(importOrder.getAssignedStaff());
        }
        exportRequest.setExportRequestDetails(new ArrayList<>());
        ExportRequest export = exportRequestRepository.save(exportRequest);

        importOrder.setExportRequest(exportRequest);
        importOrderRepository.save(importOrder);
        notificationService.handleNotification(
            NotificationUtil.WAREHOUSE_MANAGER_CHANNEL,
            NotificationUtil.EXPORT_REQUEST_CREATED_EVENT,
            export.getId(),
            "Đơn xuất mã #" + export.getId() + " đã được tạo",
            accountRepository.findByRole(AccountRole.WAREHOUSE_MANAGER)
        );

        return mapToResponse(export);
    }

    public ExportRequestResponse createExportProductionRequest(ExportRequestRequest request) {
        LOGGER.info("Creating export production request");
        if(!checkType(ExportType.PRODUCTION, request.getType()) && !checkType(ExportType.BORROWING, request.getType())) {
            LOGGER.error("Invalid export type: " + request.getType());
            throw new IllegalArgumentException("Invalid export type: " + request.getType());
        }
        if(request.getDepartmentId() == null && request.getReceiverName() == null
        && request.getReceiverPhone() == null && request.getReceiverAddress() == null) {
            LOGGER.error("Department ID, receiver name, phone, and address cannot be null");
            throw new IllegalArgumentException("Department ID, receiver name, phone, and address cannot be null");
        }

        ExportRequest exportRequest = new ExportRequest();
        exportRequest.setId(createExportRequestId());
        exportRequest.setExportReason(request.getExportReason());
        exportRequest.setReceiverName(request.getReceiverName());
        exportRequest.setReceiverPhone(request.getReceiverPhone());
        exportRequest.setReceiverAddress(request.getReceiverAddress());

        if(request.getDepartmentId() != null) {
           Department department = departmentRepository.findById(request.getDepartmentId()).orElseThrow(
                   () -> new IllegalArgumentException("Department not found with ID: " + request.getDepartmentId())
           );
            exportRequest.setDepartmentId(department.getId());
            exportRequest.setReceiverName(department.getDepartmentResponsible());
            exportRequest.setReceiverPhone(department.getPhone());
            exportRequest.setReceiverAddress(department.getLocation());
        }

        exportRequest.setType(request.getType());

        LOGGER.info("Check counting date and counting time is valid?");
        validateForTimeDate(request.getCountingDate(), request.getCountingTime());
        exportRequest.setCountingDate(request.getCountingDate());
        exportRequest.setCountingTime(request.getCountingTime());

        LOGGER.info("Check export date and export time is valid?");
        validateForTimeDate(request.getExportDate(), request.getExportTime());
        exportRequest.setExportDate(request.getExportDate());
        exportRequest.setStatus(RequestStatus.IN_PROGRESS);
        exportRequest.setExportRequestDetails(new ArrayList<>());
        ExportRequest export = exportRequestRepository.save(exportRequest);

        notificationService.handleNotification(
            NotificationUtil.WAREHOUSE_MANAGER_CHANNEL,
            NotificationUtil.EXPORT_REQUEST_CREATED_EVENT,
            export.getId(),
            "Đơn xuất mã #" + export.getId() + " đã được tạo",
            accountRepository.findByRole(AccountRole.WAREHOUSE_MANAGER)
        );
        return mapToResponse(export);
    }

    public ExportRequestResponse assignStaffToExportRequest(AssignStaffExportRequest request) {
        LOGGER.info("Assigning staff for confirm to export request with ID: " + request.getExportRequestId());
        ExportRequest exportRequest = exportRequestRepository.findById(request.getExportRequestId()).orElseThrow();

        if(exportRequest.getAssignedStaff() != null) {
            LOGGER.info("Return working for pre staff: {}",exportRequest.getAssignedStaff().getEmail());
            StaffPerformance staffPerformance = staffPerformanceRepository.
                    findByExportRequestIdAndAssignedStaff_IdAndExportCounting(exportRequest.getId(),exportRequest.getAssignedStaff().getId(), false);
            if(staffPerformance != null) {
                LOGGER.info("Delete working time for pre staff: {}",exportRequest.getAssignedStaff().getEmail());
                staffPerformanceRepository.delete(staffPerformance);
                notificationService.handleNotification(
                    NotificationUtil.STAFF_CHANNEL + exportRequest.getAssignedStaff().getId(),
                    NotificationUtil.EXPORT_REQUEST_ASSIGNED_EVENT,
                    exportRequest.getId(),
                    "Bạn đã được hủy phân công cho đơn xuất mã #" + exportRequest.getId(),
                    List.of(exportRequest.getAssignedStaff())
                );
            }
        }

        if (request.getAccountId() != null) {
            LOGGER.info("Assigning staff with account ID: " + request.getAccountId() + " to export request");
            Account staff = accountRepository.findById(request.getAccountId()).orElseThrow(
                    () -> new IllegalArgumentException("Staff not found with ID: " + request.getAccountId())
            );
            validateAccountForAssignment(staff);
            Configuration configuration = configurationRepository.findAll().getFirst();
            StaffPerformance staffPerformance = new StaffPerformance();
            staffPerformance.setExpectedWorkingTime(configuration.getTimeToAllowConfirm());
            staffPerformance.setDate(exportRequest.getExportDate());
            staffPerformance.setExportRequestId(exportRequest.getId());
            staffPerformance.setAssignedStaff(staff);
            staffPerformanceRepository.save(staffPerformance);
            exportRequest.setAssignedStaff(staff);
            exportRequestRepository.save(exportRequest);
        }

        exportRequest.setStatus(RequestStatus.IN_PROGRESS);
        exportRequestRepository.save(exportRequest);
        notificationService.handleNotification(
            NotificationUtil.STAFF_CHANNEL + exportRequest.getAssignedStaff().getId(),
            NotificationUtil.EXPORT_REQUEST_ASSIGNED_EVENT,
            exportRequest.getId(),
            "Bạn được phân công cho đơn xuất mã #" + exportRequest.getId(),
            List.of(exportRequest.getAssignedStaff())
        );
        return mapToResponse(exportRequest);
    }

    public ExportRequestResponse assignCountingStaff(AssignStaffExportRequest request) {
        LOGGER.info("Assigning staff for counting to export request with ID: " + request.getExportRequestId());
        ExportRequest exportRequest = exportRequestRepository.findById(request.getExportRequestId()).orElseThrow();

        if(exportRequest.getAssignedStaff() != null) {
            LOGGER.info("Return working for pre staff: {}",exportRequest.getAssignedStaff().getEmail());
            StaffPerformance staffPerformance = staffPerformanceRepository.
                    findByExportRequestIdAndAssignedStaff_IdAndExportCounting(exportRequest.getId(),exportRequest.getAssignedStaff().getId(),true);
            if(staffPerformance != null) {
                LOGGER.info("Delete working time for pre staff: {}",exportRequest.getAssignedStaff().getEmail());
                staffPerformanceRepository.delete(staffPerformance);
                notificationService.handleNotification(
                    NotificationUtil.STAFF_CHANNEL + exportRequest.getAssignedStaff().getId(),
                    NotificationUtil.EXPORT_REQUEST_ASSIGNED_EVENT,
                    exportRequest.getId(),
                    "Bạn đã được hủy phân công cho đơn xuất mã #" + exportRequest.getId(),
                    List.of(exportRequest.getAssignedStaff())
                );
            }
        }

        if (request.getAccountId() != null) {
            LOGGER.info("Assigning staff with account ID: " + request.getAccountId() + " to export request");
            Account staff = accountRepository.findById(request.getAccountId()).orElseThrow(
                    () -> new IllegalArgumentException("Staff not found with ID: " + request.getAccountId())
            );
            validateAccountForAssignment(staff);
            setTimeForCountingStaffPerformance(staff, exportRequest);
            exportRequest.setCountingStaffId(staff.getId());
            notificationService.handleNotification(
                NotificationUtil.STAFF_CHANNEL + staff.getId(),
                NotificationUtil.EXPORT_REQUEST_ASSIGNED_EVENT,
                exportRequest.getId(),
                "Bạn được phân công cho đơn xuất mã #" + exportRequest.getId(),
                List.of(staff)
            );
        }
        exportRequest.setStatus(RequestStatus.IN_PROGRESS);
        exportRequestRepository.save(exportRequest);
        return mapToResponse(exportRequest);
    }

    public ExportRequestResponse confirmCountedExportRequest(String exportRequestId) {
        LOGGER.info("Confirming counted export request with ID: " + exportRequestId);
        ExportRequest exportRequest = exportRequestRepository.findById(exportRequestId).orElseThrow(
                () -> new NoSuchElementException("Export request not found with ID: " + exportRequestId));
        exportRequest.setExportDate(LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh")).plusDays(1));
        exportRequest.setStatus(RequestStatus.COUNT_CONFIRMED);

        if(exportRequest.getType().equals(ExportType.SELLING)) {
            boolean hasLackStatus = exportRequest.getExportRequestDetails().stream()
                    .anyMatch(detail -> detail.getStatus() == DetailStatus.LACK);
            if(!hasLackStatus) {
                exportRequest.setExportDate(LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh")).plusDays(1));
                exportRequest.setStatus(RequestStatus.WAITING_EXPORT);
            }
        }

        if (exportRequest.getType().equals(ExportType.PRODUCTION)) {
            boolean hasLackStatus = exportRequest.getExportRequestDetails().stream()
                    .anyMatch(detail -> detail.getStatus() == DetailStatus.LACK);
            if (hasLackStatus) {
                exportRequest.setStatus(RequestStatus.CANCELLED);
            } else {
//                createImportForInternalExport(exportRequest);
                exportRequest.setStatus(RequestStatus.WAITING_EXPORT);
            }
        }

        return mapToResponse(exportRequestRepository.save(exportRequest));
    }


    private void createImportForInternalExport(ExportRequest exportRequest) {
        LOGGER.info("Creating import request for internal export with ID: " + exportRequest.getId());
        Configuration config = configurationRepository.findAll().getFirst();

        Map<Item, List<Pair<InventoryItem, Double>>> excessMap = new HashMap<>();

        for (ExportRequestDetail detail : exportRequest.getExportRequestDetails()) {
            List<InventoryItem> selectedItems = inventoryItemRepository.findByExportRequestDetail_Id(detail.getId());

            if (selectedItems.isEmpty()) continue;

            double requestedMeasurement = detail.getMeasurementValue();
            double totalMeasurement = selectedItems.stream()
                    .mapToDouble(InventoryItem::getMeasurementValue)
                    .sum();

            double maxAllowed = requestedMeasurement + requestedMeasurement * config.getMaxDispatchErrorPercent() / 100;

            if (totalMeasurement > requestedMeasurement) {
                double excess = totalMeasurement - requestedMeasurement;
                InventoryItem lastItem = selectedItems.get(selectedItems.size() - 1);
                lastItem.setReasonForDisposal("Dư thừa trong quá trình xuất sản xuất, tự động tạo ImportRequest để xử lý.");
                inventoryItemRepository.save(lastItem);
                excessMap.computeIfAbsent(detail.getItem(), k -> new ArrayList<>())
                        .add(Pair.of(lastItem, excess));

                LOGGER.info("Detail {}, requested = {}, total = {}, excess = {}", detail.getId(), requestedMeasurement, totalMeasurement, excess);

                if (totalMeasurement > maxAllowed) {
                    LOGGER.warn("TotalMeasurement ({}) > maxAllowed ({}), vẫn xử lý nhập lại", totalMeasurement, maxAllowed);
                }
            } else {
                LOGGER.info("No excess: requested = {}, total = {}", requestedMeasurement, totalMeasurement);
            }
        }

        if (excessMap.isEmpty()) {
            LOGGER.info("No excess to process for export request {}", exportRequest.getId());
            return;
        }

        // Create ImportRequest
        OptionalInt latestBatchSuffix = findLatestBatchSuffixForToday();
        int batchSuffix = latestBatchSuffix.isPresent() ? latestBatchSuffix.getAsInt() + 1 : 1;
        ImportRequest importRequest = new ImportRequest();
        importRequest.setId(createImportRequestId());
        importRequest.setImportReason("Tự động tạo để bù phần measurement vượt ngưỡng từ nhiều yêu cầu xuất sản xuất.");
        importRequest.setStatus(RequestStatus.IN_PROGRESS);
        importRequest.setType(ImportType.RETURN);
        importRequest.setStartDate(LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh")));
        importRequest.setEndDate(LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh")));
        importRequest.setCreatedDate(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")));
        importRequest.setBatchCode(getTodayPrefix() + batchSuffix);
        importRequest.setCreatedBy("system");
        importRequest.setImportOrders(new ArrayList<>());
        importRequest = importRequestRepository.save(importRequest);

        // Tạo ImportRequestDetail
        Map<Item, ImportRequestDetail> importRequestDetails = new HashMap<>();
        for (Map.Entry<Item, List<Pair<InventoryItem, Double>>> entry : excessMap.entrySet()) {
            int quantity = entry.getValue().size();
            double totalExcess = entry.getValue().stream().mapToDouble(Pair::getRight).sum();

            ImportRequestDetail detail = new ImportRequestDetail();
            detail.setImportRequest(importRequest);
            detail.setItem(entry.getKey());
            detail.setExpectQuantity(quantity);
            detail.setActualQuantity(quantity);
            detail.setOrderedQuantity(quantity);
            detail.setExpectMeasurementValue(totalExcess);
            detail.setActualMeasurementValue(totalExcess);
            detail = importRequestDetailRepository.save(detail);

            importRequestDetails.put(entry.getKey(), detail);
        }

        // Tạo ImportOrder
        ImportOrder importOrder = new ImportOrder();
        importOrder.setExportRequest(exportRequest);
        LOGGER.info("Creating ImportOrder for ExportRequest with ID: {}", exportRequest.getId());
        importOrder.setId(createImportOrderId(importRequest));


        importOrder.setImportRequest(importRequest);
        importOrder.setStatus(RequestStatus.COUNTED);
        importOrder.setCreatedDate(LocalDateTime.now());
        importOrder.setCreatedBy("system");
        importOrder.setDateReceived(LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh")));
        importOrder.setActualDateReceived(LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh")));
        importOrder.setNote("Tự động tạo để bù phần measurement vượt ngưỡng từ nhiều yêu cầu xuất sản xuất.");
        importOrder.setAssignedStaff(accountRepository.findById(exportRequest.getCountingStaffId())
                .orElseThrow(() -> new NoSuchElementException("Assigned staff not found with ID: " + exportRequest.getCountingStaffId())));
        importOrder.setActualTimeReceived(LocalTime.now(ZoneId.of("Asia/Ho_Chi_Minh")));
        importOrder.setTimeReceived(LocalTime.now(ZoneId.of("Asia/Ho_Chi_Minh")));
        importOrder = importOrderRepository.save(importOrder);

        // Tạo ImportOrderDetail và InventoryItem mới cho từng phần dư
        for (Map.Entry<Item, List<Pair<InventoryItem, Double>>> entry : excessMap.entrySet()) {
            Item item = entry.getKey();
            List<Pair<InventoryItem, Double>> excessList = entry.getValue();

            ImportRequestDetail requestDetail = importRequestDetails.get(item);

            ImportOrderDetail orderDetail = new ImportOrderDetail();
            orderDetail.setImportOrder(importOrder);
            orderDetail.setItem(item);
            orderDetail.setStatus(DetailStatus.MATCH);
            orderDetail.setActualQuantity(excessList.size());
            orderDetail.setExpectQuantity(excessList.size());
            double totalMeasurement = excessList.stream().mapToDouble(Pair::getRight).sum();
            orderDetail.setExpectMeasurementValue(totalMeasurement);
            orderDetail.setActualMeasurementValue(totalMeasurement);
            orderDetail = importOrderDetailRepository.save(orderDetail);

            // Tạo inventory item mới
            for (Pair<InventoryItem, Double> pair : excessList) {
                InventoryItem oldItem = pair.getLeft();
                double excessValue = pair.getRight();

                InventoryItem newItem = new InventoryItem();
                newItem.setId(createInventoryItemId(orderDetail, excessList.indexOf(pair)));
                newItem.setParent(oldItem);
                newItem.setItem(oldItem.getItem());
                newItem.setMeasurementValue(excessValue);
                newItem.setStatus(ItemStatus.AVAILABLE);
                newItem.setImportOrderDetail(orderDetail);
                newItem.setStoredLocation(oldItem.getStoredLocation());
                newItem.setImportedDate(LocalDateTime.now());

                inventoryItemRepository.save(newItem);
            }
        }
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

    public ExportRequestResponse completeExportRequest(String exportRequestId) {
        LOGGER.info("Completing export request with ID: " + exportRequestId);
        ExportRequest exportRequest = exportRequestRepository.findById(exportRequestId).orElseThrow(
                () -> new NoSuchElementException("Export request not found with ID: " + exportRequestId));

        exportRequest.setStatus(RequestStatus.COMPLETED);
        updateInventoryItemAndLocationAfterExport(exportRequest);
        handleExportItems(exportRequest);
        return mapToResponse(exportRequestRepository.save(exportRequest));
    }

    public ExportRequestResponse updateExportStatus (String exportRequestId, RequestStatus status) {
        LOGGER.info("Updating export request status for export request with ID: " + exportRequestId);
        ExportRequest exportRequest = exportRequestRepository.findById(exportRequestId).orElseThrow(
                () -> new NoSuchElementException("Export request not found with ID: " + exportRequestId));

        if(status == RequestStatus.CANCELLED) {
            LOGGER.info("Updating export request status to CANCELLED");
            if(exportRequest.getStatus() == RequestStatus.COMPLETED) {
                throw new IllegalStateException("Cannot cancel export request: Status is "+ exportRequest.getStatus());
            }
            LOGGER.info("Return working for pre confirm staff: {}",exportRequest.getAssignedStaff().getEmail());
            StaffPerformance staffPerformance = staffPerformanceRepository.findByExportRequestIdAndAssignedStaff_IdAndExportCounting
                    (exportRequest.getId(),exportRequest.getAssignedStaff().getId(), false);
            if(staffPerformance != null) {
                LOGGER.info("Delete working time for pre staff: {}",exportRequest.getAssignedStaff().getEmail());
                staffPerformanceRepository.delete(staffPerformance);
            }

            for(ExportRequestDetail exportRequestDetail : exportRequest.getExportRequestDetails()) {
                LOGGER.info("remove item in export request detail: {}", exportRequestDetail.getId());
                exportRequestDetail.getInventoryItems()
                        .forEach(inventoryItem -> {
                            LOGGER.info("Update item status to AVAILABLE: {}", inventoryItem.getId());
                            inventoryItem.setStatus(ItemStatus.AVAILABLE);
                            inventoryItem.setIsTrackingForExport(false);
                            inventoryItem.setExportRequestDetail(null);
                            inventoryItemRepository.save(inventoryItem);
                        });
            }
        }

        exportRequest.setStatus(status);
        return mapToResponse(exportRequestRepository.save(exportRequest));
    }

    public ExportRequestResponse updateExportDateTime(String exportRequestId, LocalDate date) {
        LOGGER.info("Updating export date and time for export request with ID: " + exportRequestId);
        ExportRequest exportRequest = exportRequestRepository.findById(exportRequestId).orElseThrow(
                () -> new NoSuchElementException("Export request not found with ID: " + exportRequestId));

        validateForTimeDate(date, null);
        exportRequest.setExportDate(date);

        // reassign confirm staff
        if(exportRequest.getAssignedStaff() != null) {
            LOGGER.info("Return working for pre staff: {}",exportRequest.getAssignedStaff().getEmail());
            StaffPerformance staffPerformance = staffPerformanceRepository.
                    findByExportRequestIdAndAssignedStaff_IdAndExportCounting(exportRequest.getId(),exportRequest.getAssignedStaff().getId(), false);
            if(staffPerformance != null) {
                LOGGER.info("Delete working time for pre staff: {}",exportRequest.getAssignedStaff().getEmail());
                staffPerformanceRepository.delete(staffPerformance);
            }
        }
        // assign new confirm staff
        autoAssignConfirmStaff(exportRequest);
        return mapToResponse(exportRequest);
    }

    public ExportRequestResponse extendExportRequest(String exportRequestId, LocalDate extendedDate,
                                                     String extendReason) {
        LOGGER.info("Extending export request with ID: " + exportRequestId);
        ExportRequest exportRequest = exportRequestRepository.findById(exportRequestId).orElseThrow(
                () -> new NoSuchElementException("Export request not found with ID: " + exportRequestId));
        if(exportRequest.getIsExtended()) {
            throw new IllegalStateException("Export request has already been extended");
        }
        Configuration configuration = configurationRepository.findAll().stream()
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("Configuration not found"));
        exportRequest.setStatus(RequestStatus.EXTENDED);
        exportRequest.setIsExtended(true);

        if(extendedDate == null) {
            extendedDate = exportRequest.getExportDate().plusDays(configuration.getMaxAllowedDaysForExtend());
        }
        exportRequest.setExtendedDate(extendedDate);

        exportRequest.setExtendedReason(extendReason);

        LOGGER.info("auto asign staff for export request with ID: " + exportRequestId);
        autoAssignCountingStaff(exportRequest);
        return mapToResponse(exportRequestRepository.save(exportRequest));
    }

    public ExportRequestResponse updateCountingDateAndTime(String exportRequestId, LocalDate date, LocalTime time) {
        LOGGER.info("Updating counting date and time for export request with ID: " + exportRequestId);
        ExportRequest exportRequest = exportRequestRepository.findById(exportRequestId).orElseThrow(
                () -> new NoSuchElementException("Export request not found with ID: " + exportRequestId));

        validateForTimeDate(date, time);
        exportRequest.setCountingDate(date);
        exportRequest.setCountingTime(time);

        // reassign counting staff
        if(exportRequest.getCountingStaffId() != null) {
            LOGGER.info("Return working for pre counting staff: {}",exportRequest.getCountingStaffId());
            StaffPerformance staffPerformance = staffPerformanceRepository.
                    findByExportRequestIdAndAssignedStaff_IdAndExportCounting(exportRequest.getId(),exportRequest.getCountingStaffId(), true);
            if(staffPerformance != null) {
                LOGGER.info("Delete working time for pre counting staff: {}",exportRequest.getCountingStaffId());
                staffPerformanceRepository.delete(staffPerformance);
            }
        }
        // assign new counting staff
        autoAssignCountingStaff(exportRequest);
        return mapToResponse(exportRequestRepository.save(exportRequest));
    }

    public ExportRequestResponse renewExportRequest(RenewExportRequestRequest request) {
        ExportRequest oldExportRequest = exportRequestRepository.findById(request.getExportRequestId())
                .orElseThrow(() -> new NoSuchElementException("Export request not found with ID: " + request.getExportRequestId()));

        if(oldExportRequest.getStatus() != RequestStatus.COUNT_CONFIRMED && oldExportRequest.getStatus() != RequestStatus.WAITING_EXPORT) {
            LOGGER.error("Cannot renew export request: Invalid status " + oldExportRequest.getStatus());
            throw new IllegalStateException("Cannot renew export request: Invalid status " + oldExportRequest.getStatus());

        }

        if(request.getItems().size() > oldExportRequest.getExportRequestDetails().size()) {
            LOGGER.error("Cannot renew export request: Number of items exceeds original request");
            throw new IllegalArgumentException("Cannot renew export request: Number of items exceeds original request");
        }
        ExportRequest newExportRequest = new ExportRequest();
        String id = createExportRequestId();
        newExportRequest.setId(id);
        newExportRequest.setExportReason(oldExportRequest.getExportReason());
        newExportRequest.setReceiverName(oldExportRequest.getReceiverName());
        newExportRequest.setReceiverPhone(oldExportRequest.getReceiverPhone());
        newExportRequest.setReceiverAddress(oldExportRequest.getReceiverAddress());
        newExportRequest.setType(oldExportRequest.getType());
        newExportRequest.setCountingDate(oldExportRequest.getCountingDate());
        newExportRequest.setCountingTime(oldExportRequest.getCountingTime());
        newExportRequest.setExportDate(oldExportRequest.getExportDate());
        newExportRequest.setExpectedReturnDate(oldExportRequest.getExpectedReturnDate());
        newExportRequest.setExportDate(oldExportRequest.getExportDate().plusDays(1));
        newExportRequest.setProviderId(oldExportRequest.getProviderId());
        newExportRequest.setDepartmentId(oldExportRequest.getDepartmentId());
        newExportRequest.setCountingStaffId(oldExportRequest.getCountingStaffId());
        newExportRequest.setNote(oldExportRequest.getNote());
        newExportRequest.setStatus(RequestStatus.COUNT_CONFIRMED);
        newExportRequest.setAssignedStaff(oldExportRequest.getAssignedStaff());
        newExportRequest = exportRequestRepository.save(newExportRequest);

        List<ExportRequestDetail> newExportRequestDetails = new ArrayList<>();

        for (RenewExportRequestRequest.itemList item : request.getItems()) {
            ExportRequestDetail matchedDetail = oldExportRequest.getExportRequestDetails().stream()
                    .filter(detail -> detail.getItem().getId().equals(item.getItemId()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Item not found in original export request: " + item.getItemId()));

            if (item.getQuantity() != null && item.getQuantity() > matchedDetail.getQuantity()) {
                LOGGER.error("Invalid quantity for item: " + item.getItemId());
                throw new IllegalArgumentException("Invalid quantity for item: " + item.getItemId());
            }

            if (item.getMeasurementValue() != null && item.getMeasurementValue() > matchedDetail.getMeasurementValue()) {
                LOGGER.error("Invalid measurement value for item: " + item.getItemId());
                throw new IllegalArgumentException("Invalid measurement value for item: " + item.getItemId());
            }

            ExportRequestDetail newDetail = new ExportRequestDetail();
            newDetail.setExportRequest(newExportRequest);
            newDetail.setItem(matchedDetail.getItem());
            newDetail.setQuantity(item.getQuantity());
            newDetail.setActualQuantity(item.getQuantity());
            newDetail.setMeasurementValue(item.getMeasurementValue());
            newDetail.setActualMeasurementValue(item.getMeasurementValue());
            newDetail.setStatus(DetailStatus.MATCH);
            newDetail = exportRequestDetailRepository.save(newDetail);
            newExportRequestDetails.add(newDetail);

            List<InventoryItem> inventoryItems = matchedDetail.getInventoryItems().stream()
                    .limit(item.getQuantity()).toList();

            List<InventoryItem> notSelectedItems = matchedDetail.getInventoryItems().stream()
                    .filter(i -> !inventoryItems.contains(i))
                    .toList();

            for (InventoryItem inventoryItem : inventoryItems) {
                inventoryItem.setExportRequestDetail(newDetail);
                inventoryItemRepository.save(inventoryItem);
            }

            for(InventoryItem inventoryItem : notSelectedItems) {
                inventoryItem.setExportRequestDetail(null);
                inventoryItem.setIsTrackingForExport(false);
                inventoryItem.setStatus(ItemStatus.AVAILABLE);
                inventoryItem.setNeedReturnToProvider(false);
                inventoryItem.setNeedToLiquidate(false);
                inventoryItemRepository.save(inventoryItem);
            }
        }

        newExportRequest.setExportRequestDetails(newExportRequestDetails);
        return mapToResponse(exportRequestRepository.save(newExportRequest));
    }

    private void updateInventoryItemAndLocationAfterExport(ExportRequest exportRequest) {
        LOGGER.info("Updating inventory item after export request");

        List<ExportRequestDetail> exportRequestDetails = exportRequest.getExportRequestDetails();
        for(ExportRequestDetail exportRequestDetail : exportRequestDetails) {
            for(InventoryItem inventoryItem : exportRequestDetail.getInventoryItems()) {
                LOGGER.info("Updating inventory item id: {}", inventoryItem.getId());
                inventoryItem.setStatus(ItemStatus.UNAVAILABLE);
                inventoryItemRepository.save(inventoryItem);

                StoredLocation location = inventoryItem.getStoredLocation();
                if (location != null) {
                    LOGGER.info("Updating stored location id: {}", location.getId());
                    location.setCurrentCapacity(location.getCurrentCapacity() - 1);
                    location.setFulled(false);
                    if(location.getCurrentCapacity() == 0) {
                        location.setUsed(false);
                    }
                    storedLocationRepository.save(location);
                }
            }
        }
    }

    private void handleExportItems(ExportRequest exportRequest) {
        Map<String, Item> updatedItems = new HashMap<>();
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

    private void setTimeForCountingStaffPerformance(Account account, ExportRequest exportRequest) {
        int totalMinutes = 0;
        for (ExportRequestDetail detail : exportRequest.getExportRequestDetails()) {
            LOGGER.info("Calculating expected working time for item: " + detail.getItem().getName());
            totalMinutes += detail.getQuantity() * detail.getItem().getCountingMinutes();
        }
        LocalTime expectedWorkingTime = LocalTime.of(0, 0).plusMinutes(totalMinutes);
        StaffPerformance staffPerformance = new StaffPerformance();
        staffPerformance.setExpectedWorkingTime(expectedWorkingTime);
        if(exportRequest.getIsExtended()) {
            staffPerformance.setDate(exportRequest.getExtendedDate().minusDays(1));
        }else {
            staffPerformance.setDate(exportRequest.getCountingDate());
        }
        staffPerformance.setExportRequestId(exportRequest.getId());
        staffPerformance.setAssignedStaff(account);
        staffPerformance.setExportCounting(true);
        staffPerformanceRepository.save(staffPerformance);
    }


    private boolean checkType(ExportType expect, ExportType actual) {
        return expect == actual;
    }

    private ExportRequestResponse mapToResponse(ExportRequest exportRequest) {
        return new ExportRequestResponse(
            exportRequest.getId(),
            exportRequest.getExportReason(),
            exportRequest.getReceiverName(),
            exportRequest.getReceiverPhone(),
            exportRequest.getReceiverAddress(),
            exportRequest.getDepartmentId(),
            exportRequest.getProviderId(),
            exportRequest.getStatus(),
            exportRequest.getType(),
            exportRequest.getExportDate(),
            exportRequest.getExpectedReturnDate(),
            exportRequest.getIsExtended(),
            exportRequest.getExtendedDate(),
            exportRequest.getExtendedReason(),
            exportRequest.getAssignedStaff() != null ? exportRequest.getAssignedStaff().getId() : null,
            exportRequest.getCountingDate(),
            exportRequest.getCountingTime(),
            exportRequest.getCountingStaffId(),
            exportRequest.getPaper() != null ? exportRequest.getPaper().getId() : null,
            exportRequest.getImportOrder() != null ?
                exportRequest.getImportOrder().getId() : null,
                exportRequest.getExportRequestDetails().isEmpty()
                        ? List.of()
                        : exportRequest.getExportRequestDetails().stream()
                        .map(ExportRequestDetail::getId)
                        .toList(),
            exportRequest.getCreatedBy(),
            exportRequest.getUpdatedBy(),
            exportRequest.getCreatedDate(),
            exportRequest.getUpdatedDate()
        );
    }

    private void validateAccountForAssignment(Account account) {
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new IllegalStateException("Cannot assign staff: Account is not active");
        }

        if (account.getRole() != AccountRole.STAFF) {
            throw new IllegalStateException("Cannot assign staff: Account is not a staff member");
        }
    }

    private void updateAccountStatusForExportRequest(Account account, ExportRequest exportRequest) {
        LOGGER.info("Update account status to INACTIVE");
        if(exportRequest.getAssignedStaff() != null) {
            // If the import order is being reassigned, set the previous staff's status to ACTIVE
            LOGGER.info("Update previous staff status to ACTIVE");
            Account preStaff = exportRequest.getAssignedStaff();
            preStaff.setStatus(AccountStatus.ACTIVE);
            accountRepository.save(preStaff);
        }
        account.setStatus(AccountStatus.INACTIVE);
        accountRepository.save(account);
    }

    private void validateForTimeDate(LocalDate date, LocalTime time) {
        LOGGER.info("Validating time and date for export request");
        Configuration configuration = configurationRepository.findAll().stream()
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("Configuration not found with name: export request"));

        long minutesToAdd = configuration.getCreateRequestTimeAtLeast().getHour() * 60
                + configuration.getCreateRequestTimeAtLeast().getMinute();

        LOGGER.info("Check if date is in the past");
        if (date.isBefore(LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh")))) {
            throw new IllegalStateException("Cannot set time for  export request: Date is in the past");
        }

        if (time != null) {
            LOGGER.info("Check if time set is too early");
            if (date.isEqual(LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"))) &&
                    LocalTime.now()
                            .plusMinutes(minutesToAdd)
                            .isAfter(time)) {
                throw new IllegalStateException("Cannot set time for  export request: Time is too early");
            }

        }
    }

    private ExportRequest autoAssignCountingStaff(ExportRequest exportRequest) {
        ActiveAccountRequest activeAccountRequest = new ActiveAccountRequest();
        if(exportRequest.getIsExtended()) {
            activeAccountRequest.setDate(exportRequest.getExtendedDate());
        }else {
            activeAccountRequest.setDate(exportRequest.getCountingDate());
        }
        activeAccountRequest.setExportRequestId(exportRequest.getId());
        LOGGER.info("date assign to counting staff is: {}", activeAccountRequest.getDate());
        List<AccountResponse> accountResponse = accountService.getAllActiveStaffsInDate(activeAccountRequest);

        Account account = accountRepository.findById(accountResponse.get(0).getId())
                .orElseThrow(() -> new NoSuchElementException("Account not found with ID: " + accountResponse.get(0).getId()));
        exportRequest.setCountingStaffId(account.getId());
        setTimeForCountingStaffPerformance(account, exportRequest);
        autoAssignConfirmStaff(exportRequest);
        notificationService.handleNotification(
            NotificationUtil.STAFF_CHANNEL + account.getId(),
            NotificationUtil.EXPORT_REQUEST_ASSIGNED_EVENT,
            exportRequest.getId(),
            "Bạn được phân công cho đơn xuất mã #" + exportRequest.getId(),
            List.of(account)
        );
        return exportRequestRepository.save(exportRequest);

    }

    private void autoAssignConfirmStaff(ExportRequest exportRequest) {
        LOGGER.info("Auto assigning confirm staff for export request with ID: " + exportRequest.getId());
        ActiveAccountRequest activeAccountRequest = new ActiveAccountRequest();
        if(exportRequest.getIsExtended()) {
            activeAccountRequest.setDate(exportRequest.getExtendedDate());
        } else {
            activeAccountRequest.setDate(exportRequest.getExportDate());
        }
        activeAccountRequest.setExportRequestId(exportRequest.getId());
        Configuration configuration = configurationRepository.findAll().getFirst();
        List<AccountResponse> accountResponses = accountService.getAllActiveStaffsInDate(activeAccountRequest);
        List<AccountResponse> responses = new ArrayList<>();

        for(AccountResponse accountResponse : accountResponses) {
            List<ExportRequest> checkExportRequest = exportRequestRepository.findAllByAssignedStaff_IdAndExportDate(
                    accountResponse.getId(),
                    exportRequest.getExportDate()
            );
            responses.add(accountResponse);
        }

        Account account = accountRepository.findById(responses.get(0).getId())
                .orElseThrow(() -> new NoSuchElementException("Account not found with ID: " + responses.get(0).getId()));

        exportRequest.setAssignedStaff(account);
        LOGGER.info("Confirm Account is: {}", account.getEmail());
        StaffPerformance staffPerformance = new StaffPerformance();
        staffPerformance.setExpectedWorkingTime(configuration.getTimeToAllowConfirm());
        if(exportRequest.getIsExtended()) {
            staffPerformance.setDate(exportRequest.getExtendedDate());
        } else {
            staffPerformance.setDate(exportRequest.getExportDate());
        }
        staffPerformance.setAssignedStaff(account);
        staffPerformance.setExportCounting(false);
        staffPerformance.setExportRequestId(exportRequest.getId());
        staffPerformanceRepository.save(staffPerformance);
        exportRequestRepository.save(exportRequest);
        notificationService.handleNotification(
            NotificationUtil.STAFF_CHANNEL + account.getId(),
            NotificationUtil.EXPORT_REQUEST_ASSIGNED_EVENT,
            exportRequest.getId(),
            "Bạn được phân công cho đơn xuất mã #" + exportRequest.getId() + " để xác nhận số lượng",
            List.of(account)
        );
    }

    private String createExportRequestId() {
        String prefix = "PX";
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"));

        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);

        int todayCount = exportRequestRepository.countByCreatedAtBetween(startOfDay, endOfDay);

        String datePart = today.format(DateTimeFormatter.BASIC_ISO_DATE); // yyyyMMdd
        String sequence = String.format("%03d", todayCount + 1);          // 001, 002, ...

        return String.format("%s-%s-%s", prefix, datePart, sequence);
    }

} 