package com.example.kuetcentrallibrary.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.app.MediaRouteButton;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.example.kuetcentrallibrary.Adapters.BorrowListAdapter;
import com.example.kuetcentrallibrary.Holders.BorrowSampleHolder;
import com.example.kuetcentrallibrary.Holders.CookieHolderExchange;
import com.example.kuetcentrallibrary.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Map;

public class SummaryActivity extends AppCompatActivity {

    private static Type holderArrayType;
    private static SharedPreferences sharedPreferences;
    private static LinearLayout progressLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);

        setTitle("Borrowed Items");

        progressLayout = findViewById(R.id.progress_bar);

        CookieHolderExchange cookieHolderExchange = null;
        Map<String,String> cookies = null;

        Intent intent = getIntent();

        if(intent != null) {
            cookieHolderExchange = (CookieHolderExchange) intent.getSerializableExtra("cookieHolder");
        }

        if (cookieHolderExchange != null) {
            cookies = cookieHolderExchange.cookieHolder;
        }

        ListView listView = findViewById(R.id.borrow_list);

        //saved data load
        holderArrayType = new TypeToken<ArrayList<BorrowSampleHolder> >(){}.getType();

        sharedPreferences = getSharedPreferences("data",Context.MODE_PRIVATE);
        String json = sharedPreferences.getString("summary",null);

        if (json != null) {
            Gson gson = new Gson();
            ArrayList<BorrowSampleHolder> list = gson.fromJson(json, holderArrayType);
            BorrowListAdapter adapter = new BorrowListAdapter(SummaryActivity.this,list,cookies,true);
            listView.setAdapter(adapter);
        }
        else progressVis();

        if(cookies != null) {
            new GetHtml(listView,cookies,SummaryActivity.this,null, null)
                    .execute("http://library.kuet.ac.bd:8000/cgi-bin/koha/opac-user.pl");
        }
    }

    private void progressVis(){
        progressLayout.setVisibility(View.VISIBLE);
    }

    private static void progressInvis(){
        progressLayout.setVisibility(View.INVISIBLE);
    }

    public static class GetHtml extends AsyncTask<String, String, Document> {

        private Map<String,String> cookieHolder;
        private ListView listView;
        private Context context;
        private ArrayList<BorrowSampleHolder> list;
        private boolean isFirst;
        private final BorrowListAdapter adapter;
        private boolean hasNetProblem;

        public GetHtml(ListView listView, Map<String, String> cookieHolder, Context context, ArrayList<BorrowSampleHolder> list, BorrowListAdapter adapter) {
            this.listView = listView;
            this.cookieHolder = cookieHolder;
            this.context = context;
            this.list = list;
            this.adapter = adapter;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            if(listView == null) isFirst = false;
            else isFirst = true;
        }

        @Override
        protected Document doInBackground(String... strings) {
            Document document = null;

            try {
                document = Jsoup.connect(strings[0])
                        .cookies(cookieHolder)
                        .userAgent("Mozilla/5.0")
                        .post();
            } catch (IOException e) {
                e.printStackTrace();
            }


            return document;
        }

        @Override
        protected void onPostExecute(Document document) {
            super.onPostExecute(document);

            progressInvis();

            if(document == null){
                Toast.makeText(context,"No internet or server down\nFailed to update",Toast.LENGTH_LONG).show();

                return;
            }

            Elements check = document.select("div#members");

            if( !check.first().text().contains("Welcome")){
                Toast.makeText(context,"Your session has been expired\nFailed to update",Toast.LENGTH_LONG).show();

                return;
            }

            ArrayList<BorrowSampleHolder> borrowSampleHolderArrayList = new ArrayList<>();
            Elements table = document.select("tbody");
            if( table.first().text().equals("You have nothing checked out")){
                Toast.makeText(context,table.first().text(),Toast.LENGTH_LONG).show();
            }
            else{
                Elements rows = table.first().select("tr");

                for(Element row : rows) {
                    BorrowSampleHolder borrowSampleHolder = new BorrowSampleHolder();

                    Elements data = row.select("td");

                    //image
                    Elements imageAnchor = data.select("a");
                    Elements image = imageAnchor.select("img");
                    borrowSampleHolder.imageUrl = image.attr("src");

                    //title
                    data = data.next();
                    Elements titleAnchor = data.select("a");
                    borrowSampleHolder.bookUrl = titleAnchor.attr("href");
                    borrowSampleHolder.title = titleAnchor.first().text();

                    //author
                    data = data.next();
                    borrowSampleHolder.author = data.first().text();
                    if(borrowSampleHolder.author.equals("")) borrowSampleHolder.author = "Not Specified";

                    //due
                    data = data.next();
                    Elements spanDue = data.select("span");
                    borrowSampleHolder.due = spanDue.first().text();

                    //callNo
                    data = data.next();
                    borrowSampleHolder.callNo = data.first().text();

                    //renew
                    data =data.next();
                    Elements renAnchor = data.select("a");
                    borrowSampleHolder.renewUrl = "http://library.kuet.ac.bd:8000"+renAnchor.attr("href");
                    Elements renSpan = data.select("span");
                    borrowSampleHolder.renewText = renSpan.first().text();

                    Elements fineTd = data.next();
                    borrowSampleHolder.fine = fineTd.text();

                    borrowSampleHolderArrayList.add(borrowSampleHolder);
                }


                if(isFirst){
                    BorrowListAdapter borrowListAdapter = new BorrowListAdapter(context,borrowSampleHolderArrayList,cookieHolder,  false);
                    listView.setAdapter(borrowListAdapter);
                }
                else{
                    list.clear();
                    list.addAll(borrowSampleHolderArrayList);
                    adapter.notifyDataSetChanged();
                }
            }



            //Saving current state
            Gson gson = new Gson();
            String json = gson.toJson(borrowSampleHolderArrayList,holderArrayType);

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("summary",json);

            editor.apply();
        }
    }
}
