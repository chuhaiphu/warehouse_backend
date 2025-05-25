package capstonesu25.warehouse.model.configuration;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConfigurationDto {
    private Long id;
    private List<String> itemIds;
    private LocalTime workingTimeStart;
    private LocalTime workingTimeEnd;
    private LocalTime createRequestTimeAtLeast;
    private LocalTime timeToAllowAssign;
    private LocalTime timeToAllowConfirm;
    private LocalTime timeToAllowCancel;
    private Integer daysToAllowExtend;
    private Integer maxAllowedDaysForExtend;
}
