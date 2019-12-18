package com.example.kuetcentrallibrary.Adapters;

import android.content.Context;
import android.content.res.TypedArray;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.kuetcentrallibrary.R;

public class CustomAdpater extends BaseAdapter {
    private Context context;
    private String[] options;
    private int[] menu_pic_id;

    public CustomAdpater(Context context, String[] options, int[] menu_pic_id) {
        this.context = context;
        this.options = options;
        this.menu_pic_id=menu_pic_id;
    }

    @Override
    public int getCount() {
        return options.length;
    }

    @Override
    public String getItem(int i) {
        return options[i];
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        final String LOGOUT = "Log Out";
        if (getItem(position).equals(LOGOUT)) return 1;
        else return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if(view == null){
            view = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.main_list_element,viewGroup,false);
        }

        TextView textView = view.findViewById(R.id.element_text);
        ImageView imageView = view.findViewById(R.id.menu_pic);
        textView.setText(options[i]);
//        int pic_id = menu_pic_id.getResourceId(i,-1);
        imageView.setImageResource(menu_pic_id[i]);

        if(getItemViewType(i) == 1){     //the logout button
            textView.setBackgroundColor(context.getResources().getColor(R.color.colorAccent));
            textView.setTextColor(context.getResources().getColor(R.color.white));
        }

//        if(i == 1) view.setVisibility(View.GONE);

        return view;
    }
}
