package capstonesu25.warehouse.model.exportrequest.exportproduction;

import capstonesu25.warehouse.enums.ExportType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExportRequestRequest {
    private String exportReason;

    private String receiverName;

    private String receiverPhone;

    private Long departmentId;

    private String receiverAddress;

    @NotNull(message = "cannot be null")
    @NotBlank(message = "cannot be blank")
    private ExportType type;

    @NotNull(message = "cannot be null")
    private LocalDate exportDate;

    @NotNull(message = "cannot be null")
    private LocalTime exportTime;

    private Long assignedWareHouseKeeperId;
} 