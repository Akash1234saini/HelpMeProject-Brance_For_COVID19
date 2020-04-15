package com.proapps.akashsaini.helpme;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class NumberAdapter extends ArrayAdapter<Number> {

    private Context mContext;

    NumberAdapter(AppCompatActivity context, ArrayList<Number> numbers) {
        super(context, 0 , numbers);
        mContext  = context;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull final ViewGroup parent) {
        View rootView = convertView;

        if (rootView == null)
            rootView = LayoutInflater.from(getContext()).inflate(R.layout.number_layout, parent, false);

        TextView numberTitle = rootView.findViewById(R.id.state);
        TextView number = rootView.findViewById(R.id.number);

        final Number currentNumber = getItem(position);

        assert currentNumber != null;
        numberTitle.setText(currentNumber.getmNumberTitle());
        number.setText(currentNumber.getmNumber());

        return rootView;
    }
}
