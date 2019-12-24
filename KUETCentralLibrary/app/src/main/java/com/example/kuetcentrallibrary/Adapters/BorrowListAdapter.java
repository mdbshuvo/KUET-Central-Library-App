package com.example.kuetcentrallibrary.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.Toast;

import com.example.kuetcentrallibrary.Holders.BorrowSampleHolder;
import com.example.kuetcentrallibrary.Auxilaries.ImageSaveRetrieve;
import com.example.kuetcentrallibrary.R;
import com.example.kuetcentrallibrary.Activities.SummaryActivity;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Map;

public class BorrowListAdapter extends BaseAdapter {
    private final Context context;
    private final ArrayList<BorrowSampleHolder> list;
    private final boolean hasSavedState;
    private Map<String, String> cookieHolder;
    private BorrowListAdapter adapter;
    private boolean[] isFinishedDownload;

    public BorrowListAdapter(Context context, ArrayList<BorrowSampleHolder> list, Map<String, String> cookieHolder, boolean hasSavedState) {
        this.context = context;
        this.list = list;
        this.cookieHolder = cookieHolder;
        this.hasSavedState = hasSavedState;
        this.adapter = this;
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
    public Object getItem(int i) {
        return list.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(final int i, View view, ViewGroup viewGroup) {
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
        final Button renewButton = view.findViewById(R.id.button);

//        Picasso.get().load(list.get(i).imageUrl).into(imageView);
        textViewTitle.setText(list.get(i).title);
        textViewAuthor.setText(list.get(i).author);
        textViewCall.setText(list.get(i).callNo);
        textViewDue.setText(list.get(i).due);
        textViewRemained.setText(list.get(i).renewText);
        textViewFine.setText(list.get(i).fine);
        renewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Warning");
                builder.setMessage("Are you sure you want to renenw this item?");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int j) {

                        renewButton.setEnabled(false);
                        renewButton.setBackgroundColor(context.getResources().getColor(R.color.backgroundGray));
                        renewButton.setText(context.getResources().getString(R.string.renewing));
                        new Renew(renewButton).execute(list.get(i).renewUrl);

                        new SummaryActivity.GetHtml(null,cookieHolder,context,list,adapter)
                                .execute("http://library.kuet.ac.bd:8000/cgi-bin/koha/opac-user.pl");
                    }
                });

                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int j) {
                        Toast.makeText(context,"You choose not to renew",Toast.LENGTH_LONG).show();
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        //image showing
        if(hasSavedState || isFinishedDownload[i]){
            imageView.setImageBitmap(ImageSaveRetrieve.retrieveImage(context,"summary_"+i));
        }else{
            new PictureDownloader(imageView,isFinishedDownload,context,i).execute(list.get(i).imageUrl);
        }

        //offline mode
        if(cookieHolder == null /* || renewal  available is zero*/){
            renewButton.setVisibility(View.GONE);
        }

        return view;
    }

    private class Renew extends AsyncTask<String, String, Document> {
        private Button button;

        public Renew(Button renewButton) {
            button = renewButton;
        }

        @Override
        protected Document doInBackground(String... strings) {

            Document document = null;
            try {
                document = Jsoup.connect(strings[0])
                        .cookies(cookieHolder)
                        .userAgent("Mozilla/5.0")
                        .get();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return document;
        }

        @Override
        protected void onPostExecute(Document document) {
            super.onPostExecute(document);
            if(document == null){
                Toast.makeText(context,"No internet or server down\nFailed to renew",Toast.LENGTH_LONG).show();
            }
            else{
                Elements check = document.select("div#members");

                if( !check.first().text().contains("Welcome")){
                    Toast.makeText(context,"Your session has been expired\nFailed to renew",Toast.LENGTH_LONG).show();
                }
                else{
                    Toast.makeText(context,"Successfully renewed",Toast.LENGTH_LONG).show();
                }
            }

            button.setEnabled(true);
            button.setBackgroundColor(context.getResources().getColor(R.color.buttonBackground));
            button.setText(context.getResources().getString(R.string.renew));
        }
    }
    private static class PictureDownloader extends AsyncTask<String,String,Bitmap> {

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
            ImageSaveRetrieve.saveImage(context,"summary_"+position,bitmap);
            isFinishedDownload[position] = true;
        }
    }
}
