package com.petitur.resources;

import android.content.Context;
import android.net.Uri;
import android.support.v4.content.AsyncTaskLoader;

import com.petitur.R;
import com.petitur.data.*;

import java.util.List;

public class ImageSyncAsyncTaskLoader extends AsyncTaskLoader<String> implements
        FirebaseDao.FirebaseOperationsHandler {


    private final static int NUM_OBJECTS_TO_UPDATE_BEFORE_DISPLAYING_IMAGES = 6;
    private final String mTask;
    private List<Pet> mPetsList;
    private List<Family> mFamiliesList;
    private List<Foundation> mFoundationsList;
    private String mProfileType;
    private String mCurrentImage;
    private int mPositionInObjectsList;
    private FirebaseDao mFirebaseDao;
    private boolean isCancelled;

    public ImageSyncAsyncTaskLoader(Context context,
                                    String task,
                                    String profileType,
                                    List<Pet> pets,
                                    List<Family> families,
                                    List<Foundation> foundations,
                                    OnImageSyncOperationsHandler onImageSyncOperationsHandler) {
        super(context);
        this.mTask = task;
        this.mProfileType = profileType;
        this.mPetsList = pets;
        this.mFamiliesList = families;
        this.mFoundationsList = foundations;
        this.onImageSyncOperationsHandler = onImageSyncOperationsHandler;
    }

    //Service methods
    @Override protected void onStartLoading() {
        if (mProfileType==null) return;

        isCancelled = false;
        mFirebaseDao = new FirebaseDao(getContext(), this);
        forceLoad();
    }
    @Override public String loadInBackground() {
        if (!isCancelled) startUpdatingImagesForObjects();
        return null;
    }


    //Functional methods
    private void startUpdatingImagesForObjects() {

        mCurrentImage = "mainImage";
        mPositionInObjectsList = 0;

        if (mProfileType.equals(getContext().getString(R.string.pet_profile)) && mPetsList!= null && mPetsList.size()>0) {
            mFirebaseDao.getImage(mPetsList.get(0), mCurrentImage);
        }
        else if (mProfileType.equals(getContext().getString(R.string.family_profile)) && mFamiliesList!= null && mFamiliesList.size()>0) {
            mFirebaseDao.getImage(mFamiliesList.get(0), mCurrentImage);
        }
        else if (mProfileType.equals(getContext().getString(R.string.foundation_profile)) && mFoundationsList!= null && mFoundationsList.size()>0) {
            mFirebaseDao.getImage(mFoundationsList.get(0), mCurrentImage);
        }

    }
    public void stopUpdatingImagesForObjects() {
        isCancelled = true;
        if (mFirebaseDao!=null) mFirebaseDao.removeListeners();
    }
    private void updateLocalImageForCurrentObject(String imageName, Uri imageUri) {

        if (mPositionInObjectsList == objectsListSize()) return;

        Object listElement = null;
        if (mProfileType.equals(getContext().getString(R.string.pet_profile))) {
            listElement = mPetsList.get(mPositionInObjectsList);
        }
        else if (mProfileType.equals(getContext().getString(R.string.family_profile))) {
            listElement = mFamiliesList.get(mPositionInObjectsList);
        }
        else if (mProfileType.equals(getContext().getString(R.string.foundation_profile))) {
            listElement = mFoundationsList.get(mPositionInObjectsList);
        }

        Utilities.updateImageOnLocalDevice(getContext(), listElement, mFirebaseDao, imageName, imageUri);
    }
    private int objectsListSize() {
        if (mProfileType.equals(getContext().getString(R.string.pet_profile))) {
            return mPetsList.size();
        }
        else if (mProfileType.equals(getContext().getString(R.string.family_profile))) {
            return mFamiliesList.size();
        }
        else if (mProfileType.equals(getContext().getString(R.string.foundation_profile))) {
            return mFoundationsList.size();
        }
        return 0;
    }
    private void getNextImage(String currentImageName) {

        if (mPositionInObjectsList == objectsListSize()) return;

        if (mTask.equals(getContext().getString(R.string.task_sync_list_main_images))) {
            if (mPositionInObjectsList < objectsListSize()) {
                mPositionInObjectsList++;
                if (mPositionInObjectsList < objectsListSize()) requestImageFromFirebase();
            }
            if (mPositionInObjectsList > 0 && mPositionInObjectsList % NUM_OBJECTS_TO_UPDATE_BEFORE_DISPLAYING_IMAGES == 0) {
                tellCallingClassToUpdateImageDisplay();
            }
        }
        else if (mTask.equals(getContext().getString(R.string.task_sync_single_object_images))) {
            switch (currentImageName) {
                case "mainImage": {
                    mCurrentImage = "image1";
                    requestImageFromFirebase();
                    break;
                }
                case "image1": {
                    mCurrentImage = "image2";
                    requestImageFromFirebase();
                    break;
                }
                case "image2": {
                    mCurrentImage = "image3";
                    requestImageFromFirebase();
                    break;
                }
                case "image3": {
                    mCurrentImage = "image4";
                    requestImageFromFirebase();
                    break;
                }
                case "image4": {
                    mCurrentImage = "image5";
                    requestImageFromFirebase();
                    break;
                }
                case "image5": {
                    tellCallingClassToUpdateImageDisplay();
                    break;
                }
            }
        }


    }
    private void tellCallingClassToUpdateImageDisplay() {
        onImageSyncOperationsHandler.onDisplayRefreshRequested();
    }
    private void requestImageFromFirebase() {

        if (mPositionInObjectsList == objectsListSize()) return;

        if (mProfileType.equals(getContext().getString(R.string.pet_profile))) {
            mFirebaseDao.getImage(mPetsList.get(mPositionInObjectsList), mCurrentImage);
        }
        else if (mProfileType.equals(getContext().getString(R.string.family_profile))) {
            mFirebaseDao.getImage(mFamiliesList.get(mPositionInObjectsList), mCurrentImage);
        }
        else if (mProfileType.equals(getContext().getString(R.string.foundation_profile))) {
            mFirebaseDao.getImage(mFoundationsList.get(mPositionInObjectsList), mCurrentImage);
        }
    }


    //Communication with other classes:

    //Communication with Firebase Dao handler
    @Override public void onPetListFound(List<Pet> pets) {
    }
    @Override public void onFamilyListFound(List<Family> families) {
    }
    @Override public void onFoundationListFound(List<Foundation> foundations) {
    }
    @Override public void onUserListFound(List<User> usersList) {
    }
    @Override public void onMapMarkerListFound(List<MapMarker> markersList) {

    }

    @Override
    public void onImageAvailable(boolean imageWasDownloaded, Uri imageUri, String currentImageName) {

        if (isCancelled) return;

        updateLocalImageForCurrentObject(currentImageName, imageUri);
        getNextImage(currentImageName);
    }
    @Override public void onImageUploaded(List<String> uploadTimes) {

    }

    //Communication with parent activity
    private OnImageSyncOperationsHandler onImageSyncOperationsHandler;
    public interface OnImageSyncOperationsHandler {
        void onDisplayRefreshRequested();
    }
}
