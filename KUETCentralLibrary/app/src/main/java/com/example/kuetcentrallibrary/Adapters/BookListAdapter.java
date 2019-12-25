package com.example.kuetcentrallibrary.Adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.kuetcentrallibrary.Activities.BookListActivity;
import com.example.kuetcentrallibrary.Activities.SearchBooksActivity;
import com.example.kuetcentrallibrary.R;

public class BookListAdapter extends BaseAdapter {
    private final String[] books;
    private final Context context;

    public BookListAdapter(Context context, String[] books) {
        this.context = context;
        this.books = books;
    }

    @Override
    public int getCount() {
        return books.length;
    }

    @Override
    public Object getItem(int position) {
        return books[position];
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View view, ViewGroup parent) {
        if(view == null){
            view = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.suggestion_item_layout,parent,false);
        }

        TextView termText = view.findViewById(R.id.year);
        Button firstButton = view.findViewById(R.id.first);
        Button secondButton = view.findViewById(R.id.second);

        final String[] title = books[position].split("by");

        termText.setVisibility(View.GONE);

        firstButton.setText(title[0]);
        secondButton.setText(title[1]);

        firstButton.setTypeface(Typeface.DEFAULT_BOLD);
        firstButton.setTextScaleX((float) 1.5);

        firstButton.setClickable(false);
        secondButton.setClickable(false);

        return view;
    }
}
