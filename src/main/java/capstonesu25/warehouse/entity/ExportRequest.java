package capstonesu25.warehouse.entity;
import capstonesu25.warehouse.enums.ExportType;
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
@Table(name = "export_request")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)

public class ExportRequest {

    @Id
    private String id;

    @Column(name = "export_reason")
    private String exportReason;

    @Column(name = "receiver_name")
    private String receiverName;

    @Column(name = "receiver_phone")
    private String receiverPhone;

    @Column(name = "receiver_address")
    private String receiverAddress;

    @Column(name = "status")
    private RequestStatus status;

    @Column(name = "type")
    private ExportType type;

    @Column(name = "export_date")
    private LocalDate exportDate;

    @Column(name = "export_time")
    private LocalTime exportTime;

    @Column(name = "expected_return_date")
    private LocalDate expectedReturnDate;

    @Column(name = "provider_id")
    private Long providerId;

    @Column(name = "department_id")
    private Long departmentId;

    @Column(name = "counting_staff_id")
    private Long countingStaffId;

    @Column(name = "counting_date")
    private LocalDate countingDate;

    @Column(name = "counting_time")
    private LocalTime countingTime;

    @Column(name = "is_extended", nullable = false)
    private Boolean isExtended = false;

    @Column(name = "extended_date")
    private LocalDate extendedDate;

    @Column(name = "extended_time")
    private LocalTime extendedTime;

    @Column(name = "extended_reason")
    private String extendedReason;

    @Column(name = "note")
    private String note;

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

    @ManyToOne
    @JoinColumn(name = "assigned_staff_id")
    private Account assignedStaff;

    @OneToOne(mappedBy = "exportRequest", cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    private Paper paper;

    @OneToMany(mappedBy = "exportRequest", cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    private List<ImportRequest> importRequests;

    @OneToMany(mappedBy = "exportRequest", cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    private List<ExportRequestDetail> exportRequestDetails;
}
