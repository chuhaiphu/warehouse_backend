package capstonesu25.warehouse.model.item;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ImExNumberItem {
    private List<String> importOrderIds;
    private List<String> exportRequestIds;
    private Double ImportMeasurementValue;
    private Double ExportMeasurementValue;
}
