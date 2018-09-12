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

public class VetEventRecycleViewAdapter extends RecyclerView.Adapter<VetEventRecycleViewAdapter.VetEventViewHolder> {

    private final Context mContext;
    private List<String> vetDates;
    private List<String> vetDescriptions;
    private Map<String, String> vetEvents;
    final private VetEventClickHandler mOnClickHandler;
    private int mSelectedIndex;

    public VetEventRecycleViewAdapter(Context context, VetEventClickHandler listener, Map<String, String> vetEvents) {
        this.mContext = context;
        this.mOnClickHandler = listener;
        this.vetEvents = vetEvents;
        this.vetDates = getDatesListFromMap();
        this.vetDescriptions = getDescriptionsListFromMapAccordingToDates();
    }

    @NonNull @Override public VetEventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.list_item_vet_event, parent, false);
        view.setFocusable(true);
        return new VetEventViewHolder(view);
    }
    @Override public void onBindViewHolder(@NonNull VetEventViewHolder holder, int position) {

        holder.dateTextView.setText(vetDates.get(position));
        holder.decriptionTextView.setText(vetDescriptions.get(position));
    }

    public void setSelectedVetEvent(int selectedIndex) {
        if (mSelectedIndex != selectedIndex) {
            mSelectedIndex = selectedIndex;
            this.notifyDataSetChanged();
        }
    }

    @Override public int getItemCount() {
        return (vetEvents == null) ? 0 : vetEvents.size();
    }

    public void setContents(Map<String, String> vetEvents) {
        this.vetEvents = vetEvents;
        this.vetDates = getDatesListFromMap();
        this.vetDescriptions = getDescriptionsListFromMapAccordingToDates();

        if (vetEvents != null) {
            this.notifyDataSetChanged();
        }
    }

    private List<String> getDatesListFromMap() {
        if (vetEvents==null) return new ArrayList<>();
        List<String> dates =  new ArrayList<>(vetEvents.keySet());
        dates = Utilities.sortListAccordingToDates(dates, true);
        return dates;
    }
    private List<String> getDescriptionsListFromMapAccordingToDates() {
        if (vetDates==null) return new ArrayList<>();
        List<String> descriptions = new ArrayList<>();
        for (String key : vetDates) {
            descriptions.add(vetEvents.get(key));
        }
        return descriptions;
    }

    public class VetEventViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.vet_event_list_item_date_text) TextView dateTextView;
        @BindView(R.id.vet_event_list_item_description_text) TextView decriptionTextView;

        VetEventViewHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int clickedPosition = getAdapterPosition();
            mOnClickHandler.onVetEventClick(vetDates.get(clickedPosition));
        }
    }

    public interface VetEventClickHandler {
        void onVetEventClick(String date);
    }
}
