package capstonesu25.warehouse.entity;

import capstonesu25.warehouse.enums.DetailStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "export_request_detail")
@Data
@NoArgsConstructor
@AllArgsConstructor

public class ExportRequestDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "measurement_value")
    private Double measurementValue;

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "actual_quantity")
    private Integer actualQuantity;

    @Column(name = "status")
    private DetailStatus status;

    @ManyToOne
    @JoinColumn(name = "export_request_id")
    private ExportRequest exportRequest;

    @ManyToOne
    @JoinColumn(name = "item_id")
    private Item item;

    @ManyToMany
    @JoinTable(
            name = "export_request_detail_inventory_item",
            joinColumns = @JoinColumn(name = "export_request_detail_id"),
            inverseJoinColumns = @JoinColumn(name = "inventory_item_id")
    )
    private List<InventoryItem> inventoryItems = new ArrayList<>();


}
