package capstonesu25.warehouse.model.importrequest.importrequestdetail;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ImportRequestDetailExcelRow {
    private Long itemId;
    private Integer quantity;
    private Long providerId;
}
