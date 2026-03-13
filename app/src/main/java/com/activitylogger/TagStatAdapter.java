package com.activitylogger;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;
public class TagStatAdapter extends RecyclerView.Adapter<TagStatAdapter.StatViewHolder> {
    public interface OnTagClickListener { void onTagClick(TagStat stat); }
    private List<TagStat> stats = new ArrayList<>();
    private OnTagClickListener listener;
    public void setStats(List<TagStat> data) { stats=data; notifyDataSetChanged(); }
    public void setOnTagClickListener(OnTagClickListener l) { listener=l; }
    @Override public StatViewHolder onCreateViewHolder(ViewGroup p, int v) {
        return new StatViewHolder(LayoutInflater.from(p.getContext()).inflate(R.layout.item_tag_stat, p, false));
    }
    @Override public void onBindViewHolder(StatViewHolder h, int pos) { h.bind(stats.get(pos), listener); }
    @Override public int getItemCount() { return stats.size(); }
    static class StatViewHolder extends RecyclerView.ViewHolder {
        TextView tvTagName, tvCount;
        StatViewHolder(View v) { super(v);
            tvTagName=(TextView)v.findViewById(R.id.tv_tag_name);
            tvCount=(TextView)v.findViewById(R.id.tv_entry_count); }
        void bind(final TagStat stat, final OnTagClickListener l) {
            tvTagName.setText(stat.getName());
            tvCount.setText(stat.getEntryCount() + " entries");
            try { GradientDrawable bg = new GradientDrawable();
                bg.setShape(GradientDrawable.RECTANGLE); bg.setCornerRadius(50f);
                bg.setColor(Color.parseColor(stat.getColor())); tvCount.setBackground(bg);
            } catch (IllegalArgumentException e) {}
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) { if (l!=null) l.onTagClick(stat); }
            });
        }
    }
}
