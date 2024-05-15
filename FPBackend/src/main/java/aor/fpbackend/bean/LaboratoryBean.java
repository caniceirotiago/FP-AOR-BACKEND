package aor.fpbackend.bean;

import aor.fpbackend.dao.LaboratoryDao;
import aor.fpbackend.entity.LaboratoryEntity;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;

import java.io.Serializable;


    @Stateless
    public class LaboratoryBean implements Serializable {
        private static final long serialVersionUID = 1L;

        @EJB
        LaboratoryDao laboratoryDao;



        public boolean createLaboratory(String location) {
            //TODO
            // Include existing location verification
            LaboratoryEntity laboratory = new LaboratoryEntity(location);
            laboratoryDao.persist(laboratory);
            return true;
        }

}
