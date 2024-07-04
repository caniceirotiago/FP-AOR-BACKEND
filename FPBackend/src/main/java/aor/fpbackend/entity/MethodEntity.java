package aor.fpbackend.entity;

import aor.fpbackend.enums.MethodEnum;
import jakarta.persistence.*;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "method")

@NamedQuery(name = "Method.findMethodById", query = "SELECT m FROM MethodEntity m WHERE m.id = :methodId")
@NamedQuery(name = "Method.findMethodByName", query = "SELECT m FROM MethodEntity m WHERE m.name = :name")
@NamedQuery(name = "Method.countMethodByName", query = "SELECT count(m) FROM MethodEntity m WHERE m.name = :name")

public class MethodEntity implements Serializable {
    private static final long serialVersionUID = 1L;
    
    @Id
    @Column(name = "id", updatable = false)
    private long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "name", nullable = false, unique = true)
    private MethodEnum name;

    @Column(name = "description", nullable = false)
    private String description;

    @ManyToMany(mappedBy = "methods")
    private Set<RoleEntity> roles = new HashSet<>();

    // Construtores, getters e setters
    public MethodEntity() {}

    public MethodEntity(MethodEnum name, String description, long id) {
        this.name = name;
        this.description = description;
        this.id = id;
    }

    public MethodEntity(MethodEnum methodEnum) {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public MethodEnum getName() {
        return name;
    }

    public void setName(MethodEnum name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Set<RoleEntity> getRoles() {
        return roles;
    }

    public void setRoles(Set<RoleEntity> roles) {
        this.roles = roles;
    }
}
