package capstonesu25.warehouse.model.paper;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;
@Data
@NoArgsConstructor
public class PaperRequest {
    private Long id;
    @NotNull(message = "can not be null")
    private MultipartFile signProviderUrl;
    private String signProviderName;
    @NotNull(message = "can not be null")
    private MultipartFile signReceiverUrl;
    private String signReceiverName;
    private String description;
    private String importOrderId;
    private String exportRequestId;
}
