package capstonesu25.warehouse.entity;
import lombok.*;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "import_request")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImportRequest {

    @Id
    @Column(name = "unique_id")
    private String uniqueID;

    @Column(name = "import_reason")
    private String importReason;

    @Column(name = "import_type")
    private String importType;

    @Column(name = "import_datetime")
    private LocalDateTime importDatetime;

    @ManyToOne
    @JoinColumn(name = "provider_id")
    private Provider provider;

    @OneToMany(mappedBy = "importRequest")
    private List<ImportOrder> importOrders;

    @ManyToOne
    @JoinColumn(name = "assigned_staff_id")
    private Account assignedStaff;
}