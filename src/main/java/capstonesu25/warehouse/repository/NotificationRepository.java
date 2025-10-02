package capstonesu25.warehouse.repository;

import capstonesu25.warehouse.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    @Transactional
    void deleteByReceiverId(Long receiverId);

    @Query("SELECT n FROM Notification n WHERE n.receiver.id = :receiverId ORDER BY n.createdDate DESC")
    List<Notification> findAllByReceiverIdOrderByCreatedDateDesc(@Param("receiverId") Long receiverId);
}
