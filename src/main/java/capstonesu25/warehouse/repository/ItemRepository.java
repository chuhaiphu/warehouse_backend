package capstonesu25.warehouse.repository;

import capstonesu25.warehouse.entity.Item;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {
    Page<Item> findByCategoryId(Long categoryId, Pageable pageable);
    Page<Item> findByProviderId(Long providerId, Pageable pageable);
    long countByCategoryId(Long categoryId);
    long countByProviderId(Long providerId);
}