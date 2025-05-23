package capstonesu25.warehouse.model.importorder;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExtendImportOrderRequest {
    @NotNull(message = "Import order ID cannot be null")
    private String importOrderId;
    private LocalDate extendedDate;
    private LocalTime extendedTime;
    @NotNull(message = "Extend reason cannot be null")
    @NotEmpty(message = "Extend reason cannot be empty")
    private String extendReason;
}
