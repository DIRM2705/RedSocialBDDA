package com.redsocial.controller;

import com.redsocial.dto.UserRequest;
import com.redsocial.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/users")
public class UserController
{
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // POST /api/users
    @PostMapping
    public ResponseEntity<?> createUser(@Valid @RequestBody UserRequest userRequest) {
        userService.registerUser(
                userRequest.getUsername(),
                userRequest.getEmail(),
                userRequest.getPassword(),
                userRequest.getProfilePictureURL(),
                userRequest.getBio());
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "Usuario creado exitosamente"));
    }

    // POST /api/users/login
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@Valid @RequestBody UserRequest userRequest) {
        boolean isAuthenticated = userService.authenticateUser(
                userRequest.getUsername(),
                userRequest.getPassword());
        if (isAuthenticated) {
            return ResponseEntity.ok(Map.of("message", "Inicio de sesión exitoso"));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Credenciales inválidas"));
        }
    }

    // DELETE /api/users/{userId}
    @DeleteMapping("/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable long userId) {
        userService.deleteUser(userId);
        return ResponseEntity.ok(Map.of("message", "Usuario eliminado exitosamente"));
    }

    // GET /api/users/{userId}/profile
    @GetMapping("/{userId}/profile")
    public ResponseEntity<?> getUserProfile(@PathVariable long userId) {
        userService.getProfile(userId);
        return ResponseEntity.ok(Map.of("message", "Perfil del usuario obtenido exitosamente"));
    }

    // PUT /api/users/{userId}
    @PutMapping("/{userId}")
    public ResponseEntity<?> updateUser(@PathVariable long userId, @Valid @RequestBody UserRequest userRequest) {
        userService.updateUser(userId, userRequest.getUsername(), userRequest.getEmail(), userRequest.getPassword());
        return ResponseEntity.ok(Map.of("message", "Usuario actualizado exitosamente"));
    }

    // PUT /api/users/{userId}/profile
    @PutMapping("/{userId}/profile")
    public ResponseEntity<?> updateUserProfile(@PathVariable long userId, @Valid @RequestBody UserRequest userRequest) {
        userService.updateUserProfile(userId, userRequest.getProfilePictureURL(), userRequest.getBio());
        return ResponseEntity.ok(Map.of("message", "Perfil del usuario actualizado exitosamente"));
    }

    // PUT /api/users/{userId}/follow?userId=1
    @PutMapping("/{userId}/followers")
    public ResponseEntity<?> updateFollowers(@PathVariable long userId, @RequestParam long followerId) {
        userService.addFollower(userId, followerId);
        return ResponseEntity.ok(Map.of("message", "Seguidores actualizados exitosamente"));
    }

    // PUT /api/users/{userId}/following?userId=1
    @PutMapping("/{userId}/following")
    public ResponseEntity<?> updateFollowing(@PathVariable long userId, @RequestParam long followingId) {
        userService.addFollowing(userId, followingId);
        return ResponseEntity.ok(Map.of("message", "Seguidos actualizados exitosamente"));
    }

    // DELETE /api/users/{userId}/followers?userId=1
    @DeleteMapping("/{userId}/followers")
    public ResponseEntity<?> removeFollower(@PathVariable long userId, @RequestParam long followerId) {
        userService.removeFollower(userId, followerId);
        return ResponseEntity.ok(Map.of("message", "Seguidor eliminado exitosamente"));
    }

    // DELETE /api/users/{userId}/following?userId=1
    @DeleteMapping("/{userId}/following")
    public ResponseEntity<?> removeFollowing(@PathVariable long userId, @RequestParam long followingId) {
        userService.removeFollowing(userId, followingId);
        return ResponseEntity.ok(Map.of("message", "Seguido eliminado exitosamente"));
    }
}
