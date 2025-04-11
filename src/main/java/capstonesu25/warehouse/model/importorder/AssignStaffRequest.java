package capstonesu25.warehouse.model.importorder;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssignStaffRequest {
    private Long importOrderId;
    private Long accountId;
} 