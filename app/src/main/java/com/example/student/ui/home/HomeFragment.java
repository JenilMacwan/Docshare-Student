package com.example.student.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AccelerateInterpolator; // Import AccelerateInterpolator
import android.view.animation.DecelerateInterpolator; // Import DecelerateInterpolator
import android.widget.Button; // Keep Button or change to MaterialButton if needed

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.student.R;
import com.example.student.Sem1;
import com.example.student.Sem2;
import com.example.student.Sem3;
import com.example.student.Sem4;
import com.example.student.Sem5;
import com.example.student.Sem6;
import com.example.student.Sem7;
import com.example.student.Sem8;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList; // Use ArrayList for easier type casting if needed
import java.util.Calendar;
import java.util.List;

public class HomeFragment extends Fragment {

    // Keep HomeViewModel if you use it elsewhere, otherwise it can be removed for this specific animation
    private HomeViewModel homeViewModel;
    private MaterialButton bt1, bt2;
    // It's slightly better practice to declare the List with the specific type
    private List<MaterialButton> oddSemButtons;
    private List<MaterialButton> evenSemButtons;
    private boolean isOddSemVisible = false;
    private boolean isEvenSemVisible = false;

    // Animation constants
    private static final long ANIMATION_DURATION = 300; // ms
    private static final long STAGGER_DELAY = 50; // ms delay between each button animation
    private static final float TRANSLATION_DISTANCE = 50f; // distance buttons slide (in pixels)

    public HomeFragment() {
        super(R.layout.fragment_home);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        // Main buttons
        bt1 = view.findViewById(R.id.bt1);
        bt2 = view.findViewById(R.id.bt2);

        // Initialize lists with MaterialButton type
        oddSemButtons = new ArrayList<>();
        oddSemButtons.add(view.findViewById(R.id.bt1_1));
        oddSemButtons.add(view.findViewById(R.id.bt1_2));
        oddSemButtons.add(view.findViewById(R.id.bt1_3));
        oddSemButtons.add(view.findViewById(R.id.bt1_4));

        evenSemButtons = new ArrayList<>();
        evenSemButtons.add(view.findViewById(R.id.bt2_1));
        evenSemButtons.add(view.findViewById(R.id.bt2_2));
        evenSemButtons.add(view.findViewById(R.id.bt2_3));
        evenSemButtons.add(view.findViewById(R.id.bt2_4));

        // --- Click listeners for main buttons ---
        bt1.setOnClickListener(v -> {
            // If Odd are hidden, show them
            if (!isOddSemVisible) {
                // First, hide Even if they are visible
                if (isEvenSemVisible) {
                    animateHideButtons(evenSemButtons);
                    isEvenSemVisible = false;
                }
                // Now, show Odd
                animateShowButtons(oddSemButtons);
                isOddSemVisible = true;
            } else {
                // If Odd are visible, hide them
                animateHideButtons(oddSemButtons);
                isOddSemVisible = false;
            }
        });

        bt2.setOnClickListener(v -> {
            // If Even are hidden, show them
            if (!isEvenSemVisible) {
                // First, hide Odd if they are visible
                if (isOddSemVisible) {
                    animateHideButtons(oddSemButtons);
                    isOddSemVisible = false;
                }
                // Now, show Even
                animateShowButtons(evenSemButtons);
                isEvenSemVisible = true;
            } else {
                // If Even are visible, hide them
                animateHideButtons(evenSemButtons);
                isEvenSemVisible = false;
            }
        });

        // --- Click listeners for semester buttons (Intent logic remains the same) ---
        setupSemesterButtonClickListeners();

        // Ensure buttons start hidden WITHOUT animation
        setButtonsVisibility(oddSemButtons, View.GONE);
        setButtonsVisibility(evenSemButtons, View.GONE);
    }


    /**
     * Animates the appearance of a list of buttons.
     * They fade in and slide down slightly with a staggered delay.
     *
     * @param buttons The list of MaterialButtons to show.
     */
    private void animateShowButtons(List<MaterialButton> buttons) {
        for (int i = 0; i < buttons.size(); i++) {
            MaterialButton button = buttons.get(i);
            // Prepare the button for animation: invisible and slightly above its final position
            button.setVisibility(View.VISIBLE);
            button.setAlpha(0f);
            button.setTranslationY(-TRANSLATION_DISTANCE); // Start above

            // Animate to final state
            button.animate()
                    .alpha(1f)
                    .translationY(0f) // Move to original Y position
                    .setDuration(ANIMATION_DURATION)
                    .setStartDelay(i * STAGGER_DELAY) // Apply stagger
                    .setInterpolator(new DecelerateInterpolator()) // Smooth easing out
                    .start();
        }
    }

    /**
     * Animates the disappearance of a list of buttons.
     * They fade out and slide down slightly.
     *
     * @param buttons The list of MaterialButtons to hide.
     */
    private void animateHideButtons(List<MaterialButton> buttons) {
        for (int i = 0; i < buttons.size(); i++) {
            MaterialButton button = buttons.get(i);

            // Don't animate if already hidden or hiding
            if (button.getVisibility() != View.VISIBLE || button.getAnimation() != null && !button.getAnimation().hasEnded()) {
                continue;
            }

            // Animate to hidden state
            button.animate()
                    .alpha(0f)
                    .translationY(TRANSLATION_DISTANCE) // Move down
                    .setDuration(ANIMATION_DURATION)
                    // No stagger needed for hiding usually, but you could add: .setStartDelay(i * STAGGER_DELAY / 2)
                    .setInterpolator(new AccelerateInterpolator()) // Smooth speeding up
                    .withEndAction(() -> {
                        // Reset properties after animation completes
                        button.setVisibility(View.GONE);
                        button.setTranslationY(0f); // Reset translation for next show animation
                        button.setAlpha(1f);       // Reset alpha for next show animation
                    })
                    .start();
        }
    }

    /**
     * Sets the initial visibility of buttons without animation.
     *
     * @param buttons    The list of buttons.
     * @param visibility View.VISIBLE, View.GONE, or View.INVISIBLE.
     */
    private void setButtonsVisibility(List<MaterialButton> buttons, int visibility) {
        for (MaterialButton button : buttons) {
            button.setVisibility(visibility);
        }
    }


    /**
     * Sets up the click listeners for all individual semester buttons.
     */
    private void setupSemesterButtonClickListeners() {
        oddSemButtons.get(0).setOnClickListener(v -> navigateToSemester(Sem1.class, "Sem1"));
        oddSemButtons.get(1).setOnClickListener(v -> navigateToSemester(Sem3.class, "Sem3"));
        oddSemButtons.get(2).setOnClickListener(v -> navigateToSemester(Sem5.class, "Sem5"));
        oddSemButtons.get(3).setOnClickListener(v -> navigateToSemester(Sem7.class, "Sem7"));

        evenSemButtons.get(0).setOnClickListener(v -> navigateToSemester(Sem2.class, "Sem2"));
        evenSemButtons.get(1).setOnClickListener(v -> navigateToSemester(Sem4.class, "Sem4"));
        evenSemButtons.get(2).setOnClickListener(v -> navigateToSemester(Sem6.class, "Sem6"));
        evenSemButtons.get(3).setOnClickListener(v -> navigateToSemester(Sem8.class, "Sem8"));
    }

    private void navigateToSemester(Class<?> activityClass, String semesterName) {
        if (getContext() != null) {
            Intent intent = new Intent(getContext(), activityClass);
            intent.putExtra("semester", semesterName);
            startActivity(intent);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Cancel any ongoing animations when the view is destroyed
        // to prevent potential memory leaks or crashes
        cancelAnimations(oddSemButtons);
        cancelAnimations(evenSemButtons);
    }

    private void cancelAnimations(List<MaterialButton> buttons) {
        if (buttons != null) {
            for (MaterialButton button : buttons) {
                if (button != null) {
                    button.animate().cancel();
                }
            }
        }
    }
}



