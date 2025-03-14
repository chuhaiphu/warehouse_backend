package capstonesu25.warehouse.model.storedlocation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StoredLocationRequest {
    private Long id;
    private String zone;
    private String floor;
    private String row;
    private String batch;
    private boolean isUsed;
    private boolean isFulled;
    private List<Long> inventoryItemIds; // References to inventory items
}
