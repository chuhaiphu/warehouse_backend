package capstonesu25.warehouse.model.exportrequest.exportrequestdetail;

import capstonesu25.warehouse.enums.DetailStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ExportRequestDetailResponse {
    private Long id;
    private Double measurementValue;
    private Double actualMeasurementValue;
    private DetailStatus status;
    private Long exportRequestId;
    private Long itemId;
    private List<Long> inventoryItemIds;
}
