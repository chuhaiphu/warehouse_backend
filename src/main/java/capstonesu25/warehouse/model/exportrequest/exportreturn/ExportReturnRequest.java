package capstonesu25.warehouse.model.exportrequest.exportreturn;

import capstonesu25.warehouse.enums.ExportType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ExportReturnRequest {
    private LocalDate countingDate;

    private LocalTime countingTime;

    @NotNull(message = "cannot be null")
    private LocalDate exportDate;

    @NotNull(message = "cannot be null")
    private LocalTime exportTime;

    private List<String> importRequestIds;

    @NotNull(message = "cannot be null")
    private String exportReason;

    @NotNull(message = "cannot be null")
    private Long providerId;

    private ExportType type;


}
