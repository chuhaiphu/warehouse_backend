package capstonesu25.warehouse.entity;
import lombok.*;
import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "provider")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Provider {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "phone")
    private String phone;

    @Column(name = "address")
    private String address;

    @ManyToMany
    @JoinTable(
            name = "provider_item", // join table name
            joinColumns = @JoinColumn(name = "provider_id"), // FK to Provider
            inverseJoinColumns = @JoinColumn(name = "item_id") // FK to Item
    )
    private List<Item> items;

    @OneToMany(mappedBy = "provider", cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    private List<ImportRequest> importRequest;
}
