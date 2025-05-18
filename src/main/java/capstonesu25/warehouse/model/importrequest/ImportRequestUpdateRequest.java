package capstonesu25.warehouse.model.importrequest;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ImportRequestUpdateRequest {
    private String importRequestId;
    
    @NotNull(message = "Import reason cannot be null")
    @NotBlank(message = "Import reason cannot be blank")
    private String importReason;
} 