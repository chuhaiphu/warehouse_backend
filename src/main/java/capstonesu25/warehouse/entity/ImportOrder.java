package capstonesu25.warehouse.entity;
import capstonesu25.warehouse.enums.RequestStatus;
import lombok.*;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Entity
@Table(name = "import_order")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)

public class ImportOrder {
    @Id
    private String id;

    @Column(name = "status")
    private RequestStatus status;

    @Column(name = "note")
    private String note;

    @Column(name = "date_received")
    private LocalDate dateReceived;

    @Column(name = "time_received")
    private LocalTime timeReceived;

    @Column(name = "actual_date_received")
    private LocalDate actualDateReceived;

    @Column(name = "actual_time_received")
    private LocalTime actualTimeReceived;

    @Column(name = "is_extended", nullable = false)
    private boolean isExtended = false;

    @Column(name = "extended_date")
    private LocalDate extendedDate;

    @Column(name = "extended_time")
    private LocalTime extendedTime;

    @Column(name = "extended_reason")
    private String extendedReason;

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

    @OneToMany(mappedBy = "importOrder", cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    private List<ImportOrderDetail> importOrderDetails;

    @OneToOne(mappedBy = "importOrder", cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    private Paper paper;

    @ManyToOne
    @JoinColumn(name = "import_request_id")
    private ImportRequest importRequest;

    @ManyToOne
    @JoinColumn(name = "assigned_staff_id")
    private Account assignedStaff;

}
