package capstonesu25.warehouse.service;

import capstonesu25.warehouse.entity.*;
import capstonesu25.warehouse.enums.*;
import capstonesu25.warehouse.model.account.AccountResponse;
import capstonesu25.warehouse.model.account.ActiveAccountRequest;
import capstonesu25.warehouse.model.exportrequest.exportrequestdetail.ExportRequestDetailRequest;
import capstonesu25.warehouse.model.exportrequest.exportrequestdetail.ExportRequestDetailResponse;
import capstonesu25.warehouse.repository.*;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExportRequestDetailService {
    private final ExportRequestRepository exportRequestRepository;
    private final ExportRequestDetailRepository exportRequestDetailRepository;
    private final ItemRepository itemRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final AccountService accountService;
    private final AccountRepository accountRepository;
    private final ConfigurationRepository configurationRepository;
    private final StaffPerformanceRepository staffPerformanceRepository;

    private static final Integer LIQUIDATION = 30;
    private static final Logger LOGGER = LoggerFactory.getLogger(ExportRequestDetailService.class);

    public Page<ExportRequestDetailResponse> getAllByExportRequestId(String exportRequestId, int page, int limit) {
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

    public void createExportRequestDetail(List<ExportRequestDetailRequest> exportRequestDetailRequests, String exportRequestId) {
        LOGGER.info("Finding export request by id: {}", exportRequestId);

        ExportRequest exportRequest = exportRequestRepository.findById(exportRequestId)
                .orElseThrow(() -> new RuntimeException("Export request not found"));
        if (exportRequest.getType().equals(ExportType.RETURN)) {
            LOGGER.info("Export request type is RETURN, skipping validation for import order details");
            List<ImportOrderDetail> importOrderDetails = exportRequest.getImportOrder().getImportOrderDetails();

            for (ExportRequestDetailRequest requestDetail : exportRequestDetailRequests) {
                ImportOrderDetail importDetail = importOrderDetails.stream()
                        .filter(detail -> detail.getItem().getId().equals(requestDetail.getItemId()))
                        .findFirst()
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Item ID " + requestDetail.getItemId() + " not found in the import order"));

                if (requestDetail.getQuantity() > importDetail.getActualQuantity()) {
                    throw new IllegalArgumentException("Requested quantity for item " + requestDetail.getItemId() +
                            " (" + requestDetail.getQuantity() + ") exceeds quantity in import order (" +
                            importDetail.getActualQuantity() + ")");
                }
            }
        }
        // Create export request details from the request data
        for (ExportRequestDetailRequest request : exportRequestDetailRequests) {
            ExportRequestDetail exportRequestDetail = new ExportRequestDetail();
            exportRequestDetail.setExportRequest(exportRequest);

            if (request.getMeasurementValue() != null) {
                exportRequestDetail.setMeasurementValue(request.getMeasurementValue());
            }
            if(exportRequest.getType().equals(ExportType.INTERNAL)) {
                exportRequestDetail.setQuantity(0);
            }else {
                exportRequestDetail.setQuantity(request.getQuantity());
            }
            exportRequestDetail.setActualQuantity(0);

            // Get Item by ID
            Item item = itemRepository.findById(request.getItemId()).orElseThrow(
                    () -> new RuntimeException("Item not found with ID: " + request.getItemId())
            );


            // Handling for  BORROWING types
            if(exportRequest.getType().equals(ExportType.INTERNAL)) {
                exportRequestDetail.setMeasurementValue(request.getMeasurementValue());
            }

            exportRequestDetail.setItem(item);
            exportRequestDetailRepository.save(exportRequestDetail);
        }

        // Save export request details to the repository
        List<ExportRequestDetail> savedDetails = exportRequest.getExportRequestDetails();
        LOGGER.info("Export request details created successfully with size: {}", savedDetails.size());

        // Choose inventory items for export request detail
            LOGGER.info("Choosing inventory items for export request detail ");
            chooseInventoryItemsForThoseCases(savedDetails);

        // Auto assign counting staff for the export request
        if(!exportRequest.getType().equals(ExportType.RETURN)){
                LOGGER.info("Auto assigning counting staff for export request with ID: {}", exportRequestId);
                 autoAssignCountingStaff(exportRequest);
        }
        if(exportRequest.getAssignedStaff() == null) {
            autoAssignConfirmStaff(exportRequest);
        }

    }


    public ExportRequestDetailResponse updateActualQuantity(Long exportRequestDetailId, String inventoryItemId) {
        LOGGER.info("Updating actual quantity for export request detail with ID: {}", exportRequestDetailId);
        ExportRequestDetail exportRequestDetail = exportRequestDetailRepository.findById(exportRequestDetailId)
                .orElseThrow(() -> new RuntimeException("Export request detail not found"));

       ExportRequest exportRequest = exportRequestDetail.getExportRequest();


       InventoryItem inventoryItem = inventoryItemRepository.findById(inventoryItemId)
               .orElseThrow(() -> new RuntimeException("Inventory item not found"));

       if(!exportRequestDetail.getInventoryItems().contains(inventoryItem)) {
           throw new IllegalArgumentException("Inventory item with ID: "+inventoryItemId+ "is not stable for export request detail ");
       }

       if(inventoryItem.getIsTrackingForExport() == true) {
           throw new IllegalArgumentException("Inventory item with ID: "+inventoryItemId+" has been tracked");
       }
        if(exportRequestDetail.getActualQuantity() >= exportRequestDetail.getQuantity()) {
            throw new IllegalArgumentException("Actual quantity cannot be greater to requested quantity");
        }
       exportRequestDetail.setActualQuantity(exportRequestDetail.getActualQuantity() + 1);
       inventoryItem.setIsTrackingForExport(true);
        exportRequest.getExportRequestDetails().forEach(detail -> {
            if (detail.getStatus() == null) {
                detail.setStatus(DetailStatus.LACK);
                exportRequestDetailRepository.save(detail);
            }
        });
       exportRequestDetail.setStatus(DetailStatus.LACK);
       if(exportRequestDetail.getActualQuantity().equals(exportRequestDetail.getQuantity())) {
           exportRequestDetail.setStatus(DetailStatus.MATCH);
       }
       inventoryItemRepository.save(inventoryItem);

        return mapToResponse(exportRequestDetailRepository.save(exportRequestDetail));
    }

    public ExportRequestDetailResponse resetTracking (Long exportRequestDetailId, String inventoryItemId) {
        LOGGER.info("Resetting tracking for export request detail with ID: {}", exportRequestDetailId);
        ExportRequestDetail exportRequestDetail = exportRequestDetailRepository.findById(exportRequestDetailId)
                .orElseThrow(() -> new RuntimeException("Export request detail not found"));

        InventoryItem inventoryItem = inventoryItemRepository.findById(inventoryItemId)
                .orElseThrow(() -> new RuntimeException("Inventory item not found"));

        if(!exportRequestDetail.getInventoryItems().contains(inventoryItem)) {
            throw new IllegalArgumentException("Inventory item with ID: "+inventoryItemId+ "is not stable for export request detail ");
        }

        if(!inventoryItem.getIsTrackingForExport()) {
            throw new IllegalArgumentException("Inventory item with ID: "+inventoryItemId+" is not being tracked");
        }

        inventoryItem.setIsTrackingForExport(false);
        inventoryItemRepository.save(inventoryItem);

        exportRequestDetail.setActualQuantity(exportRequestDetail.getActualQuantity() - 1);
        if(exportRequestDetail.getActualQuantity() < exportRequestDetail.getQuantity()) {
            exportRequestDetail.setStatus(DetailStatus.LACK);
        }
        return mapToResponse(exportRequestDetailRepository.save(exportRequestDetail));
    }

    private void chooseInventoryItemsForThoseCases(List<ExportRequestDetail> exportRequestDetails) {
        Map<ExportType, List<ExportRequestDetail>> grouped = exportRequestDetails.stream()
                .collect(Collectors.groupingBy(detail -> detail.getExportRequest().getType()));

        grouped.forEach((type, details) -> {
            switch (type) {
                case RETURN -> autoChooseInventoryItemsForReturn(details);
                case SELLING -> autoChooseInventoryItemsForSelling(details);
//                case LIQUIDATION -> autoChooseInventoryItemsForLiquidation(details);
                default -> autoChooseInventoryItemsForProduction(details); // INTERNAL
            }
        });
    }

    private void autoChooseInventoryItemsForSelling(List<ExportRequestDetail> details) {
        LOGGER.info("Auto choosing inventory items for SELLING for {} details", details.size());

        for (ExportRequestDetail detail : details) {
            int quantity = detail.getQuantity();

            List<InventoryItem> sortedInventoryItems = inventoryItemRepository
                    .findByItem_IdAndParentNullAndStatus(detail.getItem().getId(), ItemStatus.AVAILABLE)
                    .stream()
                    .sorted(Comparator.comparing(InventoryItem::getImportedDate).reversed())
                    .limit(quantity)
                    .toList();

            for (InventoryItem inventoryItem : sortedInventoryItems) {
                inventoryItem.setExportRequestDetail(detail);
                inventoryItem.setStatus(ItemStatus.UNAVAILABLE);
                inventoryItemRepository.save(inventoryItem);
            }

            LOGGER.info("Done choosing inventory for SELLING detail ID: {}", detail.getId());
        }
    }

    private void autoChooseInventoryItemsForReturn(List<ExportRequestDetail> details) {
        LOGGER.info("Auto choosing inventory items for RETURN for {} details", details.size());

        for (ExportRequestDetail detail : details) {
            int quantity = detail.getQuantity();
            detail.setActualQuantity(quantity);
            detail.setMeasurementValue(detail.getItem().getMeasurementValue() * quantity);
            detail.setActualMeasurementValue(detail.getItem().getMeasurementValue() * quantity);
            detail.setStatus(DetailStatus.MATCH);
            ImportOrder importOrder = detail.getExportRequest().getImportOrder();

            List<InventoryItem> inventoryItems = importOrder.getImportOrderDetails().stream()
                    .filter(d -> detail.getItem().getId().equals(d.getItem().getId()))
                    .flatMap(d -> d.getInventoryItems().stream())
                    .filter(item -> item.getExportRequestDetail() == null)
                    .limit(quantity)
                    .toList();

            if (inventoryItems.size() < quantity) {
                throw new IllegalArgumentException("Không đủ inventory items để hoàn trả cho item " + detail.getItem().getId() +
                        ". Yêu cầu: " + quantity + ", Có sẵn: " + inventoryItems.size());
            }

            for (InventoryItem inventoryItem : inventoryItems) {
                inventoryItem.setExportRequestDetail(detail);
                inventoryItem.setStatus(ItemStatus.RETURN);
                inventoryItemRepository.save(inventoryItem);
            }

            exportRequestDetailRepository.save(detail);
            LOGGER.info("Done choosing inventory for RETURN detail ID: {}", detail.getId());
        }
    }


    private void autoChooseInventoryItemsForProduction(List<ExportRequestDetail> details) {
        LOGGER.info("Auto choosing inventory items for PRODUCTION for {} details", details.size());

        Configuration configuration = configurationRepository.findAll().getFirst();
        Map<Item, Pair<Double, Integer>> excessMap = new HashMap<>();

        for (ExportRequestDetail detail : details) {
            List<InventoryItem> inventoryItemList = inventoryItemRepository.findByItem_Id(detail.getItem().getId());

            List<InventoryItem> sortedInventoryItems = inventoryItemList.stream().
                    filter(inventoryItem -> inventoryItem.getExportRequestDetail() == null &&
                            inventoryItem.getStatus() == ItemStatus.AVAILABLE &&
                            inventoryItem.getMeasurementValue() > 0)
                    .sorted(Comparator.comparing(InventoryItem::getMeasurementValue))
                    .toList();

            double requestedMeasurement = detail.getMeasurementValue();
            double maxAllowedMeasurement = requestedMeasurement + (requestedMeasurement * configuration.getMaxDispatchErrorPercent() / 100);

            List<InventoryItem> selectedItems = new ArrayList<>();
            double total = 0.0;

            for (InventoryItem item : sortedInventoryItems) {
                if (total >= requestedMeasurement) break;
                selectedItems.add(item);
                total += item.getMeasurementValue();
            }

            if (total < requestedMeasurement) {
                throw new IllegalArgumentException("Không thể chọn đủ inventory items cho item " + detail.getItem().getId() + ", yêu cầu: " + requestedMeasurement);
            }

            if (total > maxAllowedMeasurement) {
                double excess = total - requestedMeasurement;
                int quantity = selectedItems.size();

                excessMap.merge(detail.getItem(),
                        Pair.of(excess, quantity),
                        (oldVal, newVal) -> Pair.of(
                                oldVal.getLeft() + newVal.getLeft(),
                                oldVal.getRight() + newVal.getRight()
                        )
                );
            }

            detail.setQuantity(selectedItems.size());
            exportRequestDetailRepository.save(detail);

            for (InventoryItem item : selectedItems) {
                item.setExportRequestDetail(detail);
                item.setStatus(ItemStatus.UNAVAILABLE);
                inventoryItemRepository.save(item);
            }

            LOGGER.info("Done choosing inventory for PRODUCTION detail ID: {}", detail.getId());
        }

    }

    private void autoAssignCountingStaff(ExportRequest exportRequest) {
        ActiveAccountRequest activeAccountRequest = ActiveAccountRequest.builder()
                .date(exportRequest.getCountingDate())
                .exportRequestId(exportRequest.getId())
                .build();

        List<AccountResponse> accountResponse = accountService.getAllActiveStaffsInDate(activeAccountRequest);

        Account account = accountRepository.findById(accountResponse.get(0).getId())
                .orElseThrow(() -> new NoSuchElementException("Account not found with ID: " + accountResponse.get(0).getId()));
        exportRequest.setCountingStaffId(account.getId());
        setTimeForCountingStaffPerformance(account, exportRequest);
        exportRequestRepository.save(exportRequest);
        autoAssignConfirmStaff(exportRequest);

    }
    private void setTimeForCountingStaffPerformance(Account account, ExportRequest exportRequest) {
        int totalMinutes = 0;
        for (ExportRequestDetail detail : exportRequest.getExportRequestDetails()) {
            LOGGER.info("Calculating expected working time for item " );
            totalMinutes += detail.getQuantity() * detail.getItem().getCountingMinutes();
        }
        LocalTime expectedWorkingTime = LocalTime.of(0, 0).plusMinutes(totalMinutes);
        StaffPerformance staffPerformance = new StaffPerformance();
        staffPerformance.setExpectedWorkingTime(expectedWorkingTime);
        staffPerformance.setDate(exportRequest.getCountingDate());
        staffPerformance.setExportRequestId(exportRequest.getId());
        staffPerformance.setAssignedStaff(account);
        staffPerformance.setExportCounting(true);
        staffPerformanceRepository.save(staffPerformance);
        LOGGER.info("Expected working time for counting staff: " + expectedWorkingTime);
    }
    private void autoAssignConfirmStaff(ExportRequest exportRequest) {
        LOGGER.info("Auto assigning confirm staff for export request with ID: " + exportRequest.getId());
        ActiveAccountRequest activeAccountRequest = new ActiveAccountRequest();
        activeAccountRequest.setDate(exportRequest.getExportDate());
        Configuration configuration = configurationRepository.findAll().getFirst();
        List<AccountResponse> accountResponses = accountService.getAllActiveStaffsInDate(activeAccountRequest);
        List<AccountResponse> responses = new ArrayList<>();

        for(AccountResponse accountResponse : accountResponses) {
            List<ExportRequest> checkExportRequest = exportRequestRepository.findAllByAssignedStaff_IdAndExportDate(
                    accountResponse.getId(),
                    exportRequest.getExportDate()
            );
            LOGGER.info("Checking export requests size {} ", checkExportRequest.size());
                responses.add(accountResponse);

        }

        Account account = accountRepository.findById(responses.get(0).getId())
                .orElseThrow(() -> new NoSuchElementException("Account not found with ID: " + responses.get(0).getId()));

        exportRequest.setAssignedStaff(account);
        LOGGER.info("Confirm Account is: {}", account.getEmail());
        StaffPerformance staffPerformance = new StaffPerformance();
        staffPerformance.setExpectedWorkingTime(configuration.getTimeToAllowConfirm());
        staffPerformance.setDate(exportRequest.getExportDate());
        staffPerformance.setAssignedStaff(account);
        staffPerformance.setExportCounting(false);
        staffPerformance.setExportRequestId(exportRequest.getId());
        staffPerformanceRepository.save(staffPerformance);
        exportRequestRepository.save(exportRequest);
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
            List<String> inventoryItemIds = exportRequestDetail.getInventoryItems().stream()
                    .map(InventoryItem::getId)
                    .collect(Collectors.toList());
            response.setInventoryItemIds(inventoryItemIds);
        } else {
            response.setInventoryItemIds(new ArrayList<>());
        }
        return response;
    }

}