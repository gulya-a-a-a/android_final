package com.geekbrains.theweatherapp.activities;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.geekbrains.theweatherapp.R;
import com.geekbrains.theweatherapp.broadcast.CellularMissedReceiver;
import com.geekbrains.theweatherapp.broadcast.LowBatteryReceiver;
import com.geekbrains.theweatherapp.service.GoogleAuth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private AppBarConfiguration mAppBarConfiguration;
    private DrawerLayout mDrawerLayout;
    private NavController mNavController;
    private NavigationView mNavigationView;

    private View mUserDataLayout;
    private SignInButton mSib;
    private ImageView mUserImage;
    private TextView mUserNameTV, mUserEmailTV;

    private GoogleAuth mGoogleAuth;

    private BroadcastReceiver mLowBatteryReceiver = new LowBatteryReceiver(),
            mMissedCellularReceiver = new CellularMissedReceiver();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        initDrawer();
        initBroadcastReceivers();
        initNotificationChannel();

        initGoogleAuth();
    }


    private void initNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager
                    = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannel channel = new NotificationChannel("1",
                    "WeatherAppNotifications",
                    NotificationManager.IMPORTANCE_LOW);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    private void initBroadcastReceivers() {
        registerReceiver(mLowBatteryReceiver, new IntentFilter(Intent.ACTION_BATTERY_LOW));
        registerReceiver(mLowBatteryReceiver, new IntentFilter(Intent.ACTION_BATTERY_OKAY));
        registerReceiver(mMissedCellularReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    private void initDrawer() {
        mDrawerLayout = findViewById(R.id.drawer_layout);
        mNavigationView = findViewById(R.id.navigation_view);

        mAppBarConfiguration = new AppBarConfiguration.Builder(R.id.nav_cities,
                R.id.nav_settings).setDrawerLayout(mDrawerLayout).build();

        mNavController = Navigation.findNavController(this, R.id.nav_host_fragment);

        NavigationUI.setupActionBarWithNavController(this, mNavController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(mNavigationView, mNavController);

        mNavigationView.setNavigationItemSelectedListener(this);
    }



    private void initGoogleAuth() {
        View navHeader = mNavigationView.getHeaderView(0);
        mSib = navHeader.findViewById(R.id.sign_in_button);
        mSib.setSize(SignInButton.SIZE_WIDE);
        mSib.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });

        mUserDataLayout = navHeader.findViewById(R.id.user_data_layout);
        mUserImage = mUserDataLayout.findViewById(R.id.user_image);
        mUserNameTV = mUserDataLayout.findViewById(R.id.user_name);
        mUserEmailTV = mUserDataLayout.findViewById(R.id.user_email);

        mGoogleAuth = new GoogleAuth(this);
    }

    private void signIn() {
        Intent signInIntent = mGoogleAuth.getGoogleSignInClient().getSignInIntent();
        startActivityForResult(signInIntent, 100);
    }

    private void handleSignInResult(Task<GoogleSignInAccount> task) {
        try {
            GoogleSignInAccount account = task.getResult(ApiException.class);
            assert account != null;

            mSib.setVisibility(View.GONE);
            mUserNameTV.setText(account.getDisplayName());
            mUserEmailTV.setText(account.getEmail());
            Picasso.get().load(account.getPhotoUrl()).into(new Target() {
                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                    mUserImage.setImageBitmap(bitmap);
                }

                @Override
                public void onBitmapFailed(Exception e, Drawable errorDrawable) {

                }

                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) {

                }
            });

            mUserDataLayout.setVisibility(View.VISIBLE);

        } catch (ApiException e) {
            Log.w(MainActivity.class.getSimpleName(), "signInResult:failed code=" + e.getStatusCode());
            mUserDataLayout.setVisibility(View.GONE);
            mSib.setVisibility(View.VISIBLE);
        }
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
            super.onBackPressed();
        } else {
            getSupportFragmentManager().popBackStack();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        Intent intent, shareIntent;
        boolean res = false;
        switch (id) {
            case R.id.nav_send:
                break;
            case R.id.nav_share:
                intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TEXT, "CITY NAME");
                shareIntent = Intent.createChooser(intent, null);
                startActivity(shareIntent);
                break;
            case R.id.nav_settings:
            case R.id.nav_cities:
                mNavController.navigate(id);
                res = true;
            default:
                break;
        }
        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        drawerLayout.closeDrawer(GravityCompat.START);
        return res;
    }
}
