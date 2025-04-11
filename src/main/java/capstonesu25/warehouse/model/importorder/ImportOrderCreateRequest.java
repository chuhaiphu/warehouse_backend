package capstonesu25.warehouse.model.importorder;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ImportOrderCreateRequest {
    @NotNull(message = "Import request ID cannot be null")
    private Long importRequestId;

    private Long accountId;

    private LocalDate dateReceived;

    private LocalTime timeReceived;

    private String note;
} 