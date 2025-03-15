package com.example.autopartsshop.models;

import java.util.HashMap;
import java.util.Map;

public class Admin {
    private int id;
    private String name;
    private String email;
    private String password;
    private String phone;
    private String role;
    private String bio;
    private String profileImage;
    private String createdAt;
    private Map<String, Boolean> permissions;

    // Constructor with id
    public Admin(int id, String name, String email, String password, String phone, String role,
                 String bio, String profileImage, String createdAt) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.phone = phone;
        this.role = role;
        this.bio = bio;
        this.profileImage = profileImage;
        this.createdAt = createdAt;
        this.permissions = new HashMap<>();
    }

    // Constructor without id for new admin creation
    public Admin(String name, String email, String password, String phone, String role) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.phone = phone;
        this.role = role;
        this.permissions = new HashMap<>();
    }

    // Empty constructor
    public Admin() {
        this.permissions = new HashMap<>();
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    // Permission management
    public boolean hasPermission(String permissionKey) {
        Boolean permission = permissions.get(permissionKey);
        return permission != null && permission;
    }

    public void setPermission(String permissionKey, boolean value) {
        permissions.put(permissionKey, value);
    }

    public Map<String, Boolean> getPermissions() {
        return permissions;
    }

    public void setPermissions(Map<String, Boolean> permissions) {
        this.permissions = permissions;
    }
}