package capstonesu25.warehouse.model.account;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ActiveAccountRequest {
    @NotNull(message = "Date cannot be null")
    private LocalDate date;
    private Long importOrderId;
    private Long exportRequestId;
}
