package capstonesu25.warehouse.model.stockcheck;

import capstonesu25.warehouse.enums.StockCheckType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StockCheckRequestRequest {
    private String stockCheckReason;

    private StockCheckType type;

    private LocalDate startDate;

    private LocalDate expectedCompletedDate;

    private LocalDate countingDate;

    private LocalTime countingTime;

    private String note;

//    private Long assignedWareHouseKeeperId;
}
