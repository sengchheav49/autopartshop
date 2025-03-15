package com.example.autopartsshop.models;

public class Brand {
    private int id;
    private String name;
    private byte[] logoData; // Store the brand logo as BLOB
    private String description;
    private String countryOfOrigin;
    private String website;
    private boolean isActive;

    // Default constructor
    public Brand() {}

    // Constructor with all fields
    public Brand(int id, String name, byte[] logoData, String description, 
                String countryOfOrigin, String website, boolean isActive) {
        this.id = id;
        this.name = name;
        this.logoData = logoData;
        this.description = description;
        this.countryOfOrigin = countryOfOrigin;
        this.website = website;
        this.isActive = isActive;
    }

    // Constructor without ID (for new brands)
    public Brand(String name, byte[] logoData, String description, 
                String countryOfOrigin, String website, boolean isActive) {
        this.name = name;
        this.logoData = logoData;
        this.description = description;
        this.countryOfOrigin = countryOfOrigin;
        this.website = website;
        this.isActive = isActive;
    }

    // Getters and setters
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

    public byte[] getLogoData() {
        return logoData;
    }

    public void setLogoData(byte[] logoData) {
        this.logoData = logoData;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCountryOfOrigin() {
        return countryOfOrigin;
    }

    public void setCountryOfOrigin(String countryOfOrigin) {
        this.countryOfOrigin = countryOfOrigin;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    @Override
    public String toString() {
        return name;
    }
} 