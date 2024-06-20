package aor.fpbackend.dao;

import aor.fpbackend.entity.NotificationEntity;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hibernate.Hibernate;

import java.util.List;

@Stateless
public class NotificationDao extends AbstractDao<NotificationEntity>{
    private static final long serialVersionUID = 1L;
    @PersistenceContext
    private EntityManager em;

    public NotificationDao() {
        super(NotificationEntity.class);
    }

    public List<NotificationEntity> getUnreadbByUserNotifications(Long userId) {
        List<NotificationEntity> notifications = em.createQuery("SELECT n FROM NotificationEntity n WHERE n.user.id = :userId AND n.isRead = false", NotificationEntity.class)
                .setParameter("userId", userId)
                .getResultList();

        // Initialize the lazy collection
        for (NotificationEntity notification : notifications) {
            Hibernate.initialize(notification.getUser().getLaboratory().getUsers());
        }

        return notifications;
    }
    public NotificationEntity findNotificationById(Long notificationId) {
        NotificationEntity notificationEntity = em.createQuery("SELECT n FROM NotificationEntity n WHERE n.id = :notificationId", NotificationEntity.class)
                .setParameter("notificationId", notificationId)
                .getSingleResult();
        return notificationEntity;
    }

}
