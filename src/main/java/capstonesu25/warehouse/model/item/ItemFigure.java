package capstonesu25.warehouse.model.item;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ItemFigure {
    private Integer totalInStock;
    private Integer totalOutOfStock;
}
