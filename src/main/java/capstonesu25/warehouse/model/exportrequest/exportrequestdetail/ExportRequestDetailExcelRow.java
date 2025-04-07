package capstonesu25.warehouse.model.exportrequest.exportrequestdetail;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExportRequestDetailExcelRow {
    private Long itemId;
    private Integer quantity;
} 