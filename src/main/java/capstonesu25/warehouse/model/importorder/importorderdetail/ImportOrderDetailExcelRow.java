package capstonesu25.warehouse.model.importorder.importorderdetail;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ImportOrderDetailExcelRow {
    private Long itemId;
    private int quantity;
} 