package capstonesu25.warehouse.model.importrequest.importrequestdetail;

import capstonesu25.warehouse.enums.ImportType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ImportRequestCreateWithDetailRequest {
    private String itemId;
    private Integer quantity;
    private Long providerId;
    
    // Thêm các field cần thiết để tạo ImportRequest
    private String importReason;
    private ImportType importType;
    private String exportRequestId;
}
