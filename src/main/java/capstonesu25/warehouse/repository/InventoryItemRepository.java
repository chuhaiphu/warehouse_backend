package capstonesu25.warehouse.repository;

import capstonesu25.warehouse.entity.InventoryItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InventoryItemRepository extends JpaRepository<InventoryItem, Long> {
    Page<InventoryItem> findByImportOrderDetailId(Long importOrderDetailId, Pageable pageable);
    Page<InventoryItem> findByExportRequestDetailId(Long exportRequestDetailId, Pageable pageable);
    Page<InventoryItem> findByStoredLocationId(Long storedLocationId, Pageable pageable);
    List<InventoryItem> findByItem_IdAndImportOrderDetail_IdAndQuantity(Long itemId, Long importOrderDetailId, Integer quantity);
    List<InventoryItem> findByItem_IdAndExportRequestDetail_IdAndQuantity(Long itemId, Long exportRequestDetailId, Integer quantity);

}
