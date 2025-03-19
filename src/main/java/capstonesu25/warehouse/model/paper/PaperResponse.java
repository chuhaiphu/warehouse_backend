package capstonesu25.warehouse.model.paper;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@RequiredArgsConstructor
@NoArgsConstructor
public class PaperResponse {
    private Long id;
    private String signProviderUrl;
    private String signWarehouseUrl;
    private String description;
    private Long importOrderId;
    private Long exportRequestId;
}
