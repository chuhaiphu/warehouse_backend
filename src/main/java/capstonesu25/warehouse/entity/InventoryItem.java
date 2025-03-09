package capstonesu25.warehouse.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
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

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "status")
    private String status;

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

    @ManyToOne
    @JoinColumn(name = "export_request_detail_id")
    private ExportRequestDetail exportRequestDetail;

    @ManyToOne
    @JoinColumn(name = "import_order_detail_id")
    private ImportOrderDetail importOrderDetail;

    @ManyToOne
    @JoinColumn(name = "stored_location_id")
    private StoredLocation storedLocation;
}
