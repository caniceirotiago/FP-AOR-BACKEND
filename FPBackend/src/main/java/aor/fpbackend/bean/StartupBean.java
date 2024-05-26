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
        configBean.createDefaultConfigIfNotExistent("sessionTimeout", 36000000); //10 horas  em milissegundos
        configBean.createDefaultConfigIfNotExistent("maxProjectMembers", 4);
    }

    @Transactional
    private void createMethods() throws DatabaseOperationException {
        methodBean.createMethodIfNotExistent(MethodEnum.UPDATE_ROLE, "updates user role",MethodEnum.UPDATE_ROLE.getValue());
        methodBean.createMethodIfNotExistent(MethodEnum.ADD_SKILL, "add skill to user's list", MethodEnum.ADD_SKILL.getValue());
        methodBean.createMethodIfNotExistent(MethodEnum.ALL_SKILLS, "retrieves all persisted skills", MethodEnum.ALL_SKILLS.getValue());
        methodBean.createMethodIfNotExistent(MethodEnum.SKILL_BY_USER, "all skills by userId", MethodEnum.SKILL_BY_USER.getValue());
        methodBean.createMethodIfNotExistent(MethodEnum.SKILL_FIRST_LETTER, "all skills by first letter", MethodEnum.SKILL_FIRST_LETTER.getValue());
        methodBean.createMethodIfNotExistent(MethodEnum.REMOVE_SKILL, "remove skill from user's list", MethodEnum.REMOVE_SKILL.getValue());
        methodBean.createMethodIfNotExistent(MethodEnum.ADD_INTEREST, "add interest to user's list", MethodEnum.ADD_INTEREST.getValue());
        methodBean.createMethodIfNotExistent(MethodEnum.ALL_INTERESTS, "retrieves all persisted interests", MethodEnum.ALL_INTERESTS.getValue());
        methodBean.createMethodIfNotExistent(MethodEnum.INTEREST_BY_USER, "all interests by userId", MethodEnum.INTEREST_BY_USER.getValue());
        methodBean.createMethodIfNotExistent(MethodEnum.INTEREST_FIRST_LETTER, "all interests by first letter", MethodEnum.INTEREST_FIRST_LETTER.getValue());
        methodBean.createMethodIfNotExistent(MethodEnum.REMOVE_INTEREST, "remove interest from user's list", MethodEnum.REMOVE_INTEREST.getValue());
        methodBean.createMethodIfNotExistent(MethodEnum.ADD_KEYWORD, "add keyword to project's list", MethodEnum.ADD_KEYWORD.getValue());
        methodBean.createMethodIfNotExistent(MethodEnum.ALL_KEYWORDS, "retrieves all persisted keywords", MethodEnum.ALL_KEYWORDS.getValue());
        methodBean.createMethodIfNotExistent(MethodEnum.KEYWORD_BY_PROJECT, "all keywords by userId", MethodEnum.KEYWORD_BY_PROJECT.getValue());
        methodBean.createMethodIfNotExistent(MethodEnum.KEYWORD_FIRST_LETTER, "all keywords by first letter", MethodEnum.KEYWORD_FIRST_LETTER.getValue());
        methodBean.createMethodIfNotExistent(MethodEnum.REMOVE_KEYWORD, "remove keyword from user's list", MethodEnum.REMOVE_KEYWORD.getValue());
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
        roleBean.addPermission(UserRoleEnum.ADMIN, MethodEnum.REMOVE_SKILL);
        roleBean.addPermission(UserRoleEnum.STANDARD_USER, MethodEnum.REMOVE_SKILL);
        roleBean.addPermission(UserRoleEnum.ADMIN, MethodEnum.ADD_INTEREST);
        roleBean.addPermission(UserRoleEnum.STANDARD_USER, MethodEnum.ADD_INTEREST);
        roleBean.addPermission(UserRoleEnum.ADMIN, MethodEnum.ALL_INTERESTS);
        roleBean.addPermission(UserRoleEnum.STANDARD_USER, MethodEnum.ALL_INTERESTS);
        roleBean.addPermission(UserRoleEnum.ADMIN, MethodEnum.INTEREST_BY_USER);
        roleBean.addPermission(UserRoleEnum.STANDARD_USER, MethodEnum.INTEREST_BY_USER);
        roleBean.addPermission(UserRoleEnum.ADMIN, MethodEnum.INTEREST_FIRST_LETTER);
        roleBean.addPermission(UserRoleEnum.STANDARD_USER, MethodEnum.INTEREST_FIRST_LETTER);
        roleBean.addPermission(UserRoleEnum.ADMIN, MethodEnum.REMOVE_INTEREST);
        roleBean.addPermission(UserRoleEnum.STANDARD_USER, MethodEnum.REMOVE_INTEREST);
        roleBean.addPermission(UserRoleEnum.ADMIN, MethodEnum.ADD_KEYWORD);
        roleBean.addPermission(UserRoleEnum.STANDARD_USER, MethodEnum.ADD_KEYWORD);
        roleBean.addPermission(UserRoleEnum.ADMIN, MethodEnum.ALL_KEYWORDS);
        roleBean.addPermission(UserRoleEnum.STANDARD_USER, MethodEnum.ALL_KEYWORDS);
        roleBean.addPermission(UserRoleEnum.ADMIN, MethodEnum.KEYWORD_BY_PROJECT);
        roleBean.addPermission(UserRoleEnum.STANDARD_USER, MethodEnum.KEYWORD_BY_PROJECT);
        roleBean.addPermission(UserRoleEnum.ADMIN, MethodEnum.KEYWORD_FIRST_LETTER);
        roleBean.addPermission(UserRoleEnum.STANDARD_USER, MethodEnum.KEYWORD_FIRST_LETTER);
        roleBean.addPermission(UserRoleEnum.ADMIN, MethodEnum.REMOVE_KEYWORD);
        roleBean.addPermission(UserRoleEnum.STANDARD_USER, MethodEnum.REMOVE_KEYWORD);
    }

}
