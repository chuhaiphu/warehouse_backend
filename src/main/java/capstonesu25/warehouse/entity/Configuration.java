package capstonesu25.warehouse.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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


}
