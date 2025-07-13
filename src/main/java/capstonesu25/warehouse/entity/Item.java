package capstonesu25.warehouse.entity;
import lombok.*;
import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "item")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Item {

    @Id
    private String id;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "measurement_unit")
    private String measurementUnit;

    @Column(name = "total_measurement_value")
    private Double totalMeasurementValue;

    @Column(name = "measurement_value")
    private Double measurementValue;

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "unit_type")
    private String unitType;

    @Column(name = "days_until_due")
    private Integer daysUntilDue;

    @Column(name = "minimum_stock_quantity")
    private Integer minimumStockQuantity;

    @Column(name = "maximum_stock_quantity")
    private Integer maximumStockQuantity;

    @Column(name = "counting_minutes")
    private Integer countingMinutes = 0;

    @ManyToOne
    @JoinColumn(name = "configuration_id")
    private Configuration configuration;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToMany(mappedBy = "items")
    private List<Provider> providers;

    @OneToMany(mappedBy = "item", cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    private List<ImportOrderDetail> importOrderDetails;

    @OneToMany(mappedBy = "item", cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    private List<ImportRequestDetail> importRequestDetails;

    @OneToMany(mappedBy = "item", cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    private List<ExportRequestDetail> exportRequestDetails;

    @OneToMany(mappedBy = "item", cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    private List<InventoryItem> inventoryItems;

    @OneToMany(mappedBy = "item", cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    private List<StoredLocation> storedLocations;

}
