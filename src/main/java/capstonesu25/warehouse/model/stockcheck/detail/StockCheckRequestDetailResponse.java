package capstonesu25.warehouse.model.stockcheck.detail;

import capstonesu25.warehouse.enums.DetailStatus;
import lombok.Builder;

import java.util.List;
@Builder
public class StockCheckRequestDetailResponse {
    private Long id;
    private Double measurementValue;
    private Integer quantity;
    private Integer actualQuantity;
    private Double actualMeasurementValue;
    private DetailStatus status;
    private String stockCheckRequestId;
    private String itemId;
    private List<String> inventoryItemIds;
}
