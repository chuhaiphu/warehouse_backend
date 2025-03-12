package capstonesu25.warehouse.repository;

import capstonesu25.warehouse.entity.ImportRequestDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ImportRequestDetailRepository extends JpaRepository<ImportRequestDetail, Long>{
}
