package com.petitur.resources;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.petitur.BuildConfig;
import com.petitur.R;
import com.petitur.adapters.ImagesRecycleViewAdapter;
import com.petitur.data.*;
import com.petitur.ui.PreferencesActivity;
import com.petitur.ui.UpdateFamilyActivity;
import com.petitur.ui.UpdateFoundationActivity;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class Utilities {

    private static final String DEBUG_TAG = "Petitur Utilities";
    public static final int FIREBASE_SIGN_IN_KEY = 123;

    //App utilities
    public static void closeApp(Activity activity) {
        //Close app (inspired by: https://stackoverflow.com/questions/17719634/how-to-exit-an-android-app-using-code)
        Intent homeIntent = new Intent(Intent.ACTION_MAIN);
        homeIntent.addCategory(Intent.CATEGORY_HOME);
        homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        activity.startActivity(homeIntent);
    }
    public static void startPreferencesActivity(Activity activity) {
        Intent intent = new Intent(activity.getApplicationContext(), PreferencesActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        activity.startActivity(intent);
    }
    public static void startUpdateFamilyProfileActivity(Activity activity) {
        Intent intent = new Intent(activity.getApplicationContext(), UpdateFamilyActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        activity.startActivity(intent);
    }
    public static void startUpdateFoundationProfileActivity(Activity activity) {
        Intent intent = new Intent(activity.getApplicationContext(), UpdateFoundationActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        activity.startActivity(intent);
    }
    public static void handleUserSignIn(Activity activity, FirebaseUser mCurrentFirebaseUser, FirebaseAuth mFirebaseAuth, Menu mMenu) {
        if (mCurrentFirebaseUser==null) {
            Utilities.setAppPreferenceUserHasNotRefusedSignIn(activity.getApplicationContext(), true);
            Utilities.showSignInScreen(activity);
        }
        else {
            Utilities.setAppPreferenceUserHasNotRefusedSignIn(activity.getApplicationContext(), false);
            mFirebaseAuth.signOut();
            Utilities.updateSignInMenuItem(mMenu, activity.getBaseContext(), false);
        }
    }
    public static boolean overwriteLocalImagesWithTempImages(Context context, Uri[] mTempImageUris, Object object) {

        boolean requireOnlineSync = false;
        for (int i=0; i<mTempImageUris.length; i++) {
            Uri tempImageUri = mTempImageUris[i];

            if (tempImageUri!=null) {

                String imageName = "";
                switch (i) {
                    case 0: imageName = "mainImage"; break;
                    case 1: imageName = "image1"; break;
                    case 2: imageName = "image2"; break;
                    case 3: imageName = "image3"; break;
                    case 4: imageName = "image4"; break;
                    case 5: imageName = "image5"; break;
                }
                if (!imageName.equals("")) {
                    Uri copiedImageUri = Utilities.updateLocalImageFromUri(context, tempImageUri, object, imageName);
                    requireOnlineSync = true;
                }
            }
        }
        return requireOnlineSync;
    }
    public static boolean startSyncingImagesIfNotAlreadySyncing(Context context, boolean mCurrentlySyncingImages, Object object, FirebaseDao mFirebaseDao) {

        if (Utilities.internetIsAvailable(context)) {
            if (mCurrentlySyncingImages) {
                //Toast.makeText(context, R.string.please_wait_syncing_images, Toast.LENGTH_SHORT).show();
            }
            else {
                mFirebaseDao.getAllObjectImages(object);
                mCurrentlySyncingImages = true;
            }
        }
        else {
            mCurrentlySyncingImages = false;
            Toast.makeText(context, R.string.no_internet_sync_later, Toast.LENGTH_SHORT).show();
        }

        return mCurrentlySyncingImages;
    }


    //File utilities
    private static Uri moveFile(Uri source, String destinationDirectory, String destinationFilename) {

        if (source == null) {
            Log.i(DEBUG_TAG, "Tried to move an image with null Uri, aborting.");
            return null;
        }
        if (directoryIsInvalid(destinationDirectory)) {
            Log.i(DEBUG_TAG, "Tried to move an image to an invalid directory, aborting.");
            return null;
        }

        File sourceFile = new File(source.getPath());
        File destinationFileDirectory = new File(destinationDirectory);
        if (!destinationFileDirectory.exists()) destinationFileDirectory.mkdirs();

        File destinationFile = new File(destinationFileDirectory, destinationFilename+".jpg");
        if (destinationFile.exists()) destinationFile.delete(); //Allows replacing files

        FileChannel outputChannel = null;
        FileChannel inputChannel = null;
        try {
            destinationFile = new File(destinationFileDirectory, destinationFilename+".jpg");

            //if (!destinationFile.exists()) destinationFile.createNewFile();
            outputChannel = new FileOutputStream(destinationFile, false).getChannel();
            inputChannel = new FileInputStream(sourceFile).getChannel();
            inputChannel.transferTo(0, inputChannel.size(), outputChannel);
            inputChannel.close();
            sourceFile.delete();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (inputChannel != null) inputChannel.close();
                if (outputChannel != null) outputChannel.close();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
        return Uri.fromFile(destinationFile);
    }
    private static void deleteFileAtUri(Uri uri) {
        if (uri==null) {
            Log.i(DEBUG_TAG, "Tried to delete an image with null Uri, aborting.");
            return;
        }
        File fdelete = new File(uri.getPath());
        if (fdelete.exists()) {
            if (fdelete.delete()) {
                Log.i(DEBUG_TAG, "file deleted:" + uri.getPath());
            } else {
                Log.i(DEBUG_TAG, "file not deleted:" + uri.getPath());
            }
        }
    }
    public static String getImagesDirectoryForObject(Context context, Object object) {
        String imageDirectory;
        if (object instanceof Pet) {
            Pet pet = (Pet) object;
            imageDirectory = context.getFilesDir().getAbsolutePath()+"/dogs/"+ pet.getUI()+"/images/";
        }
        else if (object instanceof Family) {
            Family family = (Family) object;
            imageDirectory = context.getFilesDir().getAbsolutePath()+"/families/"+ family.getUI()+"/images/";
        }
        else if (object instanceof Foundation) {
            Foundation foundation = (Foundation) object;
            imageDirectory = context.getFilesDir().getAbsolutePath()+"/foundations/"+ foundation.getUI()+"/images/";
        }
        else return null;
        return imageDirectory;
    }
    public static String getTempImagesDirectory(Context context) {
        return context.getFilesDir().getAbsolutePath()+"/temp/images/";
    }
    private static File getFileWithTrials(String directory, String fileName) {
        File imageFile = new File(directory, fileName);

        //If somehow the the app was not able to get the uri (e.g. sometimes file is not "found"), then try up to 4 more times before giving up
        int tries = 3;
        while (!(imageFile.exists() && imageFile.length()>0) && tries>0) {
            imageFile = new File(directory, fileName);
            tries--;
        }
        return imageFile;
    }
    public static boolean directoryIsInvalid(String localDirectory) {
        return (TextUtils.isEmpty((localDirectory)) || localDirectory.contains("//"));
    }


    //UI utilities
    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager =(InputMethodManager) activity.getBaseContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null && activity.getCurrentFocus() != null) {
            inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
        }
    }
    public static int getLinearRecyclerViewPosition(RecyclerView recyclerView) {
        LinearLayoutManager layoutManager = ((LinearLayoutManager) recyclerView.getLayoutManager());
        return layoutManager.findFirstVisibleItemPosition();
    }
    public static int getSpinnerPositionFromText(Spinner spinnerAdapter, String userSelection) {

        int index = 0;
        for (int i=0;i<spinnerAdapter.getCount();i++){
            if (spinnerAdapter.getItemAtPosition(i).equals(userSelection)){
                index = i;
                break;
            }
        }
        return index;
    }
    public static void showSignInScreen(Activity activity) {

        Utilities.setAppPreferenceUserHasNotRefusedSignIn(activity, false);
        FirebaseAuth auth = FirebaseAuth.getInstance();
        //List<AuthUI.IdpConfig> providers = Arrays.asList(
        //        new AuthUI.IdpConfig.EmailBuilder().build(),
        //        new AuthUI.IdpConfig.GoogleBuilder().build());

        List<AuthUI.IdpConfig> providers = Collections.singletonList(new AuthUI.IdpConfig.EmailBuilder().build());

        activity.startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .build(),
                Utilities.FIREBASE_SIGN_IN_KEY);
    }
    public static void updateSignInMenuItem(Menu menu, Context context, boolean signedIn) {
        if (signedIn) {
            menu.findItem(R.id.action_signin).setTitle(context.getString(R.string.sign_out));
        }
        else {
            menu.findItem(R.id.action_signin).setTitle(context.getString(R.string.sign_in));
        }

    }


    //Image utilities
    public static void loadGenericAppImageIntoImageView(Context context, ImageView image) {

        Uri imageUri = Uri.fromFile(new File("//android_asset/splashscreen_intro_image.jpg"));
        Picasso.with(context)
                .load(imageUri)
                .placeholder(image.getDrawable()) //inspired by: https://github.com/square/picasso/issues/257
                //.error(R.drawable.ic_image_not_available)
                .noFade()
                .into(image);
    }
    public static boolean shrinkImageWithUri(Context context, Uri uri, int width, int height){

        if (uri==null) return false;

        //inspired by: from: https://stackoverflow.com/questions/16954109/reduce-the-size-of-a-bitmap-to-a-specified-size-in-android

        //If the image is already small, don't change it (file.length()==0 means the image wasn't found)
        File file = new File(uri.getPath());
        while (file.length()/1024 > (long) context.getResources().getInteger(R.integer.max_image_file_size)) {
            BitmapFactory.Options bmpFactoryOptions = new BitmapFactory.Options();
            bmpFactoryOptions.inJustDecodeBounds = false;
            Bitmap bitmap;

            int heightRatio = (int) Math.ceil(bmpFactoryOptions.outHeight / (float) height);
            int widthRatio = (int) Math.ceil(bmpFactoryOptions.outWidth / (float) width);

            if (heightRatio > 1 || widthRatio > 1) {
                if (heightRatio > widthRatio) {
                    bmpFactoryOptions.inSampleSize = heightRatio;
                } else {
                    bmpFactoryOptions.inSampleSize = widthRatio;
                }
            }

            bmpFactoryOptions.inJustDecodeBounds = false;
            bitmap = BitmapFactory.decodeFile(uri.toString(), bmpFactoryOptions);

            if (bitmap==null) {
                //TODO: fix decoding of large images
                Toast.makeText(context, R.string.image_too_large, Toast.LENGTH_SHORT).show();
                return false;
            }

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            byte[] imageInByte = stream.toByteArray();

            //this gives the size of the compressed image in kb
            long lengthbmp = imageInByte.length / 1024;

            try {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, new FileOutputStream(uri.toString()));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            height = (int) Math.ceil(height * 0.75);
            height = (int) Math.ceil(height * 0.75);
            file = new File(uri.getPath());
        }
        return true;

    }
    public static List<Uri> getExistingImageUriListForObject(Context context, Object object, boolean skipMainImage) {

        String directory = getImagesDirectoryForObject(context, object);
        List<Uri> uris = new ArrayList<>();
        if(directoryIsInvalid(directory)) return uris;

        File imageFile;

        if (!skipMainImage) {
            imageFile = getFileWithTrials(directory, "mainImage.jpg");
            if (imageFile.exists() && imageFile.length()>0) {
                uris.add(Uri.fromFile(imageFile));
            }
        }

        imageFile = getFileWithTrials(directory, "image1.jpg");
        if (imageFile.exists() && imageFile.length()>0) uris.add(Uri.fromFile(imageFile));

        imageFile = getFileWithTrials(directory, "image2.jpg");
        if (imageFile.exists() && imageFile.length()>0) uris.add(Uri.fromFile(imageFile));

        imageFile = getFileWithTrials(directory, "image3.jpg");
        if (imageFile.exists() && imageFile.length()>0) uris.add(Uri.fromFile(imageFile));

        imageFile = getFileWithTrials(directory, "image4.jpg");
        if (imageFile.exists() && imageFile.length()>0) uris.add(Uri.fromFile(imageFile));

        imageFile = getFileWithTrials(directory, "image5.jpg");
        if (imageFile.exists() && imageFile.length()>0) uris.add(Uri.fromFile(imageFile));

        return uris;
    }
    public static List<Uri> getLocalImageUriList(Context context, Object object) {

        String directory = getImagesDirectoryForObject(context, object);
        List<Uri> uris = new ArrayList<>();
        for (int i=0; i<6; i++) uris.add(null);
        if(directoryIsInvalid(directory)) return uris;

        File imageFile;

        imageFile = getFileWithTrials(directory, "mainImage.jpg");
        if (imageFile.exists() && imageFile.length()>0) uris.set(0, Uri.fromFile(imageFile));

        imageFile = getFileWithTrials(directory, "image1.jpg");
        if (imageFile.exists() && imageFile.length()>0) uris.set(1, Uri.fromFile(imageFile));

        imageFile = getFileWithTrials(directory, "image2.jpg");
        if (imageFile.exists() && imageFile.length()>0) uris.set(2, Uri.fromFile(imageFile));

        imageFile = getFileWithTrials(directory, "image3.jpg");
        if (imageFile.exists() && imageFile.length()>0) uris.set(3, Uri.fromFile(imageFile));

        imageFile = getFileWithTrials(directory, "image4.jpg");
        if (imageFile.exists() && imageFile.length()>0) uris.set(4, Uri.fromFile(imageFile));

        imageFile = getFileWithTrials(directory, "image5.jpg");
        if (imageFile.exists() && imageFile.length()>0) uris.set(5, Uri.fromFile(imageFile));

        return uris;
    }
    public static String getNameOfFirstAvailableImageInImagesList(Context context, Object object) {

        String directory = getImagesDirectoryForObject(context, object);
        if(directoryIsInvalid(directory)) return "";

        File imagesDir = new File(directory);
        if (!imagesDir.exists()) imagesDir.mkdirs();

        File image1File = new File(directory, "image1.jpg");
        long length = image1File.length();
        boolean exists = image1File.exists();
        if (!exists || length==0) return "image1";

        File image2File = new File(directory, "image2.jpg");
        length = image2File.length();
        exists = image2File.exists();
        if (!exists || length==0) return "image2";

        File image3File = new File(directory, "image3.jpg");
        length = image3File.length();
        exists = image3File.exists();
        if (!exists || length==0) return "image3";

        File image4File = new File(directory, "image4.jpg");
        length = image4File.length();
        exists = image4File.exists();
        if (!exists || length==0) return "image4";

        File image5File = new File(directory, "image5.jpg");
        length = image5File.length();
        exists = image5File.exists();
        if (!exists || length==0) return "image5";

        return "image1";
    }
    public static Uri getLocalImageUriForObject(Context context, Object object, String imageName) {

        String imageDirectory = getImagesDirectoryForObject(context, object);
        if(directoryIsInvalid(imageDirectory)) return null;

        return Utilities.getImageUriWithPath(imageDirectory,imageName);
    }
    private static Uri getImageUriWithPath(String directory, String imageName) {

        if (directoryIsInvalid(directory)) return null;

        File imagesDir = new File(directory);
        if (!imagesDir.exists()) imagesDir.mkdirs();

        try {
            File imageFile = getFileWithTrials(directory, imageName + ".jpg");
            long length = imageFile.length();
            boolean exists = imageFile.exists();
            if (exists && length > 0) {
                return Uri.fromFile(imageFile);
            } else return null;
        }
        catch (Exception e) {
            //e.printStackTrace();
        }
        return null;
    }
    public static Uri getImageUriForObjectWithFileProvider(Context context, Object object, String imageName) {

        //Inspired by: https://inthecheesefactory.com/blog/how-to-share-access-to-file-with-fileprovider-on-android-nougat/en
        //Note that in contrast to the above tutorial, I use the internal app files directory and changed provider_paths.xml accordingly
        String directory = getImagesDirectoryForObject(context, object);
        File imagesDir = new File(directory);
        if (!imagesDir.exists()) imagesDir.mkdirs();
        File imageFile = new File(directory, imageName+".jpg");

        return FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", imageFile);
    }
    public static void deleteAllLocalObjectImages(Context context, Object object) {
        deleteFileAtUri(getLocalImageUriForObject(context, object, "mainImage"));
        deleteFileAtUri(getLocalImageUriForObject(context, object, "image1"));
        deleteFileAtUri(getLocalImageUriForObject(context, object, "image2"));
        deleteFileAtUri(getLocalImageUriForObject(context, object, "image3"));
        deleteFileAtUri(getLocalImageUriForObject(context, object, "image4"));
        deleteFileAtUri(getLocalImageUriForObject(context, object, "image5"));
    }
    public static boolean imageNameIsInvalid(String imageName) {

        if (TextUtils.isEmpty(imageName)
                || !(imageName.equals("mainImage")
                || imageName.equals("image1")
                || imageName.equals("image2")
                || imageName.equals("image3")
                || imageName.equals("image4")
                || imageName.equals("image5"))){
            Log.i(DEBUG_TAG, "Invalid filename for image in FirebaseDao storage method.");
            return true;
        }
        return false;
    }
    public static void displayTempImageInImageView(Context context, String imageName, ImageView imageView) {

        String localDirectory = getTempImagesDirectory(context);
        if (directoryIsInvalid(localDirectory)) {
            Log.i(DEBUG_TAG, "Tried to access an invalid directory, aborting.");
            return;
        }
        Uri localImageUri = Utilities.getImageUriWithPath(localDirectory, imageName);
        displayUriInImageView(context, localImageUri, imageView);
    }
    public static void displayObjectImageInImageView(Context context, Object object, String imageName, ImageView imageView) {

        String localDirectory = getImagesDirectoryForObject(context, object);
        if (directoryIsInvalid(localDirectory)) {
            Log.i(DEBUG_TAG, "Tried to access an invalid directory, aborting.");
            return;
        }
        Uri localImageUri = Utilities.getImageUriWithPath(localDirectory, imageName);
        displayUriInImageView(context, localImageUri, imageView);
    }
    public static void displayUriInImageView(Context context, Uri uri, ImageView imageView) {
        if (uri!=null) {
            Picasso.with(context)
                    .load(uri.toString())
                    .placeholder(imageView.getDrawable()) //inspired by: https://github.com/square/picasso/issues/257
                    //.error(R.drawable.ic_image_not_available)
                    .memoryPolicy(MemoryPolicy.NO_CACHE) //Prevents picasso from thinking that older images are valid, and forces it to load the new image
                    .noFade()
                    .into(imageView);
        }
        else {
            loadGenericAppImageIntoImageView(context, imageView);
        }
    }
    public static String getImageNameFromUri(String uriString) {
        if (uriString.contains("mainImage")) return "mainImage";
        if (uriString.contains("image1")) return "image1";
        if (uriString.contains("image2")) return "image2";
        if (uriString.contains("image3")) return "image3";
        if (uriString.contains("image4")) return "image4";
        if (uriString.contains("image5")) return "image5";
        else return "mainImage";
    }
    public static Uri[] registerAndDisplayTempImage(Context context, Uri tempImageUri,
                                                    Uri[] tempImageUris, String imageName, Object object,
                                                    ImageView imageViewMain, ImagesRecycleViewAdapter imagesRecycleViewAdapter) {

        if (imageName.equals("mainImage")) {
            tempImageUris[0] = tempImageUri;
            Utilities.displayTempImageInImageView(context, "mainImage", imageViewMain);
        }
        else {
            List<Uri> uris = Utilities.getLocalImageUriList(context, object);
            switch (imageName) {
                case "image1":
                    tempImageUris[1] = tempImageUri;
                    uris.set(1, tempImageUri);
                    break;
                case "image2":
                    tempImageUris[2] = tempImageUri;
                    uris.set(2, tempImageUri);
                    break;
                case "image3":
                    tempImageUris[3] = tempImageUri;
                    uris.set(3, tempImageUri);
                    break;
                case "image4":
                    tempImageUris[4] = tempImageUri;
                    uris.set(4, tempImageUri);
                    break;
                case "image5":
                    tempImageUris[5] = tempImageUri;
                    uris.set(5, tempImageUri);
                    break;
            }

            List<Uri> displayedImageUris = new ArrayList<>();
            for (int i=1; i<uris.size(); i++) {
                Uri uri = uris.get(i);
                if (uri!=null) displayedImageUris.add(uri);
            }
            imagesRecycleViewAdapter.setContents(displayedImageUris);
        }

        return tempImageUris;
    }


    //Location utlities
    public static Address getAddressObjectFromAddressString(Context context, String location) {

        //inspired by: https://stackoverflow.com/questions/20166328/how-to-get-longitude-latitude-from-the-city-name-android-code

        List<Address> addresses = new ArrayList<>();
        if(Geocoder.isPresent() && !TextUtils.isEmpty(location)){
            try {
                Geocoder gc = new Geocoder(context);
                addresses = gc.getFromLocationName(location, 5); // get the found Address Objects

//                List<LatLng> latLong = new ArrayList<>(addresses.size()); // A list to save the coordinates if they are available
//                for (Address address : addresses){
//                    if(address.hasLatitude() && address.hasLongitude()){
//                        latLong.add(new LatLng(address.getLatitude(), address.getLongitude()));
//                    }
//                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (addresses.size()>0) return addresses.get(0);
        else return null;
    }
    public static String[] getExactAddressFromGeoCoordinates(Context context, double latitude, double longitude) {

        if (context==null || latitude==0.0 && longitude==0.0) return new String[]{ null, null, null, null };

        Geocoder gcd = new Geocoder(context, Locale.getDefault());
        List<Address> addresses;
        try {
            addresses = gcd.getFromLocation(latitude, longitude, 1);
            if (addresses.size() > 0) {
                String address = addresses.get(0).getAddressLine(0);
                String street = (Arrays.asList(address.split(","))).get(0).trim();
                String city = addresses.get(0).getLocality();
                String state = addresses.get(0).getAdminArea();
                String country = addresses.get(0).getCountryName();
                String[] fullAddress = new String[] { street , city , state, country };
                return fullAddress;
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    public static String getAddressStringFromComponents(String stN, String st, String ct, String se, String cn) {
        StringBuilder builder = new StringBuilder("");
        if (!TextUtils.isEmpty(stN)) {
            builder.append(stN);
            builder.append(" ");
        }
        if (!TextUtils.isEmpty(st)) {
            builder.append(st);
            if (!TextUtils.isEmpty(ct)) builder.append(", ");
        }
        if (!TextUtils.isEmpty(ct)) {
            builder.append(ct);
            if (!TextUtils.isEmpty(cn)) builder.append(", ");
        }
        if (!TextUtils.isEmpty(se)) {
            builder.append(se);
            if (!TextUtils.isEmpty(se)) builder.append(", ");
        }
        if (!TextUtils.isEmpty(cn)) {
            builder.append(cn);
        }
        return builder.toString();
    }
    public static double[] getGeoCoordinatesFromAddressString(Context context, String address) {
        Geocoder geocoder = new Geocoder(context);
        List<Address> addresses;
        try {
            addresses = geocoder.getFromLocationName(address, 1);
            if(addresses.size() > 0) {
                double latitude= addresses.get(0).getLatitude();
                double longitude= addresses.get(0).getLongitude();
                return new double[]{latitude, longitude};
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return null;
    }
    public static boolean checkLocationPermission(Context context) {
        if (context!=null && ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        else return false;
    }


    //Database utilities
    public static FirebaseFirestore getDatabase() {
        //inspired by: https://github.com/firebase/quickstart-android/issues/15
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .setTimestampsInSnapshotsEnabled(true)
                .build();
        db.setFirestoreSettings(settings);
        return db;
    }
    public static String cleanIdentifierForFirebase(String string) {
        if (TextUtils.isEmpty(string)) return "";
        string = string.replaceAll("\\.","*");
        string = string.replaceAll("#","*");
        string = string.replaceAll("\\$","*");
        string = string.replaceAll("\\[","*");
        string = string.replaceAll("]","*");
        //string = string.replaceAll("\\{","*");
        //string = string.replaceAll("}","*");
        return string;
    }
    public static void updateFirebaseUserName(final Context context, final FirebaseUser user, String password, final String newInfo) {

        if (user.getEmail()==null) {
            Toast.makeText(context, R.string.error_accessing_user_info, Toast.LENGTH_SHORT).show();
            return;
        }

        AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), password);
        user.reauthenticate(credential)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(DEBUG_TAG, "User re-authenticated.");

                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(newInfo)
                                    //.setPhotoUri(Uri.parse("https://example.com/jane-q-user/profile.jpg"))
                                    .build();

                            user.updateProfile(profileUpdates)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(context, R.string.successfully_updated_name, Toast.LENGTH_SHORT).show();
                                                Log.d(DEBUG_TAG, "User profile updated.");
                                            }
                                            else {
                                                Toast.makeText(context, R.string.failed_to_update_name, Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        }
                        else {
                            Toast.makeText(context, R.string.authentication_failed, Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }
    public static void updateFirebaseUserPassword(final Context context, final FirebaseUser user, String password, final String newInfo) {

        if (user.getEmail()==null) {
            Toast.makeText(context, R.string.error_accessing_user_info, Toast.LENGTH_SHORT).show();
            return;
        }

        AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), password);
        user.reauthenticate(credential)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(DEBUG_TAG, "User re-authenticated.");
                            user.updatePassword(newInfo)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(context, R.string.successfully_updated_password, Toast.LENGTH_SHORT).show();
                                                Log.d(DEBUG_TAG, "User password updated.");
                                            }
                                            else {
                                                Toast.makeText(context, R.string.failed_to_update_password, Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        }
                        else {
                            Toast.makeText(context, R.string.authentication_failed, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
    public static void updateFirebaseUserEmail(final Context context, final FirebaseUser user, String password, final String newInfo) {

        if (user.getEmail()==null) {
            Toast.makeText(context, R.string.error_accessing_user_info, Toast.LENGTH_SHORT).show();
            return;
        }

        AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), password);
        user.reauthenticate(credential)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(DEBUG_TAG, "User re-authenticated.");
                            user.updateEmail(newInfo)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(context, R.string.successfully_updated_email, Toast.LENGTH_SHORT).show();
                                                Log.d(DEBUG_TAG, "User email updated.");
                                            }
                                            else {
                                                Toast.makeText(context, R.string.failed_to_update_email, Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        }
                        else {
                            Toast.makeText(context, R.string.authentication_failed, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
    public static List<QueryCondition> getQueryConditionsForSingleObjectSearchByOwnerId(Object object) {

        List<QueryCondition> queryConditions = new ArrayList<>();
        QueryCondition queryCondition;
        if (object instanceof User) {
            User user = (User) object;
            queryCondition = new QueryCondition("equalsString", "oI", user.getOI(), true, 0);
            queryConditions.add(queryCondition);
        }
        else if (object instanceof Family) {
            Family family = (Family) object;
            queryCondition = new QueryCondition("equalsString", "oI", family.getOI(), true, 0);
            queryConditions.add(queryCondition);
        }
        else if (object instanceof Foundation) {
            Foundation foundation = (Foundation) object;
            queryCondition = new QueryCondition("equalsString", "oI", foundation.getOI(), true, 0);
            queryConditions.add(queryCondition);
        }
        else {
            Log.i(DEBUG_TAG, "Warning! Asked for non-unique object (Pet/MapMarker/other) in query method " +
                    "that implements conditions for unique user-associated object (User/Family/Foundation)");
        }

        queryCondition = new QueryCondition("limit", "", "", true, 10);
        queryConditions.add(queryCondition);

        return queryConditions;
    }
    public static List<QueryCondition> getQueryConditionsForMultipleObjectSearchByOwnerId(Object object, int limit) {

        List<QueryCondition> queryConditions = new ArrayList<>();
        QueryCondition queryCondition;
        if (object instanceof User) {
            User user = (User) object;
            queryCondition = new QueryCondition("equalsString", "oI", user.getOI(), true, 0);
            queryConditions.add(queryCondition);
        }
        else if (object instanceof Pet) {
            Pet pet = (Pet) object;
            queryCondition = new QueryCondition("equalsString", "oI", pet.getOI(), true, 0);
            queryConditions.add(queryCondition);
        }
        else if (object instanceof Family) {
            Family family = (Family) object;
            queryCondition = new QueryCondition("equalsString", "oI", family.getOI(), true, 0);
            queryConditions.add(queryCondition);
        }
        else if (object instanceof Foundation) {
            Foundation foundation = (Foundation) object;
            queryCondition = new QueryCondition("equalsString", "oI", foundation.getOI(), true, 0);
            queryConditions.add(queryCondition);
        }
        else if (object instanceof MapMarker) {
            MapMarker mapMarker = (MapMarker) object;
            queryCondition = new QueryCondition("equalsString", "oI", mapMarker.getOI(), true, 0);
            queryConditions.add(queryCondition);
        }

        queryCondition = new QueryCondition("limit", "", "", true, limit);
        queryConditions.add(queryCondition);

        return queryConditions;
    }
    public static List<QueryCondition> getQueryConditionsForSingleObjectSearchByUniqueId(Object object) {

        List<QueryCondition> queryConditions = new ArrayList<>();
        QueryCondition queryCondition;
        if (object instanceof User) {
            User user = (User) object;
            queryCondition = new QueryCondition("equalsString", "uI", user.getUI(), true, 0);
            queryConditions.add(queryCondition);
        }
        else if (object instanceof Pet) {
            Pet pet = (Pet) object;
            queryCondition = new QueryCondition("equalsString", "uI", pet.getUI(), true, 0);
            queryConditions.add(queryCondition);
        }
        else if (object instanceof Family) {
            Family family = (Family) object;
            queryCondition = new QueryCondition("equalsString", "uI", family.getUI(), true, 0);
            queryConditions.add(queryCondition);
        }
        else if (object instanceof Foundation) {
            Foundation foundation = (Foundation) object;
            queryCondition = new QueryCondition("equalsString", "uI", foundation.getUI(), true, 0);
            queryConditions.add(queryCondition);
        }
        else if (object instanceof MapMarker) {
            MapMarker mapMarker = (MapMarker) object;
            queryCondition = new QueryCondition("equalsString", "uI", mapMarker.getUI(), true, 0);
            queryConditions.add(queryCondition);
        }

        queryCondition = new QueryCondition("limit", "", "", true, 10);
        queryConditions.add(queryCondition);

        return queryConditions;
    }
    public static void synchronizeImageOnAllDevices(Context context, Object object, FirebaseDao firebaseDao, String imageName, Uri downloadedImageUri, boolean imageWasDownloaded) {

        String localDirectory = getImagesDirectoryForObject(context, object);
        if(directoryIsInvalid(localDirectory)) return;

        Uri localImageUri = Utilities.getImageUriWithPath(localDirectory, imageName);

        if ((imageWasDownloaded || localImageUri == null) && downloadedImageUri != null) {
            //The image was downloaded only if it was newer than the local image
            Utilities.updateLocalImageFromUri(context, downloadedImageUri, localDirectory, imageName);
        }
        else if (localImageUri != null){
            firebaseDao.putImageInFirebaseStorage(object, localImageUri, imageName);
        }
    }
    public static void updateImageOnLocalDevice(Context context, Object object, FirebaseDao firebaseDao, String imageName, Uri downloadedImageUri) {

        String localDirectory = getImagesDirectoryForObject(context, object);
        if(directoryIsInvalid(localDirectory)) return;

        //The image was downloaded only if it was newer than the local image (If it wasn't downloaded, the downloadedImageUri is the same as the local image Uri)
        Uri localImageUri = Utilities.getImageUriWithPath(localDirectory, imageName);

        if (downloadedImageUri != null) {
            if (localImageUri == null) {
                Utilities.updateLocalImageFromUri(context, downloadedImageUri, localDirectory, imageName);
            }
            else {
                String localUriPath = localImageUri.getPath();
                String downloadedUriPath = downloadedImageUri.getPath();

                //If the downloaded image is newer, then update the image in the local directory
                if (!downloadedUriPath.equals(localUriPath)) {
                    Utilities.updateLocalImageFromUri(context, downloadedImageUri, localDirectory, imageName);
                }

                //If the local image is newer, then do nothing
            }
        }
    }
    public static Uri updateLocalImageFromUri(Context context, Uri originalImageUri, Object object, String imageName) {

        String directory = getImagesDirectoryForObject(context, object);
        if(directoryIsInvalid(directory)) return null;

        Uri copiedImageUri = moveFile(originalImageUri, directory, imageName);
        return copiedImageUri;
    }
    public static Uri updateTempObjectImage(Context context, Uri originalImageUri, String imageName) {

        String directory = getTempImagesDirectory(context);
        if(directoryIsInvalid(directory)) return null;

        Uri copiedImageUri = moveFile(originalImageUri, directory, imageName);
        return copiedImageUri;
    }
    public static boolean checkIfImagesReadyForDisplay(boolean[] mImagesReady, String imageName) {
        switch (imageName) {
            case "mainImage": mImagesReady[0] = true; break;
            case "image1": mImagesReady[1] = true; break;
            case "image2": mImagesReady[2] = true; break;
            case "image3": mImagesReady[3] = true; break;
            case "image4": mImagesReady[4] = true; break;
            case "image5": mImagesReady[5] = true; break;
        }
        boolean allImagesReady = true;
        for (boolean isReady : mImagesReady) {
            if (!isReady) { allImagesReady = false; break; }
        }
        return allImagesReady;
    }
    public static void displayAllAvailableImages(Context context, Object object, ImageView mImageViewMain, ImagesRecycleViewAdapter mFamilyImagesRecycleViewAdapter) {
        Utilities.displayObjectImageInImageView(context, object, "mainImage", mImageViewMain);
        List<Uri> uris = Utilities.getExistingImageUriListForObject(context, object, true);
        mFamilyImagesRecycleViewAdapter.setContents(uris);
    }
    public static boolean allUrisNull(Uri[] uris) {
        boolean noMoreTempImages = true;
        for (Uri uri : uris) {
            if (uri!=null) noMoreTempImages = false;
        }
        return noMoreTempImages;
    }


    //Internet utilities
    public static boolean internetIsAvailable(Context context) {
        //adapted from https://stackoverflow.com/questions/43315393/android-internet-connection-timeout
        final ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connMgr == null) return false;

        NetworkInfo activeNetworkInfo = connMgr.getActiveNetworkInfo();

        return activeNetworkInfo != null;
    }
    public static void goToWebLink(Context context, String url) {

        if (context==null) return;

        //Prepare the website
        if (!TextUtils.isEmpty(url)) {
            if (url.length()>8 && url.substring(0,8).equals("https://")
                    || (url.length()>7 && url.substring(0,7).equals("http://"))) {
                //Website is valid, do nothing.
            }
            else if (url.length()>6 && url.substring(0,6).equals("ftp://")) {
                Toast.makeText(context, R.string.cannot_open_ftp, Toast.LENGTH_SHORT).show();
            }
            else if (url.length()>7 && url.substring(0,7).equals("smtp://")) {
                Toast.makeText(context, R.string.cannot_open_smtp, Toast.LENGTH_SHORT).show();
            }
            else {
                url = "http://" + url;
            }
        }

        //Try accessing the website. If the website is still not formatted correctly (ie. gibberish), then fail silently
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            context.startActivity(intent);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }


    //Preference utilities
    public static void setAppPreferenceUserHasNotRefusedSignIn(Context context, boolean requestedSignInState) {
        if (context != null) {
            SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.app_preferences), Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putBoolean(context.getString(R.string.app_preference_sign_in_state), requestedSignInState);
            editor.apply();
        }
    }
    public static boolean getAppPreferenceUserHasNotRefusedSignIn(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.app_preferences), Context.MODE_PRIVATE);
        return sharedPref.getBoolean(context.getString(R.string.app_preference_sign_in_state), true);
    }
    public static void setAppPreferenceUserLongitude(Context context, double longitude) {
        if (context != null) {
            SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.app_preferences), Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString(context.getString(R.string.user_longitude), Double.toString(longitude));
            editor.apply();
        }
    }
    public static Double getAppPreferenceUserLongitude(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.app_preferences), Context.MODE_PRIVATE);
        return Double.parseDouble(sharedPref.getString(context.getString(R.string.user_longitude), "0.0"));
    }
    public static void setAppPreferenceUserLatitude(Context context, double latitude) {
        if (context != null) {
            SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.app_preferences), Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString(context.getString(R.string.user_latitude), Double.toString(latitude));
            editor.apply();
        }
    }
    public static Double getAppPreferenceUserLatitude(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.app_preferences), Context.MODE_PRIVATE);
        return Double.parseDouble(sharedPref.getString(context.getString(R.string.user_latitude), "0.0"));
    }
    public static void setAppPreferenceFirstTimeUsingApp(Context context, boolean firstTimeFlag) {
        if (context != null) {
            SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.app_preferences), Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putBoolean(context.getString(R.string.first_time_using_app), firstTimeFlag);
            editor.apply();
        }
    }
    public static boolean getAppPreferenceFirstTimeUsingApp(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.app_preferences), Context.MODE_PRIVATE);
        return sharedPref.getBoolean(context.getString(R.string.first_time_using_app), true);
    }
    public static void setAppPreferenceProfileImagesRvPosition(Context context, int position) {
        if (context != null) {
            SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.app_preferences), Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putInt(context.getString(R.string.saved_profile_images_rv_position), position);
            editor.apply();
        }
    }
    public static int getAppPreferenceProfileImagesRvPosition(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.app_preferences), Context.MODE_PRIVATE);
        return sharedPref.getInt(context.getString(R.string.saved_profile_images_rv_position), 0);
    }

}
