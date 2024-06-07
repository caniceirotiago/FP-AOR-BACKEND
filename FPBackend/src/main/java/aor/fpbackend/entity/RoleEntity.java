package aor.fpbackend.entity;

import aor.fpbackend.enums.UserRoleEnum;
import jakarta.persistence.*;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "role")

@NamedQuery(name = "Role.findRoleById", query = "SELECT r FROM RoleEntity r WHERE r.id = :roleId")
@NamedQuery(name = "Role.findRoleByName", query = "SELECT r FROM RoleEntity r WHERE r.name = :name")
@NamedQuery(name = "Role.countRoleByName", query = "SELECT count(r) FROM RoleEntity r WHERE r.name = :name")
@NamedQuery(name = "Role.isMethodAssociatedWithRole", query = "SELECT COUNT(r) FROM RoleEntity r JOIN r.methods m WHERE r.id = :roleId AND m.name = :method")

public class RoleEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "name",nullable = false, unique = true)
    private UserRoleEnum name;

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

    public RoleEntity(UserRoleEnum name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UserRoleEnum getName() {
        return name;
    }

    public void setName(UserRoleEnum name) {
        this.name = name;
    }

    public Set<MethodEntity> getMethods() {
        return methods;
    }

    public void setMethods(Set<MethodEntity> methods) {
        this.methods = methods;
    }

}
