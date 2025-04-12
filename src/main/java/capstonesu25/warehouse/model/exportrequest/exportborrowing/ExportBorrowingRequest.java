package capstonesu25.warehouse.model.exportrequest.exportborrowing;

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
public class ExportBorrowingRequest {
    @NotNull(message = "cannot be null")
    private LocalDate exportDate;

    @NotNull(message = "cannot be null")
    private LocalTime exportTime;

    private String receiverName;

    private String receiverPhone;

    private String receiverAddress;

    private Long departmentId;

    @NotNull(message = "cannot be null")
    private LocalDate expectedReturnDate;

    private String exportReason;

    private ExportType type;

}
