package capstonesu25.warehouse.utils;

import capstonesu25.warehouse.entity.ImportRequest;
import capstonesu25.warehouse.entity.ImportRequestDetail;
import capstonesu25.warehouse.entity.ImportOrder;
import capstonesu25.warehouse.entity.ImportOrderDetail;
import capstonesu25.warehouse.entity.ItemProvider;
import capstonesu25.warehouse.enums.ImportType;
import capstonesu25.warehouse.model.importrequest.ImportRequestResponse;
import capstonesu25.warehouse.model.importrequest.importrequestdetail.ImportRequestDetailResponse;
import capstonesu25.warehouse.model.importorder.ImportOrderResponse;
import capstonesu25.warehouse.model.importorder.importorderdetail.ImportOrderDetailResponse;
import capstonesu25.warehouse.repository.ItemProviderRepository;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class Mapper {

    public static ImportRequestDetailResponse mapToImportRequestDetailResponse(ImportRequestDetail importRequestDetail, ItemProviderRepository itemProviderRepository) {
        ItemProvider itemProvider = new ItemProvider();
        if(importRequestDetail.getImportRequest().getType().equals(ImportType.ORDER)) {
            Long providerId = importRequestDetail.getImportRequest()
                    .getProvider()
                    .getId();

            String itemId = importRequestDetail.getItem().getId();

            itemProvider = itemProviderRepository
                    .findByProvider_IdAndItem_Id(providerId, itemId)
                    .orElseThrow(() -> new NoSuchElementException(
                            "No ItemProvider found for providerId=" + providerId + " and itemId=" + itemId
                    ));
        }

        return new ImportRequestDetailResponse(
                importRequestDetail.getId(),
                importRequestDetail.getImportRequest() != null ? importRequestDetail.getImportRequest().getId() : null,
                itemProvider != null? itemProvider.getProviderCode() : null,
                importRequestDetail.getItem() != null ? importRequestDetail.getItem().getId() : null,
                importRequestDetail.getItem() != null ? importRequestDetail.getItem().getName() : null,
                importRequestDetail.getInventoryItemId(),
                importRequestDetail.getActualQuantity(),
                importRequestDetail.getExpectQuantity(),
                importRequestDetail.getOrderedQuantity(),
                importRequestDetail.getActualMeasurementValue(),
                importRequestDetail.getExpectMeasurementValue(),
                importRequestDetail.getOrderedMeasurementValue(),
                importRequestDetail.getStatus() != null ? importRequestDetail.getStatus() : null
        );
    }
    
    public static ImportRequestResponse mapToImportRequestResponse(ImportRequest importRequest, ItemProviderRepository itemProviderRepository) {
        List<ImportRequestDetailResponse> details = importRequest.getDetails() != null
                ? importRequest.getDetails().stream()
                .map(d -> Mapper.mapToImportRequestDetailResponse(d, itemProviderRepository))
                .toList()
                : List.of();

        return new ImportRequestResponse(
                importRequest.getId(),
                importRequest.getImportReason(),
                importRequest.getType(),
                importRequest.getStatus(),
                importRequest.getProvider() != null ? importRequest.getProvider().getId() : null,
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
                importRequest.getEndDate(),
                importRequest.getExportRequestId(),
                importRequest.getDepartmentId()
        );
    }

    public static ImportOrderDetailResponse mapToImportOrderDetailResponse(
            ImportOrderDetail importOrderDetail,
            ItemProviderRepository itemProviderRepository) {
        ItemProvider itemProvider = new ItemProvider();

        if(importOrderDetail.getImportOrder().getImportRequest().getType().equals(ImportType.ORDER)) {
            Long providerId = importOrderDetail.getImportOrder()
                    .getImportRequest()
                    .getProvider()
                    .getId();

            String itemId = importOrderDetail.getItem().getId();

           itemProvider = itemProviderRepository
                    .findByProvider_IdAndItem_Id(providerId, itemId)
                    .orElseThrow(() -> new NoSuchElementException(
                            "No ItemProvider found for providerId=" + providerId + " and itemId=" + itemId
                    ));
        }


        return new ImportOrderDetailResponse(
                importOrderDetail.getId(),
                importOrderDetail.getImportOrder().getId(),
                itemProvider != null ? itemProvider.getProviderCode() : null,
                importOrderDetail.getItem().getId(),
                importOrderDetail.getItem().getName(),
                importOrderDetail.getInventoryItemId(),
                importOrderDetail.getExpectQuantity(),
                importOrderDetail.getActualQuantity(),
                importOrderDetail.getExpectMeasurementValue(),
                importOrderDetail.getActualMeasurementValue(),
                importOrderDetail.getStatus()
        );
    }


    public static ImportOrderResponse mapToImportOrderResponse(ImportOrder importOrder, ItemProviderRepository itemProviderRepository) {
        List<ImportOrderDetailResponse> details = importOrder.getImportOrderDetails() != null
                ? importOrder.getImportOrderDetails().stream()
                .map(d -> Mapper.mapToImportOrderDetailResponse(d, itemProviderRepository))
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
                importOrder.getAssignedStaff() != null ? importOrder.getAssignedStaff().getId() : null,
                importOrder.getExportRequest() != null ? importOrder.getExportRequest().getId() : null,
                importOrder.getImportRequest().getType()
        );
    }
}