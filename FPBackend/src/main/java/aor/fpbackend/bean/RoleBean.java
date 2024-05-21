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

import java.io.Serializable;


@Stateless
public class RoleBean implements Serializable {
    @EJB
    RoleDao roleDao;

    @EJB
    MethodDao methodDao;
    private static final long serialVersionUID = 1L;


    public void createRoleIfNotExists(UserRoleEnum name) throws DatabaseOperationException {
        if (!roleDao.checkRoleExist(name)) {
            RoleEntity role = new RoleEntity(name);
            roleDao.persist(role);
        }
    }


    public void addPermission(UserRoleEnum roleEnum, MethodEnum methodEnum) throws DatabaseOperationException {
        // Retrieve a RoleEntity and a MethodEntity instance from database
        RoleEntity role = roleDao.findRoleByName(roleEnum);
        MethodEntity method = methodDao.findMethodByName(methodEnum);
        // Add the method to the set of methods for the role
        role.getMethods().add(method);
        // Add the role to the set of roles for the method
        method.getRoles().add(role);
    }

}
