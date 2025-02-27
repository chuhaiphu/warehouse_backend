package capstonesu25.warehouse.entity;

import jakarta.persistence.*;

public class ImportOrderDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany
    private GroupItem groupItem;

    @ManyToOne
    @JoinColumn(name = "import_order_id") // Khóa ngoại trỏ đến ImportOrder
    private ImportOrder importOrder;

    @Column(name = "expect_imported_quantity")
    private int expectImportedQuantity;

    @Column(name = "actual_imported_quantity")
    private int actualImportedQuantity;
}
