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
import java.time.ZoneId;
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
    private final StockCheckRequestRepository stockCheckRequestRepository;

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

        Paper paper = convertToEntity(request);

        if(request.getImportOrderId() != null) {
            //update status
            if(request.getSignProviderName() == null) {
                throw new IllegalArgumentException("Sign provider name cannot be null");
            }

            LOGGER.info("Updating import order status at creating paper");
            ImportOrder importOrder = importOrderRepository.findById(request.getImportOrderId()).orElseThrow();
            if(LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh")).isBefore(importOrder.getDateReceived())) {
                throw new IllegalArgumentException("Cannot confirm import order before expected date received");
            }
            importOrder.setStatus(RequestStatus.COUNTED);
            String signProviderUrl = cloudinaryUtil.uploadImage(request.getSignProviderUrl());
            String signReceiverName = cloudinaryUtil.uploadImage(request.getSignReceiverUrl());
            importOrder.setActualDateReceived(LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh")));
            importOrder.setActualTimeReceived(LocalTime.now(ZoneId.of("Asia/Ho_Chi_Minh")));
            importOrderRepository.save(importOrder);
            paper.setImportOrder(importOrder);
            paper.setSignProviderUrl(signProviderUrl);
            paper.setSignReceiverUrl(signReceiverName);
            paper.setSignProviderName(request.getSignProviderName());
            paper.setSignReceiverName(importOrder.getAssignedStaff().getFullName());
            paperRepository.save(paper);
            // * Notification
            notificationService.handleNotification(
                    NotificationUtil.DEPARTMENT_CHANNEL,
                    NotificationUtil.IMPORT_ORDER_COUNTED_EVENT + "-" + request.getImportOrderId(),
                    request.getImportOrderId(),
                    "Đơn nhập mã #" + request.getImportOrderId() + " đã được đếm",
                    accountRepository.findByRole(AccountRole.DEPARTMENT)
            );
            notificationService.handleNotification(
                    NotificationUtil.WAREHOUSE_MANAGER_CHANNEL,
                    NotificationUtil.IMPORT_ORDER_COUNTED_EVENT + "-" + request.getImportOrderId(),
                    request.getImportOrderId(),
                    "Đơn nhập mã #" + request.getImportOrderId() + " đã được đếm",
                    accountRepository.findByRole(AccountRole.WAREHOUSE_MANAGER)
            );
        }
        if(request.getExportRequestId() != null) {
            //update status
            if(request.getSignReceiverUrl() == null) {
                throw new IllegalArgumentException("Sign receiver url cannot be null");
            }
            LOGGER.info("Updating export request status at creating paper");
            ExportRequest exportRequest = exportRequestRepository.findById(request.getExportRequestId()).orElseThrow();
            exportRequest.setStatus(RequestStatus.COUNTED);
            if(LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh")).isBefore(exportRequest.getExportDate())) {
                throw new IllegalArgumentException("Cannot confirm export request before export date received");
            }
            String signProviderUrl = cloudinaryUtil.uploadImage(request.getSignProviderUrl());
            String signReceiverName = cloudinaryUtil.uploadImage(request.getSignReceiverUrl());
            exportRequestRepository.save(exportRequest);
            paper.setExportRequest(exportRequest);
            paper.setSignProviderUrl(signProviderUrl);
            paper.setSignReceiverUrl(signReceiverName);
            paper.setSignProviderName(exportRequest.getAssignedStaff().getFullName());
            paper.setSignReceiverName(request.getSignReceiverName());
            paperRepository.save(paper);
            // * Notification
            notificationService.handleNotification(
                    NotificationUtil.DEPARTMENT_CHANNEL,
                    NotificationUtil.EXPORT_REQUEST_COUNTED_EVENT + "-" + request.getExportRequestId(),
                    request.getExportRequestId(),
                    "Đơn xuất mã #" + request.getExportRequestId() + " đã được đếm",
                    accountRepository.findByRole(AccountRole.DEPARTMENT)
            );
            notificationService.handleNotification(
                    NotificationUtil.WAREHOUSE_MANAGER_CHANNEL,
                    NotificationUtil.EXPORT_REQUEST_COUNTED_EVENT + "-" + request.getExportRequestId(),
                    request.getExportRequestId(),
                    "Đơn xuất mã #" + request.getExportRequestId() + " đã được đếm",
                    accountRepository.findByRole(AccountRole.WAREHOUSE_MANAGER)
            );
        }
        if(request.getStockCheckRequestId() != null) {
            LOGGER.info("Updating stock check request status at creating paper");
            StockCheckRequest stockCheckRequest = stockCheckRequestRepository.findById(request.getStockCheckRequestId()).orElseThrow();
            stockCheckRequest.setStatus(RequestStatus.COUNTED);
            String signProviderUrl = cloudinaryUtil.uploadImage(request.getSignProviderUrl());
            String signReceiverName = cloudinaryUtil.uploadImage(request.getSignReceiverUrl());
            stockCheckRequestRepository.save(stockCheckRequest);
            paper.setStockCheckRequest(stockCheckRequest);
            paper.setSignProviderUrl(signProviderUrl);
            paper.setSignReceiverUrl(signReceiverName);
            paper.setSignProviderName(stockCheckRequest.getAssignedStaff().getFullName());
            paper.setSignReceiverName(request.getSignReceiverName());
            paperRepository.save(paper);
        }

    }

    public void resetPaper (Long paperId) {
        LOGGER.info("Resetting paper with id: {}", paperId);
        Paper paper = paperRepository.findById(paperId).orElseThrow();
        if(paper.getImportOrder() != null) {
            ImportOrder importOrder = paper.getImportOrder();
            importOrder.setStatus(RequestStatus.IN_PROGRESS);
            importOrder.setActualDateReceived(null);
            importOrder.setActualTimeReceived(null);
            importOrder.setPaper(null);
            importOrderRepository.save(importOrder);
        }
        if(paper.getExportRequest() != null) {
            ExportRequest exportRequest = paper.getExportRequest();
            exportRequest.setStatus(RequestStatus.IN_PROGRESS);
            exportRequest.setPaper(null);
            exportRequestRepository.save(exportRequest);
        }
        if(paper.getStockCheckRequest() != null) {
            StockCheckRequest stockCheckRequest = paper.getStockCheckRequest();
            stockCheckRequest.setStatus(RequestStatus.IN_PROGRESS);
            stockCheckRequest.setPaper(null);
            stockCheckRequestRepository.save(stockCheckRequest);
        }
        paperRepository.delete(paper);
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
        response.setSignProviderName(paper.getSignProviderName());
        response.setSignReceiverUrl(paper.getSignReceiverUrl());
        response.setSignReceiverName(paper.getSignReceiverName());
        return response;
    }

}
