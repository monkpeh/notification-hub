package com.notifyhub.user;

import jakarta.persistence.*;

@Entity
@Table(name = "app_role")
public class AppRoleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, unique = true, length = 50)
    private String name;

    protected AppRoleEntity() {
        // JPA requires a no-arg constructor
    }

    public AppRoleEntity(String name) {
        this.name = name;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
}
