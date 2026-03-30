package com.example.studyflow.ui.settings;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.studyflow.utils.SharedPrefsHelper;

public class SettingsViewModel extends AndroidViewModel {
    private final SharedPrefsHelper prefsHelper;
    private final MutableLiveData<String> _userName = new MutableLiveData<>();
    public LiveData<String> userName = _userName;

    private final MutableLiveData<String> _userEmail = new MutableLiveData<>();
    public LiveData<String> userEmail = _userEmail;

    public SettingsViewModel(@NonNull Application application) {
        super(application);
        prefsHelper = new SharedPrefsHelper(application);
        _userName.setValue(prefsHelper.getUserName());
        _userEmail.setValue(prefsHelper.getUserEmail());
    }

    public void saveUserName(String name) {
        prefsHelper.setUserName(name);
        _userName.setValue(name);
    }

    public void saveUserEmail(String email) {
        prefsHelper.setUserEmail(email);
        _userEmail.setValue(email);
    }
}
