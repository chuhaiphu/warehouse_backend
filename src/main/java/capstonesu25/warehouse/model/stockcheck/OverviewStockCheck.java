package capstonesu25.warehouse.model.stockcheck;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OverviewStockCheck {
    private Integer numberOfOngoingStockCheck;
    private Integer numberOfFinishStockCheck;
}
