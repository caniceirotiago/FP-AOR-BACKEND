package aor.fpbackend.entity;

import jakarta.persistence.*;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "role")

@NamedQuery(name = "Role.findRoleById", query = "SELECT r FROM RoleEntity r WHERE r.id = :roleId")
@NamedQuery(name = "Role.countRoleByName", query = "SELECT count(r) FROM RoleEntity r WHERE r.name = :name")

public class RoleEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false)
    private long id;

    @Column(name = "name",nullable = false, unique = true)
    private String name;

    @ManyToMany
    @JoinTable(
            name = "role_method",
            joinColumns = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "method_id")
    )
    private Set<MethodEntity> methods = new HashSet<>();


    // Construtores, getters e setters
    public RoleEntity() {
    }

    public RoleEntity(String name) {
        this.name = name;
    }

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

    public Set<MethodEntity> getMethods() {
        return methods;
    }

    public void setMethods(Set<MethodEntity> methods) {
        this.methods = methods;
    }

    @Override
    public String toString() {
        return "RoleEntity{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", methods=" + methods +
                '}';
    }
}
