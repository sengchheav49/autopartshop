package com.example.autopartsshop.models;

import java.text.NumberFormat;
import java.util.Locale;

public class Product {
    private int id;
    private String name;
    private String description;
    private double price;
    private String category;
    private byte[] imageData;
    private byte[] brandLogoImageData;
    private String brandName;// Store image as byte[] for BLOB storage
    private int brandId; // Add this field for brand ID
    private int stock;
    private String createdAt;
    private int year;
    private int mileage;
    
    // Additional BLOB storage fields
    private byte[] technicalSpecsData; // For PDF or document storage
    private byte[] videoThumbnailData; // For video thumbnail storage
    private byte[] additionalImagesData; // For storing multiple images as one blob (could be serialized)
    private byte[] manualData; // For storing PDF manuals

    // Default constructor
    public Product() {}

    // Constructor with image data
    public Product(int id, String name, String description, double price, String category, 
                  byte[] imageData, int stock, String createdAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.category = category;
        this.imageData = imageData; // Now correctly set as byte[]
        this.stock = stock;
        this.createdAt = createdAt;
        this.brandLogoImageData = brandLogoImageData;
        this.brandName = brandName;
    }

    // Complete constructor with all BLOB fields
    public Product(int id, String name, String description, double price, String category,
                  byte[] imageData, byte[] brandLogoImageData, String brandName, int stock,
                  String createdAt, int year, int mileage, byte[] technicalSpecsData,
                  byte[] videoThumbnailData, byte[] additionalImagesData, byte[] manualData) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.category = category;
        this.imageData = imageData;
        this.brandLogoImageData = brandLogoImageData;
        this.brandName = brandName;
        this.stock = stock;
        this.createdAt = createdAt;
        this.year = year;
        this.mileage = mileage;
        this.technicalSpecsData = technicalSpecsData;
        this.videoThumbnailData = videoThumbnailData;
        this.additionalImagesData = additionalImagesData;
        this.manualData = manualData;
    }

    // Constructor without ID and createdAt
    public Product(String name, String description, double price, String category, byte[] imageData, int stock) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.category = category;
        this.imageData = imageData;
        this.stock = stock;
        this.brandLogoImageData = brandLogoImageData;
        this.brandName = brandName;
    }

    // Getter and Setter for image
    public byte[] getImageData() {
        return imageData;
    }

    public void setImageData(byte[] imageData) {
        this.imageData = imageData;
    }
    
    public byte[] getBrandLogoImageData() {
        return brandLogoImageData;
    }
    
    public void setBrandLogoImageData(byte[] brandLogoImageData) {
        this.brandLogoImageData = brandLogoImageData;
    }

    // Getters and setters for new BLOB fields
    public byte[] getTechnicalSpecsData() {
        return technicalSpecsData;
    }

    public void setTechnicalSpecsData(byte[] technicalSpecsData) {
        this.technicalSpecsData = technicalSpecsData;
    }

    public byte[] getVideoThumbnailData() {
        return videoThumbnailData;
    }

    public void setVideoThumbnailData(byte[] videoThumbnailData) {
        this.videoThumbnailData = videoThumbnailData;
    }

    public byte[] getAdditionalImagesData() {
        return additionalImagesData;
    }

    public void setAdditionalImagesData(byte[] additionalImagesData) {
        this.additionalImagesData = additionalImagesData;
    }

    public byte[] getManualData() {
        return manualData;
    }

    public void setManualData(byte[] manualData) {
        this.manualData = manualData;
    }

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

    public String getBrandName() {
        return brandName;
    }

    public void setBrandName(String brandName) {
        this.brandName = brandName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getMileage() {
        return mileage;
    }

    public void setMileage(int mileage) {
        this.mileage = mileage;
    }

    public String getFormattedPrice() {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.US);
        return formatter.format(price);
    }

    // Add getter and setter for brandId
    public int getBrandId() {
        return brandId;
    }

    public void setBrandId(int brandId) {
        this.brandId = brandId;
    }
}
