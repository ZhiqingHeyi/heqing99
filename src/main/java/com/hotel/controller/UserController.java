package com.hotel.controller;

import com.hotel.entity.User;
import com.hotel.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody User user) {
        return ResponseEntity.ok(userService.register(user));
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody Map<String, String> credentials) {
        String token = userService.authenticate(
                credentials.get("username"),
                credentials.get("password")
        );
        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<User> getUserByUsername(@PathVariable String username) {
        return ResponseEntity.ok(userService.getUserByUsername(username));
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User user) {
        user.setId(id);
        return ResponseEntity.ok(userService.updateUser(user));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/password")
    public ResponseEntity<Void> changePassword(
            @PathVariable Long id,
            @RequestBody Map<String, String> passwords) {
        userService.changePassword(
                id,
                passwords.get("oldPassword"),
                passwords.get("newPassword")
        );
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/role/{role}")
    public ResponseEntity<List<User>> getUsersByRole(@PathVariable User.UserRole role) {
        return ResponseEntity.ok(userService.getUsersByRole(role));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Void> toggleUserStatus(
            @PathVariable Long id,
            @RequestParam boolean enabled) {
        userService.toggleUserStatus(id, enabled);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/staff/active")
    public ResponseEntity<List<User>> getAllActiveStaff() {
        return ResponseEntity.ok(userService.getAllActiveStaff());
    }

    @GetMapping("/count/{role}")
    public ResponseEntity<Long> countUsersByRole(@PathVariable User.UserRole role) {
        return ResponseEntity.ok(userService.countUsersByRole(role));
    }
}