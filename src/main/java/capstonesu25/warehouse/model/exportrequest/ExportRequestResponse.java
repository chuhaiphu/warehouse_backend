package capstonesu25.warehouse.model.exportrequest;

import capstonesu25.warehouse.enums.ExportType;
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
public class ExportRequestResponse {
    private String exportRequestId;
    private String exportReason;
    private String receiverName;
    private String receiverPhone;
    private String receiverAddress;
    private Long departmentId;
    private Long providerId;
    private ImportStatus status;
    private ExportType type;
    private LocalDate exportDate;
    private LocalTime exportTime;
    private LocalDate expectedReturnDate;
    private Long assignedWareHouseKeeperId;
    private LocalDate countingDate;
    private LocalTime countingTime;
    private Long countingStaffId;
    private Long paperId;
    private List<String> importRequestIds;
    private List<Long> exportRequestDetailIds;
    private String createdBy;
    private String updatedBy;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
} 