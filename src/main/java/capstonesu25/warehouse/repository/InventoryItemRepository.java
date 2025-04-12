package capstonesu25.warehouse.repository;

import capstonesu25.warehouse.entity.InventoryItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface InventoryItemRepository extends JpaRepository<InventoryItem, Long> {
    Page<InventoryItem> findByImportOrderDetailId(Long importOrderDetailId, Pageable pageable);
    @Query("SELECT i FROM InventoryItem i JOIN i.exportRequestDetails d WHERE d.id = :exportRequestDetailId")
    Page<InventoryItem> findByExportRequestDetailId(@Param("exportRequestDetailId") Long exportRequestDetailId, Pageable pageable);
    Page<InventoryItem> findByStoredLocationId(Long storedLocationId, Pageable pageable);
    List<InventoryItem> findByItem_IdAndParentNull(Long itemId);
    List<InventoryItem> findByItem_Id(Long itemId);
    List<InventoryItem> findByItem_IdAndExpiredDateLessThanEqual(Long itemId, LocalDateTime expiredDate);

}
