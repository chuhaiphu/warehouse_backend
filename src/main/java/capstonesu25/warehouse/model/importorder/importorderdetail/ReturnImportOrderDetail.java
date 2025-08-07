package capstonesu25.warehouse.model.importorder.importorderdetail;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReturnImportOrderDetail {
    private String inventoryItemId;
    private Double measurementValue;
}
