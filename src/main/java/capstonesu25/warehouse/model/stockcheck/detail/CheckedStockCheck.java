package capstonesu25.warehouse.model.stockcheck.detail;

import capstonesu25.warehouse.enums.ItemStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CheckedStockCheck {
    private String inventoryItemId;
    private Double measurementValue;
    private ItemStatus status;
    private String note;
}
