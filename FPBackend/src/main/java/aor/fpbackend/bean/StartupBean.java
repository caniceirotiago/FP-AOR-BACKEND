package aor.fpbackend.bean;

import aor.fpbackend.entity.RoleEntity;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

import java.util.List;

//TODO Neste momento esta classe está a interagir de forma direta com a base de dados, o que não é uma boa prática.
//TODO Terá depois de ser feita a converssão para um sistema indireto que utiliza o dao apropriado ou outros beans para o efeito.
//TODO Sugiro que comentários em portugues sejam apenas temporários.

@Singleton
@Startup
public class StartupBean {
    @PersistenceContext(unitName = "PersistenceUnit")
    private EntityManager entityManager;

    @PostConstruct
    public void init() {
        createData();
    }

    private void createData()  {
        //Create roles
        createRoles();

        //Create functions

        //Create an admin

        // Create a test user

        // Create a test project where the test user is a manager

        // Create a test task in the test project

    }
    @Transactional
    private void createRoles() {
        createRoleIfNotExists("Admin");
        createRoleIfNotExists("Standard User");
        createRoleIfNotExists("Unauthenticated User");
    }

    private void createRoleIfNotExists(String roleName) {
        List<RoleEntity> roles = entityManager.createQuery(
                        "SELECT r FROM RoleEntity r WHERE r.name = :roleName", RoleEntity.class)
                .setParameter("roleName", roleName)
                .getResultList();

        if (roles.isEmpty()) {
            RoleEntity role = new RoleEntity(roleName);
            entityManager.persist(role);
        }
    }

}
