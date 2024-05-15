package aor.fpbackend.dto;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;



    @XmlRootElement
    public class LaboratoryDto {

        @XmlElement
        long id;
        @XmlElement
        String location;

        public LaboratoryDto() {
        }


}
