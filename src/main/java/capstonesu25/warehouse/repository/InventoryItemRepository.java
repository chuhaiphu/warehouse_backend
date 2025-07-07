package capstonesu25.warehouse.repository;

import capstonesu25.warehouse.entity.InventoryItem;
import capstonesu25.warehouse.enums.ItemStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryItemRepository extends JpaRepository<InventoryItem, String> {
    Page<InventoryItem> findByImportOrderDetailId(Long importOrderDetailId, Pageable pageable);
    Page<InventoryItem> findByExportRequestDetailId(Long exportRequestDetailId, Pageable pageable);
    Page<InventoryItem> findByStoredLocationId(Long storedLocationId, Pageable pageable);
    Page<InventoryItem> findByItem_IdAndParentNullAndStatusAndNeedReturnToProvider(String itemId,ItemStatus status, Boolean needReturnToProvider, Pageable pageable);
    List<InventoryItem> findByItem_IdAndParentNullAndStatus(String itemId, ItemStatus status);
    List<InventoryItem> findByItem_IdAndParentNullAndStatusAndNeedReturnToProvider(
            String itemId, ItemStatus status, Boolean needReturnToProvider);
    List<InventoryItem> findByItem_Id(String itemId);
    List<InventoryItem> findByItem_IdAndExpiredDateLessThanEqual(String itemId, LocalDateTime expiredDate);
    List<InventoryItem> findByImportOrderDetailIdIn(List<Long> importOrderDetailIds);

    List<InventoryItem> findByExportRequestDetail_Id(Long exportRequestDetailId);


}
