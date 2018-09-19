package com.petitur.resources;

import android.content.Context;
import android.location.Address;
import android.net.Uri;
import android.support.v4.content.AsyncTaskLoader;
import android.text.TextUtils;

import com.petitur.R;
import com.petitur.data.Family;
import com.petitur.data.FirebaseDao;
import com.petitur.data.Foundation;
import com.petitur.data.MapMarker;
import com.petitur.data.Pet;
import com.petitur.data.User;

import java.util.List;

public class GeoAdressLookupAsyncTaskLoader extends AsyncTaskLoader<Address> {

    private final String mAddressString;

    public GeoAdressLookupAsyncTaskLoader(Context context, String addressString) {
        super(context);
        this.mAddressString = addressString;
    }

    //Service methods
    @Override protected void onStartLoading() {
        if (TextUtils.isEmpty(mAddressString)) return;
        forceLoad();
    }
    @Override public Address loadInBackground() {
        return Utilities.getAddressObjectFromAddressString(getContext(), mAddressString);
    }

}
