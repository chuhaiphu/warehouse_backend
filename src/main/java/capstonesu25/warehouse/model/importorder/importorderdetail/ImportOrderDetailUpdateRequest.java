package capstonesu25.warehouse.model.importorder.importorderdetail;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ImportOrderDetailUpdateRequest {
    @NotNull(message = "Item ID cannot be null")
    private String itemId;

    @NotNull(message = "Actual quantity cannot be null")
    private int actualQuantity;
} 