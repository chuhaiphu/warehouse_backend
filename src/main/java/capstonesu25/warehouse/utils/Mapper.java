package capstonesu25.warehouse.utils;

import capstonesu25.warehouse.entity.ImportRequest;
import capstonesu25.warehouse.entity.ImportRequestDetail;
import capstonesu25.warehouse.entity.ImportOrder;
import capstonesu25.warehouse.entity.ImportOrderDetail;
import capstonesu25.warehouse.model.importrequest.ImportRequestResponse;
import capstonesu25.warehouse.model.importrequest.importrequestdetail.ImportRequestDetailResponse;
import capstonesu25.warehouse.model.importorder.ImportOrderResponse;
import capstonesu25.warehouse.model.importorder.importorderdetail.ImportOrderDetailResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class Mapper {
    
    public static ImportRequestDetailResponse mapToImportRequestDetailResponse(ImportRequestDetail importRequestDetail) {
        return new ImportRequestDetailResponse(
                importRequestDetail.getId(),
                importRequestDetail.getImportRequest() != null ? importRequestDetail.getImportRequest().getId() : null,
                importRequestDetail.getItem() != null ? importRequestDetail.getItem().getId() : null,
                importRequestDetail.getItem() != null ? importRequestDetail.getItem().getName() : null,
                importRequestDetail.getActualQuantity(),
                importRequestDetail.getExpectQuantity(),
                importRequestDetail.getOrderedQuantity(),
                importRequestDetail.getStatus() != null ? importRequestDetail.getStatus() : null
        );
    }
    
    public static ImportRequestResponse mapToImportRequestResponse(ImportRequest importRequest) {
        List<ImportRequestDetailResponse> details = importRequest.getDetails() != null ?
                importRequest.getDetails().stream()
                        .map(Mapper::mapToImportRequestDetailResponse)
                        .collect(Collectors.toList()) :
                List.of();

        return new ImportRequestResponse(
                importRequest.getId(),
                importRequest.getImportReason(),
                importRequest.getType(),
                importRequest.getStatus(),
                importRequest.getProvider() != null ? importRequest.getProvider().getId() : null,
                importRequest.getExportRequest() != null ? importRequest.getExportRequest().getId() : null,
                details,
                importRequest.getImportOrders() != null ?
                        importRequest.getImportOrders().stream().map(ImportOrder::getId).toList() :
                        List.of(),
                importRequest.getCreatedBy(),
                importRequest.getUpdatedBy(),
                importRequest.getCreatedDate(),
                importRequest.getUpdatedDate(),
                importRequest.getBatchCode(),
                importRequest.getStartDate(),
                importRequest.getEndDate()
        );
    }

    public static ImportOrderDetailResponse mapToImportOrderDetailResponse(ImportOrderDetail importOrderDetail) {
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

    public static ImportOrderResponse mapToImportOrderResponse(ImportOrder importOrder) {
        List<ImportOrderDetailResponse> details = importOrder.getImportOrderDetails() != null
                ? importOrder.getImportOrderDetails().stream()
                        .map(Mapper::mapToImportOrderDetailResponse)
                        .toList()
                : List.of();

        return new ImportOrderResponse(
                importOrder.getId(),
                importOrder.getImportRequest() != null ? importOrder.getImportRequest().getId() : null,
                importOrder.getDateReceived(),
                importOrder.getTimeReceived(),
                importOrder.isExtended(),
                importOrder.getExtendedDate(),
                importOrder.getExtendedTime(),
                importOrder.getExtendedReason(),
                importOrder.getNote(),
                importOrder.getStatus() != null ? importOrder.getStatus() : null,
                details,
                importOrder.getActualDateReceived(),
                importOrder.getActualTimeReceived(),
                importOrder.getCreatedBy(),
                importOrder.getUpdatedBy(),
                importOrder.getCreatedDate(),
                importOrder.getUpdatedDate(),
                importOrder.getPaper() != null ? importOrder.getPaper().getId() : null,
                importOrder.getAssignedStaff() != null ? importOrder.getAssignedStaff().getId() : null);
    }
}