package aor.fpbackend.dto.Report;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.io.Serializable;


@XmlRootElement
public class ReportAverageMembersDto implements Serializable {

    @XmlElement
    private double averageMembers;

    public ReportAverageMembersDto() {
    }

    public ReportAverageMembersDto(double averageMembers) {
        this.averageMembers = averageMembers;
    }

    public double getAverageMembers() {
        return averageMembers;
    }

    public void setAverageMembers(double averageMembers) {
        this.averageMembers = averageMembers;
    }
}
