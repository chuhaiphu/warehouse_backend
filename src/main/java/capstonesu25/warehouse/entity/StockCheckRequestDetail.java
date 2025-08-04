package capstonesu25.warehouse.entity;

import capstonesu25.warehouse.enums.DetailStatus;
import capstonesu25.warehouse.utils.StringListConverter;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Entity
@Table(name = "stock_check_request_detail")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockCheckRequestDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "measurement_value")
    private Double measurementValue;

    @Column(name = "actual_measurement_value")
    private Double actualMeasurementValue;

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "actual_quantity")
    private Integer actualQuantity;

    @Column(name = "status")
    private DetailStatus status;

    @Convert(converter = StringListConverter.class)
    @Column(name = "inventory_item_ids", columnDefinition = "TEXT")
    private List<String> inventoryItemsId;

    @ManyToOne
    @JoinColumn(name = "stock_check_request_id")
    private StockCheckRequest stockCheckRequest;

    @ManyToOne
    @JoinColumn(name = "item_id")
    private Item item;

}
