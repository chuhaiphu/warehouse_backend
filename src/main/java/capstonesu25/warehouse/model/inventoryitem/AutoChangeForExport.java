package capstonesu25.warehouse.model.inventoryitem;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AutoChangeForExport {
    @NotNull(message = "inventoryItemId cannot be null")
    @NotBlank(message = "inventoryItemId cannot be blank")
    private String inventoryItemId;
    private String note;
}
