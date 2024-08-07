package aor.fpbackend.entity;

import aor.fpbackend.enums.InterestTypeEnum;
import jakarta.persistence.*;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "interest")

@NamedQuery(name = "Interest.countInterestByName", query = "SELECT COUNT(i) FROM InterestEntity i WHERE LOWER(i.name) = LOWER(:name)")
@NamedQuery(name = "Interest.findInterestByName", query = "SELECT i FROM InterestEntity i WHERE LOWER(i.name) = LOWER(:name)")
@NamedQuery(name = "Interest.findInterestById", query = "SELECT i FROM InterestEntity i WHERE i.id = :interestId")

public class InterestEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false)
    private long id;

    @Column(name = "name", unique = true, nullable = false, length = 30)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private InterestTypeEnum type;

    @ManyToMany(mappedBy = "userInterests")
    private Set<UserEntity> users = new HashSet<>();

    // Constructors
    public InterestEntity() {
    }

    public InterestEntity(String name, InterestTypeEnum type) {
        this.name = name;
        this.type = type;
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

    public InterestTypeEnum getType() {
        return type;
    }

    public void setType(InterestTypeEnum type) {
        this.type = type;
    }

    public Set<UserEntity> getUsers() {
        return users;
    }

    public void setUsers(Set<UserEntity> users) {
        this.users = users;
    }

}
