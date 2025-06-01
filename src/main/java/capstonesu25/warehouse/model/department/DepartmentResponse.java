package capstonesu25.warehouse.model.department;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DepartmentResponse {
    private Long id;
    private String departmentName;
    private String departmentResponsible;
    private String location;
    private String phone;
}
