package capstonesu25.warehouse.model.exportrequest.exportrequestdetail;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExportRequestDetailRequest {
    private String itemId;
    private Integer quantity;
    private Double measurementValue;
    private String inventoryItemId;
}
