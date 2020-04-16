package com.proapps.akashsaini.helpme;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;

public class LicenseAgreementActivity extends AppCompatActivity {

    private SharedPreferences sharedPreferences;
    private final static String TAG = LicenseAgreementActivity.class.getCanonicalName();
    private final static int REQUEST_CODE_CALL_PHONE_AND_INTERNET = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        loadLocale();
        setContentView(R.layout.activity_agreement_license);

        TextView links = findViewById(R.id.linksTextView);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        boolean licenseAgreed = sharedPreferences.getBoolean("licenseAccepted", false);
        if (licenseAgreed) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }

        SpannableString spannableString = new SpannableString(getString(R.string.link_of_TnC_and_PP));
        ClickableSpan termAndCondition = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://vishalsaini16297.wixsite.com/privacypolicycovid19/blank-page-1"));
                startActivity(webIntent);
            }
        };

        ClickableSpan privacyAndPolicy = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://vishalsaini16297.wixsite.com/privacypolicycovid19/blank-page"));
                startActivity(webIntent);
            }
        };

        spannableString.setSpan(termAndCondition, 87, 103, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(privacyAndPolicy, 112, 126, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        links.setText(spannableString);
        links.setMovementMethod(LinkMovementMethod.getInstance());
    }

    public void allowPermissions(View view) {

        if ((ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED)
                || (ActivityCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE, Manifest.permission.INTERNET},
                    REQUEST_CODE_CALL_PHONE_AND_INTERNET);

        } else {
            agreementAccepted();
        }
    }

    private void agreementAccepted() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE_CALL_PHONE_AND_INTERNET)
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                sharedPreferences.edit().putBoolean("licenseAccepted", true).apply();
                agreementAccepted();
            } else {
                Toast.makeText(LicenseAgreementActivity.this, "Allow permissions to agree", Toast.LENGTH_SHORT).show();
            }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.license_agreement_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.action_language) {

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

                    switch (position){
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

    private void setLocale(String language){
        Locale locale = new Locale(language);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());

        // set language into preference
        sharedPreferences.edit().putString("selected_language",language).apply();
    }

    private void loadLocale(){
        String selectedLanguage = sharedPreferences.getString("selected_language", "");
//        Log.i(TAG, selectedLanguage);
        setLocale(selectedLanguage);
    }
}
