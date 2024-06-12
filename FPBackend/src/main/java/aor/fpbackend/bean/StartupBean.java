package aor.fpbackend.bean;

import aor.fpbackend.enums.LocationEnum;
import aor.fpbackend.enums.MethodEnum;
import aor.fpbackend.enums.UserRoleEnum;
import aor.fpbackend.exception.DatabaseOperationException;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;

import jakarta.transaction.Transactional;

import java.io.Serializable;


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
        userBean.createDefaultUserIfNotExistent("admin", "https://i.pinimg.com/474x/7e/71/9b/7e719be79d55353a3ce6551d704e43ca.jpg",1, 2);
        userBean.createDefaultUserIfNotExistent("standardUser", "https://i.pinimg.com/474x/0a/a8/58/0aa8581c2cb0aa948d63ce3ddad90c81.jpg",2, 2);
        userBean.createDefaultUserIfNotExistent("BrunoAleixo", "https://cdn.pixabay.com/photo/2013/07/12/14/36/man-148582_640.png",2, 3);
        userBean.createDefaultUserIfNotExistent("MasterJohn", "https://cdn.pixabay.com/photo/2015/03/04/22/35/avatar-659651_640.png", 2, 4);
        userBean.createDefaultUserIfNotExistent("BQuim", "https://img.freepik.com/premium-vector/user-woman-avatar-female-profile-icon-woman-character-portrait-with-smile-vector_555028-184.jpg", 2, 5);
        userBean.createDefaultUserIfNotExistent("SuperToy", "https://i.pinimg.com/originals/54/8a/65/548a659c2b06a877516d3c998f5b0939.png", 2, 5);
    }

    @Transactional
    private void createDefaultConfigs() throws DatabaseOperationException {
        configBean.createDefaultConfigIfNotExistent("sessionTimeout", 36000000); //10 horas  em milissegundos
        configBean.createDefaultConfigIfNotExistent("maxProjectMembers", 4);
    }

    @Transactional
    private void createMethods() throws DatabaseOperationException {
        methodBean.createMethodIfNotExistent(MethodEnum.ADMIN_LEVEL_USERS, "user methods with only admin level access",MethodEnum.ADMIN_LEVEL_USERS.getValue());
        methodBean.createMethodIfNotExistent(MethodEnum.STANDARD_LEVEL_USERS, "user methods with standard user level access",MethodEnum.STANDARD_LEVEL_USERS.getValue());
        methodBean.createMethodIfNotExistent(MethodEnum.ADMIN_LEVEL_SKILLS, "skill methods with only admin level access",MethodEnum.ADMIN_LEVEL_SKILLS.getValue());
        methodBean.createMethodIfNotExistent(MethodEnum.STANDARD_LEVEL_SKILLS, "skill methods with standard user level access",MethodEnum.STANDARD_LEVEL_SKILLS.getValue());
        methodBean.createMethodIfNotExistent(MethodEnum.ADMIN_LEVEL_INTERESTS, "interest methods with only admin level access",MethodEnum.ADMIN_LEVEL_INTERESTS.getValue());
        methodBean.createMethodIfNotExistent(MethodEnum.STANDARD_LEVEL_INTERESTS, "interest methods with standard user level access",MethodEnum.STANDARD_LEVEL_INTERESTS.getValue());
        methodBean.createMethodIfNotExistent(MethodEnum.ADMIN_LEVEL_KEYWORDS, "keyword methods with only admin level access",MethodEnum.ADMIN_LEVEL_KEYWORDS.getValue());
        methodBean.createMethodIfNotExistent(MethodEnum.STANDARD_LEVEL_KEYWORDS, "keyword methods with standard user level access",MethodEnum.STANDARD_LEVEL_KEYWORDS.getValue());
        methodBean.createMethodIfNotExistent(MethodEnum.ADMIN_LEVEL_PROJECTS, "project methods with only admin level access",MethodEnum.ADMIN_LEVEL_PROJECTS.getValue());
        methodBean.createMethodIfNotExistent(MethodEnum.STANDARD_LEVEL_PROJECTS, "project methods with standard user level access",MethodEnum.STANDARD_LEVEL_PROJECTS.getValue());
        methodBean.createMethodIfNotExistent(MethodEnum.ADMIN_LEVEL_ASSETS, "asset methods with only admin level access",MethodEnum.ADMIN_LEVEL_ASSETS.getValue());
        methodBean.createMethodIfNotExistent(MethodEnum.STANDARD_LEVEL_ASSETS, "asset methods with standard user level access",MethodEnum.STANDARD_LEVEL_ASSETS.getValue());
        methodBean.createMethodIfNotExistent(MethodEnum.ADMIN_LEVEL_TASKS, "task methods with only admin level access",MethodEnum.ADMIN_LEVEL_TASKS.getValue());
        methodBean.createMethodIfNotExistent(MethodEnum.STANDARD_LEVEL_TASKS, "task methods with standard user level access",MethodEnum.STANDARD_LEVEL_TASKS.getValue());
        methodBean.createMethodIfNotExistent(MethodEnum.ADMIN_LEVEL_MEMBERSHIPS, "membership methods with only admin level access",MethodEnum.ADMIN_LEVEL_MEMBERSHIPS.getValue());
        methodBean.createMethodIfNotExistent(MethodEnum.STANDARD_LEVEL_MEMBERSHIPS, "membership methods with standard user level access",MethodEnum.STANDARD_LEVEL_MEMBERSHIPS.getValue());methodBean.createMethodIfNotExistent(MethodEnum.ADMIN_LEVEL_USERS, "users methods with only admin level access",MethodEnum.ADMIN_LEVEL_USERS.getValue());
    }

    @Transactional
    private void addPermissions() throws DatabaseOperationException {
        roleBean.addPermission(UserRoleEnum.ADMIN, MethodEnum.ADMIN_LEVEL_USERS);
        roleBean.addPermission(UserRoleEnum.ADMIN, MethodEnum.STANDARD_LEVEL_USERS);
        roleBean.addPermission(UserRoleEnum.STANDARD_USER, MethodEnum.STANDARD_LEVEL_USERS);
        roleBean.addPermission(UserRoleEnum.ADMIN, MethodEnum.ADMIN_LEVEL_SKILLS);
        roleBean.addPermission(UserRoleEnum.ADMIN, MethodEnum.STANDARD_LEVEL_SKILLS);
        roleBean.addPermission(UserRoleEnum.STANDARD_USER, MethodEnum.STANDARD_LEVEL_SKILLS);
        roleBean.addPermission(UserRoleEnum.ADMIN, MethodEnum.ADMIN_LEVEL_INTERESTS);
        roleBean.addPermission(UserRoleEnum.ADMIN, MethodEnum.STANDARD_LEVEL_INTERESTS);
        roleBean.addPermission(UserRoleEnum.STANDARD_USER, MethodEnum.STANDARD_LEVEL_INTERESTS);
        roleBean.addPermission(UserRoleEnum.ADMIN, MethodEnum.ADMIN_LEVEL_KEYWORDS);
        roleBean.addPermission(UserRoleEnum.ADMIN, MethodEnum.STANDARD_LEVEL_KEYWORDS);
        roleBean.addPermission(UserRoleEnum.STANDARD_USER, MethodEnum.STANDARD_LEVEL_KEYWORDS);
        roleBean.addPermission(UserRoleEnum.ADMIN, MethodEnum.ADMIN_LEVEL_PROJECTS);
        roleBean.addPermission(UserRoleEnum.ADMIN, MethodEnum.STANDARD_LEVEL_PROJECTS);
        roleBean.addPermission(UserRoleEnum.STANDARD_USER, MethodEnum.STANDARD_LEVEL_PROJECTS);
        roleBean.addPermission(UserRoleEnum.ADMIN, MethodEnum.ADMIN_LEVEL_ASSETS);
        roleBean.addPermission(UserRoleEnum.ADMIN, MethodEnum.STANDARD_LEVEL_ASSETS);
        roleBean.addPermission(UserRoleEnum.STANDARD_USER, MethodEnum.STANDARD_LEVEL_ASSETS);
        roleBean.addPermission(UserRoleEnum.ADMIN, MethodEnum.ADMIN_LEVEL_TASKS);
        roleBean.addPermission(UserRoleEnum.ADMIN, MethodEnum.STANDARD_LEVEL_TASKS);
        roleBean.addPermission(UserRoleEnum.STANDARD_USER, MethodEnum.STANDARD_LEVEL_TASKS);
        roleBean.addPermission(UserRoleEnum.ADMIN, MethodEnum.ADMIN_LEVEL_MEMBERSHIPS);
        roleBean.addPermission(UserRoleEnum.ADMIN, MethodEnum.STANDARD_LEVEL_MEMBERSHIPS);
        roleBean.addPermission(UserRoleEnum.STANDARD_USER, MethodEnum.STANDARD_LEVEL_MEMBERSHIPS);
    }
}
