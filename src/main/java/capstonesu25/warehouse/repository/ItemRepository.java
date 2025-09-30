package capstonesu25.warehouse.repository;

import capstonesu25.warehouse.entity.Item;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemRepository extends JpaRepository<Item, String> {
    Page<Item> findByCategoryId(Long categoryId, Pageable pageable);
    Page<Item> findByItemProviders_Provider_Id(Long providerId, Pageable pageable);

    long countByCategoryId(Long categoryId);
}