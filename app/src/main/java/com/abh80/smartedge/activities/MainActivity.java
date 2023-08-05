package com.abh80.smartedge.activities;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.abh80.smartedge.R;
import com.abh80.smartedge.aod_edgelighting.CustomThemeActivity;
import com.abh80.smartedge.flashalert.FlashMainActivity;
import com.abh80.smartedge.plugins.ExportedPlugins;
import com.abh80.smartedge.utils.adapters.RecylerViewSettingsAdapter;
import com.abh80.smartedge.utils.SettingStruct;
import com.airbnb.lottie.BuildConfig;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    private SharedPreferences sharedPreferences;
    private InterstitialAd mInterstitialAd;
    private final ArrayList<SettingStruct> settings = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            if (sharedPreferences.getBoolean("clip_copy_enabled", true)) {
                ClipboardManager clipboard = (ClipboardManager)
                        getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("smart edge error log", throwable.getMessage() + " : " + Arrays.toString(throwable.getStackTrace()));
                clipboard.setPrimaryClip(clip);
//                sendCrashNotification();
            }
            Runtime.getRuntime().exit(0);
        });
        init();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PermissionDialogTwo cdd=new PermissionDialogTwo(MainActivity.this);
        cdd.show();

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });
        loadInterAd();

        checkOverlayPermission();

        if ((Settings.Secure.getString(this.getContentResolver(), "enabled_notification_listeners") != null && !Settings.Secure.getString(this.getContentResolver(), "enabled_notification_listeners").contains(getApplicationContext().getPackageName())
        ) || ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            startActivity(new Intent(this, PermissionActivity.class));
        }

        settings.add(new SettingStruct("Enable Smart Edge", "App Settings", SettingStruct.TYPE_CUSTOM) {
            @Override
            public void onClick(Context c) {
                Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                startActivity(intent);
                Toast.makeText(MainActivity.this, "Installed Apps -> Smart Edge", Toast.LENGTH_SHORT).show();
            }
        });

        settings.add(new SettingStruct("Flash Alert", "App Settings", SettingStruct.TYPE_CUSTOM) {
            @Override
            public void onClick(Context c) {
                if (Constants.isNetworkAvailable(MainActivity.this)) {
                    if (mInterstitialAd != null) {
                        mInterstitialAd.show(MainActivity.this);
                        mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                            @Override
                            public void onAdClicked() {
                                // Called when a click is recorded for an ad.
                                Log.d(TAG, "Ad was clicked.");
                            }

                            @Override
                            public void onAdDismissedFullScreenContent() {
                                // Called when ad is dismissed.
                                // Set the ad reference to null so you don't show the ad a second time.
                                Log.d(TAG, "Ad dismissed fullscreen content.");
                                mInterstitialAd = null;
                                Intent intent = new Intent(MainActivity.this, FlashMainActivity.class);
                                startActivity(intent);
                                loadInterAd();
                            }

                            @Override
                            public void onAdFailedToShowFullScreenContent(AdError adError) {
                                // Called when ad fails to show.
                                Log.e(TAG, "Ad failed to show fullscreen content.");
                                mInterstitialAd = null;
                                Intent intent = new Intent(MainActivity.this, FlashMainActivity.class);
                                startActivity(intent);
                                loadInterAd();
                            }

                            @Override
                            public void onAdImpression() {
                                // Called when an impression is recorded for an ad.
                                Log.d(TAG, "Ad recorded an impression.");
                            }

                            @Override
                            public void onAdShowedFullScreenContent() {
                                // Called when ad is shown.
                                Log.d(TAG, "Ad showed fullscreen content.");
                            }
                        });
                    } else {
                        Intent intent = new Intent(MainActivity.this, FlashMainActivity.class);
                        startActivity(intent);
                    }

                } else {
                    Intent intent = new Intent(MainActivity.this, FlashMainActivity.class);
                    startActivity(intent);
                }
            }
        });

        settings.add(new SettingStruct("Edge Light", "App Settings", SettingStruct.TYPE_CUSTOM) {
            @Override
            public void onClick(Context c) {

                if (Constants.isNetworkAvailable(MainActivity.this)) {
                    if (mInterstitialAd != null) {
                        mInterstitialAd.show(MainActivity.this);
                        mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                            @Override
                            public void onAdClicked() {
                                // Called when a click is recorded for an ad.
                                Log.d(TAG, "Ad was clicked.");
                            }

                            @Override
                            public void onAdDismissedFullScreenContent() {
                                // Called when ad is dismissed.
                                // Set the ad reference to null so you don't show the ad a second time.
                                Log.d(TAG, "Ad dismissed fullscreen content.");
                                mInterstitialAd = null;
                                Intent intent = new Intent(MainActivity.this, CustomThemeActivity.class);
                                startActivity(intent);
                                loadInterAd();
                            }

                            @Override
                            public void onAdFailedToShowFullScreenContent(AdError adError) {
                                // Called when ad fails to show.
                                Log.e(TAG, "Ad failed to show fullscreen content.");
                                mInterstitialAd = null;
                                Intent intent = new Intent(MainActivity.this, CustomThemeActivity.class);
                                startActivity(intent);
                                loadInterAd();
                            }

                            @Override
                            public void onAdImpression() {
                                // Called when an impression is recorded for an ad.
                                Log.d(TAG, "Ad recorded an impression.");
                            }

                            @Override
                            public void onAdShowedFullScreenContent() {
                                // Called when ad is shown.
                                Log.d(TAG, "Ad showed fullscreen content.");
                            }
                        });
                    }else {

                        Intent intent = new Intent(MainActivity.this, CustomThemeActivity.class);
                        startActivity(intent);
                    }

                } else {

                    Intent intent = new Intent(MainActivity.this, CustomThemeActivity.class);
                    startActivity(intent);
                }
            }
        });

        settings.add(new SettingStruct("Manage Overlay Layout", "App Settings", SettingStruct.TYPE_CUSTOM) {
            @Override
            public void onClick(Context c) {
                startActivity(new Intent(MainActivity.this, OverlayLayoutSettingActivity.class));
            }
        });

        settings.add(new SettingStruct("Overlay color", "App Settings", SettingStruct.TYPE_CUSTOM) {
            @Override
            public void onClick(Context ctx) {
                startActivity(new Intent(MainActivity.this, AppearanceActivity.class));
            }
        });

        settings.add(new SettingStruct("Invert long press and click functions", "App Settings", SettingStruct.TYPE_TOGGLE) {
            @Override
            public void onCheckChanged(boolean checked, Context ctx) {
                sharedPreferences.edit().putBoolean("invert_click", checked).apply();
            }

            @Override
            public boolean onAttach(Context ctx) {
                return sharedPreferences.getBoolean("invert_click", false);
            }
        });
        settings.add(new SettingStruct("Enable on lockscreen", "App Settings", SettingStruct.TYPE_TOGGLE) {
            @Override
            public boolean onAttach(Context ctx) {
                return sharedPreferences.getBoolean("enable_on_lockscreen", false);
            }

            @Override
            public void onCheckChanged(boolean checked, Context ctx) {
                sharedPreferences.edit().putBoolean("enable_on_lockscreen", checked).apply();
            }
        });
        settings.add(new SettingStruct("Copy crash logs to clipboard", "App Settings") {
            @Override
            public boolean onAttach(Context ctx) {
                return sharedPreferences.getBoolean("clip_copy_enabled", true);
            }

            @Override
            public void onCheckChanged(boolean checked, Context ctx) {
                sharedPreferences.edit().putBoolean("clip_copy_enabled", checked).apply();
            }
        });
        settings.add(null);
        ExportedPlugins.getPlugins().forEach(x -> {
            settings.add(new SettingStruct("Enable " + x.getName() + " Plugin", x.getName() + " Plugin Settings") {
                             @Override
                             public boolean onAttach(Context ctx) {
                                 return sharedPreferences.getBoolean(x.getID() + "_enabled", true);
                             }

                             @Override
                             public void onCheckChanged(boolean checked, Context ctx) {
                                 sharedPreferences.edit().putBoolean(x.getID() + "_enabled", checked).apply();
                             }
                         }
            );
            if (x.getSettings() != null) {
                settings.addAll(x.getSettings());
            }
            settings.add(null);
        });
        RecylerViewSettingsAdapter adapter = new RecylerViewSettingsAdapter(this, settings);
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(new ItemDecoration());
        recyclerView.invalidateItemDecorations();
    }

    private RecyclerView recyclerView;

    @Override
    protected void onResume() {
        super.onResume();
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);

    }

    private void init() {
        if (sharedPreferences == null) {

            sharedPreferences = getSharedPreferences(getPackageName(), MODE_PRIVATE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
        try {
            unregisterReceiver(broadcastReceiver);
        } catch (Exception ignored) {
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        Intent intent = new Intent(getPackageName() + ".SETTINGS_CHANGED");
        Bundle b = new Bundle();
        sharedPreferences.getAll().forEach((key, value) -> {
            if (value instanceof Boolean) {
                b.putBoolean(key, (boolean) value);

            }
        });
        intent.putExtra("settings", b);
        sendBroadcast(intent);
    }

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(getPackageName() + ".UPDATE_AVAIL")) {
                new MaterialAlertDialogBuilder(MainActivity.this).setTitle("New Update Available")
                        .setMessage("We would like to update this app from " + BuildConfig.VERSION_NAME + " --> " + intent.getExtras().getString("version") +
                                ".\n\nUpdating app generally means better and more stable experience.")
                        .setCancelable(false)
                        .setNegativeButton("Later", (dialogInterface, i) -> {
                            dialogInterface.dismiss();
                        })
                        .setPositiveButton("Update Now", ((dialogInterface, i) -> {
                            if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                                MainActivity.this.sendBroadcast(new Intent(getPackageName() + ".START_UPDATE"));
                                Toast.makeText(MainActivity.this, "Updating in background! Please don't kill the app", Toast.LENGTH_SHORT).show();
                            } else
                                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 102);
                            dialogInterface.dismiss();
                            if (!getPackageManager().canRequestPackageInstalls()) {
                                startActivityForResult(new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES)
                                        .setData(Uri.parse(String.format("package:%s", getPackageName()))), 103);
                                Toast.makeText(MainActivity.this, "Please provide install access to update the application.", Toast.LENGTH_SHORT).show();
                            }
                        }))
                        .show();
            }
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 102 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            MainActivity.this.sendBroadcast(new Intent(getPackageName() + ".START_UPDATE"));
            Toast.makeText(MainActivity.this, "Updating in background! Please don't kill the app", Toast.LENGTH_SHORT).show();
        }
    }

    private int dpToInt(int v) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v, getResources().getDisplayMetrics());
    }

    public class ItemDecoration extends RecyclerView.ItemDecoration {
        @SuppressLint("UseCompatLoadingForDrawables")
        @Override
        public void onDraw(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
            int color = MaterialColors.getColor(MainActivity.this, com.google.android.material.R.attr.colorOnSecondary, getColor(R.color.background_color));
            super.onDraw(c, parent, state);
            int childCount = recyclerView.getChildCount();
            int width = recyclerView.getWidth();
            Rect cornerBounds = new Rect();
            c.getClipBounds(cornerBounds);
            for (int i = 0; i < childCount; i++) {
                View childAt = recyclerView.getChildAt(i);
                RecylerViewSettingsAdapter.ViewHolder viewHolder = (RecylerViewSettingsAdapter.ViewHolder) recyclerView.getChildViewHolder(childAt);
                int vo = recyclerView.getChildAdapterPosition(childAt);
                if (!viewHolder.isItem) {
                    if (settings.size() >= vo + 2 && settings.get(vo + 1) != null) {
                        c.drawText(settings.get(vo + 1).category, cornerBounds.left + dpToInt(10), childAt.getBottom() - dpToInt(30), new Paint() {
                            {
                                setColor(MainActivity.this.getColor(R.color.quite_white));
                                setTextSize(dpToInt(16));
                            }
                        });
                    }
                    Drawable cornerBottom = getDrawable(R.drawable.rounded_corner_setting_bottom);
                    Drawable cornerTop = getDrawable(R.drawable.rounded_corner_setting_top);
                    if (recyclerView.getChildAt(i + 1) != null) {
                        View v = recyclerView.getChildAt(i + 1);
                        cornerTop.setBounds(cornerBounds.left, (int) v.getY() - dpToInt(20), cornerBounds.right, (int) v.getY());
                        cornerTop.draw(c);
                    }
                    if (recyclerView.getChildAt(i - 1) != null) {
                        View v = recyclerView.getChildAt(i - 1);
                        cornerBottom.setBounds(cornerBounds.left, v.getBottom(), cornerBounds.right, v.getBottom() + dpToInt(20));
                        cornerBottom.draw(c);
                    }
                } else {
                    Rect bounds = new Rect(cornerBounds.left, (int) childAt.getY(), cornerBounds.right, childAt.getBottom());
                    c.drawRect(bounds, new Paint() {
                        {
                            setColor(Color.parseColor("#FF4286"));
                        }
                    });
                }

            }
            if (recyclerView.getChildCount() < 1) return;
            if (((RecylerViewSettingsAdapter.ViewHolder) recyclerView.getChildViewHolder(recyclerView.getChildAt(0))).isItem) {
                Drawable roundedCornerTop = getDrawable(R.drawable.rounded_corner_setting_top);
                roundedCornerTop.setBounds(cornerBounds.left, cornerBounds.top, cornerBounds.right, (int) recyclerView.getChildAt(0).getY());
                roundedCornerTop.draw(c);
            }
            if (((RecylerViewSettingsAdapter.ViewHolder) recyclerView.getChildViewHolder(recyclerView.getChildAt(recyclerView.getChildCount() - 1))).isItem) {
                Drawable roundedCornerBottom = getDrawable(R.drawable.rounded_corner_setting_bottom);
                roundedCornerBottom.setBounds(cornerBounds.left,
                        recyclerView.getChildAt(recyclerView.getChildCount() - 1).getBottom(),
                        cornerBounds.right,
                        recyclerView.getChildAt(recyclerView.getChildCount() - 1).getBottom() + dpToInt(20));
                roundedCornerBottom.draw(c);
            }
        }
    }

    private final void checkOverlayPermission() {
        if (!Settings.canDrawOverlays((Context) this)) {
            PermissionDialog.INSTANCE.showDialog((Context) this);
        }
    }

    private void loadInterAd() {
        AdRequest adRequest = new AdRequest.Builder().build();
        InterstitialAd.load(this,getResources().getString(R.string.interstitial_id), adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {

                        mInterstitialAd = interstitialAd;
                        Log.i(TAG, "onAdLoaded");
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        // Handle the error
                        Log.d(TAG, loadAdError.toString());
                        mInterstitialAd = null;
                    }
                });
    }

}

