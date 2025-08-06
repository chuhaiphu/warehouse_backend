package capstonesu25.warehouse.model.stockcheck.detail;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateActualStockCheck {
    private Long stockCheckDetailId;
    @NotNull(message = "cannot be null")
    @NotEmpty(message = "cannot be empty")
    private String inventoryItemId;
}
