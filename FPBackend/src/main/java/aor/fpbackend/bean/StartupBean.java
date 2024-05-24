package aor.fpbackend.bean;

import aor.fpbackend.enums.LocationEnum;
import aor.fpbackend.enums.MethodEnum;
import aor.fpbackend.enums.UserRoleEnum;
import aor.fpbackend.exception.DatabaseOperationException;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;

import jakarta.transaction.Transactional;

import java.io.Serializable;
import java.util.Base64;


@Singleton
@Startup
public class StartupBean implements Serializable {
    private static final long serialVersionUID = 1L;

    @EJB
    RoleBean roleBean;
    @EJB
    LaboratoryBean labBean;
    @EJB
    UserBean userBean;
    @EJB
    ConfigurationBean configBean;
    @EJB
    MethodBean methodBean;


    @PostConstruct
    public void init() {
        try {
            createData();
        } catch (DatabaseOperationException e) {
            throw new IllegalStateException("Failed to initialize application data", e);
        }
    }

    private void createData() throws DatabaseOperationException {
        // Create roles
        createRoles();

        // Create laboratories
        createLaboratories();

        // Create an admin
        // Create a test user
        createUsers();

        // Create default session timeout
        // Create default max members per project
        createDefaultConfigs();

        // Create methods
        createMethods();

        // Define permissions
        addPermissions();

        // Create a test project where the test user is a manager

        // Create a test task in the test project

    }

    @Transactional
    private void createRoles() throws DatabaseOperationException {
        roleBean.createRoleIfNotExists(UserRoleEnum.ADMIN);
        roleBean.createRoleIfNotExists(UserRoleEnum.STANDARD_USER);
    }

    @Transactional
    private void createLaboratories() throws DatabaseOperationException {
        labBean.createLaboratoryIfNotExists(LocationEnum.LISBOA);
        labBean.createLaboratoryIfNotExists(LocationEnum.COIMBRA);
        labBean.createLaboratoryIfNotExists(LocationEnum.PORTO);
        labBean.createLaboratoryIfNotExists(LocationEnum.TOMAR);
        labBean.createLaboratoryIfNotExists(LocationEnum.VISEU);
        labBean.createLaboratoryIfNotExists(LocationEnum.VILA_REAL);
    }


    @Transactional
    private void createUsers() throws DatabaseOperationException {
        userBean.createDefaultUserIfNotExistent("admin", 1, 2);
        userBean.createDefaultUserIfNotExistent("standardUser", 2, 2);
    }

    @Transactional
    private void createDefaultConfigs() throws DatabaseOperationException {
        configBean.createDefaultConfigIfNotExistent("sessionTimeout", 1800);
        configBean.createDefaultConfigIfNotExistent("maxProjectMembers", 4);
    }

    @Transactional
    private void createMethods() throws DatabaseOperationException {
        methodBean.createMethodIfNotExistent(MethodEnum.UPDATE_ROLE, "updates user role");
        methodBean.createMethodIfNotExistent(MethodEnum.ADD_SKILL, "creates new skill");
        methodBean.createMethodIfNotExistent(MethodEnum.ALL_SKILLS, "retrieves all persisted skills");
        methodBean.createMethodIfNotExistent(MethodEnum.SKILL_BY_USER, "all skills by userId");
        methodBean.createMethodIfNotExistent(MethodEnum.SKILL_FIRST_LETTER, "all skills by first letter");

    }

    @Transactional
    private void addPermissions() throws DatabaseOperationException {
        roleBean.addPermission(UserRoleEnum.ADMIN, MethodEnum.UPDATE_ROLE);
        roleBean.addPermission(UserRoleEnum.ADMIN, MethodEnum.ADD_SKILL);
        roleBean.addPermission(UserRoleEnum.STANDARD_USER, MethodEnum.ADD_SKILL);
        roleBean.addPermission(UserRoleEnum.ADMIN, MethodEnum.ALL_SKILLS);
        roleBean.addPermission(UserRoleEnum.STANDARD_USER, MethodEnum.ALL_SKILLS);
        roleBean.addPermission(UserRoleEnum.ADMIN, MethodEnum.SKILL_BY_USER);
        roleBean.addPermission(UserRoleEnum.STANDARD_USER, MethodEnum.SKILL_BY_USER);
        roleBean.addPermission(UserRoleEnum.ADMIN, MethodEnum.SKILL_FIRST_LETTER);
        roleBean.addPermission(UserRoleEnum.STANDARD_USER, MethodEnum.SKILL_FIRST_LETTER);
    }




}
