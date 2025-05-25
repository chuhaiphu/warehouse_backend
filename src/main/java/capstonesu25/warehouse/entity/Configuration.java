package capstonesu25.warehouse.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Entity
@Table(name = "configuration")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Configuration {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(mappedBy = "configuration", cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    private List<Item> items;

    @Column(name = "working_time_start")
    private LocalTime workingTimeStart;

    @Column(name = "working_time_end")
    private LocalTime workingTimeEnd;

    @Column(name = "create_request_time_at_least")
    // Thoi gian toi thieu de cac phieu duoc tao
    private LocalTime createRequestTimeAtLeast;

    @Column(name = "time_to_allow_assign")
    private LocalTime timeToAllowAssign;

    @Column(name = "time_to_allow_confirm")
    private LocalTime timeToAllowConfirm;

    @Column(name = "time_to_allow_cancel")
    // time to allow auto cancel when the request is created
    private LocalTime timeToAllowCancel;

    @Column(name = "extended_to_cancel_date")
    private Integer extendedToCancelDate;

    @Column(name = "max_allowed_date_for_extended")
    private Integer maxAllowedDateForExtended;

    @Column(name = "maximum_time_for_import_request_process")
    private Integer maximumTimeForImportRequestProcess;


}
