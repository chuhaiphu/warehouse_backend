package capstonesu25.warehouse.repository;

import capstonesu25.warehouse.entity.ImportRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ImportRequestRepository extends JpaRepository<ImportRequest, Long> {
}
