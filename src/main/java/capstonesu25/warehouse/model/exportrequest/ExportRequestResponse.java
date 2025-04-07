package capstonesu25.warehouse.model.exportrequest;

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
public class ExportRequestResponse {
    private Long exportRequestId;
    private String exportReason;
    private String receiverName;
    private String receiverPhone;
    private String receiverAddress;
    private String status;
    private String type;
    private LocalDate exportDate;
    private LocalTime exportTime;
    private Long assignedWareHouseKeeperId;
    private Long paperId;
    private List<Long> importRequestIds;
    private List<Long> exportRequestDetailIds;
    private String createdBy;
    private String updatedBy;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
} 