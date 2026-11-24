package com.movtery.zalithlauncher.ui.adapter;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.movtery.zalithlauncher.R;
import com.movtery.zalithlauncher.feature.mojang.MojangContentManager.NewsItem;

import java.util.List;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.ViewHolder> {
    private final List<NewsItem> items;

    public NewsAdapter(List<NewsItem> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_news_article, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        NewsItem item = items.get(position);
        holder.category.setText(item.category);
        holder.title.setText(item.title);

        int[] arts = {
            R.drawable.img_hero_wilderness,
            R.drawable.img_mc_hero_1_21,
            R.drawable.img_mc_hero_1_20,
            R.drawable.img_mc_hero_1_19,
            R.drawable.img_mc_hero_1_18,
            R.drawable.img_mc_hero_1_16,
            R.drawable.img_mc_hero_1_14,
            R.drawable.img_mc_hero_1_13
        };
        if (item.imageUrl != null && !item.imageUrl.isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(item.imageUrl)
                    .placeholder(arts[position % arts.length])
                    .into(holder.image);
        } else {
            holder.image.setImageResource(arts[position % arts.length]);
        }

        holder.readMore.setOnClickListener(v -> {
            if (item.readMoreUrl != null && !item.readMoreUrl.isEmpty()) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(item.readMoreUrl));
                holder.itemView.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView category, title;
        View readMore;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.news_image);
            category = itemView.findViewById(R.id.news_category);
            title = itemView.findViewById(R.id.news_title);
            readMore = itemView.findViewById(R.id.news_read_more);
        }
    }
}
