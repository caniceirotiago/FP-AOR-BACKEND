package aor.fpbackend.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "project")
public class ProjectEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", nullable = true, length = 2048)
    private String description;

    @Column(name = "motivation", nullable = true, length = 2048)
    private String motivation;

    @Column(name = "state", nullable = false)
    private String state;

    @Column(name = "creation_date", nullable = false)
    private Instant creationDate;

    @Column(name = "initial_date", nullable = true)
    private Instant initialDate;

    @Column(name = "final_date", nullable = true)
    private Instant finalDate;

    @Column(name = "conclusion_date", nullable = true)
    private Instant conclusionDate;

    @Column(name = "max_members")
    private Integer maxMembers;
    @OneToMany(mappedBy = "project")
    private Set<ProjectMembershipEntity> members = new HashSet<>();
    @OneToMany(mappedBy = "project")
    private Set<TaskEntity> tasks = new HashSet<>();

    public ProjectEntity() {}

    // Getters and setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getMotivation() {
        return motivation;
    }

    public void setMotivation(String motivation) {
        this.motivation = motivation;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Instant getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Instant creationDate) {
        this.creationDate = creationDate;
    }

    public Instant getInitialDate() {
        return initialDate;
    }

    public void setInitialDate(Instant initialDate) {
        this.initialDate = initialDate;
    }

    public Instant getFinalDate() {
        return finalDate;
    }

    public void setFinalDate(Instant finalDate) {
        this.finalDate = finalDate;
    }

    public Instant getConclusionDate() {
        return conclusionDate;
    }

    public void setConclusionDate(Instant conclusionDate) {
        this.conclusionDate = conclusionDate;
    }

    public Integer getMaxMembers() {
        return maxMembers;
    }

    public void setMaxMembers(Integer maxMembers) {
        this.maxMembers = maxMembers;
    }

    public Set<ProjectMembershipEntity> getMembers() {
        return members;
    }

    public void setMembers(Set<ProjectMembershipEntity> members) {
        this.members = members;
    }

    public Set<TaskEntity> getTasks() {
        return tasks;
    }

    public void setTasks(Set<TaskEntity> tasks) {
        this.tasks = tasks;
    }

    @Override
    public String toString() {
        return "ProjectEntity{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", motivation='" + motivation + '\'' +
                ", state='" + state + '\'' +
                ", creationDate=" + creationDate +
                ", initialDate=" + initialDate +
                ", finalDate=" + finalDate +
                ", conclusionDate=" + conclusionDate +
                ", maxMembers=" + maxMembers +
                '}';
    }
}
