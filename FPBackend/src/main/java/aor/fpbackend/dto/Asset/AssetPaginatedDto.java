package aor.fpbackend.dto.Asset;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.io.Serializable;
import java.util.List;

@XmlRootElement
public class AssetPaginatedDto implements Serializable {
    @XmlElement
    private List<AssetGetDto> assetsForPage;
    @XmlElement
    private long totalAssets;


    public AssetPaginatedDto() {
    }

    public AssetPaginatedDto(List<AssetGetDto> assetsForPage, long totalAssets) {
        this.assetsForPage = assetsForPage;
        this.totalAssets = totalAssets;
    }

    public List<AssetGetDto> getAssetsForPage() {
        return assetsForPage;
    }

    public void setAssetsForPage(List<AssetGetDto> assetsForPage) {
        this.assetsForPage = assetsForPage;
    }

    public long getTotalAssets() {
        return totalAssets;
    }

    public void setTotalAssets(long totalAssets) {
        this.totalAssets = totalAssets;
    }
}
