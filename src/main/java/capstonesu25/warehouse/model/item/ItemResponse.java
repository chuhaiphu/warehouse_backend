package capstonesu25.warehouse.model.item;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ItemResponse {
    private String id;
    private String name;
    private String description;
    private String measurementUnit;
    private Double totalMeasurementValue;
    private Integer quantity;
    private String unitType;
    private Integer daysUntilDue;
    private Integer minimumStockQuantity;
    private Integer maximumStockQuantity;
    private Long categoryId;
    private List<Long> providerIds;
    // Replace OneToMany relationships with list of IDs
    private List<Long> importOrderDetailIds;
    private List<Long> importRequestDetailIds;
    private List<Long> exportRequestDetailIds;
    private List<String> inventoryItemIds;
}
