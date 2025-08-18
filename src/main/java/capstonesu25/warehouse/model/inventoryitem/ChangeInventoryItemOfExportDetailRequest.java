package capstonesu25.warehouse.model.inventoryitem;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChangeInventoryItemOfExportDetailRequest {
    @NotNull(message = "cannot be null")
    @NotEmpty(message = "cannot be empty")
    private List<String> oldInventoryItemIds;

    @NotNull(message = "cannot be null")
    @NotEmpty(message = "cannot be empty")
    private List<String> newInventoryItemIds;

    @NotNull(message = "cannot be null")
    @NotEmpty(message = "cannot be empty")
    private String note;
}
