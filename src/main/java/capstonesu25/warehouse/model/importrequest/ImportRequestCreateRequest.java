package capstonesu25.warehouse.model.importrequest;

import capstonesu25.warehouse.enums.ImportType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ImportRequestCreateRequest {
    @NotNull(message = "Import reason cannot be null")
    @NotBlank(message = "Import reason cannot be blank")
    private String importReason;

    @NotNull(message = "Import type cannot be null")
    private ImportType importType;

    private LocalDate startDate;

    private LocalDate endDate;

    private Long departmentId;

    private List<ReturnImportRequestDetail> returnImportRequestDetails;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ReturnImportRequestDetail {
        private String inventoryItemId;
        private Double measurementValue;
    }
}