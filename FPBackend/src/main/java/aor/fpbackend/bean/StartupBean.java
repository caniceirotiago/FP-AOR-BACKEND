package aor.fpbackend.bean;

import aor.fpbackend.enums.LocationEnum;
import aor.fpbackend.enums.MethodEnum;
import aor.fpbackend.enums.UserRoleEnum;
import aor.fpbackend.exception.DatabaseOperationException;
import aor.fpbackend.utils.GlobalSettings;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.transaction.Transactional;
import org.apache.logging.log4j.LogManager;

import java.io.Serializable;

/**
 * StartupBean is a singleton EJB that initializes application data at startup.
 * It sets up initial roles, laboratories, users, configurations, methods, and permissions.
 * This bean ensures that the necessary data is available for the application to function correctly.
 *
 * <p>
 * Technologies Used:
 * <ul>
 *     <li>Jakarta EE: For dependency injection and EJB management.</li>
 *     <li>SLF4J: For logging operations.</li>
 * </ul>
 * </p>
 *
 * Dependencies are injected using the {@link EJB} annotation, which includes beans for role,
 * laboratory, user, configuration, and method management. The bean uses utility classes for
 * logging and ensures that transactions are handled appropriately.
 */
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
    private static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger(StartupBean.class);


    /**
     * Initializes the application data at startup.
     * This method is called after the bean's construction and dependency injection.
     */
    @PostConstruct
    public void init() throws DatabaseOperationException {
        try {
            createData();
        } catch (DatabaseOperationException e) {
            LOGGER.error("Error creating initial data: " + e.getMessage());
            throw e;
        }
    }


    /**
     * Creates the initial data for the application.
     * This includes roles, laboratories, users, configurations, methods, and permissions.
     *
     * @throws DatabaseOperationException if there is an error during the creation of data.
     */
    private void createData() throws DatabaseOperationException {
        createRoles();
        createLaboratories();
        createUsers();
        createDefaultConfigs();
        createMethods();
        addPermissions();
    }


    /**
     * Creates the initial roles for the application.
     * If a role already exists, it will not be created again.
     *
     * @throws DatabaseOperationException if there is an error during role creation.
     */
    @Transactional
    private void createRoles() throws DatabaseOperationException {
        roleBean.createRoleIfNotExists(UserRoleEnum.ADMIN);
        roleBean.createRoleIfNotExists(UserRoleEnum.STANDARD_USER);
    }

    /**
     * Creates the initial laboratories for the application.
     * If a laboratory already exists, it will not be created again.
     *
     * @throws DatabaseOperationException if there is an error during laboratory creation.
     */
    @Transactional
    private void createLaboratories() throws DatabaseOperationException {
        labBean.createLaboratoryIfNotExists(LocationEnum.LISBOA);
        labBean.createLaboratoryIfNotExists(LocationEnum.COIMBRA);
        labBean.createLaboratoryIfNotExists(LocationEnum.PORTO);
        labBean.createLaboratoryIfNotExists(LocationEnum.TOMAR);
        labBean.createLaboratoryIfNotExists(LocationEnum.VISEU);
        labBean.createLaboratoryIfNotExists(LocationEnum.VILA_REAL);
    }

    /**
     * Creates the initial users for the application.
     * If a user already exists, they will not be created again.
     *
     * @throws DatabaseOperationException if there is an error during user creation.
     */
    @Transactional
    private void createUsers() throws DatabaseOperationException {
        userBean.createDefaultUserIfNotExistent("admin", "https://i.pinimg.com/474x/7e/71/9b/7e719be79d55353a3ce6551d704e43ca.jpg",1, 2);
        userBean.createDefaultUserIfNotExistent("standardUser", "https://i.pinimg.com/474x/0a/a8/58/0aa8581c2cb0aa948d63ce3ddad90c81.jpg",2, 2);
        userBean.createDefaultUserIfNotExistent("David", "https://cdn.pixabay.com/photo/2013/07/12/14/36/man-148582_640.png",2, 3);
        userBean.createDefaultUserIfNotExistent("Diogo", "https://cdn.pixabay.com/photo/2015/03/04/22/35/avatar-659651_640.png", 2, 4);
        userBean.createDefaultUserIfNotExistent("Maria", "https://img.freepik.com/premium-vector/user-woman-avatar-female-profile-icon-woman-character-portrait-with-smile-vector_555028-184.jpg", 2, 5);
        userBean.createDefaultUserIfNotExistent("Manuela", "https://i.pinimg.com/originals/54/8a/65/548a659c2b06a877516d3c998f5b0939.png", 2, 5);
    }

    /**
     * Creates the initial configurations for the application.
     * If a configuration already exists, it will not be created again.
     *
     * @throws DatabaseOperationException if there is an error during configuration creation.
     */
    @Transactional
    private void createDefaultConfigs() throws DatabaseOperationException {
        configBean.createDefaultConfigIfNotExistent("sessionTimeout", GlobalSettings.DEFAULT_SESSION_TIMEOUT_MILLIS);
        configBean.createDefaultConfigIfNotExistent("maxProjectMembers", GlobalSettings.DEFAULT_NUMBER_MEMBERS_PER_PROJECT);
    }

    /**
     * Creates the initial methods for the application.
     * If a method already exists, it will not be created again.
     *
     * @throws DatabaseOperationException if there is an error during method creation.
     */
    @Transactional
    private void createMethods() throws DatabaseOperationException {
        methodBean.createMethodIfNotExistent(MethodEnum.UPDATE_ROLE, "updates user role",MethodEnum.UPDATE_ROLE.getValue());
        methodBean.createMethodIfNotExistent(MethodEnum.ADD_SKILL_USER, "add skill to user's list", MethodEnum.ADD_SKILL_USER.getValue());
        methodBean.createMethodIfNotExistent(MethodEnum.ADD_SKILL_PROJECT, "add skill to project's list", MethodEnum.ADD_SKILL_PROJECT.getValue());
        methodBean.createMethodIfNotExistent(MethodEnum.ALL_SKILLS, "retrieves all persisted skills", MethodEnum.ALL_SKILLS.getValue());
        methodBean.createMethodIfNotExistent(MethodEnum.SKILLS_BY_USER, "all skills by userId", MethodEnum.SKILLS_BY_USER.getValue());
        methodBean.createMethodIfNotExistent(MethodEnum.SKILLS_FIRST_LETTER, "all skills by first letter", MethodEnum.SKILLS_FIRST_LETTER.getValue());
        methodBean.createMethodIfNotExistent(MethodEnum.REMOVE_SKILL_USER, "remove skill from user's list", MethodEnum.REMOVE_SKILL_USER.getValue());
        methodBean.createMethodIfNotExistent(MethodEnum.ADD_INTEREST, "add interest to user's list", MethodEnum.ADD_INTEREST.getValue());
        methodBean.createMethodIfNotExistent(MethodEnum.ALL_INTERESTS, "retrieves all persisted interests", MethodEnum.ALL_INTERESTS.getValue());
        methodBean.createMethodIfNotExistent(MethodEnum.INTERESTS_BY_USER, "all interests by userId", MethodEnum.INTERESTS_BY_USER.getValue());
        methodBean.createMethodIfNotExistent(MethodEnum.INTERESTS_FIRST_LETTER, "all interests by first letter", MethodEnum.INTERESTS_FIRST_LETTER.getValue());
        methodBean.createMethodIfNotExistent(MethodEnum.REMOVE_INTEREST, "remove interest from user's list", MethodEnum.REMOVE_INTEREST.getValue());
        methodBean.createMethodIfNotExistent(MethodEnum.ADD_KEYWORD, "add keyword to project's list", MethodEnum.ADD_KEYWORD.getValue());
        methodBean.createMethodIfNotExistent(MethodEnum.ALL_KEYWORDS, "retrieves all persisted keywords", MethodEnum.ALL_KEYWORDS.getValue());
        methodBean.createMethodIfNotExistent(MethodEnum.KEYWORDS_BY_PROJECT, "all keywords by userId", MethodEnum.KEYWORDS_BY_PROJECT.getValue());
        methodBean.createMethodIfNotExistent(MethodEnum.KEYWORDS_FIRST_LETTER, "all keywords by first letter", MethodEnum.KEYWORDS_FIRST_LETTER.getValue());
        methodBean.createMethodIfNotExistent(MethodEnum.SKILLS_BY_PROJECT, "all skills by projectId", MethodEnum.SKILLS_BY_PROJECT.getValue());
        methodBean.createMethodIfNotExistent(MethodEnum.ADD_PROJECT, "create a new project", MethodEnum.ADD_PROJECT.getValue());
        methodBean.createMethodIfNotExistent(MethodEnum.ALL_PROJECTS, "get all projects", MethodEnum.ALL_PROJECTS.getValue());
        methodBean.createMethodIfNotExistent(MethodEnum.PROJECT_BY_ID, "get project by projectId", MethodEnum.PROJECT_BY_ID.getValue());
        methodBean.createMethodIfNotExistent(MethodEnum.CREATE_ASSET, "create a new asset entity", MethodEnum.CREATE_ASSET.getValue());
        methodBean.createMethodIfNotExistent(MethodEnum.ALL_ASSETS, "get all assets", MethodEnum.ALL_ASSETS.getValue());
        methodBean.createMethodIfNotExistent(MethodEnum.ASSETS_FIRST_LETTER, "get assets by first letter", MethodEnum.ASSETS_FIRST_LETTER.getValue());
        methodBean.createMethodIfNotExistent(MethodEnum.ASSETS_BY_PROJECT, "get assets by project id", MethodEnum.ASSETS_BY_PROJECT.getValue());
        methodBean.createMethodIfNotExistent(MethodEnum.TASKS_BY_ID, "all tasks by Id", MethodEnum.TASKS_BY_ID.getValue());
        methodBean.createMethodIfNotExistent(MethodEnum.PROJECT_ENUMS, "retrieve all project enum elements", MethodEnum.PROJECT_ENUMS.getValue());
        methodBean.createMethodIfNotExistent(MethodEnum.PROJECT_APPROVE, "approve a project in READY state", MethodEnum.PROJECT_APPROVE.getValue());
        methodBean.createMethodIfNotExistent(MethodEnum.ASK_TO_JOIN, "ask to join a project", MethodEnum.ASK_TO_JOIN.getValue());
        methodBean.createMethodIfNotExistent(MethodEnum.ASSET_ENUMS, "retrieve all asset enum elements", MethodEnum.ASSET_ENUMS.getValue());
        methodBean.createMethodIfNotExistent(MethodEnum.INTEREST_ENUMS, "retrieve all interest enum elements", MethodEnum.INTEREST_ENUMS.getValue());
        methodBean.createMethodIfNotExistent(MethodEnum.SKILL_ENUMS, "retrieve all skill enum elements", MethodEnum.SKILL_ENUMS.getValue());
        methodBean.createMethodIfNotExistent(MethodEnum.ASSET_UPDATE, "retrieve all skill enum elements", MethodEnum.ASSET_UPDATE.getValue());
        methodBean.createMethodIfNotExistent(MethodEnum.ASSETS_BY_ID, "retrieve all assets by Id", MethodEnum.ASSETS_BY_ID.getValue());
        methodBean.createMethodIfNotExistent(MethodEnum.ADD_ASSET, "add asset to project", MethodEnum.ADD_ASSET.getValue());
        methodBean.createMethodIfNotExistent(MethodEnum.FILTER_ASSETS, "get assets filtered", MethodEnum.FILTER_ASSETS.getValue());
        methodBean.createMethodIfNotExistent(MethodEnum.USERS_FIRST_LETTER, "get users by first letter", MethodEnum.USERS_FIRST_LETTER.getValue());
        methodBean.createMethodIfNotExistent(MethodEnum.ASSET_REMOVE, "remove asset from inventory list", MethodEnum.ASSET_REMOVE.getValue());
        methodBean.createMethodIfNotExistent(MethodEnum.GET_NOTIFICATIONS, "get unread notifications by user", MethodEnum.GET_NOTIFICATIONS.getValue());
        methodBean.createMethodIfNotExistent(MethodEnum.MARK_NOTIFICATIONS_AS_READ, "mark notification as read", MethodEnum.MARK_NOTIFICATIONS_AS_READ.getValue());
        methodBean.createMethodIfNotExistent(MethodEnum.UPDATE_CONFIG, "update system configurations", MethodEnum.UPDATE_CONFIG.getValue());
        methodBean.createMethodIfNotExistent(MethodEnum.USERS_BASIC_INFO, "retrieves a list of all users basic info", MethodEnum.USERS_BASIC_INFO.getValue());
        methodBean.createMethodIfNotExistent(MethodEnum.PROJECTS_REPORT, "retrieves a summary of projects info in pdf", MethodEnum.PROJECTS_REPORT.getValue());
        methodBean.createMethodIfNotExistent(MethodEnum.ASSETS_REPORT, "retrieves a summary of assets info in pdf", MethodEnum.ASSETS_REPORT.getValue());
        methodBean.createMethodIfNotExistent(MethodEnum.REMOVE_USER_PROJECT, "remove user from project", MethodEnum.REMOVE_USER_PROJECT.getValue());
        methodBean.createMethodIfNotExistent(MethodEnum.GET_ALL_CONFIG, "get all configurations", MethodEnum.GET_ALL_CONFIG.getValue());
    }

    /**
     * Adds permissions to roles for the application.
     * If a permission already exists, it will not be added again.
     *
     * @throws DatabaseOperationException if there is an error during permission addition.
     */

    @Transactional
    private void addPermissions() throws DatabaseOperationException {
        roleBean.addPermission(UserRoleEnum.ADMIN, MethodEnum.UPDATE_ROLE);
        roleBean.addPermission(UserRoleEnum.ADMIN, MethodEnum.ADD_SKILL_USER);
        roleBean.addPermission(UserRoleEnum.STANDARD_USER, MethodEnum.ADD_SKILL_USER);
        roleBean.addPermission(UserRoleEnum.ADMIN, MethodEnum.ADD_SKILL_PROJECT);
        roleBean.addPermission(UserRoleEnum.STANDARD_USER, MethodEnum.ADD_SKILL_PROJECT);
        roleBean.addPermission(UserRoleEnum.ADMIN, MethodEnum.ALL_SKILLS);
        roleBean.addPermission(UserRoleEnum.STANDARD_USER, MethodEnum.ALL_SKILLS);
        roleBean.addPermission(UserRoleEnum.ADMIN, MethodEnum.SKILLS_BY_USER);
        roleBean.addPermission(UserRoleEnum.STANDARD_USER, MethodEnum.SKILLS_BY_USER);
        roleBean.addPermission(UserRoleEnum.ADMIN, MethodEnum.SKILLS_FIRST_LETTER);
        roleBean.addPermission(UserRoleEnum.STANDARD_USER, MethodEnum.SKILLS_FIRST_LETTER);
        roleBean.addPermission(UserRoleEnum.ADMIN, MethodEnum.REMOVE_SKILL_USER);
        roleBean.addPermission(UserRoleEnum.STANDARD_USER, MethodEnum.REMOVE_SKILL_USER);
        roleBean.addPermission(UserRoleEnum.ADMIN, MethodEnum.ADD_INTEREST);
        roleBean.addPermission(UserRoleEnum.STANDARD_USER, MethodEnum.ADD_INTEREST);
        roleBean.addPermission(UserRoleEnum.ADMIN, MethodEnum.ALL_INTERESTS);
        roleBean.addPermission(UserRoleEnum.STANDARD_USER, MethodEnum.ALL_INTERESTS);
        roleBean.addPermission(UserRoleEnum.ADMIN, MethodEnum.INTERESTS_BY_USER);
        roleBean.addPermission(UserRoleEnum.STANDARD_USER, MethodEnum.INTERESTS_BY_USER);
        roleBean.addPermission(UserRoleEnum.ADMIN, MethodEnum.INTERESTS_FIRST_LETTER);
        roleBean.addPermission(UserRoleEnum.STANDARD_USER, MethodEnum.INTERESTS_FIRST_LETTER);
        roleBean.addPermission(UserRoleEnum.ADMIN, MethodEnum.REMOVE_INTEREST);
        roleBean.addPermission(UserRoleEnum.STANDARD_USER, MethodEnum.REMOVE_INTEREST);
        roleBean.addPermission(UserRoleEnum.ADMIN, MethodEnum.ADD_KEYWORD);
        roleBean.addPermission(UserRoleEnum.STANDARD_USER, MethodEnum.ADD_KEYWORD);
        roleBean.addPermission(UserRoleEnum.ADMIN, MethodEnum.ALL_KEYWORDS);
        roleBean.addPermission(UserRoleEnum.STANDARD_USER, MethodEnum.ALL_KEYWORDS);
        roleBean.addPermission(UserRoleEnum.ADMIN, MethodEnum.KEYWORDS_BY_PROJECT);
        roleBean.addPermission(UserRoleEnum.STANDARD_USER, MethodEnum.KEYWORDS_BY_PROJECT);
        roleBean.addPermission(UserRoleEnum.ADMIN, MethodEnum.KEYWORDS_FIRST_LETTER);
        roleBean.addPermission(UserRoleEnum.STANDARD_USER, MethodEnum.KEYWORDS_FIRST_LETTER);
        roleBean.addPermission(UserRoleEnum.ADMIN, MethodEnum.SKILLS_BY_PROJECT);
        roleBean.addPermission(UserRoleEnum.STANDARD_USER, MethodEnum.SKILLS_BY_PROJECT);
        roleBean.addPermission(UserRoleEnum.ADMIN, MethodEnum.ADD_PROJECT);
        roleBean.addPermission(UserRoleEnum.STANDARD_USER, MethodEnum.ADD_PROJECT);
        roleBean.addPermission(UserRoleEnum.ADMIN, MethodEnum.ALL_PROJECTS);
        roleBean.addPermission(UserRoleEnum.STANDARD_USER, MethodEnum.ALL_PROJECTS);
        roleBean.addPermission(UserRoleEnum.ADMIN, MethodEnum.PROJECT_BY_ID);
        roleBean.addPermission(UserRoleEnum.STANDARD_USER, MethodEnum.PROJECT_BY_ID);
        roleBean.addPermission(UserRoleEnum.ADMIN, MethodEnum.ALL_ASSETS);
        roleBean.addPermission(UserRoleEnum.STANDARD_USER, MethodEnum.ALL_ASSETS);
        roleBean.addPermission(UserRoleEnum.ADMIN, MethodEnum.ASSETS_FIRST_LETTER);
        roleBean.addPermission(UserRoleEnum.STANDARD_USER, MethodEnum.ASSETS_FIRST_LETTER);
        roleBean.addPermission(UserRoleEnum.ADMIN, MethodEnum.ASSETS_BY_PROJECT);
        roleBean.addPermission(UserRoleEnum.STANDARD_USER, MethodEnum.ASSETS_BY_PROJECT);
        roleBean.addPermission(UserRoleEnum.ADMIN, MethodEnum.TASKS_BY_ID);
        roleBean.addPermission(UserRoleEnum.STANDARD_USER, MethodEnum.TASKS_BY_ID);
        roleBean.addPermission(UserRoleEnum.ADMIN, MethodEnum.PROJECT_ENUMS);
        roleBean.addPermission(UserRoleEnum.STANDARD_USER, MethodEnum.PROJECT_ENUMS);
        roleBean.addPermission(UserRoleEnum.ADMIN, MethodEnum.PROJECT_APPROVE);
        roleBean.addPermission(UserRoleEnum.ADMIN, MethodEnum.ASK_TO_JOIN);
        roleBean.addPermission(UserRoleEnum.STANDARD_USER, MethodEnum.ASK_TO_JOIN);
        roleBean.addPermission(UserRoleEnum.ADMIN, MethodEnum.ASSET_ENUMS);
        roleBean.addPermission(UserRoleEnum.STANDARD_USER, MethodEnum.ASSET_ENUMS);
        roleBean.addPermission(UserRoleEnum.ADMIN, MethodEnum.CREATE_ASSET);
        roleBean.addPermission(UserRoleEnum.STANDARD_USER, MethodEnum.CREATE_ASSET);
        roleBean.addPermission(UserRoleEnum.ADMIN, MethodEnum.INTEREST_ENUMS);
        roleBean.addPermission(UserRoleEnum.STANDARD_USER, MethodEnum.INTEREST_ENUMS);
        roleBean.addPermission(UserRoleEnum.ADMIN, MethodEnum.SKILL_ENUMS);
        roleBean.addPermission(UserRoleEnum.STANDARD_USER, MethodEnum.SKILL_ENUMS);
        roleBean.addPermission(UserRoleEnum.ADMIN, MethodEnum.ASSET_UPDATE);
        roleBean.addPermission(UserRoleEnum.STANDARD_USER, MethodEnum.ASSET_UPDATE);
        roleBean.addPermission(UserRoleEnum.ADMIN, MethodEnum.ASSETS_BY_ID);
        roleBean.addPermission(UserRoleEnum.STANDARD_USER, MethodEnum.ASSETS_BY_ID);
        roleBean.addPermission(UserRoleEnum.ADMIN, MethodEnum.ADD_ASSET);
        roleBean.addPermission(UserRoleEnum.STANDARD_USER, MethodEnum.ADD_ASSET);
        roleBean.addPermission(UserRoleEnum.ADMIN, MethodEnum.FILTER_ASSETS);
        roleBean.addPermission(UserRoleEnum.STANDARD_USER, MethodEnum.FILTER_ASSETS);
        roleBean.addPermission(UserRoleEnum.ADMIN, MethodEnum.USERS_FIRST_LETTER);
        roleBean.addPermission(UserRoleEnum.STANDARD_USER, MethodEnum.USERS_FIRST_LETTER);
        roleBean.addPermission(UserRoleEnum.ADMIN, MethodEnum.ASSET_REMOVE);
        roleBean.addPermission(UserRoleEnum.STANDARD_USER, MethodEnum.ASSET_REMOVE);
        roleBean.addPermission(UserRoleEnum.ADMIN, MethodEnum.GET_NOTIFICATIONS);
        roleBean.addPermission(UserRoleEnum.STANDARD_USER, MethodEnum.GET_NOTIFICATIONS);
        roleBean.addPermission(UserRoleEnum.ADMIN, MethodEnum.MARK_NOTIFICATIONS_AS_READ);
        roleBean.addPermission(UserRoleEnum.STANDARD_USER, MethodEnum.MARK_NOTIFICATIONS_AS_READ);
        roleBean.addPermission(UserRoleEnum.ADMIN, MethodEnum.UPDATE_CONFIG);
        roleBean.addPermission(UserRoleEnum.ADMIN, MethodEnum.USERS_BASIC_INFO);
        roleBean.addPermission(UserRoleEnum.ADMIN, MethodEnum.PROJECTS_REPORT);
        roleBean.addPermission(UserRoleEnum.ADMIN, MethodEnum.ASSETS_REPORT);
        roleBean.addPermission(UserRoleEnum.ADMIN, MethodEnum.REMOVE_USER_PROJECT);
        roleBean.addPermission(UserRoleEnum.STANDARD_USER, MethodEnum.REMOVE_USER_PROJECT);
        roleBean.addPermission(UserRoleEnum.ADMIN, MethodEnum.GET_ALL_CONFIG);
        roleBean.addPermission(UserRoleEnum.STANDARD_USER, MethodEnum.GET_ALL_CONFIG);
    }
}
