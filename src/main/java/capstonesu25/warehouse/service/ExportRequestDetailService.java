package capstonesu25.warehouse.service;

import capstonesu25.warehouse.entity.*;
import capstonesu25.warehouse.enums.DetailStatus;
import capstonesu25.warehouse.enums.ExportType;
import capstonesu25.warehouse.model.account.AccountResponse;
import capstonesu25.warehouse.model.account.ActiveAccountRequest;
import capstonesu25.warehouse.model.exportrequest.exportrequestdetail.ExportRequestDetailExcelRow;
import capstonesu25.warehouse.model.exportrequest.exportrequestdetail.ExportRequestDetailResponse;
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

import java.time.LocalDateTime;
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

    public Page<ExportRequestDetailResponse> getAllByExportRequestId(Long exportRequestId, int page, int limit) {
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

    public void createExportRequestDetail(MultipartFile file, Long exportRequestId) {
        LOGGER.info("Creating export request detail");
        LOGGER.info("Finding export request by id: {}", exportRequestId);

        ExportRequest exportRequest = exportRequestRepository.findById(exportRequestId)
                .orElseThrow(() -> new RuntimeException("Export request not found"));

        // Process Excel file
        List<ExportRequestDetailExcelRow> excelRows = ExcelUtil.processExcelFile(file, ExportRequestDetailExcelRow.class);
        List<ExportRequestDetail> exportRequestDetails = new ArrayList<>();
        // Create export request details
        for (ExportRequestDetailExcelRow row : excelRows) {
            ExportRequestDetail exportRequestDetail = new ExportRequestDetail();
            exportRequestDetail.setExportRequest(exportRequest);
            if(row.getMeasurementValue() != null) {
                exportRequestDetail.setMeasurementValue(row.getMeasurementValue());
            }
            exportRequestDetail.setQuantity(row.getQuantity());
            exportRequestDetail.setActualQuantity(0);
            Item item = itemRepository.findById(row.getItemId())
                    .orElseThrow(() -> new RuntimeException("Item not found with ID: " + row.getItemId()));
            if(exportRequest.getType().equals(ExportType.RETURN)) {
                if(!Objects.equals(item.getProvider().getId(), exportRequest.getProviderId())) {
                    throw new RuntimeException("Item provider does not match export request provider");
                }
            }
            if(exportRequest.getType().equals(ExportType.PARTIAL)
                    || exportRequest.getType().equals(ExportType.BORROWING)) {
              exportRequestDetail.setMeasurementValue(row.getMeasurementValue());
            }
            exportRequestDetail.setItem(item);
            exportRequestDetails.add(exportRequestDetail);
        }
        List<ExportRequestDetail> list = exportRequestDetailRepository.saveAll(exportRequestDetails);
        LOGGER.info("Export request details created successfully");
        for(ExportRequestDetail detail : list) {
            LOGGER.info("choose inventory items for export request detail ID: {}", detail.getId());
            chooseInventoryItemsForThoseCases(detail);
        }
        LOGGER.info("Auto assigning counting staff for export request with ID: {}", exportRequestId);
        autoAssignCountingStaff(exportRequest);
    }

    public ExportRequestDetailResponse updateActualQuantity(Long exportRequestDetailId, Integer actual) {
        LOGGER.info("Updating actual quantity for export request detail with ID: {}", exportRequestDetailId);
        ExportRequestDetail exportRequestDetail = exportRequestDetailRepository.findById(exportRequestDetailId)
                .orElseThrow(() -> new RuntimeException("Export request detail not found"));
        exportRequestDetail.setActualQuantity(actual);
        if(Objects.equals(actual, exportRequestDetail.getQuantity())) {
            exportRequestDetail.setStatus(DetailStatus.MATCH);
        } else {
            throw new RuntimeException("Actual quantity does not match requested quantity");
        }
        return mapToResponse(exportRequestDetailRepository.save(exportRequestDetail));
    }

    private void chooseInventoryItemsForThoseCases(ExportRequestDetail exportRequestDetail) {
        switch (exportRequestDetail.getExportRequest().getType()) {
            case RETURN -> {
            //TODO
            }
            case BORROWING, PARTIAL -> autoChooseInventoryItemsForPartialAndBorrowing(exportRequestDetail);
            case LIQUIDATION -> autoChooseInventoryItemsForLiquidation(exportRequestDetail);
            default -> // case PRODUCTION
                    autoChooseInventoryItemsForProduction(exportRequestDetail);
        }
    }

    private void autoChooseInventoryItemsForProduction(ExportRequestDetail exportRequestDetail) {
        LOGGER.info("Auto choosing inventory items for production");
        Integer quantity = exportRequestDetail.getQuantity();
        if((quantity*exportRequestDetail.getItem().getMeasurementValue()) >
                exportRequestDetail.getItem().getTotalMeasurementValue()) {
            throw new RuntimeException("quantity of export request detail id: " + exportRequestDetail.getItem().getId()
                    + " is greater than total measurement value of item id: " + exportRequestDetail.getItem().getId());
        }
        // Fetch inventory items by item ID
        LOGGER.info("Finding inventory items by item ID: {}", exportRequestDetail.getItem().getId());
        List<InventoryItem> inventoryItemList = inventoryItemRepository.findByItem_IdAndParentNull(exportRequestDetail.getItem().getId());

        if (inventoryItemList.isEmpty()) {
            throw new RuntimeException("Inventory items not found for item ID: " + exportRequestDetail.getItem().getId());
        }

        // Sort the inventory items by importedDate (furthest from now first)
        LOGGER.info("Sorting inventory items by imported date");
        List<InventoryItem> sortedInventoryItems = new ArrayList<>(
                inventoryItemList.stream()
                        .sorted(Comparator.comparing(InventoryItem::getImportedDate).reversed())
                        .limit(quantity)
                        .toList()
        );

        LOGGER.info("Setting inventory items for export request detail ID: {}", exportRequestDetail.getId());
        exportRequestDetail.setInventoryItems(sortedInventoryItems);

        LOGGER.info("Saving export request detail with ID: {}", exportRequestDetail.getId());
        exportRequestDetailRepository.save(exportRequestDetail);
    }

    private void autoChooseInventoryItemsForPartialAndBorrowing(ExportRequestDetail exportRequestDetail) {
        List<InventoryItem> inventoryItemList = inventoryItemRepository.findByItem_Id(exportRequestDetail.getItem().getId());

        // FIFO - sort by imported date (oldest first)
        List<InventoryItem> sortedInventoryItems = inventoryItemList.stream()
                .sorted(Comparator.comparing(InventoryItem::getImportedDate))
                .toList();

        int quantityToSelect = exportRequestDetail.getQuantity();
        double requestedMeasurement = exportRequestDetail.getMeasurementValue();

        // Collect all "good fit" items (with parent + >= requested measurement)
        List<InventoryItem> goodFitItems = sortedInventoryItems.stream()
                .filter(i -> i.getParent() != null && i.getMeasurementValue() >= requestedMeasurement)
                .sorted(Comparator.comparingDouble(InventoryItem::getMeasurementValue)) // Closest fitting first
                .toList();

        // Pick the best N good-fit items
        List<InventoryItem> selectedItems = new ArrayList<>();
        for (InventoryItem item : goodFitItems) {
            selectedItems.add(item);
            if (selectedItems.size() == quantityToSelect) {
                break;
            }
        }

        // Fallback — fill remaining with other FIFO items (avoiding duplicates)
        List<InventoryItem> noParentList = inventoryItemRepository.findByItem_Id(exportRequestDetail.getItem().getId());
        List<InventoryItem> noParentItems = noParentList.stream()
                .filter(i -> i.getParent() == null)
                .sorted(Comparator.comparing(InventoryItem::getImportedDate)) // FIFO
                .toList();

        if (selectedItems.size() < quantityToSelect) {
            for (InventoryItem item : noParentItems) {
                if (!selectedItems.contains(item)) {
                    selectedItems.add(item);
                    if (selectedItems.size() == quantityToSelect) {
                        break;
                    }
                }
            }
        }

        // Set result or log error
        if (selectedItems.size() == quantityToSelect) {
            exportRequestDetail.setInventoryItems(selectedItems); // assumes list field
            exportRequestDetailRepository.save(exportRequestDetail);
            LOGGER.info("Selected inventory items for export request detail ID: {}", exportRequestDetail.getId());
        } else {
            LOGGER.error("Not enough inventory items found for export request detail ID: {}", exportRequestDetail.getId());
        }
    }

    private void autoChooseInventoryItemsForLiquidation(ExportRequestDetail exportRequestDetail) {
        Integer actualQuantity = exportRequestDetail.getActualQuantity();
        if((actualQuantity*exportRequestDetail.getItem().getMeasurementValue()) >
                exportRequestDetail.getItem().getTotalMeasurementValue()) {
            throw new RuntimeException("quantity of export request detail id: " + exportRequestDetail.getItem().getId()
                    + " is greater than total measurement value of item id: " + exportRequestDetail.getItem().getId());
        }
        // Fetch inventory items by item ID
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime maxExpireDate = now.plusDays(LIQUIDATION);
        List<InventoryItem> inventoryItemList = inventoryItemRepository
                .findByItem_IdAndExpiredDateLessThanEqual(exportRequestDetail.getItem().getId(), maxExpireDate);

        if (inventoryItemList.isEmpty()) {
            throw new RuntimeException("Inventory items not found for item ID: " + exportRequestDetail.getItem().getId());
        }

        // Sort the inventory items by importedDate (furthest from now first)
        List<InventoryItem> sortedInventoryItems = inventoryItemList.stream()
                .sorted(Comparator.comparing(InventoryItem::getImportedDate).reversed())
                .limit(actualQuantity)
                .toList();

        exportRequestDetail.setInventoryItems(sortedInventoryItems);
        exportRequestDetailRepository.save(exportRequestDetail);
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
            LOGGER.info("Calculating expected working time for item: " + detail.getItem().getName());
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

            if (checkExportRequest.isEmpty()) {
                responses.add(accountResponse);
            } else {
                for (ExportRequest exportCheck : checkExportRequest) {
                    if (exportCheck.getExportTime().isAfter(exportCheck.getExportTime().plusMinutes(configuration.getTimeToAllowConfirm().toSecondOfDay() / 60))) {
                        responses.add(accountResponse);
                    }
                }
            }
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