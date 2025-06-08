package capstonesu25.warehouse.model.exportrequest;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RenewExportRequestRequest {
    private String exportRequestId;
    private List<itemList> items;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class itemList {
        private String itemId;
        private Integer quantity;
        private Double measurementValue;
    }
}
