package capstonesu25.warehouse.model.importrequest.importrequestdetail;

import capstonesu25.warehouse.enums.ImportType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ImportRequestCreateWithDetailRequest {
    private String itemId;
    private Double measurementValue;
    private Integer quantity;
    private Long providerId;
    
    @NotNull(message = "Import reason cannot be null")
    @NotBlank(message = "Import reason cannot be blank")
    private String importReason;

    private ImportType importType;

    private LocalDate startDate;

    private LocalDate endDate;

    private Long departmentId;
}
