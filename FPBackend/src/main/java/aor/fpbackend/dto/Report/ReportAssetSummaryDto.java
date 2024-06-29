package aor.fpbackend.dto.Report;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.io.Serializable;
import java.util.List;


@XmlRootElement
public class ReportAssetSummaryDto implements Serializable {

    @XmlElement
    private List<Object[]> usedQuantityByProject;
    @XmlElement
    private List<Object[]> usedQuantityByAssetType;

    public ReportAssetSummaryDto() {
    }

    public ReportAssetSummaryDto(List<Object[]> usedQuantityByProject, List<Object[]> usedQuantityByAssetType) {
        this.usedQuantityByProject = usedQuantityByProject;
        this.usedQuantityByAssetType = usedQuantityByAssetType;
    }

    public List<Object[]> getUsedQuantityByProject() {
        return usedQuantityByProject;
    }

    public void setUsedQuantityByProject(List<Object[]> usedQuantityByProject) {
        this.usedQuantityByProject = usedQuantityByProject;
    }

    public List<Object[]> getUsedQuantityByAssetType() {
        return usedQuantityByAssetType;
    }

    public void setUsedQuantityByAssetType(List<Object[]> usedQuantityByAssetType) {
        this.usedQuantityByAssetType = usedQuantityByAssetType;
    }
}
