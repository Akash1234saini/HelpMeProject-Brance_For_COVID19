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

public class HelpAdapter extends ArrayAdapter<Help> {


    public HelpAdapter(Activity context, ArrayList<Help> helpList) {
        super(context, 0 ,helpList);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View rootView = convertView;

        if (rootView == null)
            rootView = LayoutInflater.from(getContext()).inflate(R.layout.help_layout, parent, false);

        // getting all instance of views
        ImageView icon = rootView.findViewById(R.id.image_icon);
        TextView title = rootView.findViewById(R.id.image_text);

        Help currentHelpItem = getItem(position);

        assert currentHelpItem != null;
        icon.setImageDrawable(rootView.getResources().getDrawable(currentHelpItem.getImage()));
        title.setText(currentHelpItem.getTitle());

        return rootView;
    }
}
