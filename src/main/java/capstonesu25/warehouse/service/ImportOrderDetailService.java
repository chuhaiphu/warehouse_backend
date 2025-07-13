package capstonesu25.warehouse.service;

import capstonesu25.warehouse.annotation.transactionLog.TransactionLoggable;
import capstonesu25.warehouse.entity.*;
import capstonesu25.warehouse.enums.AccountRole;
import capstonesu25.warehouse.enums.AccountStatus;
import capstonesu25.warehouse.enums.DetailStatus;
import capstonesu25.warehouse.enums.RequestStatus;
import capstonesu25.warehouse.model.account.AccountResponse;
import capstonesu25.warehouse.model.account.ActiveAccountRequest;
import capstonesu25.warehouse.model.importorder.ImportOrderResponse;
import capstonesu25.warehouse.model.importorder.importorderdetail.ImportOrderDetailRequest;
import capstonesu25.warehouse.model.importorder.importorderdetail.ImportOrderDetailResponse;
import capstonesu25.warehouse.model.importorder.importorderdetail.ImportOrderDetailUpdateRequest;
import capstonesu25.warehouse.repository.*;
import capstonesu25.warehouse.utils.Mapper;
import capstonesu25.warehouse.utils.NotificationUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.aop.framework.AopContext;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ImportOrderDetailService {
    private final ImportOrderRepository importOrderRepository;
    private final ImportOrderDetailRepository importOrderDetailRepository;
    private final ItemRepository itemRepository;
    private final ImportRequestDetailRepository importRequestDetailRepository;
    private final ConfigurationRepository configurationRepository;
    private final AccountRepository accountRepository;
    private final AccountService accountService;
    private final StaffPerformanceRepository staffPerformanceRepository;
    private final NotificationService notificationService;

    private static final Logger LOGGER = LoggerFactory.getLogger(ImportOrderDetailService.class);

    public Page<ImportOrderDetailResponse> getAllByImportOrderId(String importOrderId, int page, int limit) {
        LOGGER.info("Getting all import order detail by import order id: {}", importOrderId);
        Pageable pageable = PageRequest.of(Math.max(0, page - 1), limit);
        Page<ImportOrderDetail> importOrderDetailPage = importOrderDetailRepository
                .findImportOrderDetailByImportOrder_Id(importOrderId, pageable);
        return importOrderDetailPage.map(Mapper::mapToImportOrderDetailResponse);
    }

    public ImportOrderDetailResponse getById(Long importOrderDetailId) {
        LOGGER.info("Getting import order detail by id: {}", importOrderDetailId);
        ImportOrderDetail importOrderDetail = importOrderDetailRepository.findById(importOrderDetailId).orElseThrow();
        return Mapper.mapToImportOrderDetailResponse(importOrderDetail);
    }

    public ImportOrderResponse create(ImportOrderDetailRequest request, String importOrderId) {
        LOGGER.info("Creating import order detail for import order id: {}", importOrderId);
        ImportOrder importOrder = importOrderRepository.findById(importOrderId)
                .orElseThrow(() -> new NoSuchElementException("Import Order not found with ID: " + importOrderId));

        checkSameProvider(request);
        ImportOrderResponse importOrderResponse = ((ImportOrderDetailService) AopContext.currentProxy())
                .createImportOrderDetails(importOrder, request);

        ActiveAccountRequest activeAccountRequest = new ActiveAccountRequest();
        activeAccountRequest.setDate(importOrder.getDateReceived());
        activeAccountRequest.setImportOrderId(importOrder.getId());

        List<AccountResponse> accountResponse = accountService.getAllActiveStaffsInDate(activeAccountRequest);
        if (!accountResponse.isEmpty()) {
            importOrderResponse = ((ImportOrderDetailService) AopContext.currentProxy())
                    .assignStaff(importOrder.getId(), accountResponse.get(0).getId());
        }

        updateOrderedQuantityOfImportRequestDetail(importOrderId);
        return importOrderResponse;
    }

    @TransactionLoggable(type = "IMPORT_ORDER", action = "CREATE", objectIdSource = "importOrderId")
    public ImportOrderResponse createImportOrderDetails(ImportOrder importOrder, ImportOrderDetailRequest request) {
        return createImportOrderDetailsInternal(importOrder, request);
    }

    private ImportOrderResponse createImportOrderDetailsInternal(ImportOrder importOrder,
            ImportOrderDetailRequest request) {
        LOGGER.info("Setting date, time and note for import order");

        for (ImportOrderDetailRequest.ImportOrderItem importOrderItem : request.getImportOrderItems()) {
            Item item = itemRepository.findById(importOrderItem.getItemId())
                    .orElseThrow(() -> new RuntimeException("Item not found with ID: " + importOrderItem.getItemId()));
            ImportOrderDetail detail = getDetail(importOrder, importOrderItem, item);
            if (detail != null) {
                importOrderDetailRepository.save(detail);
            }
        }

        return Mapper.mapToImportOrderResponse(importOrder);
    }

    private void checkSameProvider(ImportOrderDetailRequest request) {
        LOGGER.info("Checking if all items belong to the same provider");
        for (ImportOrderDetailRequest.ImportOrderItem importOrderItem : request.getImportOrderItems()) {
            Item item = itemRepository.findById(importOrderItem.getItemId())
                    .orElseThrow(
                            () -> new NoSuchElementException("Item not found with ID: " + importOrderItem.getItemId()));
            boolean providerMatch = item.getProviders().stream()
                    .anyMatch(provider -> Objects.equals(provider.getId(), request.getProviderId()));

            if (!providerMatch) {
                throw new IllegalArgumentException("Item with ID: " + importOrderItem.getItemId() +
                        " does not belong to the provider with ID: " + request.getProviderId());
            }
        }
    }

    private void validateForTimeDate(LocalDate date, LocalTime time) {
        LOGGER.info("Validating time and date for import order");
        Configuration configuration = configurationRepository.findAll().stream()
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("Configuration not found with name: importOrder"));

        long minutesToAdd = configuration.getCreateRequestTimeAtLeast().getHour() * 60
                + configuration.getCreateRequestTimeAtLeast().getMinute();

        LOGGER.info("Check if date is in the past");
        if (date.isBefore(LocalDate.now())) {
            throw new IllegalStateException("Cannot set time for import order: Date is in the past");
        }

        if (date.isEqual(LocalDate.now()) && time.isBefore(LocalTime.now())) {
            throw new IllegalStateException("Cannot set time for import order: Time is in the past");
        }
        LOGGER.info("Check if time set is too early");
        if (date.isEqual(LocalDate.now()) &&
                LocalTime.now()
                        .plusMinutes(minutesToAdd)
                        .isAfter(time)) {
            throw new IllegalStateException("Cannot set time for import order: Time is too early");
        }

    }

    @TransactionLoggable(type = "IMPORT_ORDER", action = "ASSIGN_STAFF", objectIdSource = "importOrderId")
    public ImportOrderResponse assignStaff(String importOrderId, Long accountId) {
        LOGGER.info("Assigning staff to import order: " + importOrderId);

        ImportOrder importOrder = importOrderRepository.findById(importOrderId)
                .orElseThrow(() -> new NoSuchElementException("Import Order not found with ID: " + importOrderId));

        if (importOrder.getAssignedStaff() != null) {
            LOGGER.info("Return working for pre staff: {}", importOrder.getAssignedStaff().getEmail());
            StaffPerformance staffPerformance = staffPerformanceRepository
                    .findByImportOrderIdAndAssignedStaff_Id(importOrderId, importOrder.getAssignedStaff().getId());
            if (staffPerformance != null) {
                LOGGER.info("Delete working time for pre staff: {}", importOrder.getAssignedStaff().getEmail());
                staffPerformanceRepository.delete(staffPerformance);
                notificationService.handleNotification(
                        NotificationUtil.STAFF_CHANNEL + importOrder.getAssignedStaff().getId(),
                        NotificationUtil.IMPORT_ORDER_ASSIGNED_EVENT,
                        importOrder.getId(),
                        "Bạn đã được hủy phân công cho đơn nhập mã #" + importOrder.getId(),
                        List.of(importOrder.getAssignedStaff()));
            }
        }

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new NoSuchElementException("Account not found with ID: " + accountId));
        validateAccountForAssignment(account);
        setTimeForStaffPerformance(account, importOrder);
        importOrder.setAssignedStaff(account);
        notificationService.handleNotification(
                NotificationUtil.STAFF_CHANNEL + account.getId(),
                NotificationUtil.IMPORT_ORDER_ASSIGNED_EVENT,
                importOrder.getId(),
                "Bạn được phân công cho đơn nhập mã #" + importOrder.getId(),
                List.of(account));
        importOrder.setStatus(RequestStatus.IN_PROGRESS);
        ImportOrder savedImportOrder = importOrderRepository.save(importOrder);
        return Mapper.mapToImportOrderResponse(savedImportOrder);
    }

    private void validateAccountForAssignment(Account account) {
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new IllegalStateException("Cannot assign staff: Account is not active");
        }

        if (account.getRole() != AccountRole.STAFF) {
            throw new IllegalStateException("Cannot assign staff: Account is not a staff member");
        }
    }

    private ImportOrderDetail getDetail(ImportOrder importOrder,
            ImportOrderDetailRequest.ImportOrderItem importOrderItem, Item item) {
        LOGGER.info("Creating import order detail for item: " + item.getName());
        ImportOrderDetail detail = new ImportOrderDetail();
        detail.setImportOrder(importOrder);

        ImportRequestDetail importRequestDetail = item.getImportRequestDetails().stream()
                .filter(ird -> ird.getImportRequest().getId().equals(importOrder.getImportRequest().getId()))
                .filter(ird -> ird.getItem().getId().equals(importOrderItem.getItemId()))
                .findFirst()
                .orElse(null);

        if (importRequestDetail != null) {
            if (importRequestDetail.getActualQuantity() == 0) {
                // If no actual imports yet, check against ordered quantity
                if (importRequestDetail.getExpectQuantity() <= importRequestDetail.getOrderedQuantity()
                        || (importOrderItem.getQuantity()
                                + importRequestDetail.getOrderedQuantity()) > importRequestDetail.getExpectQuantity()) {
                    LOGGER.info("Item quantity exceeds expected quantity (ordered quantity check)");
                    return null;
                }
                if (importOrderItem.getQuantity() >= (importRequestDetail.getExpectQuantity())
                        - importRequestDetail.getOrderedQuantity()) {
                    LOGGER.info("Item quantity is more than expected quantity (ordered quantity check)");
                    int remainingQuantity = importRequestDetail.getExpectQuantity()
                            - importRequestDetail.getOrderedQuantity();
                    detail.setExpectQuantity(remainingQuantity);
                } else {
                    LOGGER.info("Item quantity is less than expected quantity");
                    detail.setExpectQuantity(importOrderItem.getQuantity());
                }
            } else {
                // If there are actual imports, check against actual quantity
                if (importRequestDetail.getExpectQuantity() <= importRequestDetail.getActualQuantity()
                        || (importOrderItem.getQuantity()
                                + importRequestDetail.getActualQuantity()) > importRequestDetail.getExpectQuantity()) {
                    LOGGER.info("Item quantity exceeds expected quantity (actual quantity check)");
                    return null;
                }
                if (importOrderItem.getQuantity() >= (importRequestDetail.getExpectQuantity())
                        - importRequestDetail.getActualQuantity()) {
                    LOGGER.info("Item quantity is more than expected quantity (actual quantity check)");
                    int remainingQuantity = importRequestDetail.getExpectQuantity()
                            - importRequestDetail.getActualQuantity();
                    detail.setExpectQuantity(remainingQuantity);
                } else {
                    LOGGER.info("Item quantity is less than expected quantity");
                    detail.setExpectQuantity(importOrderItem.getQuantity());
                }
            }
        }
        detail.setActualQuantity(0);
        detail.setItem(item);
        return detail;
    }

    private void setTimeForStaffPerformance(Account account, ImportOrder importOrder) {
        LOGGER.info("Setting expected working time for staff performance");
        int totalMinutes = 0;
        for (ImportOrderDetail detail : importOrder.getImportOrderDetails()) {
            LOGGER.info("Calculating expected working time for item: " + detail.getItem().getName());
            totalMinutes += detail.getExpectQuantity() * detail.getItem().getCountingMinutes();
        }
        LocalTime expectedWorkingTime = LocalTime.of(0, 0).plusMinutes(totalMinutes);
        LOGGER.info("Expected working time for staff: " + expectedWorkingTime);
        StaffPerformance staffPerformance = new StaffPerformance();
        staffPerformance.setExpectedWorkingTime(expectedWorkingTime);
        staffPerformance.setDate(importOrder.getDateReceived());
        staffPerformance.setImportOrderId(importOrder.getId());
        staffPerformance.setAssignedStaff(account);
        staffPerformanceRepository.save(staffPerformance);
    }

    public void updateActualQuantities(List<ImportOrderDetailUpdateRequest> requests, String importOrderId) {
        LOGGER.info("Updating actual quantities for ImportOrder ID: {}", importOrderId);

        importOrderRepository.findById(importOrderId)
                .orElseThrow(() -> new NoSuchElementException("ImportOrder not found with ID: " + importOrderId));

        List<ImportOrderDetail> details = importOrderDetailRepository
                .findImportOrderDetailByImportOrder_Id(importOrderId);
        if (details.isEmpty()) {
            throw new NoSuchElementException("No ImportOrderDetails found for ImportOrder ID: " + importOrderId);
        }

        for (ImportOrderDetail detail : details) {
            requests.stream()
                    .filter(request -> request.getItemId().equals(detail.getItem().getId()))
                    .findFirst()
                    .ifPresent(request -> {
                        detail.setActualQuantity(request.getActualQuantity());
                        updateDetailStatus(detail);
                        importOrderDetailRepository.save(detail);
                    });
        }
    }

    private void updateDetailStatus(ImportOrderDetail detail) {
        if (detail.getActualQuantity() < detail.getExpectQuantity()) {
            detail.setStatus(DetailStatus.LACK);
        } else if (detail.getActualQuantity() > detail.getExpectQuantity()) {
            detail.setStatus(DetailStatus.EXCESS);
        } else {
            detail.setStatus(DetailStatus.MATCH);
        }
    }

    private void updateOrderedQuantityOfImportRequestDetail(String importOrderId) {
        LOGGER.info("Update remaining quantity of import request detail");
        ImportOrder importOrder = importOrderRepository.findById(importOrderId)
                .orElseThrow(() -> new NoSuchElementException("ImportOrder not found with ID: " + importOrderId));
        ImportRequest importRequest = importOrder.getImportRequest();

        for (ImportRequestDetail detail : importRequest.getDetails()) {
            importOrder.getImportOrderDetails().stream()
                    .filter(orderDetail -> orderDetail.getItem().getId().equals(detail.getItem().getId()))
                    .findFirst()
                    .ifPresent(orderDetail -> {
                        detail.setOrderedQuantity(detail.getOrderedQuantity() + orderDetail.getExpectQuantity());
                        importRequestDetailRepository.save(detail);
                    });
        }
    }

    public void delete(Long importOrderDetailId) {
        LOGGER.info("Deleting import order detail by id: {}", importOrderDetailId);
        importOrderDetailRepository.deleteById(importOrderDetailId);
    }

}
