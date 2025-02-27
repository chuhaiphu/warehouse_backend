package capstonesu25.warehouse.entity;
import capstonesu25.warehouse.enums.OrderStatus;
import lombok.*;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.List;

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

    @Column(name = "status")
    private OrderStatus status;

    @OneToMany(mappedBy = "importOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ImportOrderDetail> importOrderDetails;

    @ManyToOne
    @JoinColumn(name = "group_item_id")
    private GroupItem groupItem;

    @ManyToOne
    @JoinColumn(name = "stored_location_id")
    private StoredLocation storedLocation;

    @ManyToOne
    @JoinColumn(name = "import_request_id")
    private ImportRequest importRequest;
}