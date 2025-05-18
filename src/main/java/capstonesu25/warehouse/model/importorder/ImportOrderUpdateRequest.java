package capstonesu25.warehouse.model.importorder;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ImportOrderUpdateRequest {
    private String importOrderId;
    private LocalDate dateReceived;
    private LocalTime timeReceived;
    private String note;
} 