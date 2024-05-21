package aor.fpbackend.dto;

import aor.fpbackend.enums.LocationEnum;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.io.Serializable;


@XmlRootElement
public class LaboratoryDto implements Serializable {

    @XmlElement
    long id;
    @XmlElement
    LocationEnum location;

    public LaboratoryDto() {
    }

    public LaboratoryDto(long id, LocationEnum location) {
        this.id = id;
        this.location = location;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public LocationEnum getLocation() {
        return location;
    }

    public void setLocation(LocationEnum location) {
        this.location = location;
    }
}
