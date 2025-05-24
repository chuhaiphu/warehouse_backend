package capstonesu25.warehouse.model.inventoryitem;

import capstonesu25.warehouse.enums.ItemStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InventoryItemResponse {
    private String id;
    private String reasonForDisposal;
    private Double measurementValue;
    private ItemStatus status;
    private LocalDateTime expiredDate;
    private LocalDateTime importedDate;
    private LocalDateTime updatedDate;

    // Using IDs instead of full objects to avoid infinite recursion
    private String parentId;
    private List<String> childrenIds;

    // Basic item information
    private String itemId;
    private String itemName;
    private String itemCode;

    private Long exportRequestDetailId;

    // Basic import order detail information
    private Long importOrderDetailId;

    // Basic stored location information
    private Long storedLocationId;
    private String storedLocationName;
}
