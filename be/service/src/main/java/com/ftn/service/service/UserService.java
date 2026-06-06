package com.ftn.service.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.ftn.model.Department;
import com.ftn.model.User;
import com.ftn.model.enums.UserRole;
import com.ftn.service.dto.PagedResponse;
import com.ftn.service.dto.user.UserRequest;
import com.ftn.service.dto.user.UserResponse;
import com.ftn.service.exception.BadRequestException;
import com.ftn.service.exception.DuplicateResourceException;
import com.ftn.service.exception.ResourceNotFoundException;
import com.ftn.service.repository.DepartmentRepository;
import com.ftn.service.repository.UserRepository;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, DepartmentRepository departmentRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public PagedResponse<UserResponse> findAll(Pageable pageable) {
        Page<User> page = userRepository.findAll(pageable);
        List<UserResponse> content = page.getContent().stream()
                .map(UserResponse::new).collect(Collectors.toList());
        return new PagedResponse<>(content, page);
    }

    @Transactional(readOnly = true)
    public UserResponse findById(Long id) {
        return new UserResponse(getOrThrow(id));
    }

    public UserResponse create(UserRequest request) {
        if (!StringUtils.hasText(request.getPassword())) {
            throw new BadRequestException("Password is required when creating a user");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already in use: " + request.getEmail());
        }
        if (userRepository.existsByUserCode(request.getUserCode())) {
            throw new DuplicateResourceException("User code already in use: " + request.getUserCode());
        }

        User u = new User();
        u.setUserCode(request.getUserCode());
        u.setName(request.getName());
        u.setLastName(request.getLastName());
        u.setEmail(request.getEmail());
        u.setRole(request.getRole());
        u.setActive(request.getActive() == null || request.getActive());
        u.setPassword(passwordEncoder.encode(request.getPassword()));
        u.setDepartment(resolveDepartment(request));

        return new UserResponse(userRepository.save(u));
    }

    public UserResponse update(Long id, UserRequest request) {
        User u = getOrThrow(id);

        if (!u.getEmail().equals(request.getEmail()) && userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already in use: " + request.getEmail());
        }
        if (!u.getUserCode().equals(request.getUserCode()) && userRepository.existsByUserCode(request.getUserCode())) {
            throw new DuplicateResourceException("User code already in use: " + request.getUserCode());
        }

        u.setUserCode(request.getUserCode());
        u.setName(request.getName());
        u.setLastName(request.getLastName());
        u.setEmail(request.getEmail());
        u.setRole(request.getRole());
        if (request.getActive() != null) {
            u.setActive(request.getActive());
        }

        u.setDepartment(resolveDepartment(request));

        return new UserResponse(userRepository.save(u));
    }

    public void delete(Long id) {
        User u = getOrThrow(id);
        userRepository.delete(u);
    }

    private Department resolveDepartment(UserRequest request) {
        if (request.getDepartmentId() == null) {
            if (request.getRole() == UserRole.OPERATOR) {
                throw new BadRequestException("An OPERATOR must be assigned to a department");
            }
            return null;
        }
        return departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department", request.getDepartmentId()));
    }

    private User getOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
    }
}
