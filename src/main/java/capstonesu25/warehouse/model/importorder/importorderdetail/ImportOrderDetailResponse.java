package capstonesu25.warehouse.model.importorder.importorderdetail;

import capstonesu25.warehouse.enums.DetailStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ImportOrderDetailResponse {
    private Long importOrderDetailId;
    private String importOrderId;
    private String itemId;
    private String itemName;
    private String inventoryItemId;
    private int expectQuantity;
    private int actualQuantity;
    private Double expectMeasurementValue;
    private Double actualMeasurementValue;
    private DetailStatus status;
}
