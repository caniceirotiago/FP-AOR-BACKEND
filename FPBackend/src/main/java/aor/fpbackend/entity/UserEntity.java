package aor.fpbackend.entity;

import jakarta.persistence.*;

import java.io.Serializable;
import java.time.Instant;
import java.util.Set;

@Entity
@Table(name="user")
@NamedQuery(name = "User.checkIfEmailExists", query = "SELECT COUNT(u) FROM UserEntity u WHERE u.email = :email")
@NamedQuery(name = "User.checkIfUsernameExists", query = "SELECT COUNT(u) FROM UserEntity u WHERE u.username = :username")
@NamedQuery(name = "User.getNumberOfConfirmedUsersByMonth", query = "SELECT COALESCE(EXTRACT(YEAR FROM u.confirmationTimestamp), 0) AS yearAlias, COALESCE(EXTRACT(MONTH FROM u.confirmationTimestamp), 0) AS monthAlias, COUNT(u) FROM UserEntity u WHERE u.isConfirmed = true AND u.confirmationTimestamp IS NOT NULL GROUP BY yearAlias, monthAlias ORDER BY yearAlias, monthAlias")

@NamedQuery(name = "User.findByResetPasswordToken", query = "SELECT u FROM UserEntity u WHERE u.resetPasswordToken = :resetPasswordToken")
@NamedQuery(name = "User.findUserByConfirmationToken", query = "SELECT u FROM UserEntity u WHERE u.confirmationToken = :confirmationToken")
@NamedQuery(name = "User.getConfirmedUsers", query = "SELECT u FROM UserEntity u WHERE u.isConfirmed = true")
@NamedQuery(name = "User.getUnconfirmedUsers", query = "SELECT u FROM UserEntity u WHERE u.isConfirmed = false")
@NamedQuery(name = "User.findUserByUsername", query = "SELECT u FROM UserEntity u " +
        "WHERE u.username = :username")
@NamedQuery(name = "User.findUserByEmail", query = "SELECT u FROM UserEntity u " +
        "WHERE u.email = :email")
@NamedQuery(name = "User.findUserByToken", query = "SELECT DISTINCT u FROM UserEntity u " +
        "WHERE u.token = :token")
@NamedQuery(name = "User.findAllUsers", query = "SELECT u FROM UserEntity u")
@NamedQuery(name= "User.deleteUserById", query="DELETE FROM UserEntity t " +
        "WHERE t.username = :username")
@NamedNativeQuery(name = "User.findUsersWithNonDeletedTasks",
        query = "SELECT DISTINCT u.* FROM tiago_ds2.user u " +
                "JOIN tiago_ds2.task t ON u.username = t.user_username " +
                "WHERE t.deleted = FALSE",
        resultClass = UserEntity.class
)

public class UserEntity implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @Column(name="username", nullable=false, unique = true, updatable = false)
    private String username;
    @Column(name="password", nullable=false, unique = false, updatable = true)
    private String password;
    @Column(name="email", nullable=false, unique = true, updatable = false)
    private String email;
    @Column(name="firstname", nullable=false, unique = false, updatable = true)
    private String firstName;
    @Column(name="lastname", nullable=false, unique = false, updatable = true)
    private String lastName;
    @Column(name="phonenumber", nullable=false, unique = false, updatable = true)
    private String phoneNumber;
    @Column(name="photourl", nullable=false, unique = false, updatable = true, length = 2048)
    private String photoURL;
    @Column(name="token", nullable=true, unique = true, updatable = true)
    private String token;
    @Column(name="role", nullable=true, unique = false, updatable = true)
    private String role;
    @Column(name="deleted", nullable = false,unique = false,updatable = true)
    private boolean deleted;
    @Column(name="last_activity_timestamp", nullable=true, updatable = true)
    private Instant lastActivityTimestamp;
    @Column(name="last_sent_email_timestamp", nullable=true, updatable = true)
    private Instant lastSentEmailTimestamp;
    @Column(name = "is_confirmed")
    private boolean isConfirmed = false;
    @Column(name = "confirmation_token")
    private String confirmationToken;
    @Column(name="confirmation_timestamp", nullable=true, updatable = true)
    private Instant confirmationTimestamp;
    @Column(name="reset_password_token")
    private String resetPasswordToken;
    @Column(name="reset_password_token_expiry")
    private Instant resetPasswordTokenExpiry;
    public UserEntity() {}
    public UserEntity(String username, String password, String email, String firstName, String lastName,
                      String phoneNumber, String photoURL, String token, String role, boolean deleted
            , boolean isConfirmed, String confirmationToken) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
        this.photoURL = photoURL;
        this.token = token;
        this.role = role;
        this.deleted = deleted;
        this.isConfirmed = isConfirmed;
        this.confirmationToken = confirmationToken;
    }

    public Instant getConfirmationTimestamp() {
        return confirmationTimestamp;
    }

    public void setConfirmationTimestamp(Instant confirmationTimestamp) {
        this.confirmationTimestamp = confirmationTimestamp;
    }

    public String getResetPasswordToken() {
        return resetPasswordToken;
    }

    public void setResetPasswordToken(String resetPasswordToken) {
        this.resetPasswordToken = resetPasswordToken;
    }

    public Instant getResetPasswordTokenExpiry() {
        return resetPasswordTokenExpiry;
    }

    public void setResetPasswordTokenExpiry(Instant resetPasswordTokenExpiry) {
        this.resetPasswordTokenExpiry = resetPasswordTokenExpiry;
    }

    public boolean isConfirmed() {
        return isConfirmed;
    }

    public String getConfirmationToken() {
        return confirmationToken;
    }

    public void setConfirmed(boolean confirmed) {
        isConfirmed = confirmed;
    }

    public void setConfirmationToken(String confirmationToken) {
        this.confirmationToken = confirmationToken;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setPhotoURL(String photoURL) {
        this.photoURL = photoURL;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getPhotoURL() {
        return photoURL;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }


    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
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

    public boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public Instant getLastActivityTimestamp() {
        return lastActivityTimestamp;
    }

    public void setLastActivityTimestamp(Instant lastActivityTimestamp) {
        this.lastActivityTimestamp = lastActivityTimestamp;
    }

    public Instant getLastSentEmailTimestamp() {
        return lastSentEmailTimestamp;
    }

    public void setLastSentEmailTimestamp(Instant lastSentEmailTimestamp) {
        this.lastSentEmailTimestamp = lastSentEmailTimestamp;
    }

    @Override
    public String toString() {
        return "UserEntity{" +
                "username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", email='" + email + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", photoURL='" + photoURL + '\'' +
//                ", token='" + token + '\'' +
                '}';
    }


}
