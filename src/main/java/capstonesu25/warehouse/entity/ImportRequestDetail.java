package capstonesu25.warehouse.entity;

import capstonesu25.warehouse.enums.RequestDetailStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "import_request_detail")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImportRequestDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private GroupItem groupItem;

    @Column(name = "imported_quantity")
    private int importedQuantity;

    @Column(name = "status")
    private RequestDetailStatus status;

    @Column(name = "lack_quantity")
    private int lackQuantity;

    @Column(name = "excess_quantity")
    private int excessQuantity;

    @ManyToOne
    private ImportRequest importRequest;
}
