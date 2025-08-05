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

    @Column(name = "line")
    private String line;

    @Column(name = "isRoad")
    private boolean isRoad = false;

    @Column(name = "isDoor")
    private boolean isDoor = false;

    @Column(name = "is_used")
    private boolean isUsed = false;

    @Column(name = "is_fulled")
    private boolean isFulled = false;

    @Column(name = "maximum_capacity_for_item")
    private Integer maximumCapacityForItem;

    @Column(name = "current_capacity")
    private Integer currentCapacity = 0;

    @ManyToMany(mappedBy = "storedLocations")
    private List<Item> items;

    @OneToMany(mappedBy = "storedLocation", cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    private List<InventoryItem> inventoryItems;
}
