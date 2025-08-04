package capstonesu25.warehouse.model.stockcheck.detail;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StockCheckRequestDetailRequest {
    private String itemId;
    private Integer quantity;
    private Double measurementValue;
}
