package com.example.autopartsshop.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.autopartsshop.R;
import com.example.autopartsshop.models.Category;

import java.util.List;

public class CategoryDropdownAdapter extends ArrayAdapter<Category> {

    private final LayoutInflater inflater;
    private int resource;
    private List<Category> categoryList;
    public CategoryDropdownAdapter(Context context, int resource, List<Category> categories) {
        super(context, resource, categories);
        this.inflater = LayoutInflater.from(context);
        this.resource = resource;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return createView(position, convertView, parent);
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return createView(position, convertView, parent);
    }

    private View createView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(resource, parent, false);
        }

        Category category = getItem(position);

        TextView textView = convertView.findViewById(R.id.textViewBrandName);
        ImageView imageView = convertView.findViewById(R.id.imageViewBrandLogo);

        if (category != null) {
            textView.setText(category.getName());

            // Debugging: Print URL to check
            Log.d("CategoryAdapter", "Loading image URL: " + category.getIconResource());

            // Load image from URL using Glide
            Glide.with(getContext())
                    .load(category.getIconResource()) // URL of the image
                    .error(R.drawable.all_products) // Show this if loading fails
                    .into(imageView);
        }

        return convertView;
    }
}
