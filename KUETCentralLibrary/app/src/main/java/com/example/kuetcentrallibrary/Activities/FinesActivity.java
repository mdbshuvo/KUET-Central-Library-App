package com.example.kuetcentrallibrary.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.example.kuetcentrallibrary.Adapters.FinesListAdapter;
import com.example.kuetcentrallibrary.Auxilaries.PDFSaver;
import com.example.kuetcentrallibrary.Holders.CookieHolderExchange;
import com.example.kuetcentrallibrary.Holders.FineSampleHolder;
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

public class FinesActivity extends AppCompatActivity {

    private SharedPreferences sharedPreferences;
    private Type holderArrayType;
    private LinearLayout progressLayout;
    private Map<String ,String> cookies;
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fines);

        progressLayout = findViewById(R.id.progress_bar);

        setTitle("Your Fines");
        CookieHolderExchange cookieHolderExchange = null;
        cookies = null;

        Intent intent = getIntent();

        if(intent != null) {
            cookieHolderExchange = (CookieHolderExchange) intent.getSerializableExtra("cookieHolder");
        }

        if (cookieHolderExchange != null) {
            cookies = cookieHolderExchange.cookieHolder;
        }

        sharedPreferences = getSharedPreferences("data",Context.MODE_PRIVATE);
        String json = sharedPreferences.getString("fine",null);

        listView = findViewById(R.id.fines_list);
        holderArrayType = new TypeToken<ArrayList<FineSampleHolder> >(){}.getType();

        if (json != null) {
            Gson gson = new Gson();
            ArrayList<FineSampleHolder> list = gson.fromJson(json, holderArrayType);
            FinesListAdapter adapter = new FinesListAdapter(FinesActivity.this,list);
            listView.setAdapter(adapter);
        }
        else progressVis();

        if (cookies != null) {
            new FineLoader(cookies,listView).execute("http://library.kuet.ac.bd:8000/cgi-bin/koha/opac-account.pl");
        }
    }

    private void progressVis(){
        progressLayout.setVisibility(View.VISIBLE);
    }

    private void progressInvis(){
        progressLayout.setVisibility(View.INVISIBLE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_item,menu);

        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.print_as_pdf) {
            createFile();
        }
        if (item.getItemId() == R.id.refresh_button) {
            if(cookies != null) {
                new FineLoader(cookies,listView).execute("http://library.kuet.ac.bd:8000/cgi-bin/koha/opac-account.pl");
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private static final int WRITE_REQUEST_CODE = 43;
    private void createFile() {
        Intent intent;
        intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);

        // Filter to only show results that can be "opened", such as
        // a file (as opposed to a list of contacts or timezones).
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        // Create a file with the requested MIME type.
        intent.setType("application/pdf");
        intent.putExtra(Intent.EXTRA_TITLE, "Fines");
        startActivityForResult(intent, WRITE_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == WRITE_REQUEST_CODE) {
            View content = findViewById(R.id.activity_fines_id);
            ListView listView = findViewById(R.id.fines_list);

            if (data != null) {
                Uri fileUri = data.getData();
                PDFSaver.save(FinesActivity.this,content,listView,fileUri);
            }
        }
    }

    private class FineLoader extends AsyncTask<String,String, Document>{

        private final ListView listView;
        private Map<String, String> cookieHolder;

        FineLoader(Map<String, String> cookies, ListView listView) {
            this.cookieHolder = cookies;
            this.listView = listView;
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
                Toast.makeText(FinesActivity.this,"No internet or server down\nFailed to update",Toast.LENGTH_LONG).show();

                return;
            }

            Elements check = document.select("div#members");

            if( !check.first().text().contains("Welcome")){
                Toast.makeText(FinesActivity.this,"Your session has been expired\nFailed to update",Toast.LENGTH_LONG).show();

                return;
//                Intent intent = new Intent(FinesActivity.this,LoginActivity.class);
//                startActivity(intent);
//                finish();
            }

            ArrayList<FineSampleHolder> fineSampleHolderArrayList = new ArrayList<>();

            Elements table = document.select("table#finestable");
            Elements tbody = table.select("tbody");
            Elements trs = tbody.select("tr");

            FineSampleHolder headers = new FineSampleHolder();
            headers.date = "Date";
            headers.title = "Book Name";
            headers.fine = "Fine";
            headers.outStanding = "Due";
            fineSampleHolderArrayList.add(headers);

            for(Element tr : trs){
                FineSampleHolder fineSampleHolder = new FineSampleHolder();

                Elements tds = tr.select("td");

                fineSampleHolder.date = tds.first().text();

                tds = tds.next();
                fineSampleHolder.title = tds.first().text();

                tds = tds.next();
                fineSampleHolder.fine = tds.first().text();

                tds = tds.next();
                fineSampleHolder.outStanding = tds.first().text();

                fineSampleHolderArrayList.add(fineSampleHolder);
            }

            Elements tfoot = table.select("tfoot");
            Elements tr = tfoot.select("tr");
            Elements td = tr.select("td");

            String sum = "0.00";
            if(td.size() != 0) sum = td.first().text();

            FineSampleHolder footer = new FineSampleHolder();
            footer.date = " ";
            footer.title = " ";
            footer.fine = "Total";
            footer.outStanding = sum;
            fineSampleHolderArrayList.add(footer);

            FinesListAdapter adapter = new FinesListAdapter(FinesActivity.this,fineSampleHolderArrayList);
            listView.setAdapter(adapter);

            //saving current data
            Gson gson = new Gson();
            String json = gson.toJson(fineSampleHolderArrayList,holderArrayType);

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("fine",json);

            editor.apply();
        }
    }
}
