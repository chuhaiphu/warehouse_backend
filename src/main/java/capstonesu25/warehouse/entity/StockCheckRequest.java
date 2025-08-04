package capstonesu25.warehouse.entity;

import capstonesu25.warehouse.enums.RequestStatus;
import capstonesu25.warehouse.enums.StockCheckType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
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
@Table(name = "stock_check_request")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class StockCheckRequest {

    @Id
    private String id;

    @Column(name = "stock_check_reason")
    private String stockCheckReason;

    @Column(name = "status")
    private RequestStatus status;

    @Column(name = "type")
    private StockCheckType type;

    @Column(name = "export_date")
    private LocalDate startDate;

    @Column(name = "expected_return_date")
    private LocalDate expectedCompletedDate;

    @Column(name = "counting_date")
    private LocalDate countingDate;

    @Column(name = "counting_time")
    private LocalTime countingTime;

//    @Column(name = "is_extended", nullable = false)
//    private Boolean isExtended = false;
//
//    @Column(name = "extended_date")
//    private LocalDate extendedDate;
//
//    @Column(name = "extended_reason")
//    private String extendedReason;

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

    @OneToMany(mappedBy = "stockCheckRequest", cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    private List<StockCheckRequestDetail> stockCheckRequestDetails;

    @OneToOne(mappedBy = "stockCheckRequest", cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    private Paper paper;

}
