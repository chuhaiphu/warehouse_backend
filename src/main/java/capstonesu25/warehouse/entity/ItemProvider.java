package capstonesu25.warehouse.entity;

import capstonesu25.warehouse.entity.pk.ItemProviderPK;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "provider_item")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemProvider {

    @EmbeddedId
    private ItemProviderPK id;

    @ManyToOne
    @MapsId("providerId")
    @JoinColumn(name = "provider_id")
    private Provider provider;

    @ManyToOne
    @MapsId("itemId")
    @JoinColumn(name = "item_id")
    private Item item;

    @Column(name = "provider_code")
    private String providerCode;
}
