package capstonesu25.warehouse.entity;
import lombok.*;
import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "group_item")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GroupItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;

    @Column(name = "name")
    private String name;

    @Column(name = "measurement_unit")
    private String measurementUnit;

    @Column(name = "image_url")
    private String imageURL;

    @Column(name = "max_quantity")
    private int maxQuantity;

    @Column(name = "quantity")
    private int quantity;

    @Column(name = "price")
    private double price;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    @OneToMany(mappedBy = "groupItem")
    private List<Item> items;
}