package capstonesu25.warehouse.model.storedlocation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StoredLocationResponse {
    private Long id;
    private String zone;
    private String floor;
    private String row;
    private String line;
    private boolean isRoad;
    private boolean isDoor;
    private boolean isUsed;
    private boolean isFulled;
    private List<String> inventoryItemIds; // References to inventory items
    private double maximumCapacityForItem;
    private double currentCapacity;
    private String itemId; // References to item

}
