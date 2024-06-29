package aor.fpbackend.dto.Report;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.io.Serializable;
import java.util.List;


@XmlRootElement
public class ReportAssetSummaryDto implements Serializable {

    @XmlElement
    private List<Object[]> topProjectsByUsedQuantity;

    @XmlElement
    private List<Object[]> topAssetsByUsedQuantity;

    public ReportAssetSummaryDto() {
    }

    public ReportAssetSummaryDto(List<Object[]> topProjectsByUsedQuantity, List<Object[]> topAssetsByUsedQuantity) {
        this.topProjectsByUsedQuantity = topProjectsByUsedQuantity;
        this.topAssetsByUsedQuantity = topAssetsByUsedQuantity;
    }

    public List<Object[]> getTopProjectsByUsedQuantity() {
        return topProjectsByUsedQuantity;
    }

    public void setTopProjectsByUsedQuantity(List<Object[]> topProjectsByUsedQuantity) {
        this.topProjectsByUsedQuantity = topProjectsByUsedQuantity;
    }

    public List<Object[]> getTopAssetsByUsedQuantity() {
        return topAssetsByUsedQuantity;
    }

    public void setTopAssetsByUsedQuantity(List<Object[]> topAssetsByUsedQuantity) {
        this.topAssetsByUsedQuantity = topAssetsByUsedQuantity;
    }
}
