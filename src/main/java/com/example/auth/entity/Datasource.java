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
@Table(name = "datasources")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Datasource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, columnDefinition = "NVARCHAR(100)")
    private String name;

    @Column(nullable = false, columnDefinition = "NVARCHAR(20)")
    private String status = "Active";

    @OneToMany(mappedBy = "datasource", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ProjectDatasource> projectDatasources = new HashSet<>();
}
