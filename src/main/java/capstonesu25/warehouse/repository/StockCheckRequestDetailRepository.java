package capstonesu25.warehouse.repository;

import capstonesu25.warehouse.entity.StockCheckRequestDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StockCheckRequestDetailRepository extends JpaRepository<StockCheckRequestDetail, Long> {
    List<StockCheckRequestDetail> findByStockCheckRequest_Id(String stockCheckRequestId);
}
