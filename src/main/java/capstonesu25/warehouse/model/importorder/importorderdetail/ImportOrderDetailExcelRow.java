package capstonesu25.warehouse.model.importorder.importorderdetail;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ImportOrderDetailExcelRow {
    private Long itemId;
    private Integer quantity;
    private LocalDate dateReceived;
    private LocalTime timeReceived;
    private String note;
} 