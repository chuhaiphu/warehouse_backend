package capstonesu25.warehouse.model.importrequest;

import capstonesu25.warehouse.enums.RequestStatus;
import capstonesu25.warehouse.enums.ImportType;
import capstonesu25.warehouse.model.importrequest.importrequestdetail.ImportRequestDetailResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ImportRequestResponse {
    private String importRequestId;
    private String importReason;
    private ImportType importType;
    private RequestStatus status;
    private Long providerId;
    private List<ImportRequestDetailResponse> importRequestDetails;
    private List<String> importOrdersId;
    private String createdBy;
    private String updatedBy;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
    private String batchCode;
    private LocalDate startDate;
    private LocalDate endDate;


}
