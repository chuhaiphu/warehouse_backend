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
    @Column(name = "unique_id")
    private String uniqueID;

    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @Column(name = "type")
    private String type;

    @Column(name = "link_pdf")
    private String linkPdf;

    @ManyToOne
    @JoinColumn(name = "import_order_id")
    private ImportOrder importOrder;

    @ManyToOne
    @JoinColumn(name = "export_order_id")
    private ExportOrder exportOrder;
}