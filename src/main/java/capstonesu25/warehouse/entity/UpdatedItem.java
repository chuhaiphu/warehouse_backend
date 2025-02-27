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

    private String status;
    private String description;

    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

    private String quality;

    @ManyToOne
    @JoinColumn(name = "item_id")
    private Item item;
}