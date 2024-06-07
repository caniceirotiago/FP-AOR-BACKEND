package aor.fpbackend.bean;

import aor.fpbackend.dao.*;
import aor.fpbackend.dto.*;
import aor.fpbackend.entity.*;
import aor.fpbackend.enums.LogTypeEnum;
import aor.fpbackend.enums.ProjectRoleEnum;
import aor.fpbackend.enums.UserRoleEnum;
import aor.fpbackend.exception.*;
import aor.fpbackend.utils.EmailService;
import aor.fpbackend.utils.JwtKeyProvider;
import io.jsonwebtoken.*;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.persistence.NoResultException;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.Key;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Stateless
public class MembershipBean implements Serializable {
    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = LogManager.getLogger(MembershipBean.class);

    @EJB
    UserDao userDao;
    @EJB
    EmailService emailService;
    @EJB
    ProjectDao projectDao;
    @EJB
    ProjectMembershipDao projectMemberDao;

    // TODO verificar se já é membro
    public void askToJoinProject(ProjectAskJoinDto projectAskJoinDto, SecurityContext securityContext) throws EntityNotFoundException {
        AuthUserDto authUserDto = (AuthUserDto) securityContext.getUserPrincipal();
        UserEntity userEntity = userDao.findUserById(authUserDto.getUserId());
        ProjectEntity projectEntity = projectDao.findProjectById(projectAskJoinDto.getProjectId());
        if (projectEntity != null && userEntity != null) {
            ProjectMembershipEntity projectMembershipEntity = new ProjectMembershipEntity();
            projectMembershipEntity.setProject(projectEntity);
            projectMembershipEntity.setUser(userEntity);
            projectMembershipEntity.setRole(ProjectRoleEnum.NORMAL_USER);
            projectMembershipEntity.setAccepted(false);
            projectMembershipEntity.setAcceptanceToken(UUID.randomUUID().toString());
            projectMemberDao.persist(projectMembershipEntity);
            sendJoinRequisitionToManagers(projectMembershipEntity, userEntity, projectEntity);
        } else {
            throw new EntityNotFoundException("Project or User not found");
        }
    }

    public void sendJoinRequisitionToManagers(ProjectMembershipEntity membershipEntity, UserEntity user, ProjectEntity projectEntity) {
        List<UserEntity> projectManagers = projectMemberDao.findProjectManagers(projectEntity.getId());
        for (UserEntity manager : projectManagers) {
            emailService.sendJoinRequisitionToManagersEmail(manager.getEmail(), user.getUsername(), projectEntity.getName(), membershipEntity.getAcceptanceToken());
        }
    }


    // TODO fazer verificação se pedido já foi aceite (Testar no Postman)
    // Incluir Project Log
    public void confirmProjectInvite(String token) throws EntityNotFoundException {
        ProjectMembershipEntity membershipEntity = projectMemberDao.findProjectMembershipByAcceptanceToken(token);
        if (membershipEntity == null) {
            throw new EntityNotFoundException("Project membership not found");
        }
        membershipEntity.setAccepted(true);
        membershipEntity.setAcceptanceToken(null);
    }


}
