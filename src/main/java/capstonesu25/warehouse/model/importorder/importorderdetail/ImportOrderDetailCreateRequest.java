package capstonesu25.warehouse.model.importorder.importorderdetail;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ImportOrderDetailCreateRequest {
    @NotNull(message = "Item ID cannot be null")
    private Long itemId;

    @NotNull(message = "Quantity cannot be null")
    private int quantity;
} 