package aor.fpbackend.dto.Laboratory;

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
    @XmlElement
    String locationName;

    public LaboratoryDto() {
    }

    public LaboratoryDto(long id, LocationEnum location, String locationName) {
        this.id = id;
        this.location = location;
        this.locationName = locationName;
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

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }
}
