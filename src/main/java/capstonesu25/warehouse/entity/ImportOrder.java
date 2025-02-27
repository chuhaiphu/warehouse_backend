package capstonesu25.warehouse.entity;
import lombok.*;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "import_order")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImportOrder {

    @Id
    @Column(name = "unique_id")
    private String uniqueID;

    @Column(name = "unit_of_measurement")
    private String unitOfMeasurement;

    @Column(name = "estimate_import_quantity")
    private BigDecimal estimateImportQuantity;

    @Column(name = "actual_import_quantity")
    private BigDecimal actualImportQuantity;

    @Column(name = "sign_shipper")
    private String signShipper;

    @Column(name = "sign_receiver")
    private String signReceiver;

    @ManyToOne
    @JoinColumn(name = "item_id")
    private Item item;

    @ManyToOne
    @JoinColumn(name = "stored_location_id")
    private StoredLocation storedLocation;

    @ManyToOne
    @JoinColumn(name = "import_request_id")
    private ImportRequest importRequest;
}