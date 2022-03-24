package com.nihaocloud.sesamedisk.ui.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class ItemArrayAdapter<T> extends ArrayAdapter<T> {
    private final FilterText<T> filterText;

    public ItemArrayAdapter(@NonNull Context context, int resource, FilterText<T> filterText) {
        super(context, resource);
        this.filterText = filterText;
    }

    public ItemArrayAdapter(@NonNull Context context, int resource, int textViewResourceId, FilterText<T> filterText) {
        super(context, resource, textViewResourceId);
        this.filterText = filterText;
    }

    public ItemArrayAdapter(@NonNull Context context, int resource, @NonNull T[] objects, FilterText<T> filterText) {
        super(context, resource, objects);
        this.filterText = filterText;
    }

    public ItemArrayAdapter(@NonNull Context context, int resource, int textViewResourceId, @NonNull T[] objects, FilterText<T> filterText) {
        super(context, resource, textViewResourceId, objects);
        this.filterText = filterText;
    }

    public ItemArrayAdapter(@NonNull Context context, int resource, @NonNull List<T> objects, FilterText<T> filterText) {
        super(context, resource, objects);
        this.filterText = filterText;
    }

    public ItemArrayAdapter(@NonNull Context context, int resource, int textViewResourceId, @NonNull List<T> objects, FilterText<T> filterText) {
        super(context, resource, textViewResourceId, objects);
        this.filterText = filterText;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final View view = super.getView(position, convertView, parent);
        final T item = getItem(position);
        if (view instanceof TextView && item != null) {
            final TextView v = (TextView) view;
            final String text = this.filterText.get(item);
            v.setText(text);
        }
        return view;
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final View view = super.getDropDownView(position, convertView, parent);
        final T item = getItem(position);
        if (view instanceof TextView && item != null) {
            final TextView v = (TextView) view;
            final String text = this.filterText.get(item);
            v.setText(text);
        }
        return view;
    }
}
