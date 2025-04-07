package capstonesu25.warehouse.model.paper;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PaperResponse {
    private Long id;
    private String signProviderUrl;
    private String signWarehouseUrl;
    private String description;
    private Long importOrderId;
    private Long exportRequestId;
}
