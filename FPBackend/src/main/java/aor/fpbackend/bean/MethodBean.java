package aor.fpbackend.bean;

import aor.fpbackend.dao.MethodDao;
import aor.fpbackend.entity.MethodEntity;
import aor.fpbackend.enums.MethodEnum;
import aor.fpbackend.exception.DatabaseOperationException;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;

import java.io.Serializable;


@Stateless
public class MethodBean implements Serializable {
    private static final long serialVersionUID = 1L;
    @EJB
    MethodDao methodDao;


    public void createMethodIfNotExistent(MethodEnum name, String description) throws DatabaseOperationException {
        if (!methodDao.checkMethodExist(name)) {
            MethodEntity methodEntity = new MethodEntity(name, description);
            methodDao.persist(methodEntity);
        }
    }

}
