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
    private Long id;
    private String reasonForDisposal;
    private Double measurementValue;
    private ItemStatus status;
    private LocalDateTime expiredDate;
    private LocalDateTime importedDate;
    private LocalDateTime updatedDate;

    // Using IDs instead of full objects to avoid infinite recursion
    private Long parentId;
    private List<Long> childrenIds;

    // Basic item information
    private Long itemId;
    private String itemName;
    private String itemCode;

    // Basic export request detail information
    private List<Long> exportRequestDetailIds;

    // Basic import order detail information
    private Long importOrderDetailId;

    // Basic stored location information
    private Long storedLocationId;
    private String storedLocationName;
}
