package capstonesu25.warehouse.model.importrequest;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OverviewImport {
    private Integer numberOfOngoingImport;
    private Integer numberOfFinishImport;
}
