package capstonesu25.warehouse.model.importrequest.importrequestdetail;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ImportRequestDetailRequest {
    @NotEmpty(message = "Item IDs cannot be empty")
    private List<Long> itemId;

    @NotEmpty(message = "Quantities cannot be empty")
    private List<Integer> quantity;

    private List<Integer> actualQuantity;

    private List<Integer> remainingQuantity;

    @NotNull(message = "can not be null")
    private Long providerId;
}
