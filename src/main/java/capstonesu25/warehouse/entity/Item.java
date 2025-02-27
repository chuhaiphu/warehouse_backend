package capstonesu25.warehouse.entity;
import lombok.*;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "item")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // FK to GroupItem
    @ManyToOne
    @JoinColumn(name = "group_items_id")
    private GroupItem groupItem;

    @Column(name = "quantity")
    private String quantity;

    @Column(name = "inputted_date")
    private LocalDateTime inputtedDate;

    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

    @Column(name = "expired_date")
    private LocalDateTime expiredDate;

    // FK to StoredLocation
    @ManyToOne
    @JoinColumn(name = "location_id")
    private StoredLocation location;

    @Column(name = "is_valid")
    private Boolean isValid;

    // FK to Provider
    @ManyToOne
    @JoinColumn(name = "provider_id")
    private Provider provider;

    private String status;

    @OneToMany(mappedBy = "item")
    private List<ImportOrder> importOrders;

    @OneToMany(mappedBy = "item")
    private List<ExportOrder> exportOrders;

    @OneToMany(mappedBy = "item")
    private List<UpdatedItem> updatedItems;
}
