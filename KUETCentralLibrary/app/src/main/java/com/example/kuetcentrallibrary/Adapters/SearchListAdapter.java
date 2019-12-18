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

import com.example.kuetcentrallibrary.R;
import com.example.kuetcentrallibrary.Activities.SearchBooksActivity;
import com.example.kuetcentrallibrary.Holders.SearchHolder;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class SearchListAdapter extends BaseAdapter {
    private final ArrayList<SearchHolder> list;
    private final String query;
    private final ListView searchList;
    private Context context;
    private int currentOffset;
    private boolean enableButton;
    private int buttonPos;
    private TextView resNum;


    public SearchListAdapter(Context context, ArrayList<SearchHolder> list, int currentOffset, int lastoffset, String query, ListView searchList, TextView resNum) {
        this.context = context;
        this.list = list;
        this.currentOffset = currentOffset;
        this.buttonPos = list.size();
        this.query = query;
        this.searchList = searchList;
        this.resNum = resNum;

        if(currentOffset == lastoffset) enableButton = false;
        else enableButton = true;
    }

    @Override
    public int getCount() {
        if(enableButton) return list.size()+1;
        else return list.size();
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
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        if(position == buttonPos) return 1;
        else return 0;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        int type = getItemViewType(position);

        if(type == 1){
            if(view == null){
                LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = layoutInflater.inflate(R.layout.more_button_layout,parent,false);
            }

            final Button moreButton = view.findViewById(R.id.more_button);
            moreButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int newOffset = currentOffset+50;
                    new SearchBooksActivity.Bookfinder(query,newOffset+"",context,searchList, resNum, list).execute();
                    moreButton.setEnabled(false);
                }
            });
        }
        else {
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

            textViewFine.setVisibility(View.GONE);
            renewButton.setVisibility(View.GONE);
            String imgUrl = list.get(position).imageUrl;

            if(imgUrl != null) Picasso.get().load(list.get(position).imageUrl).into(imageView);
            textViewTitle.setText(list.get(position).title);
            textViewAuthor.setText(list.get(position).author);
            textViewCall.setText(list.get(position).edition);
            textViewDue.setText(list.get(position).publisher);
            textViewRemained.setText(list.get(position).availability);
        }

        return view;
    }
}
