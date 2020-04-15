package com.proapps.akashsaini.helpme;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

public class LiveCoronaPatientAdapter extends ArrayAdapter<LiveCoronaPatient> {


    public LiveCoronaPatientAdapter(Activity context, ArrayList<LiveCoronaPatient> list) {
        super(context, 0, list);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View rootView = convertView;

        if (rootView == null)
            rootView = LayoutInflater.from(getContext()).inflate(R.layout.global_corona_live_report_layout, parent, false);

        ImageView flag = rootView.findViewById(R.id.flag_image_view);
        TextView country = rootView.findViewById(R.id.country_text_view);
        TextView cases = rootView.findViewById(R.id.cases_text_view);
        TextView deaths = rootView.findViewById(R.id.deaths_text_view);
        TextView recovered = rootView.findViewById(R.id.recovered_text_view);

        LiveCoronaPatient currentCountry = getItem(position);

        assert currentCountry != null;
        flag.setImageResource(currentCountry.getmFlagImage());
        country.setText(currentCountry.getmCountry());
        cases.setText(currentCountry.getmCases());
        deaths.setText(currentCountry.getmDeaths());
        recovered.setText(currentCountry.getmRecovered());

        return rootView;
    }
}
