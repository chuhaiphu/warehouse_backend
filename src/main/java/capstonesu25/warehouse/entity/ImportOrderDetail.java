package capstonesu25.warehouse.entity;

import capstonesu25.warehouse.enums.DetailStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "import_order_detail")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImportOrderDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "status")
    private DetailStatus status;

    @Column(name = "expect_quantity")
    private int expectQuantity;

    @Column(name = "actual_quantity")
    private int actualQuantity;

    @Column(name = "expect_measurement_value")
    private Double expectMeasurementValue;

    @Column(name = "actual_measurement_value")
    private Double actualMeasurementValue;

    @ManyToOne
    @JoinColumn(name = "item_id")
    private Item item;

    @OneToMany(mappedBy = "importOrderDetail", cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    private List<InventoryItem> inventoryItems;

    @ManyToOne
    @JoinColumn(name = "import_order_id")
    private ImportOrder importOrder;
}
