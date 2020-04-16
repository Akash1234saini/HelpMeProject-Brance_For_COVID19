package com.proapps.akashsaini.helpme;

import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    // Remote Config keys
    private static final String UPDATE_AVAILABLE_CONFIG_KEY = "update_available";
    private static final String UPDATE_VERSION_CONFIG_KEY = "update_version";

    private SharedPreferences sharedPreferences;

    private FirebaseRemoteConfig mFirebaseRemoteConfig;

    private final static String TAG = MainActivity.class.getSimpleName();

    // Two string instance to compare old and new version of app so update dialog will show at once for every new version
    private String old_app_version;
    private String new_app_version;

    private int timesPressedBackButton = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        loadLocale();
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        TabLayout tabLayout = findViewById(R.id.tabLayout);
        ViewPager viewPager = findViewById(R.id.viewPager);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation_view);

        bottomNavigationView.setSelectedItemId(R.id.action_home);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                if (item.getItemId() == R.id.action_live) {
                    Intent intent = new Intent(MainActivity.this, LiveCoronaPatientUpdateActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                    finish();
                    return true;
                } else {
                    return false;
                }
            }
        });

        // Get Remote Config instance.
        // [START get_remote_config_instance]
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();

        // Create a Remote Config Setting to enable developer mode, which you can use to increase
        // the number of fetches available per hour during development. Also use Remote Config
        // Setting to set the minimum fetch interval.
        // [START enable_dev_mode]
//        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
//                .setMinimumFetchIntervalInSeconds(3600)
//                .build();
//        mFirebaseRemoteConfig.setConfigSettingsAsync(configSettings);

        // Set default Remote Config parameter values. An app uses the in-app default values, and
        // when you need to adjust those defaults, you set an updated value for only the values you
        // want to change in the Firebase console. See Best Practices in the README for more
        // information.
        mFirebaseRemoteConfig.setDefaults(R.xml.remote_config_defaults);

        setSupportActionBar(toolbar);

        NumbersFragmentPagerAdapter adapter = new NumbersFragmentPagerAdapter(getSupportFragmentManager(), this);
        viewPager.setAdapter(adapter);

        tabLayout.setupWithViewPager(viewPager);

        // getting old version of app (basically fetching version before update new value from firebase remote config).
        old_app_version = mFirebaseRemoteConfig.getString(UPDATE_VERSION_CONFIG_KEY);

        // cacheExpirationSeconds is set to cacheExpiration here, indicating the next fetch request
        // will use fetch data from the Remote Config service, rather than cached parameter values,
        // if cached parameter values are more than cacheExpiration seconds old.
        // See Best Practices in the README for more information.
        mFirebaseRemoteConfig.fetchAndActivate()
                .addOnCompleteListener(this, new OnCompleteListener<Boolean>() {
                    @Override
                    public void onComplete(@NonNull Task<Boolean> task) {
                        if (task.isSuccessful()) {
                            boolean updated = false;
                            if (task.getResult() != null) {
                                updated = task.getResult();
                            }
                            Log.d(TAG, "Config params updated: " + updated);
                        } else {
                            Log.d(TAG, "failed to fetch values");
                        }
                        displayUpdateDialog();
                    }
                });
    }

    private void displayUpdateDialog() {
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        boolean availableUpdate = mFirebaseRemoteConfig.getBoolean(UPDATE_AVAILABLE_CONFIG_KEY);
        String updateVersion = mFirebaseRemoteConfig.getString(UPDATE_VERSION_CONFIG_KEY);

        // getting new app version.
        new_app_version = mFirebaseRemoteConfig.getString(UPDATE_VERSION_CONFIG_KEY);

        int oldVersionSum = Integer.parseInt(old_app_version.charAt(0) + "")
                + Integer.parseInt(old_app_version.charAt(2) + "")
                + Integer.parseInt(old_app_version.charAt(4) + "");
        int newVersionSum = Integer.parseInt(new_app_version.charAt(0) + "")
                + Integer.parseInt(new_app_version.charAt(2) + "")
                + Integer.parseInt(new_app_version.charAt(4) + "");

        Log.i(TAG, "old version (" + old_app_version + ")" + oldVersionSum + ", new version sum (" + new_app_version + ")" + newVersionSum);

        if (!availableUpdate)
            return;
            // check if the currently available update false then store false into shared preference.
        else if (oldVersionSum < newVersionSum) {
            sharedPreferences.edit().putBoolean("UpdateCanceled", false).apply();
        }

        // if value updated the value will be change of isChecked preference.
        boolean isChecked = sharedPreferences.getBoolean("UpdateCanceled", false);

        if (!isChecked) {

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            ViewGroup viewGroup = findViewById(android.R.id.content);
            final View dialogView = LayoutInflater.from(this).inflate(R.layout.customview, viewGroup, false);
            builder.setView(dialogView);
            builder.setCancelable(false);

            final CheckBox checkBox = dialogView.findViewById(R.id.checkBox);
            TextView dontShowAgain = dialogView.findViewById(R.id.dontShowTextView);
            dontShowAgain.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (checkBox.isChecked())
                        checkBox.setChecked(false);
                    else
                        checkBox.setChecked(true);
                }
            });

            TextView title = dialogView.findViewById(R.id.dialog_title);
            TextView message = dialogView.findViewById(R.id.dialog_message);

            String titleString = getString(R.string.update_dialog_title);
            title.setText(titleString + " (" + updateVersion + "v)");
            message.setText(R.string.update_dialog_message);

            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                    if (checkBox.isChecked())
                        sharedPreferences.edit().putBoolean("UpdateCanceled", true).apply();

                    // open Google Play Store to update app
                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getPackageName())));
                    } catch (ActivityNotFoundException e) {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + getPackageName())));
                    }
                    dialogInterface.dismiss();
                }
            })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if (checkBox.isChecked())
                                sharedPreferences.edit().putBoolean("UpdateCanceled", true).apply();
                            dialogInterface.cancel();
                        }
                    }).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        MenuItem m = menu.findItem(R.id.action_signin).setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        switch (item.getItemId()) {
            case R.id.action_signin:
                Intent signinIntent = new Intent(MainActivity.this, AddPublicHelplineNumberActivity.class);
                startActivity(signinIntent);
                break;
            case R.id.action_setting:
                Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(settingsIntent);
                break;
            case R.id.action_help:
                startActivity(new Intent(this, HelpActivity.class));
                break;
            case R.id.action_share:
                String aboutApp = "CoronaVirus HelpLines\n";
//                    aboutApp += getString(R.string.this_app_is_for);
                aboutApp += "\nDownload this app from here:\nhttps://play.google.com/store/apps/details?id=" + getPackageName();
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, "COVID19 Emergency");
                shareIntent.putExtra(Intent.EXTRA_TEXT, aboutApp);
                startActivity(Intent.createChooser(shareIntent, "Share via"));
                break;
            case R.id.action_check_for_update:
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getPackageName())));
                } catch (ActivityNotFoundException e) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + getPackageName())));
                }
                break;
            case R.id.action_search:
                Intent searchIntent = new Intent(MainActivity.this, SearchActivity.class);
                startActivity(searchIntent);
                break;
            case R.id.action_language:

                final AlertDialog.Builder customDialogBuilder = new AlertDialog.Builder(this);
                ViewGroup viewGroup = findViewById(android.R.id.content);
                final View dialogView = LayoutInflater.from(this).inflate(R.layout.laguage_list_layout, viewGroup, false);
                customDialogBuilder.setView(dialogView);

                String[] languageString = (getResources().getStringArray(R.array.selectable_languages));
                final ArrayList languageArray = new ArrayList<String>(Arrays.asList(languageString));
                final ArrayAdapter<String> adapter = new ArrayAdapter<String>(Objects.requireNonNull(this), android.R.layout.select_dialog_item);
                adapter.addAll(languageArray);

                customDialogBuilder.setAdapter(adapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int position) {

                        switch (position) {
                            case 0:
                                setLocale("en");
                                break;
                            case 1:
                                setLocale("hi");
                                break;
                            case 2:
                                setLocale("gu");
                                break;
                            case 3:
                                setLocale("mr");
                                break;
                            case 4:
                                setLocale("ur");
                                break;
                            case 5:
                                setLocale("ml");
                                break;
                            case 6:
                                setLocale("ta");
                                break;
                            case 7:
                                setLocale("pa");
                                break;
                            case 8:
                                setLocale("bn");
                                break;
                        }
                        dialog.dismiss();
                        recreate();
                    }
                }).show();
        }

        return super.onOptionsItemSelected(item);
    }

    private void setLocale(String language) {

        Locale locale = new Locale(language);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.setLocale(locale);
        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());

        // set language into preference
        sharedPreferences.edit().putString("selected_language", language).apply();
    }

    private void loadLocale() {
        String selectedLanguage = sharedPreferences.getString("selected_language", "");
//        Log.i(TAG, selectedLanguage);
        setLocale(selectedLanguage);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (timesPressedBackButton >= 2) {
            finish();
        } else
            Toast.makeText(this, R.string.press_one_more_time, Toast.LENGTH_SHORT).show();
        timesPressedBackButton++;
    }
}
