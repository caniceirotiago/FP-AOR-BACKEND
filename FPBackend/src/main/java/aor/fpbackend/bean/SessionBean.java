package aor.fpbackend.bean;

import aor.fpbackend.dao.SessionDao;
import aor.fpbackend.dao.SkillDao;
import aor.fpbackend.dao.UserDao;
import aor.fpbackend.dto.AuthUserDto;
import aor.fpbackend.dto.SkillDto;
import aor.fpbackend.entity.SessionEntity;
import aor.fpbackend.entity.SkillEntity;
import aor.fpbackend.entity.UserEntity;
import aor.fpbackend.websocket.GlobalWebSocket;
import jakarta.ejb.EJB;
import jakarta.ejb.Schedule;
import jakarta.ejb.Stateless;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.SecurityContext;
import org.apache.logging.log4j.LogManager;

import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Stateless
public class SessionBean implements Serializable {
    @EJB
    SessionDao sessionDao;

    private static final long serialVersionUID = 1L;

    private static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger(SessionBean.class);

    @Schedule(hour = "*", minute = "*/1", persistent = false)
    public void cleanupExpiredTokens() {
        List<SessionEntity> sessions = sessionDao.findSessionsExpiringInOneMinute();
        Instant now = Instant.now();

        for (SessionEntity session : sessions) {
            if (session.isActive()) {
                GlobalWebSocket.sendForcedLogoutRequest(session);
            }
        }
    }
}
