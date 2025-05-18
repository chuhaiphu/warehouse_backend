package capstonesu25.warehouse.model.importorder;

import capstonesu25.warehouse.enums.ImportStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ImportOrderResponse {
    private String importOrderId;
    private String importRequestId;
    private LocalDate dateReceived;
    private LocalTime timeReceived;
    private String note;
    private ImportStatus status;
    private List<Long> importOrderDetailIds;
    private String createdBy;
    private String updatedBy;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
    private Long paperIds;
    private Long assignedStaffId;
}
