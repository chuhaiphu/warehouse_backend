package capstonesu25.warehouse.model.importrequest;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssignStaffExportRequest {
    @NotNull(message = "Export request id is required")
    private Long exportRequestId;

    @NotNull(message = "Account id is required")
    private Long accountId;
}
