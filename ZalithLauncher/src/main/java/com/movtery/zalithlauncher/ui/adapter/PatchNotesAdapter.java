package com.movtery.zalithlauncher.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.movtery.zalithlauncher.R;
import com.movtery.zalithlauncher.feature.mojang.MojangContentManager.PatchNoteItem;

import java.util.List;

public class PatchNotesAdapter extends RecyclerView.Adapter<PatchNotesAdapter.ViewHolder> {
    private final List<PatchNoteItem> items;
    private static final int[] ARTS = {
        R.drawable.img_hero_wilderness,
        R.drawable.img_mc_hero_1_21,
        R.drawable.img_mc_hero_1_20,
        R.drawable.img_mc_hero_1_19,
        R.drawable.img_mc_hero_1_18,
        R.drawable.img_mc_hero_1_16,
        R.drawable.img_mc_hero_1_14,
        R.drawable.img_mc_hero_1_13
    };

    public PatchNotesAdapter(List<PatchNoteItem> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_patch_note, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PatchNoteItem item = items.get(position);
        if (holder.image != null) {
            holder.image.setImageResource(ARTS[position % ARTS.length]);
        }
        if (holder.subtitle != null) {
            holder.subtitle.setText("Minecraft: Java Edition");
        }
        if (holder.version != null) {
            holder.version.setText(item.version != null && !item.version.isEmpty() ? item.version : "26.3");
        }
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView subtitle, version, title, badge, summary;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.patch_note_image);
            subtitle = itemView.findViewById(R.id.patch_note_subtitle);
            version = itemView.findViewById(R.id.patch_note_version);
            title = itemView.findViewById(R.id.patch_note_title);
            badge = itemView.findViewById(R.id.patch_note_badge);
            summary = itemView.findViewById(R.id.patch_note_summary);
        }
    }
}

