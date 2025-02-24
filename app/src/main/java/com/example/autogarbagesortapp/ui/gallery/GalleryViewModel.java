package com.example.autogarbagesortapp.ui.gallery;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class GalleryViewModel extends ViewModel {

    private final MutableLiveData<String> mText, mPass;

    public GalleryViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("user");
        mPass = new MutableLiveData<>();
        mPass.setValue("password");
    }

    public LiveData<String> getText() {
        return mText;
    }
    public LiveData<String> getPass() {
        return mPass;
    }
}