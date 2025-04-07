package capstonesu25.warehouse.service;

import capstonesu25.warehouse.entity.ImportOrder;
import capstonesu25.warehouse.entity.ImportOrderDetail;
import capstonesu25.warehouse.entity.ImportRequest;
import capstonesu25.warehouse.entity.ImportRequestDetail;
import capstonesu25.warehouse.enums.DetailStatus;
import capstonesu25.warehouse.model.importorder.importorderdetail.ImportOrderDetailRequest;
import capstonesu25.warehouse.model.importorder.importorderdetail.ImportOrderDetailResponse;
import capstonesu25.warehouse.repository.ImportOrderDetailRepository;
import capstonesu25.warehouse.repository.ImportOrderRepository;
import capstonesu25.warehouse.repository.ImportRequestDetailRepository;
import capstonesu25.warehouse.repository.ItemRepository;
import capstonesu25.warehouse.utils.ExcelUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ImportOrderDetailService {
    private final ImportOrderRepository importOrderRepository;
    private final ImportOrderDetailRepository importOrderDetailRepository;
    private final ItemRepository itemRepository;
    private final ImportRequestDetailRepository importRequestDetailRepository;

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

    public void create(MultipartFile file, Long importOrderId) {
        LOGGER.info("Creating import order detail");
        LOGGER.info("Finding import order by id: {}", importOrderId);

        ImportOrder importRequest = importOrderRepository.findById(importOrderId)
                .orElseThrow(() -> new NoSuchElementException("Import Order not found with ID: " + importOrderId));

        List<ImportOrderDetailRequest> list = ExcelUtil.processExcelFile(file, ImportOrderDetailRequest.class);

        if (list.isEmpty()) {
            throw new IllegalArgumentException("Import order detail list cannot be empty");
        }

        Long providerId = itemRepository.findById(list.get(0).getItemId())
                .map(item -> item.getProvider().getId())
                .orElseThrow(() -> new NoSuchElementException("Item not found with ID: " + list.get(0).getItemId()));

        for (ImportOrderDetailRequest request : list) {
            Long currentProviderId = itemRepository.findById(request.getItemId())
                    .map(item -> item.getProvider().getId())
                    .orElseThrow(() -> new NoSuchElementException("Item not found with ID: " + request.getItemId()));

            if (!providerId.equals(currentProviderId)) {
                throw new IllegalArgumentException("All items must belong to the same provider.");
            }
        }

        for (ImportOrderDetailRequest request : list) {
            ImportOrderDetail importOrderDetail = new ImportOrderDetail();
            importOrderDetail.setImportOrder(importRequest);
            importOrderDetail.setExpectQuantity(request.getQuantity());
            importOrderDetail.setItem(itemRepository.findById(request.getItemId()).orElseThrow());
            importOrderDetail.setActualQuantity(0);
            importOrderDetailRepository.save(importOrderDetail);
        }
        updateOrderedQuantityOfImportRequestDetail(importOrderId);
        LOGGER.info("Successfully created import order details for importOrderId: {}", importOrderId);
    }


    public void updateImportOrderDetail(List<ImportOrderDetailRequest> list, Long importOrderId) {
        LOGGER.info("Updating import order detail for ImportOrder ID: {}", importOrderId);

        List<ImportOrderDetail> importOrderDetails = importOrderDetailRepository
                .findImportOrderDetailByImportOrder_Id(importOrderId);

        Map<Long, ImportOrderDetail> detailMap = importOrderDetails.stream()
                .collect(Collectors.toMap(d -> d.getItem().getId(), d -> d));

        list.forEach(request -> {
            ImportOrderDetail detail = detailMap.get(request.getItemId());
            if (detail != null) {
                updateOrderDetail(detail, request);
            }
        });

        importOrderDetailRepository.saveAll(importOrderDetails);
    }

    private void updateOrderedQuantityOfImportRequestDetail(Long importOrderId) {
        LOGGER.info("Update remaining quantity of import request detail");
        ImportOrder importOrder = importOrderRepository.findById(importOrderId)
                .orElseThrow(() -> new NoSuchElementException("ImportOrder not found with ID: " + importOrderId));
        ImportRequest importRequest = importOrder.getImportRequest();
        LOGGER.info("Finding import request by id: {}", importRequest.getId());
        for (ImportRequestDetail detail : importRequest.getDetails()) {
            for(ImportOrderDetail orderDetail : importOrder.getImportOrderDetails()) {
                if(detail.getItem().getId().equals(orderDetail.getItem().getId())){
                    LOGGER.info("Updating ordered quantity for item id: {}", detail.getItem().getId());
                    detail.setOrderedQuantity(detail.getOrderedQuantity() + orderDetail.getExpectQuantity());
                    importRequestDetailRepository.save(detail);
                    break;
                }
            }
        }
    }

    private void updateOrderDetail(ImportOrderDetail detail, ImportOrderDetailRequest request) {
        detail.setExpectQuantity(request.getQuantity());
        detail.setActualQuantity(request.getActualQuantity());
        detail.setStatus(determineStatus(request.getActualQuantity(), request.getQuantity()));
    }

    private DetailStatus determineStatus(int actualQuantity, int expectedQuantity) {
        if (actualQuantity == expectedQuantity) return DetailStatus.MATCH;
        return actualQuantity < expectedQuantity ? DetailStatus.LACK : DetailStatus.EXCESS;
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
                importOrderDetail.getStatus() != null ? importOrderDetail.getStatus() : null
        );
    }
}
