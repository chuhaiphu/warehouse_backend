package capstonesu25.warehouse.model.exportrequest;

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
    @NotNull(message = "cannot be null")
    @NotBlank(message = "cannot be blank")
    private String exportReason;

    @NotNull(message = "cannot be null")
    @NotBlank(message = "cannot be blank")
    private String receiverName;

    @NotNull(message = "cannot be null")
    @NotBlank(message = "cannot be blank")
    private String receiverPhone;

    @NotNull(message = "cannot be null")
    @NotBlank(message = "cannot be blank")
    private String receiverAddress;

    @NotNull(message = "cannot be null")
    @NotBlank(message = "cannot be blank")
    private String type;

    @NotNull(message = "cannot be null")
    private LocalDate exportDate;

    @NotNull(message = "cannot be null")
    private LocalTime exportTime;

    private Long assignedWareHouseKeeperId;
} 