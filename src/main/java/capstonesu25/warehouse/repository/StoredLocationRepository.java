package capstonesu25.warehouse.repository;

import capstonesu25.warehouse.entity.StoredLocation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StoredLocationRepository extends JpaRepository<StoredLocation, Long> {
    Page<StoredLocation> findByIsUsedFalseAndIsFulledFalse(Pageable pageable);
    Page<StoredLocation> findByZone(String zone, Pageable pageable);
    Page<StoredLocation> findByFloor(String floor, Pageable pageable);

    List<StoredLocation> findByZoneAndFloorAndLineAndRow(
            String zone, String floor, String line, String row);

    List<StoredLocation> findByItem_IdAndIsFulledFalseOrderByZoneAscFloorAscRowAscLineAsc(String itemId);

    boolean existsByZoneAndFloorAndRowAndLine(String zone, String floor, String row, String line);
}
