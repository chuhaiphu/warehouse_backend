package capstonesu25.warehouse.model.importorder.importorderdetail;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ImportOrderDetailRequest {
    @NotNull(message = "can not be null")
    @NotBlank(message = "can not be blank")
    private Long itemId;

    @NotNull(message = "can not be null")
    @NotBlank(message = "can not be blank")
    private int quantity;

    private int actualQuantity;

}
