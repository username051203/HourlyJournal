package com.activitylogger;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;

public class HourSlotAdapter extends RecyclerView.Adapter<HourSlotAdapter.SlotViewHolder> {

    public interface OnSlotClickListener {
        void onTap(HourSlot slot);
        void onLongPress(HourSlot slot);
    }

    private List<HourSlot>      slots    = new ArrayList<>();
    private OnSlotClickListener listener;
    private final Context       context;

    public HourSlotAdapter(Context context) {
        this.context = context;
    }

    public void setSlots(List<HourSlot> data) {
        slots = data != null ? data : new ArrayList<HourSlot>();
        notifyDataSetChanged();
    }

    public void setOnSlotClickListener(OnSlotClickListener l) {
        listener = l;
    }

    @Override
    public SlotViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_hour_slot, parent, false);
        return new SlotViewHolder(v);
    }

    @Override
    public void onBindViewHolder(SlotViewHolder holder, int position) {
        holder.bind(slots.get(position), listener, context);
    }

    @Override
    public int getItemCount() { return slots.size(); }

    static class SlotViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvHour;
        private final TextView tvContent;
        private final TextView tvMood;
        private final TextView tvStar;
        private final View     viewTagColor;

        SlotViewHolder(View v) {
            super(v);
            tvHour       = (TextView) v.findViewById(R.id.tv_hour);
            tvContent    = (TextView) v.findViewById(R.id.tv_slot_content);
            tvMood       = (TextView) v.findViewById(R.id.tv_mood);
            tvStar       = (TextView) v.findViewById(R.id.tv_star);
            viewTagColor = v.findViewById(R.id.view_tag_color);
        }

        void bind(final HourSlot slot, final OnSlotClickListener listener, Context ctx) {
            if (tvHour != null) {
                tvHour.setText(slot.getLabel());
                tvHour.setTextColor(ThemeHelper.textSecondary(ctx));
            }

            boolean hasEntry = slot.hasEntry();
            Entry   entry    = slot.getEntry();

            if (tvContent != null) {
                if (hasEntry && entry.getContent() != null && !entry.getContent().isEmpty()) {
                    tvContent.setText(entry.getContent());
                    tvContent.setTextColor(ThemeHelper.textPrimary(ctx));
                    tvContent.setTypeface(null, Typeface.NORMAL);
                } else {
                    tvContent.setText("Tap to enter / Hold to select");
                    tvContent.setTextColor(ThemeHelper.textHint(ctx));
                    tvContent.setTypeface(null, Typeface.NORMAL);
                }
            }

            if (tvMood != null) {
                if (hasEntry && entry.getMood() != null && !entry.getMood().isEmpty()) {
                    tvMood.setText(entry.getMood());
                    tvMood.setVisibility(View.VISIBLE);
                } else {
                    tvMood.setVisibility(View.GONE);
                }
            }

            if (tvStar != null) {
                tvStar.setVisibility(hasEntry && entry.isStarred() ? View.VISIBLE : View.GONE);
            }

            if (viewTagColor != null) {
                if (hasEntry && entry.getTagColor() != null) {
                    try {
                        viewTagColor.setBackgroundColor(Color.parseColor(entry.getTagColor()));
                        viewTagColor.setVisibility(View.VISIBLE);
                    } catch (Exception ignored) {
                        viewTagColor.setVisibility(View.INVISIBLE);
                    }
                } else {
                    viewTagColor.setVisibility(View.INVISIBLE);
                }
            }

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    if (listener != null) listener.onTap(slot);
                }
            });
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override public boolean onLongClick(View v) {
                    if (listener != null) listener.onLongPress(slot);
                    return true;
                }
            });
        }
    }
}
