package com.example.autopartsshop.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.autopartsshop.R;
import com.example.autopartsshop.models.Banner;

import java.util.List;

public class BannerAdapter extends RecyclerView.Adapter<BannerAdapter.BannerViewHolder> {

    private final List<Banner> bannerList;

    public BannerAdapter(List<Banner> bannerList) {
        this.bannerList = bannerList;
    }

    @NonNull
    @Override
    public BannerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_banner, parent, false);
        return new BannerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BannerViewHolder holder, int position) {
        Banner banner = bannerList.get(position);
        holder.bind(banner);
    }

    @Override
    public int getItemCount() {
        return bannerList.size();
    }

    static class BannerViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imageViewBanner;
        private final TextView textViewTitle;
        private final TextView textViewDescription;

        public BannerViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewBanner = itemView.findViewById(R.id.imageViewBanner);
            textViewTitle = itemView.findViewById(R.id.textViewBannerTitle);
            textViewDescription = itemView.findViewById(R.id.textViewBannerDescription);
        }

        public void bind(Banner banner) {
            imageViewBanner.setImageResource(banner.getImageResource());
            textViewTitle.setText(banner.getTitle());
            textViewDescription.setText(banner.getDescription());
        }
    }
}