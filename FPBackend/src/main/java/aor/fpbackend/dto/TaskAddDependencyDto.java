package aor.fpbackend.dto;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.io.Serializable;

@XmlRootElement
public class TaskAddDependencyDto implements Serializable {

    @XmlElement
    private long mainTaskId;

    @XmlElement
    private long dependentTaskId;


    public TaskAddDependencyDto() {
    }

    public TaskAddDependencyDto(long mainTaskId, long dependentTaskId) {
        this.mainTaskId = mainTaskId;
        this.dependentTaskId = dependentTaskId;
    }

    public long getMainTaskId() {
        return mainTaskId;
    }

    public void setMainTaskId(long mainTaskId) {
        this.mainTaskId = mainTaskId;
    }

    public long getDependentTaskId() {
        return dependentTaskId;
    }

    public void setDependentTaskId(long dependentTaskId) {
        this.dependentTaskId = dependentTaskId;
    }
}