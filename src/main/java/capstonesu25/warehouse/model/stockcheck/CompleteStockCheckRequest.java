package capstonesu25.warehouse.model.stockcheck;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CompleteStockCheckRequest {
    private List<Long> stockCheckRequestDetailIds;
}
