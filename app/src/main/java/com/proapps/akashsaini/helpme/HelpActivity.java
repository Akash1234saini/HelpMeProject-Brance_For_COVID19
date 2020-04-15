package com.proapps.akashsaini.helpme;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Arrays;

public class HelpActivity extends AppCompatActivity {

    private ArrayList<Help> mHelpArrayList;
    private HelpAdapter mHelpAdapter;
    private ListView mListView;

    private SharedPreferences sharedPreferences;

    private final static String TAG = HelpActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        mListView = findViewById(R.id.listView);

        mHelpArrayList = new ArrayList<>();
        mHelpArrayList.add(new Help(R.drawable.ic_term_and_services, getString(R.string.term_condition_label)));
        mHelpArrayList.add(new Help(R.drawable.ic_privacy_policy, getString(R.string.privacy_policy_label)));
        mHelpArrayList.add(new Help(R.drawable.ic_qsn, getString(R.string.how_to_use_app_label)));
        mHelpArrayList.add(new Help(R.drawable.ic_contact, getString(R.string.contact_us_label)));
        mHelpArrayList.add(new Help(R.drawable.ic_about, getString(R.string.about_us_label)));

        mHelpAdapter = new HelpAdapter(this, mHelpArrayList);
        mListView.setAdapter(mHelpAdapter);

        final ArrayList<String> urlKeys = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.url_keys)));

        sharedPreferences = getSharedPreferences("Settings", MODE_PRIVATE);
        final String selectedLanguage = sharedPreferences.getString("selected_language", "en");
        Log.i(TAG, selectedLanguage);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                // calling intent to open web browser passing through values
                if (selectedLanguage.equals("en") || selectedLanguage.equals("pa"))
                    goToEnglishWebAddress(urlKeys.get(position));
                else
                    goToWebAddress(urlKeys.get(position), selectedLanguage);
            }
        });
    }

    /**
     * Method that use to assign param in url and open web address with that.
     * @param page open a page from 5 pages.
     * @param language which language user currently using in app.
     */
    private void goToWebAddress(String page, String language){
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://vishalsaini16297.wixsite.com/codexeditorsofficial/" + page + "?lang=" + language)));
    }

    /**
     * This method is only for english, punjabi language, because lang=en is working for english and punjabi is available on site.
     * @param page open a page from 5 pages.
     */
    private void goToEnglishWebAddress(String page) {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://vishalsaini16297.wixsite.com/codexeditorsofficial/" + page)));
    }

}
