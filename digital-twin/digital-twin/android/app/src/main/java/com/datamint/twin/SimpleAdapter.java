package com.datamint.twin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

/** One generic RecyclerView adapter used by every list in the app. */
public class SimpleAdapter<T> extends RecyclerView.Adapter<SimpleAdapter.VH> {
    public interface Binder<T> { void bind(View view, T item); }

    private final int layout;
    private final Binder<T> binder;
    private List<T> items = new ArrayList<>();

    public SimpleAdapter(int layout, Binder<T> binder) { this.layout = layout; this.binder = binder; }

    public void set(List<T> list) {
        items = list == null ? new ArrayList<>() : new ArrayList<>(list);
        notifyDataSetChanged();
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(LayoutInflater.from(parent.getContext()).inflate(layout, parent, false));
    }

    @Override public void onBindViewHolder(@NonNull VH holder, int position) { binder.bind(holder.itemView, items.get(position)); }
    @Override public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder { VH(View v) { super(v); } }
}
