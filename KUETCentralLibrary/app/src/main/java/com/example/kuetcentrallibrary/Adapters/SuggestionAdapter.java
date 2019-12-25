package com.example.kuetcentrallibrary.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.kuetcentrallibrary.Activities.BookListActivity;
import com.example.kuetcentrallibrary.R;

public class SuggestionAdapter extends BaseAdapter {
    private final Context context;
    private final String[] terms;

    public SuggestionAdapter(Context context, String[] terms) {
        this.context = context;
        this.terms = terms;
    }

    @Override
    public int getCount() {
        return terms.length;
    }

    @Override
    public Object getItem(int position) {
        return terms[position];
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

        termText.setText(terms[position]);
        firstButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //cause bakigula lekhi nai to
                if(position>0){
                    Toast.makeText(context, "This entry is not signed yet", Toast.LENGTH_SHORT).show();
                    return;
                }

                Intent intent = new Intent(context, BookListActivity.class);
                intent.putExtra("term",( position + 1 ) * 10 + 1);

                context.startActivity(intent);
            }
        });

        secondButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //cause bakigula lekhi nai to
                if(position>0){
                    Toast.makeText(context, "This entry is not signed yet", Toast.LENGTH_SHORT).show();
                    return;
                }

                Intent intent = new Intent(context, BookListActivity.class);
                intent.putExtra("term",( position + 1 ) * 10 + 2);

                context.startActivity(intent);
            }
        });

        return view;
    }
}
