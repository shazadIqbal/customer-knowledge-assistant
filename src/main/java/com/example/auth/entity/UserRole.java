package com.example.auth.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "user_role")
@Data
@NoArgsConstructor
public class UserRole {

    @EmbeddedId
    private UserRoleId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @MapsId("roleId")
    @JoinColumn(name = "role_id")
    private Role role;

    public UserRole(User user, Role role) {
        this.id = new UserRoleId(user.getId(), role.getId());
        this.user = user;
        this.role = role;
    }
}
