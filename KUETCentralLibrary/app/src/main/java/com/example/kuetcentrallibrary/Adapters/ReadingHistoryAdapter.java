package com.example.kuetcentrallibrary.Adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.kuetcentrallibrary.Holders.BorrowSampleHolder;
import com.example.kuetcentrallibrary.Auxilaries.ImageSaveRetrieve;
import com.example.kuetcentrallibrary.R;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class ReadingHistoryAdapter extends BaseAdapter {

    private final ArrayList<BorrowSampleHolder> list;
    private final boolean hasSavedState;
    private Context context;
    private boolean[] isFinishedDownload;

    public ReadingHistoryAdapter(Context context, ArrayList<BorrowSampleHolder> list, boolean hasSavedState) {
        this.context = context;
        this.list = list;
        this.hasSavedState = hasSavedState;
        this.isFinishedDownload = new boolean[list.size()];

        for(int i = 0; i < isFinishedDownload.length; i++){
            isFinishedDownload[i] = false;
        }
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
    public View getView(int position, View view, ViewGroup viewGroup) {
        if(view == null){
            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = layoutInflater.inflate(R.layout.borrow_item_layout,viewGroup,false);
        }


        ImageView imageView = view.findViewById(R.id.imageView3);
        TextView textViewTitle = view.findViewById(R.id.textViewTitle);
        TextView textViewAuthor = view.findViewById(R.id.textViewAuthor);
        TextView textViewCall = view.findViewById(R.id.textViewCall);
        TextView textViewDue = view.findViewById(R.id.textViewDue);
        TextView textViewRemained = view.findViewById(R.id.textViewRemained);
        TextView textViewFine = view.findViewById(R.id.textViewFine);
        Button renewButton = view.findViewById(R.id.button);

//        Picasso.get().load(list.get(position).imageUrl).into(imageView);
        textViewTitle.setText(list.get(position).title);
        textViewAuthor.setText(list.get(position).author);
        textViewCall.setText(list.get(position).callNo);
        textViewDue.setText(list.get(position).due);
        textViewRemained.setVisibility(View.GONE);
        textViewFine.setVisibility(View.GONE);
        renewButton.setVisibility(View.GONE);

        if(hasSavedState || isFinishedDownload[position]){
            imageView.setImageBitmap(ImageSaveRetrieve.retrieveImage(context,"history_"+position));
        }
        else {
            new PictureDownloader(imageView,isFinishedDownload,context,position).execute(list.get(position).imageUrl);
        }


        return view;
    }

    private static class PictureDownloader extends AsyncTask<String,String, Bitmap> {

        private final ImageView imageView;
        private final Context context;
        private final int position;
        private final boolean[] isFinishedDownload;

        public PictureDownloader(ImageView imageView, boolean[] isFinishedDownload, Context context, int i) {
            this.imageView= imageView;
            this.context = context;
            this.position = i;
            this.isFinishedDownload = isFinishedDownload;
        }

        @Override
        protected Bitmap doInBackground(String... strings) {

            Bitmap bitmap = null;
            try {
                InputStream input = new java.net.URL(strings[0]).openStream();
                bitmap = BitmapFactory.decodeStream(input);
            } catch (IOException e) {
                e.printStackTrace();
            }

            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);

            imageView.setImageBitmap(bitmap);
            ImageSaveRetrieve.saveImage(context,"history_"+position,bitmap);

            isFinishedDownload[position] = true;
        }
    }
}
