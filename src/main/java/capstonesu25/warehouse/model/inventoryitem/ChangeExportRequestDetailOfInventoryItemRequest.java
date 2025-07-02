package capstonesu25.warehouse.model.inventoryitem;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChangeExportRequestDetailOfInventoryItemRequest {
    List<String> inventoryItemIds;
    @NotNull(message = "New export request detail ID cannot be null")
    Long newExportRequestDetailId;
}
