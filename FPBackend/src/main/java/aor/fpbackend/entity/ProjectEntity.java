package aor.fpbackend.entity;

import jakarta.persistence.*;
import aor.fpbackend.enums.ProjectStateEnum;

import java.io.Serializable;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "project")

@NamedQuery(name = "Project.findProjectById", query = "SELECT p FROM ProjectEntity p WHERE p.id = :projectId")
@NamedQuery(name = "Project.findProjectByName", query = "SELECT p FROM ProjectEntity p WHERE LOWER(p.name) = LOWER(:name)")
@NamedQuery(name = "Project.findAllProjects", query = "SELECT p FROM ProjectEntity p")

public class ProjectEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false)
    private long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", nullable = true, length = 2048)
    private String description;

    @Column(name = "motivation", nullable = true, length = 2048)
    private String motivation;

    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false)
    private ProjectStateEnum state;

    @Column(name = "creation_date", nullable = false)
    private Instant creationDate;

    @Column(name = "initial_date", nullable = true)
    private Instant initialDate;

    @Column(name = "final_date", nullable = true)
    private Instant finalDate;

    @Column(name = "conclusion_date", nullable = true)
    private Instant conclusionDate;

    @ManyToOne
    @JoinColumn(name = "laboratory_id")
    private LaboratoryEntity laboratory;

    @OneToMany(mappedBy = "project")
    private Set<TaskEntity> tasks = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "project_skill",
            joinColumns = @JoinColumn(name = "project_id"),
            inverseJoinColumns = @JoinColumn(name = "skill_id")
    )
    private Set<SkillEntity> projectSkills = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "project_keyword",
            joinColumns = @JoinColumn(name = "project_id"),
            inverseJoinColumns = @JoinColumn(name = "keyword_id")
    )
    private Set<KeywordEntity> projectKeywords = new HashSet<>();

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ProjectAssetEntity> projectAssetsForProject = new HashSet<>();

    @OneToMany(mappedBy = "project")
    private Set<ProjectMembershipEntity> members = new HashSet<>();

    @OneToMany(mappedBy = "group")
    private Set<GroupMessageEntity> groupMessages = new HashSet<>();

    @OneToMany(mappedBy = "project")
    private Set<ProjectLogEntity> projectLogs = new HashSet<>();

    public ProjectEntity() {
    }

    public ProjectEntity(String name, String description, String motivation, ProjectStateEnum state, Instant creationDate, Instant initialDate, Instant finalDate, Instant conclusionDate) {
        this.name = name;
        this.description = description;
        this.motivation = motivation;
        this.state = state;
        this.creationDate = creationDate;
        this.initialDate = initialDate;
        this.finalDate = finalDate;
        this.conclusionDate = conclusionDate;
    }

    // Getters and setters


    public long getId() {
        return id;
    }

    public void setId(long id) {
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

    public ProjectStateEnum getState() {
        return state;
    }

    public void setState(ProjectStateEnum state) {
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

    public LaboratoryEntity getLaboratory() {
        return laboratory;
    }

    public void setLaboratory(LaboratoryEntity laboratory) {
        this.laboratory = laboratory;
    }

    public Set<TaskEntity> getTasks() {
        return tasks;
    }

    public void setTasks(Set<TaskEntity> tasks) {
        this.tasks = tasks;
    }

    public Set<SkillEntity> getProjectSkills() {
        return projectSkills;
    }

    public void setProjectSkills(Set<SkillEntity> projectSkills) {
        this.projectSkills = projectSkills;
    }

    public Set<KeywordEntity> getProjectKeywords() {
        return projectKeywords;
    }

    public void setProjectKeywords(Set<KeywordEntity> projectKeywords) {
        this.projectKeywords = projectKeywords;
    }

    public Set<ProjectAssetEntity> getProjectAssetsForProject() {
        return projectAssetsForProject;
    }

    public void setProjectAssetsForProject(Set<ProjectAssetEntity> projectAssets) {
        this.projectAssetsForProject = projectAssets;
    }

    public Set<ProjectMembershipEntity> getMembers() {
        return members;
    }

    public void setMembers(Set<ProjectMembershipEntity> members) {
        this.members = members;
    }

    public Set<GroupMessageEntity> getGroupMessages() {
        return groupMessages;
    }

    public void setGroupMessages(Set<GroupMessageEntity> groupMessages) {
        this.groupMessages = groupMessages;
    }

    public Set<ProjectLogEntity> getProjectLogs() {
        return projectLogs;
    }

    public void setProjectLogs(Set<ProjectLogEntity> projectLogs) {
        this.projectLogs = projectLogs;
    }

    @Override
    public String toString() {
        return "ProjectEntity{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", motivation='" + motivation + '\'' +
                ", state=" + state +
                ", creationDate=" + creationDate +
                ", initialDate=" + initialDate +
                ", finalDate=" + finalDate +
                ", conclusionDate=" + conclusionDate +
                ", laboratory=" + laboratory +
                '}';
    }
}
