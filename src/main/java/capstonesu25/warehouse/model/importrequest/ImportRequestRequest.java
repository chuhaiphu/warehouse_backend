package capstonesu25.warehouse.model.importrequest;

import capstonesu25.warehouse.enums.ImportType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ImportRequestRequest {
    @NotNull(message = "cannot be null")
    @NotBlank(message = "cannot be blank")
    private String importReason;

    @NotNull(message = "cannot be null")
    @NotBlank(message = "cannot be blank")
    private ImportType importType;

    @NotNull(message = "cannot be null")
    @NotBlank(message = "cannot be blank")
    private Long providerId;

    private Long exportRequestId;



}
