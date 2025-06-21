// DocumentAdapter.java
package com.example.student;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.List;

public class DocumentAdapter extends RecyclerView.Adapter<DocumentAdapter.DocumentViewHolder> {

    private final Context context; // Make context final
    private List<Document> documentList; // Make documentList final

    public DocumentAdapter(Context context, List<Document> documentList) {
        this.context = context;
        this.documentList = documentList;
    }

    @NonNull
    @Override
    public DocumentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_documents, parent, false);
        return new DocumentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DocumentViewHolder holder, int position) {
        Document document = documentList.get(position);
        holder.subjectName.setText(document.getSubjectName());

        final String fileName = document.getName();
        final String semester = document.getSemester(); // Get semester from Document
        final String subjectName = document.getSubjectName(); // Get subject from Document

        Log.d("DocumentAdapter", "onBindViewHolder - Document Data: fileName=" + fileName + ", semester=" + semester + ", subjectName=" + subjectName);

        String fileExtension = fileExtensionFromFileName(fileName);

        holder.imageViewFileDisplay.setVisibility(View.VISIBLE);
        holder.imageViewFileDisplay.setScaleType(ImageView.ScaleType.CENTER_INSIDE);

        if (fileExtension.equals("jpg") || fileExtension.equals("jpeg") || fileExtension.equals("png") || fileExtension.equals("gif") || fileExtension.equals("bmp")) {
            fetchImageFromFirebaseStorage(fileName, semester, subjectName, holder);
        } else if (fileExtension.equals("pdf")) {
            holder.imageViewFileDisplay.setImageResource(R.drawable.pdf_icon);
        } else if (fileExtension.equals("doc") || fileExtension.equals("docx")) {
            holder.imageViewFileDisplay.setImageResource(R.drawable.word_icon);
        } else if (fileExtension.equals("xls") || fileExtension.equals("xlsx")) {
            holder.imageViewFileDisplay.setImageResource(R.drawable.excel_icon);
        } else if (fileExtension.equals("ppt") || fileExtension.equals("pptx")) {
            holder.imageViewFileDisplay.setImageResource(R.drawable.ppt_icon);
        } else {
            holder.imageViewFileDisplay.setImageResource(R.drawable.generic_file_icon);
        }

        holder.imageViewFileDisplay.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Choose Action");
            String[] options;
            if (fileExtension.equals("pdf") || fileExtension.equals("jpg") || fileExtension.equals("jpeg") || fileExtension.equals("png") || fileExtension.equals("gif") || fileExtension.equals("bmp") || fileExtension.equals("doc") || fileExtension.equals("docx") || fileExtension.equals("xls") || fileExtension.equals("xlsx") || fileExtension.equals("ppt") || fileExtension.equals("pptx")) {
                options = new String[]{"View", "Download"};
            } else {
                options = new String[]{"Download"};
            }

            builder.setItems(options, (dialog, which) -> {
                String selectedOption = options[which];
                if (selectedOption.equals("View")) {
                    fetchAndOpenFileFromFirebaseStorage(fileName, semester, subjectName, fileExtension);
                } else if (selectedOption.equals("Download")) {
                    downloadFileFromFirebaseStorage(fileName, semester, subjectName);
                }
            });
            builder.show();
        });
    }

    private String fileExtensionFromFileName(String fileName) {
        String fileExtension = "";
        if (fileName != null && fileName.lastIndexOf(".") != -1) {
            fileExtension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        }
        return fileExtension;
    }

    private void fetchImageFromFirebaseStorage(String fileName, String semester, String subjectName, DocumentViewHolder holder) { // Added semester and subjectName
        FirebaseStorage storage = FirebaseStorage.getInstance();
        // Correct file path construction here:
        StorageReference storageRef = storage.getReference().child("documents/" + semester + "/" + subjectName + "/" + fileName);

        storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
            // Load image into ImageView using Picasso
            Picasso.get()
                    .load(uri)
                    .placeholder(R.drawable.image_placeholder)
                    .error(R.drawable.image_error)
                    .into(holder.imageViewFileDisplay);
        }).addOnFailureListener(e -> {
            Log.e("FirebaseStorage", "Failed to fetch image: " + e.getMessage());
            Toast.makeText(context, "Failed to fetch image", Toast.LENGTH_SHORT).show();
        });
    }

    private void fetchAndOpenFileFromFirebaseStorage(String fileName, String semester, String subjectName, String fileExtension) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference().child("documents/" + semester + "/" + subjectName + "/" + fileName);

        Log.d("FirebaseStorage", "Fetching file from path: " + storageRef.getPath());

        storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
            String mimeType = getMimeType(fileExtension);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, mimeType);
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_NEW_TASK);
            try {
                context.startActivity(intent);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(context, "No app found to open this file", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Log.e("FirebaseStorage", "Failed to fetch file: " + e.getMessage());
            Toast.makeText(context, "Failed to fetch file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void downloadFileFromFirebaseStorage(String fileName, String semester, String subjectName) {
        if (fileName == null || semester == null || subjectName == null) {
            Log.e("FirebaseStorage", "File name, semester, or subject name is null");
            Toast.makeText(context, "Invalid file details", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference().child("documents/" + semester + "/" + subjectName + "/" + fileName);

        // Use app's private directory for downloads (works on all Android versions)
        File directory = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
        if (directory == null) {
            Log.e("FirebaseStorage", "Downloads directory is null");
            Toast.makeText(context, "Failed to access Downloads folder", Toast.LENGTH_SHORT).show();
            return;
        }

        File file = new File(directory, fileName);

        Log.d("FirebaseStorage", "Downloading file to: " + file.getAbsolutePath());

        storageRef.getFile(file).addOnSuccessListener(taskSnapshot -> {
            Toast.makeText(context, "File downloaded to Downloads folder", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> {
            Log.e("FirebaseStorage", "Failed to download file: " + e.getMessage());
            Toast.makeText(context, "Failed to download file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private String getMimeType(String fileExtension) {
        switch (fileExtension) {
            case "pdf":
                return "application/pdf";
            case "jpg":
            case "jpeg":
            case "png":
            case "gif":
            case "bmp":
                return "image/*";
            case "doc":
            case "docx":
                return "application/msword";
            case "xls":
            case "xlsx":
                return "application/vnd.ms-excel";
            case "ppt":
            case "pptx":
                return "application/vnd.ms-powerpoint";
            default:
                return "*/*";
        }
    }

    @Override
    public int getItemCount() {
        return documentList.size();
    }

    public List<Document> getdocumentList() {
        return documentList;
    }

    public static class DocumentViewHolder extends RecyclerView.ViewHolder {
        TextView subjectName;
        ImageView imageViewFileDisplay;

        public DocumentViewHolder(@NonNull View itemView) {
            super(itemView);
            subjectName = itemView.findViewById(R.id.textViewDocName);
            imageViewFileDisplay = itemView.findViewById(R.id.imageViewFileDisplay);
            Log.d("DocumentAdapter", "DocumentViewHolder - imageViewFileDisplay: " + imageViewFileDisplay);
        }
    }
}