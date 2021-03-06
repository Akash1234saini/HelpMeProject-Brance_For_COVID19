package com.proapps.akashsaini.helpme;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
    }

    public static class NumberPreferenceFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener {

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            
            addPreferencesFromResource(R.xml.root_preferences);

            Preference orderBy = findPreference(getString(R.string.settings_order_by_key));
            bindPreferenceSummaryToValue(orderBy);

            Preference sortBy = findPreference(getString(R.string.settings_sort_by_key));
            bindPreferenceSummaryToValue(sortBy);

            Preference govSortBy = findPreference(getString(R.string.settings_government_sort_by_key));
            bindPreferenceSummaryToValue(govSortBy);
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            String stringValue = newValue.toString();

            ListPreference listPreference = (ListPreference) preference;
            int preIndex = listPreference.findIndexOfValue(stringValue);
            if (preIndex >= 0) {
                // The list of entries to be shows in the list in subsequent dialogs.
                CharSequence[] labels = listPreference.getEntries();
                // Sets the summary for this Preference with a CharSequence.
                preference.setSummary(labels[preIndex]);
            }
                return true;
        }

        private void bindPreferenceSummaryToValue(Preference preference){
            preference.setOnPreferenceChangeListener(this);
            // Used to help to create Preference hierarchy from activity or XML.
            // Gets a SharedPreference instance that point to the default file
            // that is used by the preference framework in the given context.
            // Contexts: the context of the preferences whose values are wanted.
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(preference.getContext());
            String preferenceString = preferences.getString(preference.getKey(), "");
            onPreferenceChange(preference, preferenceString);
        }
    }
}