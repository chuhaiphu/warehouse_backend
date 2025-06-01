package capstonesu25.warehouse.model.account;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
    @NotNull(message = "Name can not be null")
    @NotBlank(message = "Name can not be blank")
    private String fullName;

    @NotNull(message = "Username can not be null")
    @NotBlank(message = "Username can not be blank")
    private String username;

    @NotNull(message = "Email can not be null")
    @NotBlank(message = "Email can not be blank")
    @Email
    private String email;

    @NotNull(message = "Phone can not be null")
    @NotBlank(message = "Phone can not be blank")
    @Pattern(regexp = "^\\+?[0-9]{10,12}$", message = "Invalid phone number format")
    private String phone;

    @NotNull(message = "Password can not be null")
    @NotBlank(message = "Password can not be blank")
    private String password;

    @NotNull(message = "Role can not be null")
    @Pattern(regexp = "DEPARTMENT|STAFF|WAREHOUSE_MANAGER|ACCOUNTING|ADMIN", 
            message = "Role must be one of: DEPARTMENT, STAFF, WAREHOUSE_MANAGER, ACCOUNTING, ADMIN")
    private String role;
}