package capstonesu25.warehouse.model.account;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CheckAnyKeepersIsAvailableInDateRequest {
    private List<ListItems> listItems;
    private LocalDate date;
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ListItems {
        private String itemId;
        private Integer quantity;
        private Double measurementValue;
    }
}
