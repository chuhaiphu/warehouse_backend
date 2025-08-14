package capstonesu25.warehouse.repository;

import capstonesu25.warehouse.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    @Transactional
    void deleteByReceiverId(Long receiverId);
}
