package capstonesu25.warehouse.entity;

import capstonesu25.warehouse.enums.ItemStatus;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "inventory_item")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "reason_for_disposal")
    private String reasonForDisposal;

    @Column(name = "measurement_value")
    private Double measurementValue;

    @Column(name = "status")
    private ItemStatus status;

    @Column(name = "expired_date")
    private LocalDateTime expiredDate;

    @Column(name = "imported_date")
    private LocalDateTime importedDate;

    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    @JsonBackReference
    private InventoryItem parent;

    @OneToMany(mappedBy = "parent")
    @JsonManagedReference
    private List<InventoryItem> children;

    @ManyToOne
    @JoinColumn(name = "item_id")
    private Item item;

    @ManyToMany(mappedBy = "inventoryItems")
    private List<ExportRequestDetail> exportRequestDetails = new ArrayList<>();


    @ManyToOne
    @JoinColumn(name = "import_order_detail_id")
    private ImportOrderDetail importOrderDetail;

    @ManyToOne
    @JoinColumn(name = "stored_location_id")
    private StoredLocation storedLocation;
}
