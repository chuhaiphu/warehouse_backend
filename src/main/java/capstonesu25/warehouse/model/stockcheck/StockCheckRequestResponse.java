package capstonesu25.warehouse.model.stockcheck;

import capstonesu25.warehouse.enums.RequestStatus;
import capstonesu25.warehouse.enums.StockCheckType;
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
public class StockCheckRequestResponse {
    private String id;
    private String stockCheckReason;
    private RequestStatus status;
    private StockCheckType type;
    private LocalDate startDate;
    private LocalDate expectedCompletedDate;
    private LocalDate countingDate;
    private LocalTime countingTime;
    private String note;
    private Long assignedWareHouseKeeperId;
    private List<Long> stockCheckRequestDetailIds;
    private Long paperId;
    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;
    private String createdBy;
    private String lastModifiedBy;
}
