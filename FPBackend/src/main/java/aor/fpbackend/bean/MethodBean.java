package aor.fpbackend.bean;

import aor.fpbackend.dao.MethodDao;
import aor.fpbackend.entity.MethodEntity;
import aor.fpbackend.enums.MethodEnum;
import aor.fpbackend.exception.DatabaseOperationException;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Serializable;


@Stateless
public class MethodBean implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LogManager.getLogger(MethodBean.class);
    @EJB
    MethodDao methodDao;


    public void createMethodIfNotExistent(MethodEnum name, String description, long id) throws DatabaseOperationException {
        if (!methodDao.checkMethodExist(name)) {
            MethodEntity methodEntity = new MethodEntity(name, description, id);
            methodDao.persist(methodEntity);
        }
    }

}
