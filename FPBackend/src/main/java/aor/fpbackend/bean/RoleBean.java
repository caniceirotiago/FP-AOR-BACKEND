package aor.fpbackend.bean;

import aor.fpbackend.dao.MethodDao;
import aor.fpbackend.dao.RoleDao;
import aor.fpbackend.entity.MethodEntity;
import aor.fpbackend.entity.RoleEntity;
import aor.fpbackend.enums.MethodEnum;
import aor.fpbackend.enums.UserRoleEnum;
import aor.fpbackend.exception.DatabaseOperationException;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;

import java.io.Serializable;

/**
 * RoleBean is a stateless session bean responsible for managing roles and their associated permissions within the system.
 * <br>
 * This bean handles the creation of roles and the assignment of permissions (methods) to these roles. It interacts with
 * the RoleDao and MethodDao to persist and retrieve role and method entities.
 * <br>
 * <br>
 * Key functionalities provided by this bean include:
 * <ul>
 *     <li>Creating roles if they do not already exist.</li>
 *     <li>Assigning permissions to roles, ensuring that the role-method associations are maintained.</li>
 * </ul>
 * <br>
 * <br>
 *
 * @see UserRoleEnum
 * @see MethodEnum
 * @see RoleDao
 * @see MethodDao
 * @see RoleEntity
 * @see MethodEntity
 */

@Stateless
public class RoleBean implements Serializable {
    @EJB
    RoleDao roleDao;

    @EJB
    MethodDao methodDao;
    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = LogManager.getLogger(RoleBean.class);

    /**
     * Creates a role if it does not already exist.
     *
     * @param name the name of the role to be created.
     */
    public void createRoleIfNotExists(UserRoleEnum name) throws DatabaseOperationException {
        try {
            if (!roleDao.checkRoleExist(name)) {
                RoleEntity role = new RoleEntity(name);
                roleDao.persist(role);
                LOGGER.info("Role created successfully: {}", name);
            } else {
                LOGGER.info("Role already exists: {}", name);
            }
        } catch (Exception e) {
            LOGGER.error("Error creating role: {}", name, e);
            throw new DatabaseOperationException("Error creating role");
        } finally {
            ThreadContext.clearMap();
        }
    }

    /**
     * Adds a permission to a role.
     *
     * @param roleEnum the role to which the permission is to be added.
     * @param methodEnum the permission to be added.
     * @throws DatabaseOperationException if there is an error during the database operation.
     */
    public void addPermission(UserRoleEnum roleEnum, MethodEnum methodEnum) throws DatabaseOperationException {
        try {
            RoleEntity role = roleDao.findRoleByName(roleEnum);
            MethodEntity method = methodDao.findMethodByName(methodEnum);
            if (role == null || method == null) {
                throw new DatabaseOperationException("Role or Method not found");
            }
            role.getMethods().add(method);
            method.getRoles().add(role);
            LOGGER.info("Permission {} added to role {}", methodEnum, roleEnum);
        } catch (Exception e) {
            LOGGER.error("Error adding permission {} to role {}", methodEnum, roleEnum, e);
            throw new DatabaseOperationException("Error adding permission");
        } finally {
            ThreadContext.clearMap();
        }
    }
}
