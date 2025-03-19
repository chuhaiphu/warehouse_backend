package capstonesu25.warehouse.model.inventoryitem;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QrCodeResponse {
    private Long id;
    private Long itemId;
    private Long importOrderDetailId;
    private Long exportRequestDetailId;
    private Long storedLocationId;
}
