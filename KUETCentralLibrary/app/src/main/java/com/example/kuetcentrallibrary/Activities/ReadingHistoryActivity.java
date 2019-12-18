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

import com.example.kuetcentrallibrary.Adapters.ReadingHistoryAdapter;
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

public class ReadingHistoryActivity extends AppCompatActivity {

    private SharedPreferences sharedPreferences;
    private Type holderArrayType;
    private LinearLayout progressLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reading_history);

        setTitle("Reading History");

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

        ListView historyList = findViewById(R.id.history_list);

        //offline
        sharedPreferences = getSharedPreferences("data", Context.MODE_PRIVATE);
        String json = sharedPreferences.getString("history",null);

        holderArrayType = new TypeToken<ArrayList<BorrowSampleHolder> >(){}.getType();

        if (json != null) {
            Gson gson = new Gson();
            ArrayList<BorrowSampleHolder> list = gson.fromJson(json, holderArrayType);
            ReadingHistoryAdapter adapter = new ReadingHistoryAdapter(ReadingHistoryActivity.this,list,true);
            historyList.setAdapter(adapter);
        }
        else progressVis();

        if (cookies != null) {
            new LoadHistory(historyList,cookies).execute("http://library.kuet.ac.bd:8000/cgi-bin/koha/opac-readingrecord.pl?limit=full");
        }

    }
    private void progressVis(){
        progressLayout.setVisibility(View.VISIBLE);
    }

    private void progressInvis(){
        progressLayout.setVisibility(View.INVISIBLE);
    }

    private class LoadHistory extends AsyncTask<String,String, Document>{

        private final ListView listView;
        private Map<String, String> cookieHolder;

        public LoadHistory(ListView historyList, Map<String, String> cookies) {
            this.cookieHolder = cookies;
            this.listView = historyList;
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
                Toast.makeText(ReadingHistoryActivity.this,"No internet or server down\nFailed to update",Toast.LENGTH_LONG).show();

                return;
            }

            Elements check = document.select("div#members");

            if( !check.first().text().contains("Welcome")){
                Toast.makeText(ReadingHistoryActivity.this,"Your session has been expired\nFailed to update",Toast.LENGTH_LONG).show();

                return;
            }

            Elements tbodys = document.select("tbody");
            Elements trs = tbodys.select("tr");

            ArrayList<BorrowSampleHolder> borrowSampleHolderArrayList = new ArrayList<>();

            for(Element tr : trs){
                BorrowSampleHolder borrowSampleHolder = new BorrowSampleHolder();

                Elements tds = tr.select("td");

                Elements img = tds.get(1).select("img");
                borrowSampleHolder.imageUrl = img.first().attr("src");

                Elements titleA = tds.get(2).select("a");
                borrowSampleHolder.title = titleA.first().text();
                Elements titleSpan = tds.get(2).select("span");
                borrowSampleHolder.author = titleSpan.first().text();
                if(borrowSampleHolder.author.equals("")) borrowSampleHolder.author = "Not Specified";

                borrowSampleHolder.callNo = tds.get(4).text();
                borrowSampleHolder.due = tds.get(5).text();

                borrowSampleHolderArrayList.add(borrowSampleHolder);
            }

            ReadingHistoryAdapter adapter = new ReadingHistoryAdapter(ReadingHistoryActivity.this,borrowSampleHolderArrayList, false);
            listView.setAdapter(adapter);

            //Saving current state
            Gson gson = new Gson();
            String json = gson.toJson(borrowSampleHolderArrayList,holderArrayType);

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("history",json);

            editor.apply();
        }
    }
}
