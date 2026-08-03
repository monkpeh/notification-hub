package com.notifyhub.user;

import jakarta.persistence.*;

@Entity
@Table(name = "app_user")
public class AppUserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "pid", nullable = false, unique = true, length = 20)
    private String pid;

    @Column(name = "full_name", length = 150)
    private String fullName;

    @Column(name = "email", length = 150)
    private String email;

    @Column(name = "role_id", nullable = false)
    private Long roleId;

    protected AppUserEntity() {
        // JPA requires a no-arg constructor
    }

    public AppUserEntity(String pid, String fullName, String email, Long roleId) {
        this.pid = pid;
        this.fullName = fullName;
        this.email = email;
        this.roleId = roleId;
    }

    public Long getId() { return id; }
    public String getPid() { return pid; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public Long getRoleId() { return roleId; }
    public void setRoleId(Long roleId) { this.roleId = roleId; }
}
