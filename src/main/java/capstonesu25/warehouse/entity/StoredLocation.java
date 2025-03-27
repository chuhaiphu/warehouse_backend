package capstonesu25.warehouse.entity;
import lombok.*;
import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "stored_location")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StoredLocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "zone")
    private String zone;

    @Column(name = "floor")
    private String floor;

    @Column(name = "row")
    private String row;

    @Column(name = "batch")
    private String batch;

    @Column(name = "is_used")
    private boolean isUsed;

    @Column(name = "is_fulled")
    private boolean isFulled;

    @Column(name = "maximum_capacity_for_item")
    private double maximumCapacityForItem;

    @Column(name = "current_capacity")
    private double currentCapacity;

    @ManyToOne
    @JoinColumn(name = "item_id", nullable = true)
    private Item item;

    @OneToMany(mappedBy = "storedLocation", cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    private List<InventoryItem> inventoryItems;
}
