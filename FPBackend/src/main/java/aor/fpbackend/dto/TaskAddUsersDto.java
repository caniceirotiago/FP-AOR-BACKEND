package aor.fpbackend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.io.Serializable;
import java.time.Instant;
import java.util.Set;

@XmlRootElement
public class TaskAddUsersDto implements Serializable {


    @XmlElement
    @NotNull
    private long taskId;

    @XmlElement
    private Set<UsernameDto> registeredExecutors;

    @XmlElement
    private String nonRegisteredExecutors;

    public TaskAddUsersDto() {
    }

    public TaskAddUsersDto(long taskId, Set<UsernameDto> registeredExecutors, String nonRegisteredExecutors) {
        this.taskId = taskId;
        this.registeredExecutors = registeredExecutors;
        this.nonRegisteredExecutors = nonRegisteredExecutors;
    }

    public long getTaskId() {
        return taskId;
    }

    public void setTaskId(long taskId) {
        this.taskId = taskId;
    }

    public Set<UsernameDto> getRegisteredExecutors() {
        return registeredExecutors;
    }

    public void setRegisteredExecutors(Set<UsernameDto> registeredExecutors) {
        this.registeredExecutors = registeredExecutors;
    }

    public String getNonRegisteredExecutors() {
        return nonRegisteredExecutors;
    }

    public void setNonRegisteredExecutors(String nonRegisteredExecutors) {
        this.nonRegisteredExecutors = nonRegisteredExecutors;
    }
}