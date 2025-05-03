package capstonesu25.warehouse.model.importorder.importorderdetail;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

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