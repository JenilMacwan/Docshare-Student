package com.example.student.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class HomeViewModel extends ViewModel {

    private final MutableLiveData<Boolean> showBT1Buttons = new MutableLiveData<>(false);;

    private final MutableLiveData<Boolean> showBT2Buttons = new MutableLiveData<>(false);

    public LiveData<Boolean> getShowBT1Buttons() {
        return showBT1Buttons;
    }

    public LiveData<Boolean> getShowBT2Buttons() {
        return showBT2Buttons;
    }

    public void onBT1Clicked() {
        showBT1Buttons.setValue(true);
        showBT2Buttons.setValue(false);
    }

    public void onBT2Clicked() {
        showBT1Buttons.setValue(false);
        showBT2Buttons.setValue(true);
    }
}