package capstonesu25.warehouse.service;

import capstonesu25.warehouse.entity.*;
import capstonesu25.warehouse.model.paper.PaperRequest;
import capstonesu25.warehouse.model.paper.PaperResponse;
import capstonesu25.warehouse.repository.ExportRequestRepository;
import capstonesu25.warehouse.repository.ImportOrderRepository;
import capstonesu25.warehouse.repository.ItemRepository;
import capstonesu25.warehouse.repository.PaperRepository;
import capstonesu25.warehouse.utils.CloudinaryUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaperService {
    private final PaperRepository paperRepository;
    private final ItemRepository itemRepository;
    private final ImportOrderRepository importOrderRepository;
    private final ExportRequestRepository exportRequestRepository;
    private final CloudinaryUtil cloudinaryUtil;
    private static final Logger LOGGER = LoggerFactory.getLogger(PaperService.class);

    public Page<PaperResponse> getListPaper(int page, int limit) {
        Pageable pageable = PageRequest.of(Math.max(0, page - 1), limit);
        Page<Paper> papers = paperRepository.findAll(pageable);
        return papers.map(this::convertToResponse);
    }

    public List<PaperResponse> getListPaperByImportOrderId(Long importOrderId,int page, int limit) {
        Pageable pageable = PageRequest.of(Math.max(0, page - 1), limit);
        Page<Paper> papers = paperRepository.findPapersByImportOrder_Id(importOrderId, pageable);
        return papers.stream().map(this::convertToResponse).toList();
    }

    public List<PaperResponse> getListPaperByExportRequestId(Long exportRequestId,int page, int limit) {
        Pageable pageable = PageRequest.of(Math.max(0, page - 1), limit);
        Page<Paper> papers = paperRepository.findPapersByExportRequest_Id(exportRequestId, pageable);
        return papers.stream().map(this::convertToResponse).toList();
    }

    public PaperResponse getPaperById(Long id) {
        LOGGER.info("Getting paper by id");
        Paper paper = paperRepository.findById(id).orElse(null);
        return paper != null ? convertToResponse(paper) : null;
    }

    public void createPaper(PaperRequest request) throws IOException {
        LOGGER.info("Creating paper");
        String signProviderUrl = cloudinaryUtil.uploadImage(request.getSignProviderUrl());
        String signWarehouseUrl = cloudinaryUtil.uploadImage(request.getSignWarehouseUrl());
        Paper paper = convertToEntity(request);
        paper.setSignProviderUrl(signProviderUrl);
        paper.setSignWarehouseUrl(signWarehouseUrl);
        paperRepository.save(paper);
        afterCreatedPaperUpdateItems(request);
    }

    private void afterCreatedPaperUpdateItems(PaperRequest request) {
        LOGGER.info("Updating items after creating paper");
        ImportOrder importOrder = importOrderRepository.findById(request.getImportOrderId()).orElse(null);
        if (importOrder != null) {
            List<Item> itemsToUpdate = importOrder.getImportOrderDetails().stream()
                    .map(importOrderDetail -> {
                        Item item = importOrderDetail.getItem();
                        item.setTotalMeasurementValue(item.getTotalMeasurementValue() + importOrderDetail.getActualQuantity());
                        return item;
                    })
                    .toList();
            itemRepository.saveAll(itemsToUpdate);
        }

        ExportRequest exportRequest = exportRequestRepository.findById(request.getExportRequestId()).orElse(null);
        if (exportRequest != null) {
            List<Item> itemsToUpdate = exportRequest.getExportRequestDetails().stream()
                    .map(exportRequestDetail -> {
                        Item item = exportRequestDetail.getItem();
                        item.setTotalMeasurementValue(item.getTotalMeasurementValue() - exportRequestDetail.getQuantity());
                        return item;
                    })
                    .toList();
            itemRepository.saveAll(itemsToUpdate);
        }
    }

    private Paper convertToEntity(PaperRequest request) {
        Paper paper = new Paper();
        if(request.getId() != null) {
            paper.setId(request.getId());
        }
        paper.setDescription(request.getDescription());
        paper.setImportOrder(importOrderRepository.findById(request.getImportOrderId()).orElse(null));
        paper.setExportRequest(exportRequestRepository.findById(request.getExportRequestId()).orElse(null));
        return paper;
    }

    private PaperResponse convertToResponse(Paper paper) {
        PaperResponse response = new PaperResponse();
        response.setId(paper.getId());
        response.setDescription(paper.getDescription());
        if(paper.getImportOrder() != null) {
            response.setImportOrderId(paper.getImportOrder().getId());
        }
        if(paper.getExportRequest() != null) {
            response.setExportRequestId(paper.getExportRequest().getId());
        }
        response.setSignProviderUrl(paper.getSignProviderUrl());
        response.setSignWarehouseUrl(paper.getSignWarehouseUrl());
        return response;
    }

}
