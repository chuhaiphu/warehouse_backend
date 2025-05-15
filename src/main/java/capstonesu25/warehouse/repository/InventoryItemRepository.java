package capstonesu25.warehouse.repository;

import capstonesu25.warehouse.entity.InventoryItem;
import capstonesu25.warehouse.enums.ItemStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface InventoryItemRepository extends JpaRepository<InventoryItem, Long> {
    Page<InventoryItem> findByImportOrderDetailId(Long importOrderDetailId, Pageable pageable);
    Page<InventoryItem> findByExportRequestDetailId(Long exportRequestDetailId, Pageable pageable);
    Page<InventoryItem> findByStoredLocationId(Long storedLocationId, Pageable pageable);
    List<InventoryItem> findByItem_IdAndParentNullAndStatus(Long itemId, ItemStatus status);
    List<InventoryItem> findByItem_Id(Long itemId);
    List<InventoryItem> findByItem_IdAndExpiredDateLessThanEqual(Long itemId, LocalDateTime expiredDate);

}
