package com.example.student.ui.aboutus;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.student.R;

public class AboutusFragment extends Fragment {

    // UI elements for student information
    private ImageView studentImage1, studentImage2, studentImage3, studentImage4;
    private TextView studentName1, studentName2, studentName3, studentName4;
    private TextView studentId1, studentId2, studentId3, studentId4;

    //UI element for college information
    private ImageView collegeLogo;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_aboutus, container, false);

        // Initialize UI elements for students with dataBinding

        studentName1 = root.findViewById(R.id.student_name_1);
        studentName2 = root.findViewById(R.id.student_name_2);
        studentName3 = root.findViewById(R.id.student_name_3);
        studentName4 = root.findViewById(R.id.student_name_4);

        studentId1 = root.findViewById(R.id.student_id_1);
        studentId2 = root.findViewById(R.id.student_id_2);
        studentId3 = root.findViewById(R.id.student_id_3);
        studentId4 = root.findViewById(R.id.student_id_4);

        //Initialize UI Element for College
        collegeLogo = root.findViewById(R.id.college_logo);

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Set the student data
        setStudentInfo(studentImage1, studentName1, studentId1, "student1_image", "Goswami Parth", "223SBECE54009");
        setStudentInfo(studentImage2, studentName2, studentId2, "student2_image", "Macwan Chrish", "223SBECE54013");
        setStudentInfo(studentImage3, studentName3, studentId3, "student3_image", "Macwan Jenil", "223SBECE54014");
        setStudentInfo(studentImage4, studentName4, studentId4, "student4_image", "Mehta Dhimahi", "223SBECE54015");

        //Set the college data
        //set the collegeInfo("college_logo_vsitr",collegeLogo);
    }

    private void setStudentInfo(ImageView imageView, TextView nameTextView, TextView idTextView,
                                String imageName, String name, String id) {
        // Set the student image from drawable
        int imageResource = getResources().getIdentifier(imageName, "drawable",
                getContext().getPackageName());

        // If the image resource exists, set it
        if (imageResource != 0) {
            imageView.setImageResource(imageResource);
        }

        // Set the student name
        nameTextView.setText("Student Name: " + name);

        // Set the student ID
        idTextView.setText("Student ID: " + id);
    }

    private void setcollegeInfo(String imageName,ImageView imageView){
        int imageResource = getResources().getIdentifier(imageName, "drawable",
                getContext().getPackageName());

        if(imageResource !=0){
            imageView.setImageResource(imageResource);
        }
        else{
            //If something goes wrong, the college logo will be the default.
            imageView.setImageResource(R.drawable.college_logo_bg);
        }
    }
}