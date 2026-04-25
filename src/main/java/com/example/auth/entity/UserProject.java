package com.example.auth.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;

@Entity
@Table(name = "user_project")
@Data
@NoArgsConstructor
public class UserProject {

    @EmbeddedId
    private UserProjectId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @MapsId("projectId")
    @JoinColumn(name = "project_id")
    private Project project;

    public UserProject(User user, Project project) {
        this.id = new UserProjectId(user.getId(), project.getId());
        this.user = user;
        this.project = project;
    }
}
