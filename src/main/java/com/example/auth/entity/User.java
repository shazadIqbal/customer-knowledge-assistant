package com.example.auth.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(min = 3, max = 50)
    @Column(nullable = false, unique = true, columnDefinition = "NVARCHAR(50)")
    private String username;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, columnDefinition = "NVARCHAR(100)")
    private String password;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @Column(columnDefinition = "NVARCHAR(100)")
    private String fullname;

    @Column(name = "job_title", columnDefinition = "NVARCHAR(100)")
    private String jobTitle;

    @Column(columnDefinition = "NVARCHAR(150)")
    private String email;

    @Column(columnDefinition = "NVARCHAR(20)")
    private String status = "Active";

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<UserRole> userRoles = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<UserProject> userProjects = new HashSet<>();

    public User(String username, String password, Role role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }
}
