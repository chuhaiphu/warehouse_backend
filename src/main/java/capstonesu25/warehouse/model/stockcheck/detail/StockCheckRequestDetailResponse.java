package capstonesu25.warehouse.model.stockcheck.detail;

import capstonesu25.warehouse.enums.DetailStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class StockCheckRequestDetailResponse {
    private Long id;
    private Double measurementValue;
    private Integer quantity;
    private Integer actualQuantity;
    private Double actualMeasurementValue;
    private DetailStatus status;
    private Boolean isChecked ;
    private String stockCheckRequestId;
    private String itemId;
    private List<CheckedStockCheck> inventoryItemIds;
    private List<CheckedStockCheck> checkedInventoryItemIds;
}
