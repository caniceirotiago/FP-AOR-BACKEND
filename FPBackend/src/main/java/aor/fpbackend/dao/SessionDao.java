package aor.fpbackend.dao;


import aor.fpbackend.entity.SessionEntity;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;

import java.util.ArrayList;



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

    public SessionEntity findSessionByToken(String tokenValue) {
        try {
            return (SessionEntity) em.createNamedQuery("Session.findSessionByToken")
                    .setParameter("tokenValue", tokenValue)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
    public boolean inativateSessionbyToken(String tokenValue) {
        try {
            SessionEntity session = (SessionEntity) em.createNamedQuery("Session.findSessionByToken")
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
