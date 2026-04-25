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
@Table(name = "projects")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, columnDefinition = "NVARCHAR(100)")
    private String name;

    @Column(columnDefinition = "NVARCHAR(255)")
    private String path;

    @Column(name = "database_table_name", columnDefinition = "NVARCHAR(100)")
    private String databaseTableName;

    @Column(nullable = false, columnDefinition = "NVARCHAR(20)")
    private String status = "Active";

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<UserProject> userProjects = new HashSet<>();

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ProjectDatasource> projectDatasources = new HashSet<>();
}
