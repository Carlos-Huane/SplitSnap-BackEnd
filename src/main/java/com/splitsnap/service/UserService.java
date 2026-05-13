package com.splitsnap.service;

import com.splitsnap.dto.user.UpdateProfileRequest;
import com.splitsnap.dto.user.UserResponse;
import com.splitsnap.exception.BusinessException;
import com.splitsnap.exception.EntityNotFoundException;
import com.splitsnap.model.User;
import com.splitsnap.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private static final String UPLOAD_DIR = "uploads/avatars/";
    private static final long MAX_SIZE = 2 * 1024 * 1024; // 2MB

    public UserResponse getProfile(UUID userId) {
        return UserResponse.from(findById(userId));
    }

    public UserResponse updateProfile(UUID userId, UpdateProfileRequest request) {
        User user = findById(userId);

        if (request.getName() != null) {
            user.setName(request.getName());
        }
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new BusinessException("El email ya está en uso");
            }
            user.setEmail(request.getEmail().toLowerCase());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        if (request.getNewPassword() != null) {
            if (request.getCurrentPassword() == null ||
                !passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
                throw new BusinessException("La contraseña actual es incorrecta");
            }
            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        }

        return UserResponse.from(userRepository.save(user));
    }

    public String uploadAvatar(UUID userId, MultipartFile file) throws IOException {
        if (file.getSize() > MAX_SIZE) {
            throw new BusinessException("El archivo supera el tamaño máximo de 2MB");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BusinessException("Solo se permiten archivos de imagen (jpg, png, webp)");
        }

        Path uploadPath = Paths.get(UPLOAD_DIR);
        Files.createDirectories(uploadPath);

        String filename = userId + "_" + System.currentTimeMillis() +
                          getExtension(file.getOriginalFilename());
        Files.copy(file.getInputStream(), uploadPath.resolve(filename));

        String avatarUrl = "/uploads/avatars/" + filename;
        User user = findById(userId);
        user.setAvatarUrl(avatarUrl);
        userRepository.save(user);

        return avatarUrl;
    }

    public List<UserResponse> search(String query, UUID excludeId) {
        if (query == null || query.trim().length() < 2) {
            throw new BusinessException("Ingresa al menos 2 caracteres para buscar");
        }
        return userRepository.searchByNameOrEmail(query.trim(), excludeId)
                .stream().map(UserResponse::from).toList();
    }

    public User findById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return ".jpg";
        return filename.substring(filename.lastIndexOf("."));
    }
}
