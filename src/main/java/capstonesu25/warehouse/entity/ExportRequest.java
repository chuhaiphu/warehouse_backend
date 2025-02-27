package capstonesu25.warehouse.entity;
import lombok.*;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "export_request")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExportRequest {

    @Id
    @Column(name = "unique_id")
    private String uniqueID;

    @Column(name = "export_name")
    private String exportName;

    @Column(name = "export_reason")
    private String exportReason;

    @Column(name = "export_datetime")
    private LocalDateTime exportDatetime;

    @Column(name = "priority_level")
    private Integer priorityLevel;

    @Column(name = "estimate_total_value")
    private BigDecimal estimateTotalValue;

    @OneToMany(mappedBy = "exportRequest")
    private List<ExportOrder> exportOrders;
}