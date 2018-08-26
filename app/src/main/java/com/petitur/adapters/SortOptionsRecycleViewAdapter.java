package com.petitur.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.petitur.R;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SortOptionsRecycleViewAdapter extends RecyclerView.Adapter<SortOptionsRecycleViewAdapter.SortOptionViewHolder> {

    private final Context mContext;
    private List<String> texts;
    final private SortOptionClickHandler mOnClickHandler;
    private int mSelectedOptionIndex;

    public SortOptionsRecycleViewAdapter(Context context, SortOptionClickHandler listener, List<String> texts) {
        this.mContext = context;
        this.mOnClickHandler = listener;
        this.texts = texts;
    }

    @NonNull @Override public SortOptionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.list_item_sort_options, parent, false);
        view.setFocusable(true);
        return new SortOptionViewHolder(view);
    }
    @Override public void onBindViewHolder(@NonNull SortOptionViewHolder holder, int position) {

        holder.textViewInRecyclerView.setText(texts.get(position));
        updateBackground(holder, position);
    }

    private void updateBackground(SortOptionViewHolder holder, int position) {
        if (position== mSelectedOptionIndex) {
            holder.container.setBackgroundColor(mContext.getResources().getColor(R.color.selected_item_background_color));
        }
        else {
            //Use the default android background color
            TypedValue typedValue = new TypedValue();
            mContext.getTheme().resolveAttribute(android.R.attr.windowBackground, typedValue, true);
            holder.container.setBackgroundColor(typedValue.data);
        }
    }
    public void setSelectedOption(int selectedProfileIndex) {
        if (mSelectedOptionIndex != selectedProfileIndex) {
            mSelectedOptionIndex = selectedProfileIndex;
            this.notifyDataSetChanged();
        }
    }

    @Override public int getItemCount() {
        return (texts == null) ? 0 : texts.size();
    }

    public void setContents(List<String> texts) {
        this.texts = texts;
        if (texts != null) {
            this.notifyDataSetChanged();
        }
    }

    public class SortOptionViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.sort_options_list_item) TextView textViewInRecyclerView;
        @BindView(R.id.sort_options_recyclerView_item_container) RelativeLayout container;

        SortOptionViewHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int clickedPosition = getAdapterPosition();
            mOnClickHandler.onSortOptionClick(clickedPosition);
        }
    }

    public interface SortOptionClickHandler {
        void onSortOptionClick(int clickedItemIndex);
    }
}
