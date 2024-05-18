package aor.fpbackend.bean;

import aor.fpbackend.dao.*;

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
    RoleDao roleDao;
    @EJB
    LaboratoryDao labDao;
    @EJB
    UserDao userDao;
    @EJB
    ConfigurationDao configDao;


    @PostConstruct
    public void init() {
        createData();
    }

    private void createData() {
        //Create roles
        createRoles();

        //Create laboratories
        createLaboratories();

        // Create an admin
        // Create a test user
        createUsers();

        //Create default session timeout
        createSessionTimeout();

        //Create default max members per project

        // Create functions

        // Create a test project where the test user is a manager

        // Create a test task in the test project



    }

    @Transactional
    private void createRoles() {
        roleDao.createRoleIfNotExists("Admin");
        roleDao.createRoleIfNotExists("Standard User");
    }

    @Transactional
    private void createLaboratories() {
        labDao.createLaboratoryIfNotExists("Lisboa");
        labDao.createLaboratoryIfNotExists("Coimbra");
        labDao.createLaboratoryIfNotExists("Porto");
        labDao.createLaboratoryIfNotExists("Tomar");
        labDao.createLaboratoryIfNotExists("Viseu");
        labDao.createLaboratoryIfNotExists("Vila Real");
    }


    @Transactional
    private void createUsers() {
        userDao.createDefaultUserIfNotExistent("admin", 1);
        userDao.createDefaultUserIfNotExistent("standardUser", 2);
    }

    @Transactional
    private void createSessionTimeout() {
        configDao.createDefaultSessionTimeoutIfNotExistent("sessionTimeout", 900);
    }


}
