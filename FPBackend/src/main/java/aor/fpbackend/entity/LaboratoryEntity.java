package aor.fpbackend.entity;

import aor.fpbackend.enums.LocationEnum;
import jakarta.persistence.*;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "laboratory")

@NamedQuery(name = "Laboratory.findLaboratoryById", query = "SELECT l FROM LaboratoryEntity l WHERE l.id = :labId")
@NamedQuery(name = "Laboratory.findAllLaboratories", query = "SELECT l FROM LaboratoryEntity l")
@NamedQuery(name = "Laboratory.countLaboratoryByLocation", query = "SELECT count(l) FROM LaboratoryEntity l WHERE l.location = :location")

public class LaboratoryEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false)
    private long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "location", nullable = false)
    private LocationEnum location;

    @OneToMany(mappedBy = "laboratory")
    private Set<UserEntity> users = new HashSet<>();

    @OneToMany(mappedBy = "laboratory")
    private Set<ProjectEntity> projects = new HashSet<>();

    // Constructors
    public LaboratoryEntity() {
    }

    public LaboratoryEntity(LocationEnum location) {
        this.location = location;
    }

    // Getters and setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public LocationEnum getLocation() {
        return location;
    }

    public void setLocation(LocationEnum location) {
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
