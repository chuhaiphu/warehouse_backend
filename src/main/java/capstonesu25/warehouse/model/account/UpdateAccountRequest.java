package capstonesu25.warehouse.model.account;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAccountRequest {
    @NotNull(message = "Account id can not be null")
    private Long id;

    private String fullName;

    @Email(message = "Invalid email format")
    private String email;

    @Pattern(regexp = "^\\+?[0-9]{10,12}$", message = "Invalid phone number format")
    private String phone;

    @Pattern(
            regexp = "ACTIVE|INACTIVE|ON_LEAVE|TERMINATED",
            message = "Status must be one of: ACTIVE, INACTIVE, ON_LEAVE, TERMINATED"
    )
    private String status;

    private Boolean isEnable;

    @Size(min = 6, message = "Password must have at least 6 characters")
    private String password;
}
