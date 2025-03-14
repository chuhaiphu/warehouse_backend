package capstonesu25.warehouse.model.item;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ItemRequest {
    private Long id;
    private String name;
    private String description;
    private String measurementUnit;
    private Double totalMeasurementValue;
    private String unitType;
    private Integer daysUntilDue;
    private Integer minimumStockQuantity;
    private Integer maximumStockQuantity;
    private Long categoryId;
    private Long providerId;
}
