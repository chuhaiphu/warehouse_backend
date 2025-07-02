package capstonesu25.warehouse.model.importorder;

import capstonesu25.warehouse.enums.RequestStatus;
import capstonesu25.warehouse.model.importorder.importorderdetail.ImportOrderDetailResponse;
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
    private Boolean isExtended;
    private LocalDate extendedDate;
    private LocalTime extendedTime;
    private String extendedReason;
    private String note;
    private RequestStatus status;
    private List<ImportOrderDetailResponse> importOrderDetails;
    private LocalDate actualDateReceived;
    private LocalTime actualTimeReceived;
    private String createdBy;
    private String updatedBy;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
    private Long paperIds;
    private Long assignedStaffId;
    private String exportRequestId;
}
