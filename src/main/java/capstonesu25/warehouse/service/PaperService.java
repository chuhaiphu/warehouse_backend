package capstonesu25.warehouse.service;

import capstonesu25.warehouse.entity.*;
import capstonesu25.warehouse.enums.AccountRole;
import capstonesu25.warehouse.enums.RequestStatus;
import capstonesu25.warehouse.model.paper.PaperRequest;
import capstonesu25.warehouse.model.paper.PaperResponse;
import capstonesu25.warehouse.repository.*;
import capstonesu25.warehouse.utils.CloudinaryUtil;
import capstonesu25.warehouse.utils.NotificationUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaperService {
    private final PaperRepository paperRepository;
    private final ImportOrderRepository importOrderRepository;
    private final ExportRequestRepository exportRequestRepository;
    private final CloudinaryUtil cloudinaryUtil;
    private final NotificationService notificationService;
    private final AccountRepository accountRepository;

    private static final Logger LOGGER = LoggerFactory.getLogger(PaperService.class);

    public Page<PaperResponse> getListPaper(int page, int limit) {
        Pageable pageable = PageRequest.of(Math.max(0, page - 1), limit);
        Page<Paper> papers = paperRepository.findAll(pageable);
        return papers.map(this::convertToResponse);
    }

    public List<PaperResponse> getListPaperByImportOrderId(String importOrderId,int page, int limit) {
        Pageable pageable = PageRequest.of(Math.max(0, page - 1), limit);
        Page<Paper> papers = paperRepository.findPapersByImportOrder_Id(importOrderId, pageable);
        return papers.stream().map(this::convertToResponse).toList();
    }

    public List<PaperResponse> getListPaperByExportRequestId(String exportRequestId,int page, int limit) {
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

        if(request.getImportOrderId() != null) {
            //update status
            LOGGER.info("Updating import order status at creating paper");
            ImportOrder importOrder = importOrderRepository.findById(request.getImportOrderId()).orElseThrow();
            importOrder.setStatus(RequestStatus.COUNTED);
            importOrder.setActualDateReceived(LocalDate.now());
            importOrder.setActualTimeReceived(LocalTime.now());
            importOrderRepository.save(importOrder);
            paper.setImportOrder(importOrder);
        }
        if(request.getExportRequestId() != null) {
            //update status
            LOGGER.info("Updating export request status at creating paper");
            ExportRequest exportRequest = exportRequestRepository.findById(request.getExportRequestId()).orElseThrow();
            exportRequest.setStatus(RequestStatus.COUNTED);
            exportRequestRepository.save(exportRequest);
            paper.setExportRequest(exportRequest);
        }
        paperRepository.save(paper);
        // * Notification
        notificationService.handleNotification(
            NotificationUtil.WAREHOUSE_MANAGER_CHANNEL,
            NotificationUtil.IMPORT_ORDER_COUNTED_EVENT,
            request.getImportOrderId(),
            "Đơn nhập mã #" + request.getImportOrderId() + " đã được đếm",
            accountRepository.findByRole(AccountRole.WAREHOUSE_MANAGER)
        );
    }



    private Paper convertToEntity(PaperRequest request) {
        Paper paper = new Paper();
        if(request.getId() != null) {
            paper.setId(request.getId());
        }
        paper.setDescription(request.getDescription());
        if(request.getImportOrderId() != null) {
            paper.setImportOrder(importOrderRepository.findById(request.getImportOrderId()).orElse(null));
        }
        if(request.getExportRequestId() != null){
            paper.setExportRequest(exportRequestRepository.findById(request.getExportRequestId()).orElse(null));
        }
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
