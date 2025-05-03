package capstonesu25.warehouse.service;

import capstonesu25.warehouse.entity.*;
import capstonesu25.warehouse.enums.DetailStatus;
import capstonesu25.warehouse.model.importorder.importorderdetail.ImportOrderDetailExcelRow;
import capstonesu25.warehouse.model.importorder.importorderdetail.ImportOrderDetailResponse;
import capstonesu25.warehouse.model.importorder.importorderdetail.ImportOrderDetailUpdateRequest;
import capstonesu25.warehouse.repository.*;
import capstonesu25.warehouse.utils.ExcelUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class ImportOrderDetailService {
    private final ImportOrderRepository importOrderRepository;
    private final ImportOrderDetailRepository importOrderDetailRepository;
    private final ItemRepository itemRepository;
    private final ImportRequestDetailRepository importRequestDetailRepository;
    private final ConfigurationRepository configurationRepository;

    private static final Logger LOGGER = LoggerFactory.getLogger(ImportOrderDetailService.class);

    public Page<ImportOrderDetailResponse> getAllByImportOrderId(Long importOrderId, int page, int limit) {
        LOGGER.info("Getting all import order detail by import order id: {}", importOrderId);
        Pageable pageable = PageRequest.of(Math.max(0, page - 1), limit);
        Page<ImportOrderDetail> importOrderDetailPage = importOrderDetailRepository.
                findImportOrderDetailByImportOrder_Id(importOrderId, pageable);
        return importOrderDetailPage.map(this::mapToResponse);
    }

    public ImportOrderDetailResponse getById(Long importOrderDetailId) {
        LOGGER.info("Getting import order detail by id: {}", importOrderDetailId);
        ImportOrderDetail importOrderDetail = importOrderDetailRepository.findById(importOrderDetailId).orElseThrow();
        return mapToResponse(importOrderDetail);
    }

    public void createFromExcel(MultipartFile file, Long importOrderId) {
        LOGGER.info("Creating import order details from Excel file");

        ImportOrder importOrder = importOrderRepository.findById(importOrderId)
                .orElseThrow(() -> new NoSuchElementException("Import Order not found with ID: " + importOrderId));

        List<ImportOrderDetailExcelRow> requests = ExcelUtil.processExcelFile(file, ImportOrderDetailExcelRow.class);

        if (requests.isEmpty()) {
            throw new IllegalArgumentException("Import order detail list cannot be empty");
        }

        validateItemsExistInImportRequest(requests, importOrder.getImportRequest());
        validateSameProvider(requests);

        createImportOrderDetails(importOrder, requests);
        updateOrderedQuantityOfImportRequestDetail(importOrderId);

        LOGGER.info("Successfully created import order details for importOrderId: {}", importOrderId);
    }


    private void validateItemsExistInImportRequest(List<ImportOrderDetailExcelRow> requests, ImportRequest importRequest) {
        List<Long> validItemIds = importRequest.getDetails().stream()
                .map(detail -> detail.getItem().getId())
                .toList();

        List<Long> allItemIdsFromExcel = requests.stream()
                .flatMap(request -> request.getItemId().stream())
                .toList();

        List<Long> invalidItemIds = allItemIdsFromExcel.stream()
                .filter(itemId -> !validItemIds.contains(itemId))
                .distinct()
                .toList();

        if (!invalidItemIds.isEmpty()) {
            throw new IllegalArgumentException(
                    "The following items are not in the original Import Request: " + invalidItemIds
            );
        }
    }


    private void validateSameProvider(List<ImportOrderDetailExcelRow> requests) {
        if (requests.isEmpty() || requests.get(0).getItemId().isEmpty()) {
            throw new IllegalArgumentException("Item list cannot be empty for provider validation");
        }

        // Get the providerId of the first item in the first row
        Long referenceProviderId = itemRepository.findById(requests.get(0).getItemId().get(0))
                .map(item -> item.getProvider().getId())
                .orElseThrow(() -> new NoSuchElementException("Item not found with ID: " + requests.get(0).getItemId().get(0)));

        for (ImportOrderDetailExcelRow request : requests) {
            for (Long itemId : request.getItemId()) {
                Long currentProviderId = itemRepository.findById(itemId)
                        .map(item -> item.getProvider().getId())
                        .orElseThrow(() -> new NoSuchElementException("Item not found with ID: " + itemId));

                if (!referenceProviderId.equals(currentProviderId)) {
                    throw new IllegalArgumentException("All items must belong to the same provider.");
                }
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
        if(date.isBefore(LocalDate.now())) {
            throw new IllegalStateException("Cannot set time for import order: Date is in the past");
        }

        LOGGER.info("Check if time set is too early");
        if (date.isEqual(LocalDate.now()) &&
                LocalTime.now()
                        .plusMinutes(minutesToAdd)
                        .isAfter(time)) {
            throw new IllegalStateException("Cannot set time for import order: Time is too early");
        }

    }

    private void createImportOrderDetails(ImportOrder importOrder, List<ImportOrderDetailExcelRow> requests) {
        for (ImportOrderDetailExcelRow request : requests) {
            List<Long> itemIds = request.getItemId();
            List<Integer> quantities = request.getQuantity();

            if (itemIds.size() != quantities.size()) {
                throw new IllegalArgumentException("Mismatched itemId and quantity list sizes in Excel row");
            }

            // Set shared fields once per request
            validateForTimeDate(request.getDateReceived(), request.getTimeReceived());
            importOrder.setDateReceived(request.getDateReceived());
            importOrder.setTimeReceived(request.getTimeReceived());
            importOrder.setNote(request.getNote());
            importOrderRepository.save(importOrder);

            for (int i = 0; i < itemIds.size(); i++) {
                Long itemId = itemIds.get(i);
                Integer quantity = quantities.get(i);

                ImportOrderDetail detail = new ImportOrderDetail();
                detail.setImportOrder(importOrder);
                detail.setExpectQuantity(quantity);
                detail.setActualQuantity(0);
                detail.setStatus(DetailStatus.LACK);
                detail.setItem(itemRepository.findById(itemId)
                        .orElseThrow(() -> new RuntimeException("Item not found with ID: " + itemId)));

                importOrderDetailRepository.save(detail);
            }
        }
    }

    public void updateActualQuantities(List<ImportOrderDetailUpdateRequest> requests, Long importOrderId) {
        LOGGER.info("Updating actual quantities for ImportOrder ID: {}", importOrderId);

        importOrderRepository.findById(importOrderId)
                .orElseThrow(() -> new NoSuchElementException("ImportOrder not found with ID: " + importOrderId));

        List<ImportOrderDetail> details = importOrderDetailRepository.findImportOrderDetailByImportOrder_Id(importOrderId);

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

    private void updateOrderedQuantityOfImportRequestDetail(Long importOrderId) {
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

    private ImportOrderDetailResponse mapToResponse(ImportOrderDetail importOrderDetail) {
        return new ImportOrderDetailResponse(
                importOrderDetail.getId(),
                importOrderDetail.getImportOrder().getId(),
                importOrderDetail.getItem().getId(),
                importOrderDetail.getItem().getName(),
                importOrderDetail.getExpectQuantity(),
                importOrderDetail.getActualQuantity(),
                importOrderDetail.getStatus()
        );
    }
}
