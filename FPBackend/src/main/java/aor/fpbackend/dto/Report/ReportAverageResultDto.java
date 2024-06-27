package aor.fpbackend.dto.Report;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.io.Serializable;


@XmlRootElement
public class ReportAverageResultDto implements Serializable {

    @XmlElement
    private double average;

    public ReportAverageResultDto() {
    }

    public ReportAverageResultDto(double average) {
        this.average = average;
    }

    public double getAverage() {
        return average;
    }

    public void setAverage(double average) {
        this.average = average;
    }
}
