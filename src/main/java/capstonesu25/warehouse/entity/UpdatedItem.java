package capstonesu25.warehouse.entity;
import lombok.*;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "updated_item")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdatedItem {

    @Id
    @Column(name = "unique_id")
    private String uniqueID;

    @Column(name = "status")
    private String status;

    @Column(name = "description")
    private String description;

    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

    @Column(name = "quantity")
    private int quantity;

    @ManyToOne
    @JoinColumn(name = "item_id")
    private Item item;
}