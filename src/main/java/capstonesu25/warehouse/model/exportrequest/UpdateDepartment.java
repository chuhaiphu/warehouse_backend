package capstonesu25.warehouse.model.exportrequest;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateDepartment {
    private String exportRequestId;
    private Long departmentId;
}
