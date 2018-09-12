package com.petitur.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.petitur.R;
import com.petitur.resources.Utilities;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FosteringFamiliesRecycleViewAdapter extends RecyclerView.Adapter<FosteringFamiliesRecycleViewAdapter.FosteringFamilyViewHolder> {

    private final Context mContext;
    private List<String> dateRanges;
    private List<String> familyDescriptions;
    private Map<String, String> fosteringFamilies;
    final private FosteringFamilyClickHandler mOnClickHandler;
    private int mSelectedIndex;

    public FosteringFamiliesRecycleViewAdapter(Context context, FosteringFamilyClickHandler listener, Map<String, String> fosteringFamilies) {
        this.mContext = context;
        this.mOnClickHandler = listener;
        this.fosteringFamilies = fosteringFamilies;
        this.dateRanges = getDateRangeListFromMap();
        this.familyDescriptions = getDescriptionsListFromMapAccordingToDateRanges();
    }

    @NonNull @Override public FosteringFamilyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.list_item_fostering_family, parent, false);
        view.setFocusable(true);
        return new FosteringFamilyViewHolder(view);
    }
    @Override public void onBindViewHolder(@NonNull FosteringFamilyViewHolder holder, int position) {

        holder.dateRangeTextView.setText(dateRanges.get(position));
        holder.decriptionTextView.setText(familyDescriptions.get(position));
    }

    public void setSelectedVetEvent(int selectedIndex) {
        if (mSelectedIndex != selectedIndex) {
            mSelectedIndex = selectedIndex;
            this.notifyDataSetChanged();
        }
    }

    @Override public int getItemCount() {
        return (fosteringFamilies == null) ? 0 : fosteringFamilies.size();
    }

    public void setContents(Map<String, String> vetEvents) {
        this.fosteringFamilies = vetEvents;
        this.dateRanges = getDateRangeListFromMap();
        this.familyDescriptions = getDescriptionsListFromMapAccordingToDateRanges();

        if (vetEvents != null) {
            this.notifyDataSetChanged();
        }
    }

    private List<String> getDateRangeListFromMap() {
        if (fosteringFamilies ==null) return new ArrayList<>();
        List<String> dateRanges =  new ArrayList<>(fosteringFamilies.keySet());
        dateRanges = Utilities.sortListAccordingToDateRanges(dateRanges, true);
        return dateRanges;
    }
    private List<String> getDescriptionsListFromMapAccordingToDateRanges() {
        if (dateRanges ==null) return new ArrayList<>();
        List<String> descriptions = new ArrayList<>();
        for (String key : dateRanges) {
            descriptions.add(fosteringFamilies.get(key));
        }
        return descriptions;
    }

    public class FosteringFamilyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.fostering_family_list_item_date_range_text) TextView dateRangeTextView;
        @BindView(R.id.fostering_family_list_item_description_text) TextView decriptionTextView;

        FosteringFamilyViewHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int clickedPosition = getAdapterPosition();
            mOnClickHandler.onFosteringFamilyClick(dateRanges.get(clickedPosition));
        }
    }

    public interface FosteringFamilyClickHandler {
        void onFosteringFamilyClick(String date);
    }
}
