package capstonesu25.warehouse.model.storedlocation;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class StoredLocationRequest {
    @NotNull(message = "cannot be null")
    @NotEmpty(message = "cannot be empty")
    private String zone;
    @NotNull(message = "cannot be null")
    @NotEmpty(message = "cannot be empty")
    private String floor;
    @NotNull(message = "cannot be null")
    @NotEmpty(message = "cannot be empty")
    private String row;
    @NotNull(message = "cannot be null")
    @NotEmpty(message = "cannot be empty")
    private String line;
    @NotNull(message = "cannot be null")
    private Boolean isRoad;
    @NotNull(message = "cannot be null")
    private Boolean isDoor;
    @NotNull(message = "cannot be null")
    private Integer maximumCapacityForItem;
    private String itemId; // References to item
}
