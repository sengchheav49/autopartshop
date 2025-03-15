package com.example.autopartsshop.models;

import android.graphics.Bitmap;
import android.widget.EditText;

public class User {
    private int id;
    private String username;
    private String email;
    private String password;
    private String phone;
    private String address;
    private boolean isAdmin;
    private String createdAt;
    private byte[] profileImage; // Ensure this exists



        public User(int id, String username, String email, String password, String phone, String address, boolean isAdmin, String createdAt) {
            this.id = id;
            this.username = username;
            this.email = email;
            this.password = password;
            this.phone = phone;
            this.address = address;
            this.isAdmin = isAdmin;
            this.createdAt = createdAt;
        }

        // Getters and setters...
        public User(String username, String email, String password, String phone, String address, boolean isAdmin) {
            this.username = username;
            this.email = email;
            this.password = password;
            this.phone = phone;
            this.address = address;
            this.isAdmin = isAdmin;
        }

    public  User(){};
    // Constructor with profile image

    public User(String username, String email, String password, String phone, String address, byte[] profileImage) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.phone = phone;
        this.address = address;
        this.profileImage = profileImage;
    }

    public User(String username, String email, String password, byte[] profileImage) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.profileImage = profileImage;
    }
    // Constructor including profile image
    // Constructor including profile image
    public User(int id, String username, String email, String password, String phone, String address, boolean isAdmin, String createdAt, byte[] profileImage) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.phone = phone;
        this.address = address;
        this.isAdmin = isAdmin;
        this.createdAt = createdAt;
        this.profileImage = profileImage;
    }




    // Update getter and setter
    public byte[] getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(byte[] profileImage) {
        this.profileImage = profileImage;
    }


    public int getId(){
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }

    public void setUserLogin(User user) {
        setUserLogin(user);  // Call the existing method with null for Bitmap
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", address='" + address + '\'' +
                ", isAdmin=" + isAdmin +
                '}';
    }
}