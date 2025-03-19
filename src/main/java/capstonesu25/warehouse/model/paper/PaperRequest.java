package capstonesu25.warehouse.model.paper;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.web.multipart.MultipartFile;
@Data
@RequiredArgsConstructor
@NoArgsConstructor
public class PaperRequest {
    private Long id;
    @NotNull(message = "can not be null")
    private MultipartFile signProviderUrl;
    @NotNull(message = "can not be null")
    private MultipartFile signWarehouseUrl;
    private String description;
    private Long importOrderId;
    private Long exportRequestId;
}
