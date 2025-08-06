package capstonesu25.warehouse.model.stockcheck;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AssignStaffStockCheck {
    @NotNull(message = "Stock Check ID cannot be null")
    @NotEmpty(message = "Stock Check ID cannot be empty")
    private String stockCheckId;
    private Long staffId;
}
