package capstonesu25.warehouse.model.inventoryitem;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InventoryFigure {
    private String itemId;
    private Integer totalInventoryItemAvailable;
    private Integer totalInventoryItemUnAvailable;
    private Integer totalInventoryItemNeedLiquid;
}
