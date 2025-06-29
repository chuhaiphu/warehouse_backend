package capstonesu25.warehouse.model.inventoryitem;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateInventoryLocationRequest {
    private String inventoryItemId;
    private Long storedLocationId;
}
