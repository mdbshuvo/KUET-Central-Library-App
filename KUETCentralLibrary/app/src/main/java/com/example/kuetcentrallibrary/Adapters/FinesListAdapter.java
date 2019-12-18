package com.example.kuetcentrallibrary.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.kuetcentrallibrary.Holders.FineSampleHolder;
import com.example.kuetcentrallibrary.R;

import java.util.ArrayList;

public class FinesListAdapter extends BaseAdapter {
    private final Context context;
    private final ArrayList<FineSampleHolder> list;

    public FinesListAdapter(Context context, ArrayList<FineSampleHolder> list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int i) {
        return list.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public int getItemViewType(int position) {
        if(position == 0) return 1;
        else return 0;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if(view == null){
            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = layoutInflater.inflate(R.layout.fine_smaple_layout,viewGroup,false);
        }

        TextView date = view.findViewById(R.id.fine_date);
        TextView des = view.findViewById(R.id.description_text);
        TextView amount = view.findViewById(R.id.amount_text);
        TextView out = view.findViewById(R.id.outstanding_text);

        date.setText(list.get(i).date);
        des.setText(list.get(i).title);
        amount.setText(list.get(i).fine);
        out.setText(list.get(i).outStanding);

        int type = getItemViewType(i);

        if(type == 1){
            view.setBackgroundColor(context.getResources().getColor(R.color.green));
            date.setBackgroundColor(context.getResources().getColor(R.color.green));
            amount.setBackgroundColor(context.getResources().getColor(R.color.green));
        }
        else {
            date.setBackgroundColor(context.getResources().getColor(R.color.lightGray));
            amount.setBackgroundColor(context.getResources().getColor(R.color.lightGray));
        }


        return view;
    }
}
