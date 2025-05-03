package capstonesu25.warehouse.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "staff_performance")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StaffPerformance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "import_order_id")
    private Long importOrderId;

    @Column(name = "export_request_id")
    private Long exportRequestId;

    @Column(name = "is_export_counting")
    private boolean exportCounting;

    @Column(name = "date")
    private LocalDate date;

    @Column(name = "expected_working_time")
    private LocalTime expectedWorkingTime;

    @Column(name = "actual_working_time")
    private LocalTime actualWorkingTime;

    @ManyToOne
    @JoinColumn(name = "assigned_staff_id")
    private Account assignedStaff;
}
