package com.petitur.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.petitur.R;
import com.petitur.data.Pet;
import com.petitur.resources.Utilities;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PetListRecycleViewAdapter extends RecyclerView.Adapter<PetListRecycleViewAdapter.PetViewHolder> {

    private final Context mContext;
    private List<Pet> mPets;
    final private PetListItemClickHandler mOnClickHandler;
    private int mSelectedProfileIndex;

    public PetListRecycleViewAdapter(Context context, PetListItemClickHandler listener, List<Pet> pets) {
        this.mContext = context;
        this.mOnClickHandler = listener;
        this.mPets = pets;
    }

    @NonNull @Override public PetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.list_item_pet, parent, false);
        view.setFocusable(true);
        return new PetViewHolder(view);
    }
    @Override public void onBindViewHolder(@NonNull PetViewHolder holder, int position) {

        Pet pet = mPets.get(position);

        Utilities.displayObjectImageInImageView(mContext, pet, "mainImage", holder.petImageView);

        holder.nameTextView.setText(pet.getNm());
        holder.cityTextView.setText(pet.getCt());
        holder.raceTextView.setText(pet.getRc());
        holder.ageTextView.setText(pet.getAg());
        holder.distanceTextView.setText(pet.getAg());

        String gender = pet.getGn();
        if (gender.equals("Male")) Picasso.with(mContext).load(R.drawable.ic_pet_gender_male_24dp).into(holder.genderImageView);
        else Picasso.with(mContext).load(R.drawable.ic_pet_gender_female_24dp).into(holder.genderImageView);

        updateBackground(holder, position);
    }
    private void updateBackground(PetViewHolder holder, int position) {
        if (position== mSelectedProfileIndex) {
            holder.container.setBackgroundColor(mContext.getResources().getColor(R.color.selected_item_background_color));
        }
        else {
            //Use the default android background color
            TypedValue typedValue = new TypedValue();
            mContext.getTheme().resolveAttribute(android.R.attr.windowBackground, typedValue, true);
            holder.container.setBackgroundColor(typedValue.data);
        }
    }

    public void setSelectedProfile(int selectedProfileIndex) {
        if (mSelectedProfileIndex != selectedProfileIndex) {
            mSelectedProfileIndex = selectedProfileIndex;
            this.notifyDataSetChanged();
        }
    }

    @Override public int getItemCount() {
        return (mPets == null) ? 0 : mPets.size();
    }

    public void setContents(List<Pet> pet) {
        mPets = pet;
        if (pet != null) {
            this.notifyDataSetChanged();
        }
    }

    public class PetViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.pet_list_item_name) TextView nameTextView;
        @BindView(R.id.pet_list_item_city) TextView cityTextView;
        @BindView(R.id.pet_list_item_race) TextView raceTextView;
        @BindView(R.id.pet_list_item_age) TextView ageTextView;
        @BindView(R.id.pet_list_item_distance) TextView distanceTextView;
        @BindView(R.id.pet_list_item_image) ImageView petImageView;
        @BindView(R.id.pet_list_item_gender) ImageView genderImageView;
        @BindView(R.id.pet_list_item_love) ImageView loveImageView;
        @BindView(R.id.pet_list_item_container) ConstraintLayout container;

        PetViewHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int clickedPosition = getAdapterPosition();
            mOnClickHandler.onPetListItemClick(clickedPosition);
        }
    }

    public interface PetListItemClickHandler {
        void onPetListItemClick(int clickedItemIndex);
    }
}
