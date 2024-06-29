package aor.fpbackend.bean;

import aor.fpbackend.dao.MethodDao;
import aor.fpbackend.entity.MethodEntity;
import aor.fpbackend.enums.MethodEnum;
import aor.fpbackend.exception.DatabaseOperationException;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.transaction.Transactional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Serializable;


@Stateless
public class MethodBean implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LogManager.getLogger(MethodBean.class);

    @EJB
    private MethodDao methodDao;

    /**
     * Creates a new method entity if it does not already exist.
     *
     * @param name        The name of the method.
     * @param description The description of the method.
     * @param id          The ID of the method.
     * @throws DatabaseOperationException If an error occurs during the database operation.
     */
    @Transactional
    public void createMethodIfNotExistent(MethodEnum name, String description, long id) throws DatabaseOperationException {
        try {
            if (!methodDao.checkMethodExist(name)) {
                MethodEntity methodEntity = new MethodEntity(name, description, id);
                methodDao.persist(methodEntity);
                LOGGER.info("Method {} created successfully.", name);
            } else {
                LOGGER.warn("Method {} already exists, creation skipped.", name);
            }
        } catch (Exception e) {
            LOGGER.error("Error creating method {}: {}", name, e.getMessage());
            throw new DatabaseOperationException("Error creating method: " + name);
        }
    }
}
