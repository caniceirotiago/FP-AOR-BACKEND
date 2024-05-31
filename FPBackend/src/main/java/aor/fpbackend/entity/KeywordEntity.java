package aor.fpbackend.entity;

import aor.fpbackend.enums.IntKeyTypeEnum;
import jakarta.persistence.*;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;


@Entity
@Table(name = "keyword")

@NamedQuery(name = "Keyword.countKeywordByName", query = "SELECT COUNT(k) FROM KeywordEntity k WHERE LOWER(k.name) = LOWER(:name)")
@NamedQuery(name = "Keyword.findKeywordByName", query = "SELECT k FROM KeywordEntity k WHERE LOWER(k.name) = LOWER(:name)")
@NamedQuery(name = "Keyword.findKeywordById", query = "SELECT k FROM KeywordEntity k WHERE k.id = :keyId")

public class KeywordEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false)
    private long id;

    @Column(name = "name", unique = true, nullable = false, length = 30)
    private String name;

    @ManyToMany(mappedBy = "projectKeywords")
    private Set<ProjectEntity> projects = new HashSet<>();

    // Constructors
    public KeywordEntity() {
    }

    public KeywordEntity(String name) {
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



    public Set<ProjectEntity> getProjects() {
        return projects;
    }

    public void setProjects(Set<ProjectEntity> projects) {
        this.projects = projects;
    }
}
