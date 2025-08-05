package capstonesu25.warehouse.model.storedlocation;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    @JsonProperty("isRoad")
    private boolean isRoad;
    @JsonProperty("isDoor")
    private boolean isDoor;
    @JsonProperty("isUsed")
    private boolean isUsed;
    @JsonProperty("isFulled")
    private boolean isFulled;
    private List<String> inventoryItemIds; // References to inventory items
    private double maximumCapacityForItem;
    private double currentCapacity;
    private List<String> itemId; // References to item

}
