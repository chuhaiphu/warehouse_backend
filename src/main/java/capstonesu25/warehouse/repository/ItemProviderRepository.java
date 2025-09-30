package capstonesu25.warehouse.repository;

import capstonesu25.warehouse.entity.ItemProvider;
import capstonesu25.warehouse.entity.pk.ItemProviderPK;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemProviderRepository extends JpaRepository<ItemProvider, ItemProviderPK> {
    List<ItemProvider> findByProvider_Id(Long providerId);
    List<ItemProvider> findByItem_Id(String itemId);
    Optional<ItemProvider> findByProvider_IdAndItem_Id(Long providerId, String itemId);
}
