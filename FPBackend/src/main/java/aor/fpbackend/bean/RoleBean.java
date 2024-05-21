package aor.fpbackend.bean;

import aor.fpbackend.dao.RoleDao;
import aor.fpbackend.entity.LaboratoryEntity;
import aor.fpbackend.entity.RoleEntity;
import aor.fpbackend.exception.DatabaseOperationException;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;

import java.io.Serializable;



    @Stateless
    public class RoleBean implements Serializable {
        @EJB
        RoleDao roleDao;
        private static final long serialVersionUID = 1L;


        public void createRoleIfNotExists(String name) throws DatabaseOperationException {
            if (!roleDao.checkRoleExist(name)) {
                RoleEntity role = new RoleEntity(name);
                roleDao.persist(role);
            }
        }

}
