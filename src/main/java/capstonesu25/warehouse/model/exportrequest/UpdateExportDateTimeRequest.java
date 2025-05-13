package capstonesu25.warehouse.model.exportrequest;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateExportDateTimeRequest {
    @NotNull(message = "exportDate cannot be null")
    private LocalDate exportDate;

    @NotNull(message = "exportTime cannot be null")
    private LocalTime exportTime;
}
