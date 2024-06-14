package aor.fpbackend.dao;


import aor.fpbackend.entity.SessionEntity;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;


@Stateless
public class SessionDao extends AbstractDao<SessionEntity> {
    private static final long serialVersionUID = 1L;

    public SessionDao() {
        super(SessionEntity.class);
    }

    @PersistenceContext
    private EntityManager em;


    public SessionEntity findValidSessionByToken(String tokenValue) {
        try {
            return (SessionEntity) em.createNamedQuery("Session.findValidSessionByToken")
                    .setParameter("tokenValue", tokenValue)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public SessionEntity findSessionByAuthToken(String tokenValue) {
        try {
            return (SessionEntity) em.createNamedQuery("Session.findSessionByAuthToken")
                    .setParameter("tokenValue", tokenValue)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
    public SessionEntity findSessionBySessionToken(String tokenValue) {
        try {
            return (SessionEntity) em.createNamedQuery("Session.findSessionBySessionToken")
                    .setParameter("tokenValue", tokenValue)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
    public List<SessionEntity> findSessionsExpiringInThreeMinutes() {
        Instant oneMinuteFromNow = Instant.now().plusSeconds(180);
        TypedQuery<SessionEntity> query = em.createNamedQuery("Session.findSessionsExpiringInACertainAmountOfSeconds", SessionEntity.class);
        query.setParameter("expirationTime", oneMinuteFromNow);
        return query.getResultList();
    }
    public boolean inativateSessionbyAuthToken(String tokenValue) {
        try {
            SessionEntity session = (SessionEntity) em.createNamedQuery("Session.findSessionByAuthToken")
                    .setParameter("tokenValue", tokenValue)
                    .getSingleResult();
            session.setActive(false);
            em.merge(session);
            return true;
        } catch (NoResultException e) {
            return false;
        }
    }
    public boolean inativateSessionbySessionToken(String tokenValue) {
        try {
            SessionEntity session = (SessionEntity) em.createNamedQuery("Session.findSessionBySessionToken")
                    .setParameter("tokenValue", tokenValue)
                    .getSingleResult();
            session.setActive(false);
            em.merge(session);
            return true;
        } catch (NoResultException e) {
            return false;
        }
    }
    public ArrayList<SessionEntity> findAllSessionsByUserId(long userId) {
        try {
            ArrayList<SessionEntity> sessionsByUserId = (ArrayList<SessionEntity>) em.createNamedQuery("Session.findAllSessionsByUserId")
                    .setParameter("userId", userId)
                    .getResultList();
            return sessionsByUserId;
        } catch (Exception e) {
            return null;
        }
    }
}
