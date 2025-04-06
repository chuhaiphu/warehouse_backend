package capstonesu25.warehouse.model.inventoryitem;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QrCodeRequest {
    private Long itemId;
    private Long importOrderDetailId;
    private int numberOfQrCodes;
}
