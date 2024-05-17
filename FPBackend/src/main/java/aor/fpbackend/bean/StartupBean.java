package aor.fpbackend.bean;

import aor.fpbackend.dao.LaboratoryDao;
import aor.fpbackend.dao.RoleDao;
import aor.fpbackend.entity.LaboratoryEntity;
import aor.fpbackend.entity.RoleEntity;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

import java.io.Serializable;
import java.util.List;

@Singleton
@Startup
public class StartupBean implements Serializable {
    private static final long serialVersionUID = 1L;

    @EJB
    LaboratoryDao labDao;

    @EJB
    RoleDao roleDao;

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

}
