package aor.fpbackend.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "user")

@NamedQuery(name = "User.findUserByToken", query = "SELECT u FROM UserEntity u JOIN u.sessions s WHERE s.sessionToken = :token AND u.isDeleted = false")
@NamedQuery(name = "User.findUserByConfirmationToken", query = "SELECT u FROM UserEntity u WHERE u.confirmationToken = :confirmationToken")
@NamedQuery(name = "User.findUserByResetPasswordToken", query = "SELECT u FROM UserEntity u WHERE u.resetPasswordToken = :resetPasswordToken")
@NamedQuery(name = "User.findAllUsers", query = "SELECT u FROM UserEntity u")
@NamedQuery(name = "User.findUserByUsername", query = "SELECT u FROM UserEntity u WHERE u.username = :username")
@NamedQuery(name = "User.findUserByEmail", query = "SELECT u FROM UserEntity u WHERE u.email = :email")
@NamedQuery(name = "User.countUserByEmail", query = "SELECT COUNT(u) FROM UserEntity u WHERE u.email = :email")
@NamedQuery(name = "User.countUserByUsername", query = "SELECT COUNT(u) FROM UserEntity u WHERE u.username = :username")



public class UserEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false)
    private long id;

    @Column(name = "email", nullable = false, unique = true, updatable = false)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "username", nullable = false, unique = true, updatable = false)
    private String username;

    @Column(name = "first_name", nullable = true)
    private String firstName;

    @Column(name = "last_name", nullable = true)
    private String lastName;

    @Column(name = "photo", nullable = true, length = 2048)
    private String photo;

    @Column(name = "biography", nullable = true, length = 4096)
    private String biography;

    @Column(name = "is_private", nullable = false)
    private boolean isPrivate;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted;

    @Column(name = "is_confirmed", nullable = false)
    private boolean isConfirmed;

    @Column(name = "confirmation_token")
    private String confirmationToken;

    @Column(name="confirmation_token_timestamp", nullable=true, updatable = true)
    private Instant confirmationTokenTimestamp;

    @Column(name="reset_password_token")
    private String resetPasswordToken;
    @Column(name="reset_password_token_timestamp")
    private Instant resetPasswordTimestamp;

    @ManyToOne
    @JoinColumn(name = "laboratory_id", nullable = false)
    private LaboratoryEntity laboratory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private RoleEntity role;

    @OneToMany(mappedBy = "user")
    private Set<ProjectMembershipEntity> projects = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<SessionEntity> sessions = new HashSet<>();

    @OneToMany(mappedBy = "responsibleUser")
    private Set<TaskEntity> responsibleTasks = new HashSet<>();

    @ManyToMany(mappedBy = "additionalExecuters")
    private Set<TaskEntity> tasksAsExecutor = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "user_skill",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "skill_id"))
    private Set<SkillEntity> userSkills = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "user_interest",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "interest_id"))
    private Set<InterestEntity> userInterests = new HashSet<>();

    @OneToMany(mappedBy = "user")
    private Set<NotificationEntity> notifications = new HashSet<>();

    @OneToMany(mappedBy = "sender")
    private Set<MessageEntity> sentMessages = new HashSet<>();

    @OneToMany(mappedBy = "recipient")
    private Set<IndividualMessageEntity> receivedMessages = new HashSet<>();

    @OneToMany(mappedBy = "user")
    private Set<ProjectLogEntity> projectLogs = new HashSet<>();


    public UserEntity() {}

    public UserEntity(String email, String password, String username, String firstName, String lastName, boolean isPrivate, boolean isDeleted, boolean isConfirmed, LaboratoryEntity laboratory, RoleEntity role) {
        this.email = email;
        this.password = password;
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.isPrivate = isPrivate;
        this.isDeleted = isDeleted;
        this.isConfirmed = isConfirmed;
        this.laboratory = laboratory;
        this.role = role;
    }

    // Getters and setters

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getBiography() {
        return biography;
    }

    public void setBiography(String biography) {
        this.biography = biography;
    }

    public boolean isPrivate() {
        return isPrivate;
    }

    public void setPrivate(boolean aPrivate) {
        isPrivate = aPrivate;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean isDeleted) {
        this.isDeleted = isDeleted;
    }

    public boolean isConfirmed() {
        return isConfirmed;
    }

    public void setConfirmed(boolean confirmed) {
        isConfirmed = confirmed;
    }

    public String getConfirmationToken() {
        return confirmationToken;
    }

    public void setConfirmationToken(String confirmationToken) {
        this.confirmationToken = confirmationToken;
    }

    public Instant getConfirmationTokenTimestamp() {
        return confirmationTokenTimestamp;
    }

    public void setConfirmationTokenTimestamp(Instant confirmationTokenTimestamp) {
        this.confirmationTokenTimestamp = confirmationTokenTimestamp;
    }

    public String getResetPasswordToken() {
        return resetPasswordToken;
    }

    public void setResetPasswordToken(String resetPasswordToken) {
        this.resetPasswordToken = resetPasswordToken;
    }

    public Instant getResetPasswordTimestamp() {
        return resetPasswordTimestamp;
    }

    public void setResetPasswordTimestamp(Instant resetPasswordTimestamp) {
        this.resetPasswordTimestamp = resetPasswordTimestamp;
    }

    public Set<ProjectMembershipEntity> getProjects() {
        return projects;
    }

    public void setProjects(Set<ProjectMembershipEntity> projects) {
        this.projects = projects;
    }

    public Set<SessionEntity> getSessions() {
        return sessions;
    }

    public void setSessions(Set<SessionEntity> sessions) {
        this.sessions = sessions;
    }

    public Set<TaskEntity> getResponsibleTasks() {
        return responsibleTasks;
    }

    public void setResponsibleTasks(Set<TaskEntity> responsibleTasks) {
        this.responsibleTasks = responsibleTasks;
    }

    public Set<TaskEntity> getTasksAsExecutor() {
        return tasksAsExecutor;
    }

    public void setTasksAsExecutor(Set<TaskEntity> tasksAsExecutor) {
        this.tasksAsExecutor = tasksAsExecutor;
    }

    public Set<SkillEntity> getUserSkills() {
        return userSkills;
    }

    public void setUserSkills(Set<SkillEntity> userSkills) {
        this.userSkills = userSkills;
    }

    public Set<InterestEntity> getUserInterests() {
        return userInterests;
    }

    public void setUserInterests(Set<InterestEntity> userInterests) {
        this.userInterests = userInterests;
    }

    public LaboratoryEntity getLaboratory() {
        return laboratory;
    }

    public void setLaboratory(LaboratoryEntity laboratory) {
        this.laboratory = laboratory;
    }

    public Set<NotificationEntity> getNotifications() {
        return notifications;
    }

    public void setNotifications(Set<NotificationEntity> notifications) {
        this.notifications = notifications;
    }

    public Set<MessageEntity> getSentMessages() {
        return sentMessages;
    }

    public void setSentMessages(Set<MessageEntity> sentMessages) {
        this.sentMessages = sentMessages;
    }

    public Set<IndividualMessageEntity> getReceivedMessages() {
        return receivedMessages;
    }

    public void setReceivedMessages(Set<IndividualMessageEntity> receivedMessages) {
        this.receivedMessages = receivedMessages;
    }

    public Set<ProjectLogEntity> getProjectLogs() {
        return projectLogs;
    }

    public void setProjectLogs(Set<ProjectLogEntity> projectLogs) {
        this.projectLogs = projectLogs;
    }

    public RoleEntity getRole() {
        return role;
    }

    public void setRole(RoleEntity role) {
        this.role = role;
    }

    @Override
    public String toString() {
        return "UserEntity{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", username='" + username + '\'' +
                ", firstName='" + firstName + '\'' +
                ", photo='" + photo + '\'' +
                ", biography='" + biography + '\'' +
                ", role='" + role + '\'' +
                ", isPrivate=" + isPrivate +
                ", isDeleted=" + isDeleted +
                '}';
    }
}

