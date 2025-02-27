package capstonesu25.warehouse.entity;
import lombok.*;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "export_order")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExportOrder {

    @Id
    @Column(name = "unique_id")
    private String uniqueID;

    @Column(name = "unit_of_measurement")
    private String unitOfMeasurement;

    @Column(name = "export_quantity")
    private BigDecimal exportQuantity;

    @Column(name = "item_status")
    private String itemStatus;

    @ManyToOne
    @JoinColumn(name = "item_id")
    private Item item;

    @ManyToOne
    @JoinColumn(name = "export_request_id")
    private ExportRequest exportRequest;
}