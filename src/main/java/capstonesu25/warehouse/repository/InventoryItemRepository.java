package capstonesu25.warehouse.repository;

import capstonesu25.warehouse.entity.InventoryItem;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InventoryItemRepository extends JpaRepository<InventoryItem, Long> {
    List<InventoryItem> findByImportOrderDetailId(Long importOrderDetailId, Pageable pageable);
    List<InventoryItem> findByExportRequestDetailId(Long exportRequestDetailId, Pageable pageable);
    List<InventoryItem> findByStoredLocationId(Long storedLocationId, Pageable pageable);
}
