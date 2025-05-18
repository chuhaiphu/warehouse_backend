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
    private String importOrderId;
    private String exportRequestId;
}
