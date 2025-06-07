package capstonesu25.warehouse.model.importorder.importorderdetail;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ImportOrderDetailRequest {
    private Long providerId;
    private List<ImportOrderItem> importOrderItems;
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ImportOrderItem {
        private String itemId;
        private Integer quantity;
    }
}
