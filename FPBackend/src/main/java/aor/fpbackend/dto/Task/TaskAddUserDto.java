package aor.fpbackend.dto.Task;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.io.Serializable;

@XmlRootElement
public class TaskAddUserDto implements Serializable {


    @XmlElement
    @NotNull
    @Min(value = 1, message = "ID must be greater than 0")
    private long taskId;

    @XmlElement
    private long executorId;

    @XmlElement
    private String nonRegisteredExecutors;

    public TaskAddUserDto() {
    }

    public TaskAddUserDto(long taskId, long executorId, String nonRegisteredExecutors) {
        this.taskId = taskId;
        this.executorId = executorId;
        this.nonRegisteredExecutors = nonRegisteredExecutors;
    }

    public long getTaskId() {
        return taskId;
    }

    public void setTaskId(long taskId) {
        this.taskId = taskId;
    }

    public long getExecutorId() {
        return executorId;
    }

    public void setExecutorId(long executorId) {
        this.executorId = executorId;
    }

    public String getNonRegisteredExecutors() {
        return nonRegisteredExecutors;
    }

    public void setNonRegisteredExecutors(String nonRegisteredExecutors) {
        this.nonRegisteredExecutors = nonRegisteredExecutors;
    }
}