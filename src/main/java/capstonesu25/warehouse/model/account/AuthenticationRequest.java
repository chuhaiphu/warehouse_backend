package capstonesu25.warehouse.model.account;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthenticationRequest {
    @NotNull(message = "can not be null")
    @NotBlank(message = "can not be blank")
    private String username;

    @NotNull(message = "can not be null")
    @NotBlank(message = "can not be blank")
    private String password;
}
