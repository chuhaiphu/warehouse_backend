package capstonesu25.warehouse.model.account;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TaskOfStaffPerDate {
    private LocalDate date;
    private Long staffId;
    private List<String> importOrderIds;
    private List<String> exportRequestIds;
    private List<String> stockCheckIds;
    private List<String> priorityTaskIds;
}
