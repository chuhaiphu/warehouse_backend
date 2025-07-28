package capstonesu25.warehouse.model.importorder.importorderdetail;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ImportOrderDetailUpdateRequest {
    private String itemId;

    private int actualQuantity;

    private Double actualMeasurement;

    private String InventoryItemId;

} 