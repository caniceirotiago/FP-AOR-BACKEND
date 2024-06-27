package aor.fpbackend.dto.Report;

import aor.fpbackend.enums.LocationEnum;
import aor.fpbackend.enums.ProjectStateEnum;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.io.Serializable;
import java.time.Instant;


@XmlRootElement
public class ReportProjectsLocationDto implements Serializable {

    @XmlElement
    private LocationEnum labLocation;

    @XmlElement
    private Long projectCount;

    @XmlElement
    private Double projectPercentage;

    // Default constructor
    public ReportProjectsLocationDto() {
    }

    public ReportProjectsLocationDto(LocationEnum labLocation, Long projectCount, Double projectPercentage) {
        this.labLocation = labLocation;
        this.projectCount = projectCount;
        this.projectPercentage = projectPercentage;
    }

    public LocationEnum getLabLocation() {
        return labLocation;
    }

    public void setLabLocation(LocationEnum labLocation) {
        this.labLocation = labLocation;
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
