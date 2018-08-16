package com.petitur.ui;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.petitur.R;
import com.petitur.adapters.PetListRecycleViewAdapter;
import com.petitur.resources.CustomLocationListener;
import com.petitur.resources.ImageSyncAsyncTaskLoader;

public class PetListActivity extends AppCompatActivity implements
        PetListRecycleViewAdapter.PetListItemClickHandler,
        CustomLocationListener.LocationListenerHandler,
        LoaderManager.LoaderCallbacks<String>,
        ImageSyncAsyncTaskLoader.OnImageSyncOperationsHandler{


    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pet_list);
    }

    @NonNull @Override public Loader<String> onCreateLoader(int id, @Nullable Bundle args) {
        return null;
    }
    @Override public void onLoadFinished(@NonNull Loader<String> loader, String data) {

    }
    @Override public void onLoaderReset(@NonNull Loader<String> loader) {

    }

    @Override public void onPetListItemClick(int clickedItemIndex) {

    }

    @Override public void onLocalCoordinatesFound(double longitude, double latitude) {

    }

    @Override public void onDisplayRefreshRequested() {

    }
}
