package capstonesu25.warehouse.service;

import capstonesu25.warehouse.entity.ImportOrder;
import capstonesu25.warehouse.entity.ImportOrderDetail;
import capstonesu25.warehouse.enums.DetailStatus;
import capstonesu25.warehouse.model.importorder.importorderdetail.ImportOrderDetailRequest;
import capstonesu25.warehouse.model.importorder.importorderdetail.ImportOrderDetailResponse;
import capstonesu25.warehouse.repository.ImportOrderDetailRepository;
import capstonesu25.warehouse.repository.ImportOrderRepository;
import capstonesu25.warehouse.repository.ImportRequestRepository;
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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ImportOrderDetailService {
    private final ImportOrderRepository importOrderRepository;
    private final ImportRequestRepository importRequestRepository;
    private final ImportOrderDetailRepository importOrderDetailRepository;
    private final ItemRepository itemRepository;

    private static final Logger LOGGER = LoggerFactory.getLogger(ImportOrderDetailService.class);

    public List<ImportOrderDetailResponse> getAllByImportOrderId(Long importOrderId, int page, int limit) {
        LOGGER.info("Getting all import order detail by import order id: {}", importOrderId);
        Pageable pageable = PageRequest.of(Math.max(0, page - 1), limit);
        Page<ImportOrderDetail> importOrderDetailPage = importOrderDetailRepository.
                findImportOrderDetailByImportOrder_Id(importOrderId, pageable);
        return importOrderDetailPage.getContent()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public ImportOrderDetailResponse getById(Long importOrderDetailId) {
        LOGGER.info("Getting import order detail by id: {}", importOrderDetailId);
        ImportOrderDetail importOrderDetail = importOrderDetailRepository.findById(importOrderDetailId).orElseThrow();
        return mapToResponse(importOrderDetail);
    }

    public void create(MultipartFile file, Long importOrderId) {
        LOGGER.info("Creating import order detail");
        LOGGER.info("finding import order by id: {}", importOrderId);
        ImportOrder importRequest = importOrderRepository.findById(importOrderId).orElseThrow();
        List<ImportOrderDetailRequest> list = ExcelUtil.processExcelFile(file,
                ImportOrderDetailRequest.class);

        for (ImportOrderDetailRequest request : list) {
            ImportOrderDetail importOrderDetail = new ImportOrderDetail();
            importOrderDetail.setImportOrder(importRequest);
            importOrderDetail.setExpectQuantity(request.getQuantity());
            importOrderDetail.setItem(itemRepository.findById(request.getItemId()).orElseThrow());
            importOrderDetail.setActualQuantity(0);
            importOrderDetailRepository.save(importOrderDetail);

        }
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
                importOrderDetail.getStatus()
        );
    }
}
