package capstonesu25.warehouse.entity;

import lombok.*;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "paper")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Paper {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sign_provider_url")
    private String signProviderUrl;

    @Column(name = "sign_warehouse_url")
    private String signWarehouseUrl;

    @Column(name = "description")
    private String description;

    @OneToOne
    @JoinColumn(name = "import_order_id")
    private ImportOrder importOrder;

    @OneToOne
    @JoinColumn(name = "export_request_id")
    private ExportRequest exportRequest;

}
