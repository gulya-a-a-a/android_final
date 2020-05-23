package com.geekbrains.theweatherapp.service;

import android.content.Context;

import com.geekbrains.theweatherapp.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;

public class GoogleAuth {
    private GoogleSignInClient mGoogleSignInClient;
    private final String serverClientID = "1041850371508-rrf6k3ldfiouhddn8iusce6ub8a5fh44.apps.googleusercontent.com";

    public GoogleAuth(Context signInView) {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestProfile()
                .requestIdToken(serverClientID)
                .requestServerAuthCode(serverClientID, false)
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(signInView, gso);
    }

    public GoogleSignInClient getGoogleSignInClient() {
        return mGoogleSignInClient;
    }
}
