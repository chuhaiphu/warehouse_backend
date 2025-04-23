package capstonesu25.warehouse.model.staffperformance;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class StaffPerformanceResponse {
    private Long id;
    private Long accountId;
    private LocalDate date;
    private LocalTime expectedWorkingTimeOfRequest;
    private LocalTime actualWorkingTimeOfRequest;
    private LocalTime totalActualWorkingTimeOfRequestInDay;
    private LocalTime totalExpectedWorkingTimeOfRequestInDay;
    private Integer numberOfRequestInDay;


}
