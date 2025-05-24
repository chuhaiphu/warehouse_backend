package capstonesu25.warehouse.model.exportrequest;

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
public class ExtendExportRequestRequest {
    @NotNull(message = "Export request ID cannot be null")
    private String exportRequestId;
    private LocalDate extendedDate;
    private LocalTime extendedTime;
    @NotNull(message = "Extend reason cannot be null")
    @NotEmpty(message = "Extend reason cannot be empty")
    private String extendReason;
}
