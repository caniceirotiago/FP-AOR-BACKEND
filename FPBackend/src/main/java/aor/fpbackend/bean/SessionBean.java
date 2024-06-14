package aor.fpbackend.bean;

import aor.fpbackend.dao.SessionDao;
import aor.fpbackend.entity.SessionEntity;
import aor.fpbackend.websocket.GlobalWebSocket;
import jakarta.ejb.EJB;
import jakarta.ejb.Schedule;
import jakarta.ejb.Stateless;
import org.apache.logging.log4j.LogManager;
import java.io.Serializable;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Stateless
public class SessionBean implements Serializable {
    @EJB
    SessionDao sessionDao;

    private static final long serialVersionUID = 1L;
    private static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger(SessionBean.class);

    @Schedule(hour = "*", minute = "*/1", persistent = false)
    public void cleanupExpiredTokens() {
        List<SessionEntity> sessions = sessionDao.findSessionsExpiringInThreeMinutes();
        Instant now = Instant.now();

        for (SessionEntity session : sessions) {
            Instant expirationTime = session.getTokenExpiration();
            long minutesUntilExpiration = ChronoUnit.MINUTES.between(now, expirationTime);

            if (minutesUntilExpiration <= 1) {
                if (session.isActive()) {
                    session.setActive(false);
                    sessionDao.merge(session);
                    LOGGER.info("Session inactivated: " + session.getId());
                }
            } else  {
                if (session.isActive()) {
                    GlobalWebSocket.sendForcedLogoutRequest(session);
                    LOGGER.info("Forced logout sent for session: " + session.getId());
                }
            }
        }
    }
}
