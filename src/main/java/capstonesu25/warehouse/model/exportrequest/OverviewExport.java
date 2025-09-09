package capstonesu25.warehouse.model.exportrequest;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OverviewExport {
    private Integer numberOfOngoingExport;
    private Integer numberOfFinishExport;
}
