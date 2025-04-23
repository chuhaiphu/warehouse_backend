package capstonesu25.warehouse.model.account;

import capstonesu25.warehouse.enums.AccountRole;
import capstonesu25.warehouse.enums.AccountStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccountResponse {
    private Long id;
    private String email;
    private String phone;
    private String fullName;
    private AccountStatus status;
    private Boolean isEnable;
    private Boolean isBlocked;
    private AccountRole role;
    private LocalTime totalActualWorkingTimeOfRequestInDay;
    private LocalTime totalExpectedWorkingTimeOfRequestInDay;
    private List<Long> importOrderIds;
    private List<Long> exportRequestIds;
} 