package com.petitur.resources;

import android.app.Application;
import android.content.Context;

import com.google.firebase.firestore.FirebaseFirestore;
import com.petitur.resources.LocaleHelper;
import com.petitur.resources.Utilities;

public class MainApplication extends Application {

    @Override public void onCreate() {
        super.onCreate();

        //Inpired by: https://stackoverflow.com/questions/37753991/com-google-firebase-database-databaseexception-calls-to-setpersistenceenabled
        FirebaseFirestore database = Utilities.getDatabase();

        /*Note: two ways are demonstrated here to set the persistence of Firebase without problems: as a singleton (Utilities) or using an activity that loads before all others*/
    }
    @Override protected void attachBaseContext(Context base) {
        //Inspired by: https://gunhansancar.com/change-language-programmatically-in-android/
        super.attachBaseContext(LocaleHelper.onAttach(base, "en"));
    }
}
