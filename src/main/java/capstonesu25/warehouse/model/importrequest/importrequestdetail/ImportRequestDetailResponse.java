package capstonesu25.warehouse.model.importrequest.importrequestdetail;

import capstonesu25.warehouse.enums.DetailStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ImportRequestDetailResponse {
    private Long importRequestDetailId;
    private String importRequestId;
    private String itemId;
    private String itemName;
    private int actualQuantity;
    private int expectQuantity;
    private int orderedQuantity;
    private DetailStatus status;


}
