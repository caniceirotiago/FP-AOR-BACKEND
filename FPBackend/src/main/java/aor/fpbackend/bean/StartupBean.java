package aor.fpbackend.bean;

import aor.fpbackend.dao.LaboratoryDao;
import aor.fpbackend.dao.RoleDao;
import aor.fpbackend.dao.UserDao;

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


    @PostConstruct
    public void init() {
        createData();
    }

    private void createData() {
        //Create roles
        createRoles();
        //Create laboratories
        createLaboratories();

        // Create functions

        // Create an admin

        createUsers();

        // Create a test user

        // Create a test project where the test user is a manager

        // Create a test task in the test project

        //Create default session timeout

        //Create default max members per project

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


}
