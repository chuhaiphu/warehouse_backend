package capstonesu25.warehouse.model.inventoryitem;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChangeInventoryItemOfExportDetailRequest {
    @NotNull(message = "cannot be null")
    @NotEmpty(message = "cannot be empty")
    private String oldInventoryItemId;

    @NotNull(message = "cannot be null")
    @NotEmpty(message = "cannot be empty")
    private String newInventoryItemId;

    @NotNull(message = "cannot be null")
    @NotEmpty(message = "cannot be empty")
    private String note;
}
