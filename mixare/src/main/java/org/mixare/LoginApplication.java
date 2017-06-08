package org.mixare;

import android.app.Application;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;


public class LoginApplication extends Application {
    public static GoogleApiClient mGoogleApiClient;
    public static FirebaseAuth mAuth;

    @Override
    public void onCreate() {
        super.onCreate();
    }
}
