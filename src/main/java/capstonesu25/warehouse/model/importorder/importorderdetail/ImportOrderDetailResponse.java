package capstonesu25.warehouse.model.importorder.importorderdetail;

import capstonesu25.warehouse.enums.DetailStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ImportOrderDetailResponse {
    private Long importOrderDetailId;
    private Long importOrderId;
    private Long itemId;
    private String itemName;
    private int expectQuantity;
    private int actualQuantity;
    private DetailStatus status;
}
