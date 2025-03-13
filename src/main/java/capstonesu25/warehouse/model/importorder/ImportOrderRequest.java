package capstonesu25.warehouse.model.importorder;

import capstonesu25.warehouse.enums.ImportStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Date;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ImportOrderRequest {
    private Long importOrderId;
    private ImportStatus status;

    @NotNull(message = "can not be null")
    private Long importRequestId;

    @NotNull(message = "can not be null")
    private Long accountId;

    @NotNull(message = "can not be null")
    private LocalDate dateReceived;

    @NotNull(message = "can not be null")
    private LocalTime timeReceived;

    private String note;

}
