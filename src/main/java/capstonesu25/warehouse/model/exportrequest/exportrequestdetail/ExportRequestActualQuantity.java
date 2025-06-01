package capstonesu25.warehouse.model.exportrequest.exportrequestdetail;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExportRequestActualQuantity {
    @NotNull(message = "Export request detail ID cannot be null")
    private Long exportRequestDetailId;

    @NotNull(message = "Inventory item cannot be null")
    @NotEmpty(message = "Inventory item cannot be null")
    private String inventoryItemId;
}