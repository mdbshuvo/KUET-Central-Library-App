package com.example.kuetcentrallibrary.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.kuetcentrallibrary.Holders.SearchHolder;
import com.example.kuetcentrallibrary.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class JournalListAdapter extends BaseAdapter {
    private final ArrayList<SearchHolder> list;
    private final ListView searchList;
    private Context context;


    public JournalListAdapter(Context context, ArrayList<SearchHolder> list, ListView searchList) {
        this.context = context;
        this.list = list;
        this.searchList = searchList;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {

        if(view == null){
            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = layoutInflater.inflate(R.layout.borrow_item_layout,parent,false);
        }

        ImageView imageView = view.findViewById(R.id.imageView3);
        TextView textViewTitle = view.findViewById(R.id.textViewTitle);
        TextView textViewAuthor = view.findViewById(R.id.textViewAuthor);
        TextView textViewCall = view.findViewById(R.id.textViewCall);
        TextView textViewDue = view.findViewById(R.id.textViewDue);
        TextView textViewRemained = view.findViewById(R.id.textViewRemained);
        TextView textViewFine = view.findViewById(R.id.textViewFine);
        Button renewButton = view.findViewById(R.id.button);

        textViewRemained.setVisibility(View.GONE);
        textViewFine.setVisibility(View.GONE);
        renewButton.setVisibility(View.GONE);

        String imgUrl = list.get(position).imageUrl;

        if(imgUrl != null) Picasso.get().load(list.get(position).imageUrl).into(imageView);
        textViewTitle.setText(list.get(position).title);
        textViewAuthor.setText(list.get(position).author);
        textViewCall.setText(list.get(position).callNo);
        textViewDue.setText(list.get(position).publisher);

        return view;
    }
}
