package com.petitur.resources;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.petitur.BuildConfig;
import com.petitur.R;
import com.petitur.data.*;
import com.petitur.ui.EditProfileActivity;
import com.petitur.ui.PreferencesActivity;
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
import java.util.Collections;
import java.util.List;

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
    public static void startEditProfileActtivity(Activity activity) {
        Intent intent = new Intent(activity.getApplicationContext(), EditProfileActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        activity.startActivity(intent);
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


    //Database utilities
    public static FirebaseFirestore getDatabase() {
        //inspired by: https://github.com/firebase/quickstart-android/issues/15
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
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
