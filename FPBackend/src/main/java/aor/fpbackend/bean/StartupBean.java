package aor.fpbackend.bean;

import aor.fpbackend.dao.*;

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

        // Create a test project where the test user is a manager

        // Create a test task in the test project

    }

    @Transactional
    private void createRoles() throws DatabaseOperationException {
        roleBean.createRoleIfNotExists("Admin");
        roleBean.createRoleIfNotExists("Standard User");
    }

    @Transactional
    private void createLaboratories() throws DatabaseOperationException {
        labBean.createLaboratoryIfNotExists("Lisboa");
        labBean.createLaboratoryIfNotExists("Coimbra");
        labBean.createLaboratoryIfNotExists("Porto");
        labBean.createLaboratoryIfNotExists("Tomar");
        labBean.createLaboratoryIfNotExists("Viseu");
        labBean.createLaboratoryIfNotExists("Vila Real");
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
        methodBean.createMethodIfNotExistent("updateRole", "updates user role");
    }

}
