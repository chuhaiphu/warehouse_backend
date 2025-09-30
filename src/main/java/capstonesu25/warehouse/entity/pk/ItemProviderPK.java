package capstonesu25.warehouse.entity.pk;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
public class ItemProviderPK implements Serializable {
    @Column(name = "provider_id")
    private Long providerId;

    @Column(name = "item_id")
    private String itemId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ItemProviderPK)) return false;
        ItemProviderPK that = (ItemProviderPK) o;
        return Objects.equals(providerId, that.providerId)
                && Objects.equals(itemId, that.itemId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(providerId, itemId);
    }
}

