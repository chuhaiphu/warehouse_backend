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
    @Column(name = "unique_id")
    private String uniqueID;

    @Column(name = "zone_description")
    private String zoneDescription;

    @Column(name = "floor_description")
    private String floorDescription;

    @Column(name = "row_description")
    private String rowDescription;

    @Column(name = "batch_description")
    private String batchDescription;

    @Column(name = "is_used")
    private boolean isUsed;

    @Column(name = "is_available")
    private boolean isAvailable;

    @OneToMany(mappedBy = "location")
    private List<Item> items;
}