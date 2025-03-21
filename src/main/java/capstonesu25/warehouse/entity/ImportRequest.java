package capstonesu25.warehouse.entity;
import capstonesu25.warehouse.enums.ImportStatus;
import capstonesu25.warehouse.enums.ImportType;
import lombok.*;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "import_request")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class ImportRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "import_reason")
    private String importReason;

    @Column(name = "status")
    private ImportStatus status;

    @Column(name = "type")
    private ImportType type;

    @CreatedBy
    @Column(name = "created_by", nullable = false, updatable = false)
    private String createdBy;

    @CreatedDate
    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @LastModifiedBy
    @Column(name = "updated_by", insertable = false)
    private String updatedBy;

    @LastModifiedDate
    @Column(name = "updated_date", insertable = false)
    private LocalDateTime updatedDate;

    @OneToMany(mappedBy = "importRequest", cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    private List<ImportRequestDetail> details;

    @OneToMany(mappedBy = "importRequest", cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    private List<ImportOrder> importOrders;

    @ManyToOne
    @JoinColumn(name = "export_request_id")
    private ExportRequest exportRequest;

    @ManyToOne
    @JoinColumn(name = "provider_id")
    private Provider provider;
}
