package capstonesu25.warehouse.model.account;

import capstonesu25.warehouse.enums.AccountRole;
import capstonesu25.warehouse.enums.AccountStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterResponse {
    private Long id;
    private String email;
    private String phone;
    private String fullName;
    private AccountRole role;
    private AccountStatus status;
    private Boolean isEnable;
    private Boolean isBlocked;
}