package com.example.kuetcentrallibrary.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.kuetcentrallibrary.Adapters.SearchListAdapter;
import com.example.kuetcentrallibrary.Holders.SearchHolder;
import com.example.kuetcentrallibrary.R;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;

public class SearchBooksActivity extends AppCompatActivity {

    private static LinearLayout progressLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_books);

        final SearchView searchView = findViewById(R.id.search_bar);
        Button searchButton = findViewById(R.id.search_button);
        final ListView searchList = findViewById(R.id.search_list);
        final TextView resNum = findViewById(R.id.result_num);
        progressLayout = findViewById(R.id.progress_show);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            searchView.setFocusedByDefault(true);
        }

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String query = searchView.getQuery().toString();

                if(!query.equals("")){
                    progressVis();
                    new Bookfinder(query,null,SearchBooksActivity.this,searchList,resNum,null).execute();
                }
            }
        });

    }
    private void progressVis(){
        progressLayout.setVisibility(View.VISIBLE);
    }

    private static void progressInvis(){
        progressLayout.setVisibility(View.INVISIBLE);
    }

    public static class Bookfinder extends AsyncTask<String, String, Document> {
        private String query;
        private String offset;
        private Context context;
        private ListView searchList;
        private ArrayList<SearchHolder> list;
        private TextView resNum;

        public Bookfinder(String query, String offset, Context context, ListView searchList, TextView resNum, ArrayList<SearchHolder> list) {
            this.query = query;
            this.offset = offset;
            this.context = context;
            this.searchList = searchList;
            this.list = list;
            this.resNum = resNum;
        }

        @Override
        protected Document doInBackground(String... strings) {
            Document document = null;
            String url;
            if(offset == null) url = "http://library.kuet.ac.bd:8000/cgi-bin/koha/opac-search.pl?q="+query;
            else url = "http://library.kuet.ac.bd:8000/cgi-bin/koha/opac-search.pl?q="+query+"&offset="+offset;

            try {
                document = Jsoup.connect(url)
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

            progressInvis();

            if(document == null){
                Toast.makeText(context,"Sorry the website not responding\nFailed to load",Toast.LENGTH_LONG).show();
                return;
            }

            ArrayList<SearchHolder> searchHolderArrayList = new ArrayList<>();
            if(list != null) searchHolderArrayList.addAll(list);

            Elements table = document.select("table");
            Elements pages = document.select("div.pagination");
            Elements results = document.select("#numresults");

            if(table.size() == 0){
                Toast.makeText(context,"Nothing such searched found!!",Toast.LENGTH_LONG).show();
                //Do something here
                return;
            }

            String numResStr = "";
            if(results.first()!=null){
                numResStr = results.first().text();
            }

            int lastoffset = 0;
            if(pages.first()!=null) {
                String lastHref = pages.first().select("li").last().select("a").first().attr("href");
                lastoffset = Integer.parseInt(lastHref.replaceAll("[\\D]", ""));
            }

            Elements trs = table.select("tr");
            for (Element tr : trs) {
                SearchHolder holder = new SearchHolder();

                Elements tds = tr.select("td");
                Element td = tds.last();

                String num = tds.get(1).text();

                Element aTitle = td.select("a.title").first();
                String sample = aTitle.text();
                holder.title = num + " " + sample.split("/")[0];
                holder.bookUrl = aTitle.attr("href");

                Elements pAuthor = td.select("p");
                holder.author = pAuthor.first().text();

                Elements spanPs = td.select("span.results_summary");
                holder.publisher = spanPs.select(".publisher").text();
                holder.availability = spanPs.select(".availability").text();
                holder.edition = spanPs.select(".edition").text();

                Elements img = td.select(".item-thumbnail");
                if(img.size() > 0) holder.imageUrl = img.first().attr("src");

                searchHolderArrayList.add(holder);
            }
            int currentOffset = 0;
            if(offset != null) currentOffset = Integer.parseInt(offset);


            SearchListAdapter adapter = new SearchListAdapter(context,searchHolderArrayList,currentOffset,lastoffset,query,searchList,resNum);

            // save index and top position
            int index = searchList.getFirstVisiblePosition();
            View v = searchList.getChildAt(0);
            int top = (v == null) ? 0 : (v.getTop() - searchList.getPaddingTop());

            searchList.setAdapter(adapter);

            // restore index and position
            searchList.setSelectionFromTop(index, top);

            //if(currentOffset != 0 ) searchList.setSelection(currentOffset-2);

            resNum.setText(numResStr);
        }
    }
}
