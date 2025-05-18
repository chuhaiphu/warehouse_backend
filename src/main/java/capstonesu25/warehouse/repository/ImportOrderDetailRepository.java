package capstonesu25.warehouse.repository;

import capstonesu25.warehouse.entity.ImportOrderDetail;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ImportOrderDetailRepository extends JpaRepository<ImportOrderDetail, Long> {
    Page<ImportOrderDetail> findImportOrderDetailByImportOrder_Id(String importOrderId, Pageable pageable);
    List<ImportOrderDetail> findImportOrderDetailByImportOrder_Id(String importOrderId);
}
