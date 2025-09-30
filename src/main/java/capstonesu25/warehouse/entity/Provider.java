package capstonesu25.warehouse.entity;
import java.util.ArrayList;
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

    @OneToMany(mappedBy = "provider", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemProvider> itemProviders = new ArrayList<>();

    @OneToMany(mappedBy = "provider", cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    private List<ImportRequest> importRequest;
}
