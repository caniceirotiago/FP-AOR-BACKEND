package aor.fpbackend.entity;

import jakarta.persistence.*;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "laboratory")

@NamedQuery(name = "Laboratory.findLaboratoryById", query = "SELECT l FROM LaboratoryEntity l WHERE l.id = :labId")

public class LaboratoryEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private long id;

    @Column(name = "location", nullable = false)
    private String location;

    @OneToMany(mappedBy = "laboratory")
    private Set<UserEntity> users = new HashSet<>();

    @OneToMany(mappedBy = "laboratory")
    private Set<ProjectEntity> projects = new HashSet<>();

    // Constructors
    public LaboratoryEntity() {
    }

    public LaboratoryEntity(String location) {
        this.location = location;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
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
