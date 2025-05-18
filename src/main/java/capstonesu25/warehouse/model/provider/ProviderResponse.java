package capstonesu25.warehouse.model.provider;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProviderResponse {
    private Long id;
    private String name;
    private String phone;
    private String address;
    private List<Long> itemIds;
    private List<String> importRequestId;
}
