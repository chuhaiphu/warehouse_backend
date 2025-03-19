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
public class InventoryItemRequest {
    private Long id;
    private String reasonForDisposal;
    private Integer quantity;
    private ItemStatus status;
    private LocalDateTime expiredDate;
    private LocalDateTime importedDate;
    private LocalDateTime updatedDate;
    private Long parentId; // Reference to parent inventory item
    private List<Long> childrenIds; // References to child inventory items
    private Long itemId;
    private Long exportRequestDetailId;
    private Long importOrderDetailId;
    private Long storedLocationId;
    private int numberOfItems;
}
