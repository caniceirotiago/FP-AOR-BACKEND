package aor.fpbackend.dto.Report;

import aor.fpbackend.enums.LocationEnum;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.io.Serializable;


@XmlRootElement
public class ReportProjectsLocationDto implements Serializable {

    @XmlElement
    private LocationEnum location;

    @XmlElement
    private Long projectCount;

    @XmlElement
    private Double projectPercentage;

    // Default constructor
    public ReportProjectsLocationDto() {
    }

    public ReportProjectsLocationDto(LocationEnum location, Long projectCount, Double projectPercentage) {
        this.location = location;
        this.projectCount = projectCount;
        this.projectPercentage = projectPercentage;
    }

    public LocationEnum getLocation() {
        return location;
    }

    public void setLocation(LocationEnum location) {
        this.location = location;
    }

    public Long getProjectCount() {
        return projectCount;
    }

    public void setProjectCount(Long projectCount) {
        this.projectCount = projectCount;
    }

    public Double getProjectPercentage() {
        return projectPercentage;
    }

    public void setProjectPercentage(Double projectPercentage) {
        this.projectPercentage = projectPercentage;
    }
}
