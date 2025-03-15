package com.example.autopartsshop.models;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class Category {
    private String name;
    private String getIconResource;

    public Category(String name, String getIconResource) {
        this.name = name;
        this.getIconResource = getIconResource;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIconResource() {
        return getIconResource;
    }

    public void setIconResource(String getIconResource) {
        this.getIconResource = getIconResource;
    }

    public String getFilteredImageUrl(List<Category> categoryList) {
        for (Category category : categoryList) {
            if (this.name.equalsIgnoreCase(category.getName())) {
                Log.d("Category", "Match found for: " + this.name + " -> " + category.getIconResource());
                return category.getIconResource();
            }
        }
        Log.e("Category", "No match found for: " + this.name);
        return getIconResource();
    }


    @Override
    public String toString() {
        return name;
    }


    public static List<Category> getCategorys() {
        List<Category> Categorys = new ArrayList<>();
        Categorys.add(new Category("Toyota", "https://www.carlogos.org/car-logos/toyota-logo.png"));
        Categorys.add(new Category("Honda", "https://www.carlogos.org/car-logos/honda-logo.png"));
        Categorys.add(new Category("Chevrolet", "https://www.carlogos.org/car-logos/chevrolet-logo.png"));
        Categorys.add(new Category("Ford", "https://www.carlogos.org/car-logos/ford-logo.png"));
        Categorys.add(new Category("Mercedes-Benz", "https://www.carlogos.org/car-logos/mercedes-benz-logo.png"));
        Categorys.add(new Category("Jeep", "https://www.carlogos.org/car-logos/jeep-logo.png"));
        Categorys.add(new Category("BMW", "https://www.carlogos.org/car-logos/bmw-logo.png"));
        Categorys.add(new Category("Porsche", "https://www.carlogos.org/car-logos/porsche-logo.png"));
        Categorys.add(new Category("Subaru", "https://www.carlogos.org/car-logos/subaru-logo.png"));
        Categorys.add(new Category("Nissan", "https://www.carlogos.org/car-logos/nissan-logo.png"));
        Categorys.add(new Category("Cadillac", "https://www.carlogos.org/car-logos/cadillac-logo.png"));
        Categorys.add(new Category("Volkswagen", "https://www.carlogos.org/car-logos/volkswagen-logo.png"));
        Categorys.add(new Category("Lexus", "https://www.carlogos.org/car-logos/lexus-logo.png"));
        Categorys.add(new Category("Audi", "https://www.carlogos.org/car-logos/audi-logo.png"));
        Categorys.add(new Category("Ferrari", "https://www.carlogos.org/car-logos/ferrari-logo.png"));
        
        Categorys.add(new Category("Bentley", "https://www.carlogos.org/car-logos/bentley-logo.png"));

        Categorys.add(new Category("Land Rover", "https://www.carlogos.org/car-logos/land-rover-logo.png"));

        Categorys.add(new Category("Oldsmobile", "https://www.carlogos.org/car-logos/oldsmobile-logo.png"));
        Categorys.add(new Category("Maserati", "https://www.carlogos.org/car-logos/maserati-logo.png"));
        Categorys.add(new Category("Aston Martin", "https://www.carlogos.org/car-logos/aston-martin-logo.png"));
        Categorys.add(new Category("Bugatti", "https://www.carlogos.org/car-logos/bugatti-logo.png"));

        Categorys.add(new Category("Lamborghini", "https://www.carlogos.org/car-logos/lamborghini-logo.png"));
        return Categorys;
    }
}
