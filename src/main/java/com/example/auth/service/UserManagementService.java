package com.example.auth.service;

import com.example.auth.dto.CreateUserRequest;
import com.example.auth.dto.PagedResponse;
import com.example.auth.dto.UpdateUserRequest;
import com.example.auth.dto.UserDetailResponse;
import com.example.auth.entity.*;
import com.example.auth.exception.ResourceNotFoundException;
import com.example.auth.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserManagementService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private UserProjectRepository userProjectRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional
    public UserDetailResponse createUser(CreateUserRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username '" + request.getUsername() + "' is already taken");
        }

        Role role = roleRepository.findById(request.getRoleId())
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", request.getRoleId()));

        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", request.getProjectId()));

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFullname(request.getFullname());
        user.setJobTitle(request.getJobTitle());
        user.setEmail(request.getEmail());
        user.setStatus(request.getStatus() != null ? request.getStatus() : "Active");
        user.setRole(role);

        User savedUser = userRepository.save(user);

        UserRole userRole = new UserRole(savedUser, role);
        userRoleRepository.save(userRole);

        UserProject userProject = new UserProject(savedUser, project);
        userProjectRepository.save(userProject);

        return toDetailResponse(savedUser);
    }

    public PagedResponse<UserDetailResponse> getAllUsers(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<User> userPage = userRepository.findAll(pageable);

        Page<UserDetailResponse> responsePage = userPage.map(this::toDetailResponse);
        return PagedResponse.of(responsePage);
    }

    public UserDetailResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        return toDetailResponse(user);
    }

    @Transactional
    public UserDetailResponse updateUser(Long id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        if (request.getFullname() != null) {
            user.setFullname(request.getFullname());
        }
        if (request.getJobTitle() != null) {
            user.setJobTitle(request.getJobTitle());
        }
        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }
        if (request.getStatus() != null) {
            user.setStatus(request.getStatus());
        }

        if (request.getRoleId() != null) {
            Role role = roleRepository.findById(request.getRoleId())
                    .orElseThrow(() -> new ResourceNotFoundException("Role", "id", request.getRoleId()));
            user.setRole(role);

            userRoleRepository.deleteByIdUserId(id);
            userRoleRepository.save(new UserRole(user, role));
        }

        if (request.getProjectId() != null) {
            Project project = projectRepository.findById(request.getProjectId())
                    .orElseThrow(() -> new ResourceNotFoundException("Project", "id", request.getProjectId()));

            userProjectRepository.deleteByIdUserId(id);
            userProjectRepository.save(new UserProject(user, project));
        }

        User updatedUser = userRepository.save(user);
        return toDetailResponse(updatedUser);
    }

    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User", "id", id);
        }
        userRoleRepository.deleteByIdUserId(id);
        userProjectRepository.deleteByIdUserId(id);
        userRepository.deleteById(id);
    }

    private UserDetailResponse toDetailResponse(User user) {
        UserDetailResponse response = new UserDetailResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setFullname(user.getFullname());
        response.setJobTitle(user.getJobTitle());
        response.setEmail(user.getEmail());
        response.setStatus(user.getStatus());
        response.setRole(user.getRole() != null ? user.getRole().getName() : null);

        List<UserDetailResponse.ProjectSummary> projects = userProjectRepository
                .findByIdUserId(user.getId())
                .stream()
                .map(up -> {
                    UserDetailResponse.ProjectSummary summary = new UserDetailResponse.ProjectSummary();
                    summary.setId(up.getProject().getId());
                    summary.setName(up.getProject().getName());
                    summary.setStatus(up.getProject().getStatus());
                    return summary;
                })
                .collect(Collectors.toList());

        response.setProjects(projects);
        return response;
    }
}
