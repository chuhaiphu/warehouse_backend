package capstonesu25.warehouse.model.importrequest.importrequestdetail;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ImportRequestDetailRequest {
    @NotNull(message = "can not be null")
    @NotBlank(message = "can not be blank")
    private Long itemId;

    @NotNull(message = "can not be null")
    @NotBlank(message = "can not be blank")
    private int quantity;
}
