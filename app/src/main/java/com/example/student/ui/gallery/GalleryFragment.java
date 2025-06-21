package com.example.student.ui.gallery;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.student.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;
import com.squareup.picasso.RequestCreator;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;

import java.util.HashMap;
import java.util.Map;

public class GalleryFragment extends Fragment {

    private TextView nameTextView, facultyIdTextView, emailTextView;
    private ImageView profileImageView;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private FirebaseStorage storage;
    private StorageReference storageReference;

    private static final int PICK_IMAGE_GALLERY_REQUEST = 1;
    private static final int PICK_IMAGE_CAMERA_REQUEST = 2;
    private static final int PERMISSION_REQUEST_CODE = 100;

    private String pendingAction = "";


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_gallery, container, false);

        nameTextView = view.findViewById(R.id.profile_name_textview);
        emailTextView = view.findViewById(R.id.profile_email_textview);
        facultyIdTextView = view.findViewById(R.id.profile_id_textview);
        profileImageView = view.findViewById(R.id.profile_image_view);

        nameTextView.setTextIsSelectable(true);
        emailTextView.setTextIsSelectable(true);
        facultyIdTextView.setTextIsSelectable(true);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        fetchUserData();

        profileImageView.setOnClickListener(v -> showImagePickerDialog());

        return view;
    }

    private void fetchUserData() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid();

            DocumentReference userRef = db.collection("UserProfiles").document(uid);

            userRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        String name = document.getString("name");
                        String email = document.getString("email");
                        String facultyId = document.getString("studentId");
                        String profileImageUrl = document.getString("profileImageUrl");

                        nameTextView.setText(name);
                        emailTextView.setText(email);
                        facultyIdTextView.setText(facultyId);

                        if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                            Picasso.get()
                                    .load(profileImageUrl)
                                    .placeholder(R.drawable.user)
                                    .error(R.drawable.user)
                                    .transform(new RoundedCornersTransformation(80)) // Apply Rounded Transformation here
                                    .into(profileImageView);
                        } else {
                            profileImageView.setImageResource(R.drawable.user);
                        }
                    } else {
                        Log.d("PROFILE_DEBUG", "No such document");
                        setDefaultDataNotFound();
                    }
                } else {
                    Log.d("PROFILE_DEBUG", "get failed with ", task.getException());
                    setDefaultDataNotFound();
                }
            });
        } else {
            Log.d("PROFILE_DEBUG", "No user logged in");
            setDefaultDataNotFound();
        }
    }

    private void setDefaultDataNotFound() {
        nameTextView.setText("Data not found");
        emailTextView.setText("Data not found");
        facultyIdTextView.setText("Data not found");
    }


    private void showImagePickerDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Choose Profile Picture From");
        String[] options = {"Gallery", "Camera", "Remove Profile Picture"};
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                pendingAction = "gallery";
                openGallery();
            } else if (which == 1) {
                pendingAction = "camera";
                openCamera();
            } else if (which == 2) {
                removeProfilePicture();
            }

        });
        builder.show();
    }

    private void openGallery() {
        // Using SAF - Intent.ACTION_OPEN_DOCUMENT - No READ_EXTERNAL_STORAGE needed for user-selected files
        Intent galleryIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        galleryIntent.addCategory(Intent.CATEGORY_OPENABLE);
        galleryIntent.setType("image/*"); // MIME type for images
        startActivityForResult(galleryIntent, PICK_IMAGE_GALLERY_REQUEST);
    }

    private void openCamera() {

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, PICK_IMAGE_CAMERA_REQUEST);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d("PermissionsDebug", "onRequestPermissionsResult: requestCode=" + requestCode);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0) {
                boolean allPermissionsGranted = true;
                for (int grantResult : grantResults) {
                    if (grantResult != PackageManager.PERMISSION_GRANTED) {
                        allPermissionsGranted = false;
                        break;
                    }
                }
                if (allPermissionsGranted) {
                    Log.d("PermissionsDebug", "Permissions Granted");
                    Toast.makeText(getContext(), "Permissions Granted.", Toast.LENGTH_SHORT).show();
                    if (pendingAction.equals("gallery")) {
                        openGallery();
                    } else if (pendingAction.equals("camera")) {
                        openCamera();
                    }
                } else {
                    Log.d("PermissionsDebug", "Permissions Denied");
                    Toast.makeText(getContext(), "Permission denied.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Log.d("PermissionsDebug", "Grant results are empty");
                Toast.makeText(getContext(), "Permission results are empty.", Toast.LENGTH_SHORT).show();
            }
            pendingAction = "";
        }
    }

    private void removeProfilePicture() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(getContext(), "Not logged in.", Toast.LENGTH_SHORT).show();
            return;
        }
        String uid = currentUser.getUid();
        DocumentReference userRef = db.collection("UserProfiles").document(uid);

        userRef.get().addOnCompleteListener(task -> { // Fetch current profileImageUrl before deletion
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    String profileImageUrl = document.getString("profileImageUrl");
                    if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                        StorageReference imageRef = storage.getReferenceFromUrl(profileImageUrl); // Get StorageReference from URL
                        imageRef.delete().addOnSuccessListener(aVoid -> { // Delete from Storage
                            Log.d("StorageDebug", "Profile image deleted from Storage: " + profileImageUrl);
                            updateFirestoreForProfileRemoval(userRef); // Proceed to update Firestore after Storage delete success
                        }).addOnFailureListener(e -> {
                            Log.e("StorageDebug", "Error deleting profile image from Storage: " + profileImageUrl + " - " + e.getMessage());
                            Toast.makeText(getContext(), "Error removing profile picture from storage.", Toast.LENGTH_SHORT).show();
                            updateFirestoreForProfileRemoval(userRef); // Still try to clear URL in Firestore even if storage delete fails (optional - you may adjust this behavior)
                        });
                    } else {
                        updateFirestoreForProfileRemoval(userRef); // If no image URL in Firestore, just update Firestore to ensure default display.
                    }
                } else {
                    updateFirestoreForProfileRemoval(userRef); // If no doc, proceed with Firestore update.
                }
            } else {
                updateFirestoreForProfileRemoval(userRef); // If doc fetch fails, proceed with Firestore update
            }
        });
    }

    private void updateFirestoreForProfileRemoval(DocumentReference userRef) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("profileImageUrl", null);

        userRef.update(updates)
                .addOnSuccessListener(aVoid -> {
                    Log.d("FIRESTORE_UPDATE", "Profile image URL removed from Firestore");
                    Toast.makeText(getContext(), "Profile picture removed.", Toast.LENGTH_SHORT).show();
                    profileImageView.setImageResource(R.drawable.user);
                })
                .addOnFailureListener(e -> {
                    Log.e("FIRESTORE_UPDATE", "Error removing profile image URL in Firestore", e);
                    Toast.makeText(getContext(), "Failed to remove profile picture.", Toast.LENGTH_SHORT).show();
                });
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("ActivityResultDebug", "onActivityResult: requestCode=" + requestCode + ", resultCode=" + resultCode);


        if (resultCode == Activity.RESULT_OK) {
            Uri imageUri = null;
            if (requestCode == PICK_IMAGE_GALLERY_REQUEST && data != null && data.getData() != null) {
                imageUri = data.getData();
                Log.d("ActivityResultDebug", "Gallery imageUri: " + imageUri);
                final int takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION;
                try {
                    getContext().getContentResolver().takePersistableUriPermission(imageUri, takeFlags);
                    Log.d("ActivityResultDebug", "Persistable URI permission taken successfully.");
                } catch (SecurityException e) {
                    Log.e("ActivityResultDebug", "SecurityException taking persistable URI permission: " + e.getMessage());
                    Toast.makeText(getContext(), "Error taking persistable URI permission: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    return;
                }


            } else if (requestCode == PICK_IMAGE_CAMERA_REQUEST && data != null && data.getExtras() != null) {
                Toast.makeText(getContext(), "Camera option will fully function in real device with FileProvider setup. For Simplicity use gallery for now.", Toast.LENGTH_LONG).show();
                return;
            }

            if (imageUri != null) {
                Log.d("ActivityResultDebug", "Image URI is valid, proceeding to upload: " + imageUri);
                uploadImageToFirebaseStorage(imageUri);
            } else {
                Log.d("ActivityResultDebug", "Image URI is null after activity result.");
                Toast.makeText(getContext(), "Error: Image URI is null.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.d("ActivityResultDebug", "Activity result not OK, resultCode: " + resultCode);
            if (resultCode == Activity.RESULT_CANCELED) {
                Log.d("ActivityResultDebug", "User cancelled image selection.");
            }
        }
    }

    private void uploadImageToFirebaseStorage(Uri imageUri) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(getContext(), "Not logged in.", Toast.LENGTH_SHORT).show();
            return;
        }
        String uid = currentUser.getUid();
        StorageReference profileImageRef = storageReference.child("Student_profile/" + uid + ".jpg");

        Log.d("FirebaseUploadDebug", "Attempting to upload image with URI: " + imageUri.toString());

        UploadTask uploadTask = profileImageRef.putFile(imageUri);
        uploadTask.continueWithTask(task -> {
            if (!task.isSuccessful()) {
                throw task.getException();
            }
            return profileImageRef.getDownloadUrl();
        }).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Uri downloadUri = task.getResult();
                if (downloadUri != null) {
                    String profileImageUrl = downloadUri.toString();
                    updateProfileImageUrlInFirestore(profileImageUrl);
                    Picasso.get()
                            .load(profileImageUrl)
                            .placeholder(R.drawable.user)
                            .error(R.drawable.user)
                            .transform(new RoundedCornersTransformation(80)) // Apply Rounded Transformation here during loading
                            .into(profileImageView);
                    Toast.makeText(getContext(), "Profile picture updated.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Failed to get download URL.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Log.e("FirebaseUploadDebug", "Image upload failed: ", task.getException());
                Log.e("FirebaseUploadDebug", "Upload failed Exception Message: " + task.getException().getMessage());
                Toast.makeText(getContext(), "Upload failed: User does not have permission to access this object.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateProfileImageUrlInFirestore(String profileImageUrl) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) return;
        String uid = currentUser.getUid();
        DocumentReference userRef = db.collection("UserProfiles").document(uid);

        Map<String, Object> updates = new HashMap<>();
        updates.put("profileImageUrl", profileImageUrl);

        userRef.update(updates)
                .addOnSuccessListener(aVoid -> Log.d("FIRESTORE_UPDATE", "Profile image URL updated in Firestore"))
                .addOnFailureListener(e -> Log.e("FIRESTORE_UPDATE", "Error updating profile image URL", e));
    }


    public class RoundedCornersTransformation implements Transformation {

        private final int radius;
        private final int margin;

        public RoundedCornersTransformation(int radiusDp) { // Constructor now takes radiusDp
            this.radius = radiusDp; // Set radius from constructor argument
            this.margin = 0;
        }


        @Override
        public Bitmap transform(Bitmap source) {
            Bitmap result = Bitmap.createBitmap(source.getWidth(), source.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(result);
            Paint paint = new Paint();
            paint.setShader(new BitmapShader(source, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP));
            paint.setAntiAlias(true);
            float radiusPx = radius * getResources().getDisplayMetrics().density; // Convert DP to pixels here
            RectF rectF = new RectF(margin, margin, source.getWidth() - margin, source.getHeight() - margin);
            canvas.drawRoundRect(rectF, radiusPx, radiusPx, paint); // Use radiusPx for drawRoundRect
            if (source != result) {
                source.recycle();
            }
            return result;
        }

        @Override
        public String key() {
            return "rounded_corners_" + radius; // Include radius in cache key
        }
    }
}