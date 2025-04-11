package capstonesu25.warehouse.model.exportrequest.exportrequestdetail;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExportRequestMeasurementValue {
    @NotNull(message = "Export request detail ID cannot be null")
    private Long exportRequestDetailId;

    @NotNull(message = "Actual measurement value cannot be null")
    private Double actualMeasurementValue;
}