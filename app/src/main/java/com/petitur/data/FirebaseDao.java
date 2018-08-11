package com.petitur.data;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.petitur.R;
import com.petitur.resources.Utilities;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirebaseDao {

    //See this link to understand why a single value event listener is not used here:
    //https://stackoverflow.com/questions/37185418/android-firebase-complex-or-not-query-issue-with-unique-ids#51565273

    private static final String DEBUG_TAG = "Petitur DB Debug";
    private static final int FIREBASE_IMAGE_DOWNLOAD_NOTIFICATION_ID = 4567;
    private static final int PROGRESS_MAX = 100;
    private final Context mContext;
    private final FirebaseFirestore mFirebaseDb;
    private NotificationCompat.Builder mNotificationBuilder;
    private NotificationManagerCompat mNotificationManager;


    public FirebaseDao(Context context, FirebaseOperationsHandler listener) {
        this.mContext = context;
        this.mOnOperationPerformedHandler = listener;
        mFirebaseDb = FirebaseFirestore.getInstance();
    }


    //Firebase Firestore CRUD methods
    public String createObject(Object object) {

        //Warning: must check that if object does not exist yet by using the getObject method, otherwise an identical object will be created with a different ID

        String path;
        if (objectIsInvalid(object)) return "";
        else path = getCollectionPath(object);

        if (path.equals("")) return "";

        CollectionReference collectionReference = mFirebaseDb.collection(path);
        DocumentReference documentReference = collectionReference.document();
        documentReference.set(object)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(DEBUG_TAG, "DocumentSnapshot successfully written!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(DEBUG_TAG, "Error writing document", e);
                    }
                });

        return documentReference.getId();
    }
    public void updateObject(Object object) {

        //Warning: must check if the object does not exist yet by using the getObject method, otherwise an identical object will be created with a different ID

        String path;
        if (objectIsInvalid(object)) return;
        else path = getDocumentPathById(object);

        if (path.equals("")) return;

        mFirebaseDb.document(path)
                .set(object)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(DEBUG_TAG, "DocumentSnapshot successfully written!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(DEBUG_TAG, "Error writing document", e);
                    }
                });
    }
    public void updateObjectKeyValuePair(Object object, String key, Object value) {

        String path;
        if (objectIsInvalid(object)) return;
        else path = getDocumentPathById(object);

        Map<String, Object> docData = new HashMap<>();
        docData.put(key, value);

        if (path.equals("")) return;

        mFirebaseDb.document(path)
                .set(docData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(DEBUG_TAG, "DocumentSnapshot successfully written!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(DEBUG_TAG, "Error writing document", e);
                    }
                });
    }
    public void requestObjectsWithConditions(Object object, List<QueryCondition> conditions) {

        //Warning: it is recommended to set a QueryCondition limiting the number of search results

        if (object instanceof User) {
            CollectionReference collectionReference = mFirebaseDb.collection("users");
            collectionReference = setConditionsOnCollectionQuery(collectionReference, conditions);
            collectionReference.get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    Log.d(DEBUG_TAG, document.getId() + " => " + document.getData());
                                }
                            } else {
                                Log.w(DEBUG_TAG, "Error getting documents.", task.getException());
                            }
                        }
                    });
        }
        else if (object instanceof Pet) {
            CollectionReference collectionReference = mFirebaseDb.collection("pets");
            collectionReference = setConditionsOnCollectionQuery(collectionReference, conditions);
            collectionReference.get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    Log.d(DEBUG_TAG, document.getId() + " => " + document.getData());
                                }
                            } else {
                                Log.w(DEBUG_TAG, "Error getting documents.", task.getException());
                            }
                        }
                    });
        }
        else if (object instanceof Family) {
            CollectionReference collectionReference = mFirebaseDb.collection("families");
            collectionReference = setConditionsOnCollectionQuery(collectionReference, conditions);
            collectionReference.get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    Log.d(DEBUG_TAG, document.getId() + " => " + document.getData());
                                }
                            } else {
                                Log.w(DEBUG_TAG, "Error getting documents.", task.getException());
                            }
                        }
                    });
        }
        else if (object instanceof Foundation) {
            CollectionReference collectionReference = mFirebaseDb.collection("foundations");
            collectionReference = setConditionsOnCollectionQuery(collectionReference, conditions);
            collectionReference.get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    Log.d(DEBUG_TAG, document.getId() + " => " + document.getData());
                                }
                            } else {
                                Log.w(DEBUG_TAG, "Error getting documents.", task.getException());
                            }
                        }
                    });
        }
        else if (object instanceof MapMarker) {
            CollectionReference collectionReference = mFirebaseDb.collection("mapMarkers");
            collectionReference = setConditionsOnCollectionQuery(collectionReference, conditions);
            collectionReference.get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    Log.d(DEBUG_TAG, document.getId() + " => " + document.getData());
                                }
                            } else {
                                Log.w(DEBUG_TAG, "Error getting documents.", task.getException());
                            }
                        }
                    });
        }
    }
    public void requestObjectWithId(final Object object) {

        String path;
        if (objectIsInvalid(object)) return;
        else path = getDocumentPathById(object);

        if (path.equals("")) return;

        mFirebaseDb.document(path)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        sendObjectListToInterface(documentSnapshot, object);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(DEBUG_TAG, "Error getting document", e);
                    }
                });
    }
    public void deleteObject(Object object) {

        String path;
        if (objectIsInvalid(object)) return;
        else path = getDocumentPathById(object);

        if (path.equals("")) return;

        mFirebaseDb.document(path)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(DEBUG_TAG, "DocumentSnapshot successfully deleted!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(DEBUG_TAG, "Error deleting document", e);
                    }
                });
    }


    //Firebase Firestore helper methods (prevent code repetitions in the CRUD methods)
    private String getDocumentPathById(Object object) {

        String path = "";

        if (object instanceof User) {
            User user = (User) object;
            path = "users/" + user.getUI();
        }
        else if (object instanceof Pet) {
            Pet pet = (Pet) object;
            path = "pets/" + pet.getUI();
        }
        else if (object instanceof Family) {
            Family family = (Family) object;
            path = "families/" + family.getUI();
        }
        else if (object instanceof Foundation) {
            Foundation foundation = (Foundation) object;
            path = "foundations/" + foundation.getUI();
        }
        else if (object instanceof MapMarker) {
            MapMarker mapMarker = (MapMarker) object;
            path = "mapMarkers/" + mapMarker.getUI();
        }

        return path;
    }
    private String getCollectionPath(Object object) {

        String path = "";

        if (object instanceof User) {
            path = "users";
        }
        else if (object instanceof Pet) {
            path = "pets";
        }
        else if (object instanceof Family) {
            path = "families";
        }
        else if (object instanceof Foundation) {
            path = "foundations";
        }
        else if (object instanceof MapMarker) {
            path = "mapMarkers";
        }

        return path;
    }
    private CollectionReference setConditionsOnCollectionQuery(CollectionReference collectionReference, List<QueryCondition> conditions) {
        if (conditions == null) return collectionReference;

        for (QueryCondition condition : conditions) {
            if (condition.getOperation().equals("equalsString")) {
                collectionReference.whereEqualTo(condition.getKey(), condition.getValueString());
            }
            else if (condition.getOperation().equals("equalsBoolean")) {
                collectionReference.whereEqualTo(condition.getKey(), condition.getValueBoolean());
            }
            else if (condition.getOperation().equals("lessThanString")) {
                collectionReference.whereLessThan(condition.getKey(), condition.getValueString());
            }
            else if (condition.getOperation().equals("lessThanInteger")) {
                collectionReference.whereLessThan(condition.getKey(), condition.getValueInteger());
            }
            else if (condition.getOperation().equals("greaterThanOrEqualToString")) {
                collectionReference.whereGreaterThanOrEqualTo(condition.getKey(), condition.getValueString());
            }
            else if (condition.getOperation().equals("greaterThanOrEqualToInteger")) {
                collectionReference.whereGreaterThanOrEqualTo(condition.getKey(), condition.getValueInteger());
            }
            else if (condition.getOperation().equals("orderBy")) {
                if (condition.getValueBoolean()) collectionReference.orderBy(condition.getKey());
                else collectionReference.orderBy(condition.getKey(), Query.Direction.DESCENDING);
            }
            else if (condition.getOperation().equals("limit")) {
                collectionReference.limit(condition.getValueInteger());
            }

            //Note: the following conditions must occur after "orderBy"
            else if (condition.getOperation().equals("startAtString")) {
                collectionReference.startAt(condition.getValueString());
            }
            else if (condition.getOperation().equals("startAfterString")) {
                collectionReference.startAfter(condition.getValueString());
            }
            else if (condition.getOperation().equals("startAtInteger")) {
                collectionReference.startAt(condition.getValueInteger());
            }
            else if (condition.getOperation().equals("startAfterInteger")) {
                collectionReference.startAfter(condition.getValueInteger());
            }
            else if (condition.getOperation().equals("endAtString")) {
                collectionReference.endAt(condition.getValueString());
            }
            else if (condition.getOperation().equals("endBeforeString")) {
                collectionReference.endBefore(condition.getValueString());
            }
            else if (condition.getOperation().equals("endAtInteger")) {
                collectionReference.endAt(condition.getValueInteger());
            }
            else if (condition.getOperation().equals("endBeforeInteger")) {
                collectionReference.endBefore(condition.getValueInteger());
            }
        }
        return collectionReference;
    }
    private boolean objectIsInvalid(Object object) {

        if (object instanceof User) {
            User user = (User) object;
            if (TextUtils.isEmpty(user.getUI())) {
                Log.d(DEBUG_TAG, "Attempted to perform a database action on an invalid user, aborting");
                return true;
            }
            else return false;
        }
        else if (object instanceof Pet) {
            Pet pet = (Pet) object;
            if (TextUtils.isEmpty(pet.getUI())) {
                Log.d(DEBUG_TAG, "Attempted to perform a database action on an invalid pet, aborting");
                return true;
            }
            else return false;
        }
        else if (object instanceof Family) {
            Family family = (Family) object;
            if (TextUtils.isEmpty(family.getUI())) {
                Log.d(DEBUG_TAG, "Attempted to perform a database action on an invalid family, aborting");
                return true;
            }
            else return false;
        }
        else if (object instanceof Foundation) {
            Foundation foundation = (Foundation) object;
            if (TextUtils.isEmpty(foundation.getUI())) {
                Log.d(DEBUG_TAG, "Attempted to perform a database action on an invalid foundation, aborting");
                return true;
            }
            else return false;
        }
        else if (object instanceof MapMarker) {
            MapMarker mapMarker = (MapMarker) object;
            if (TextUtils.isEmpty(mapMarker.getUI())) {
                Log.d(DEBUG_TAG, "Attempted to perform a database action on an invalid mapMarker, aborting");
                return true;
            }
            else return false;
        }
        return true;
    }
    private void sendObjectListToInterface(DocumentSnapshot documentSnapshot, Object object) {

        if (object instanceof User) {
            List<User> users = new ArrayList<>();
            if (documentSnapshot.exists()) {
                User user = documentSnapshot.toObject(User.class);
                users.add(user);
            }
            mOnOperationPerformedHandler.onUserListFound(users);
        }
        else if (object instanceof Pet) {
            List<Pet> pets = new ArrayList<>();
            if (documentSnapshot.exists()) {
                Pet pet = documentSnapshot.toObject(Pet.class);
                pets.add(pet);
            }
            mOnOperationPerformedHandler.onPetListFound(pets);
        }
        else if (object instanceof Family) {
            List<Family> families = new ArrayList<>();
            if (documentSnapshot.exists()) {
                Family family = documentSnapshot.toObject(Family.class);
                families.add(family);
            }
            mOnOperationPerformedHandler.onFamilyListFound(families);
        }
        else if (object instanceof Foundation) {
            List<Foundation> foundations = new ArrayList<>();
            if (documentSnapshot.exists()) {
                Foundation foundation = documentSnapshot.toObject(Foundation.class);
                foundations.add(foundation);
            }
            mOnOperationPerformedHandler.onFoundationListFound(foundations);
        }
        else if (object instanceof MapMarker) {
            List<MapMarker> mapMarkers = new ArrayList<>();
            if (documentSnapshot.exists()) {
                MapMarker mapMarker = documentSnapshot.toObject(MapMarker.class);
                mapMarkers.add(mapMarker);
            }
            mOnOperationPerformedHandler.onMapMarkerListFound(mapMarkers);
        }
    }


    //Firebase Storage methods
    public void putImageInFirebaseStorage(final Object object, Uri localUri, final String imageName) {

        String childPath;
        String folderPath;
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        StorageReference imageRef;

        List<String> uploadTimes;
        if (object instanceof Pet) {
            Pet pet = (Pet) object;
            folderPath = "pets/" + pet.getUI() + "/images";
            uploadTimes = pet.getIUT();
        }
        else if (object instanceof Family) {
            Family family = (Family) object;
            folderPath = "families/" + family.getUI() + "/images";
            uploadTimes = family.getIUT();
        }
        else if (object instanceof Foundation) {
            Foundation foundation = (Foundation) object;
            folderPath = "foundations/" + foundation.getUI() + "/images";
            uploadTimes = foundation.getIUT();
        }
        else return;

        childPath = folderPath + "/" + imageName + ".jpg";
        imageRef = storageRef.child(childPath);

        final List<String> finalUploadTimes = uploadTimes;
        imageRef.putFile(localUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        //StorageMetadata metaData = taskSnapshot.getMetadata();
                        long currentTime= System.currentTimeMillis();
                        if (finalUploadTimes.size()>0) {
                            switch (imageName) {
                                case "mainImage": finalUploadTimes.set(0,String.valueOf(currentTime)); break;
                                case "image1": finalUploadTimes.set(1,String.valueOf(currentTime)); break;
                                case "image2": finalUploadTimes.set(2,String.valueOf(currentTime)); break;
                                case "image3": finalUploadTimes.set(3,String.valueOf(currentTime)); break;
                                case "image4": finalUploadTimes.set(4,String.valueOf(currentTime)); break;
                                case "image5": finalUploadTimes.set(5,String.valueOf(currentTime)); break;
                            }
                            updateObjectKeyValuePair(object, "iut", finalUploadTimes);
                            mOnOperationPerformedHandler.onImageUploaded(finalUploadTimes);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        exception.printStackTrace();
                        //Toast.makeText(mContext, "Failed to upload image, check log.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
    public void getAllObjectImagesFromFirebaseStorage(Object object) {
        getImageFromFirebaseStorage(object, "mainImage");
        getImageFromFirebaseStorage(object, "image1");
        getImageFromFirebaseStorage(object, "image2");
        getImageFromFirebaseStorage(object, "image3");
        getImageFromFirebaseStorage(object, "image4");
        getImageFromFirebaseStorage(object, "image5");
    }
    public void getImageFromFirebaseStorage(Object object, final String imageName) {

        if (Utilities.imageNameIsInvalid(imageName)) return;

        String childPath;
        String folderPath;

        List<String> uploadTimes;
        if (object instanceof Pet) {
            Pet pet = (Pet) object;
            folderPath = "pets/" + pet.getUI() + "/images";
            uploadTimes = pet.getIUT();
        }
        else if (object instanceof Family) {
            Family family = (Family) object;
            folderPath = "families/" + family.getUI() + "/images";
            uploadTimes = family.getIUT();
        }
        else if (object instanceof Foundation) {
            Foundation foundation = (Foundation) object;
            folderPath = "foundations/" + foundation.getUI() + "/images";
            uploadTimes = foundation.getIUT();
        }
        else return;

        childPath = folderPath + "/" + imageName + ".jpg";

        final Uri localImageUri = Utilities.getLocalImageUriForObject(mContext, object, imageName);
        if (uploadTimes==null || uploadTimes.size()==0) {
            sendImageUriToInterface(localImageUri, imageName);
            return;
        }

        //If the image loaded into Firebase is newer than the image saved onto the local device (if it exists), then download it. Otherwise, use the local image.
        String internalStorageDirString = Utilities.getImagesDirectoryForObject(mContext, object);
        if (Utilities.directoryIsInvalid(internalStorageDirString)) {
            Log.i(DEBUG_TAG, "Serious error in getImageFromFirebaseStorage(): invalid images directory: " + internalStorageDirString);
            return;
        }
        File internalStorageDir = new File(internalStorageDirString);
        if (!internalStorageDir.exists()) internalStorageDir.mkdirs();

        File localFile = new File(internalStorageDirString, imageName + ".jpg");
        if (localFile.exists()) {
            if (localImageUri!=null) Log.i(DEBUG_TAG, "Local file does exists: " + localImageUri.toString());

            Date lastModified = new Date(localFile.lastModified());
            long lastModifiedTime = lastModified.getTime();

            long imageUploadTime = 0;

            switch (imageName) {
                case "mainImage": imageUploadTime = !TextUtils.isEmpty(uploadTimes.get(0)) ? Long.parseLong(uploadTimes.get(0)) : 0; break;
                case "image1": imageUploadTime = !TextUtils.isEmpty(uploadTimes.get(1)) ? Long.parseLong(uploadTimes.get(1)) : 0; break;
                case "image2": imageUploadTime = !TextUtils.isEmpty(uploadTimes.get(2)) ? Long.parseLong(uploadTimes.get(2)) : 0; break;
                case "image3": imageUploadTime = !TextUtils.isEmpty(uploadTimes.get(3)) ? Long.parseLong(uploadTimes.get(3)) : 0; break;
                case "image4": imageUploadTime = !TextUtils.isEmpty(uploadTimes.get(4)) ? Long.parseLong(uploadTimes.get(4)) : 0; break;
                case "image5": imageUploadTime = !TextUtils.isEmpty(uploadTimes.get(5)) ? Long.parseLong(uploadTimes.get(5)) : 0; break;
            }

            //If the local file is older than the Firebase file, then download the Firebase file to the cache directory
            if (imageUploadTime!=0 && imageUploadTime > lastModifiedTime && Utilities.internetIsAvailable(mContext)) {
                if (localImageUri!=null) Log.i(DEBUG_TAG, "Local file " + localImageUri.toString() + "with mod time " + lastModifiedTime + " is older than Firebase image with u/l time " + imageUploadTime);
                downloadFromFirebase(internalStorageDir, imageName, childPath, localImageUri);
            }
            else {
                sendImageUriToInterface(localImageUri, imageName);
            }
        }
        else {
            if (localImageUri!=null) Log.i(DEBUG_TAG, "Local file does not exist: " + localImageUri.toString());
            downloadFromFirebase(internalStorageDir, imageName, childPath, localImageUri);
        }

    }
    private void downloadFromFirebase(File internalStorageDir, final String imageName, String childPath, final Uri localImageUri) {

        File cacheDirectory = new File(mContext.getFilesDir().getAbsolutePath() + "/cache");
        if (!cacheDirectory.exists()) cacheDirectory.mkdirs();

        File localFirebaseTempImage = new File(internalStorageDir, imageName + ".jpg");
        final Uri localFirebaseTempImageUri = Uri.fromFile(localFirebaseTempImage);

        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        StorageReference imageRef = storageRef.child(childPath);

        // Issue the initial notification with zero progress
        mNotificationBuilder = new NotificationCompat.Builder(mContext, mContext.getString(R.string.tindog_notification_channel))
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Tinpet image download")
                .setContentText("Download in progress")
                .setProgress(PROGRESS_MAX, 0, false)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        mNotificationManager = NotificationManagerCompat.from(mContext);

        Log.i(DEBUG_TAG, "Attempting to download image with uri: " + localFirebaseTempImageUri.toString());
        imageRef.getFile(localFirebaseTempImage)
                .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                        mNotificationBuilder.setProgress(0, 0, false);
                        mNotificationManager.notify(FIREBASE_IMAGE_DOWNLOAD_NOTIFICATION_ID, mNotificationBuilder.build());
                        //Log.i(DEBUG_TAG, "Successfully downloaded image with uri: " + localFirebaseTempImageUri.toString());
                        sendImageUriToInterface(localFirebaseTempImageUri, imageName);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        //Log.i(DEBUG_TAG, "Download failed for image with uri: " + localFirebaseTempImageUri.toString());
                        sendImageUriToInterface(localImageUri, imageName);
                        //exception.printStackTrace();
                        //Toast.makeText(mContext, "Failed to retrieve image from Firebase storage, check log.", Toast.LENGTH_SHORT).show();
                    }
                }).addOnProgressListener(new OnProgressListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(FileDownloadTask.TaskSnapshot taskSnapshot) {
                        int progress = (int) ( (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount() );
                        mNotificationBuilder.setProgress(PROGRESS_MAX, progress, false);
                        mNotificationManager.notify(FIREBASE_IMAGE_DOWNLOAD_NOTIFICATION_ID, mNotificationBuilder.build());
                    }
                });
    }
    public void deleteAllObjectImagesFromFirebaseStorage(Object object) {
        deleteImageFromFirebaseStorage(object, "mainImage");
        deleteImageFromFirebaseStorage(object, "image1");
        deleteImageFromFirebaseStorage(object, "image2");
        deleteImageFromFirebaseStorage(object, "image3");
        deleteImageFromFirebaseStorage(object, "image4");
        deleteImageFromFirebaseStorage(object, "image5");
    }
    private void deleteImageFromFirebaseStorage(Object object, final String imageName) {

        if (Utilities.imageNameIsInvalid(imageName)) return;

        String folderPath;

        if (object instanceof Pet) {
            Pet pet = (Pet) object;
            folderPath = "pets/" + pet.getUI() + "/images";
        }
        else if (object instanceof Family) {
            Family family = (Family) object;
            folderPath = "families/" + family.getUI() + "/images";
        }
        else if (object instanceof Foundation) {
            Foundation foundation = (Foundation) object;
            folderPath = "foundations/" + foundation.getUI() + "/images";
        }
        else return;

        final String childPath = folderPath + "/" + imageName + ".jpg";

        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        StorageReference imageRef = storageRef.child(childPath);
        imageRef.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i(DEBUG_TAG, "Deleted image at: " + childPath);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Log.i(DEBUG_TAG, "Failed to delete image at: " + childPath);
                    }
                });
    }


    //Firebase Storage helper methods (prevent code repetitions in the CRUD methods)
    private void sendImageUriToInterface(Uri imageUri, String imageName) {
        mOnOperationPerformedHandler.onImageAvailable(imageUri, imageName);
    }


    //Communication with other activities/fragments
    final private FirebaseOperationsHandler mOnOperationPerformedHandler;
    public interface FirebaseOperationsHandler {
        void onPetListFound(List<Pet> pets);
        void onFamilyListFound(List<Family> families);
        void onFoundationListFound(List<Foundation> foundations);
        void onUserListFound(List<User> users);
        void onMapMarkerListFound(List<MapMarker> mapMarkers);
        void onImageAvailable(Uri imageUri, String imageName);
        void onImageUploaded(List<String> uploadTimes);
    }
    public void removeListeners() {
    }
}