package aor.fpbackend.entity;

import jakarta.persistence.*;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "skill")

@NamedQuery(name = "Skill.countSkillByName", query = "SELECT COUNT(s) FROM SkillEntity s WHERE LOWER(s.name) = LOWER(:name)")
@NamedQuery(name = "Skill.findSkillByName", query = "SELECT s FROM SkillEntity s WHERE LOWER(s.name) = LOWER(:name)")
@NamedQuery(name = "Skill.findSkillById", query = "SELECT s FROM SkillEntity s WHERE s.id = :skillId")

public class SkillEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false)
    private long id;

    @Column(name = "name", unique = true, nullable = false, length = 30)
    private String name;

    @ManyToMany(mappedBy = "userSkills")
    private Set<UserEntity> users = new HashSet<>();

    @ManyToMany(mappedBy = "projectSkills")
    private Set<ProjectEntity> projects = new HashSet<>();

    // Constructors
    public SkillEntity() {
    }

    public SkillEntity(String name) {
        this.name = name;
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

    public Set<UserEntity> getUsers() {
        return users;
    }

    public void setUsers(Set<UserEntity> users) {
        this.users = users;
    }

    public Set<ProjectEntity> getProjects() {
        return projects;
    }

    public void setProjects(Set<ProjectEntity> projects) {
        this.projects = projects;
    }
}
