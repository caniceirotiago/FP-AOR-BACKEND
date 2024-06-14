package aor.fpbackend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.io.Serializable;

@XmlRootElement
public class AssetRemoveDto implements Serializable {

    @XmlElement
    @NotNull
    @Min(value = 1, message = "ID must be greater than 0")
    private long assetId;

    public AssetRemoveDto() {
    }

    public AssetRemoveDto(long assetId) {
        this.assetId = assetId;
    }

    public long getAssetId() {
        return assetId;
    }

    public void setAssetId(long assetId) {
        this.assetId = assetId;
    }


}
