package capstonesu25.warehouse.model.staffperformance;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TaskInDateOfStaff {
    List<String> importOrderIds;
    List<String> exportRequestIdsOfCounting;
    List<String> exportRequestIdsOfConfirmation;
}
