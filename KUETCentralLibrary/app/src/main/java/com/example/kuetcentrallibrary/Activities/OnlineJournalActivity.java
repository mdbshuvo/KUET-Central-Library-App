package com.example.kuetcentrallibrary.Activities;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.kuetcentrallibrary.Adapters.JournalListAdapter;
import com.example.kuetcentrallibrary.Holders.SearchHolder;
import com.example.kuetcentrallibrary.R;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;

public class OnlineJournalActivity extends AppCompatActivity {

    private LinearLayout progressLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_online_journal);
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
                    new OnlineJournalActivity.JournalFinder(query.replaceAll(" ","+"),OnlineJournalActivity.this,searchList,resNum).execute();
                }
            }
        });

    }
    private void progressVis(){
        progressLayout.setVisibility(View.VISIBLE);
    }

    private void progressInvis(){
        progressLayout.setVisibility(View.INVISIBLE);
    }

    public class JournalFinder extends AsyncTask<String, String, Document> {
        private String query;
        private Context context;
        private ListView searchList;
        private TextView resNum;
        private boolean noRes = false;
        private int numRes;

        JournalFinder(String query, Context context, ListView searchList, TextView resNum) {
            this.query = query;
            this.context = context;
            this.searchList = searchList;
            this.resNum = resNum;
        }

        @Override
        protected Document doInBackground(String... strings) {
            Document document = null;

            try {
                String url = "http://dspace.kuet.ac.bd/discover?rpp=10&etal=0&query="+ query +"&scope=/&group_by=none&page=1";

                document = Jsoup.connect(url)
                        .userAgent("Mozilla/5.0")
                        .get();

                Elements heads = document.select(".ds-div-head");
                if(heads.size() == 2){
                    noRes = true;
                    return null;
                }

                String numString = heads.last().ownText().replaceAll("[\\D]"," ");

                int i;
                for(i=0;i<numString.length();i++) {
                    if(numString.charAt(i) != ' ') break;
                }

                for(;i<numString.length();i++) {
                    if(numString.charAt(i) == ' ') break;
                }

                String str = numString.substring(i);
                String str2 = str.replaceAll(" ","");

                numRes = Integer.parseInt(str2);

                url = "http://dspace.kuet.ac.bd/discover?rpp="+ numRes +"&etal=0&query="+ query +"&scope=/&group_by=none&page=1";

                Document newDocument = Jsoup.connect(url)
                        .userAgent("Mozilla/5.0")
                        .get();

                return newDocument;

            } catch (IOException e) {
                e.printStackTrace();
            }


            return document;
        }

        @Override
        protected void onPostExecute(Document document) {
            super.onPostExecute(document);

            progressInvis();

            if(noRes){
                String string = "Your search returned 0 result!";
                resNum.setText(string);
                return;
            }

            if(document == null){
                Toast.makeText(context,"Sorry the website not responding\nFailed to load",Toast.LENGTH_LONG).show();
                return;
            }

            final ArrayList<SearchHolder> searchHolderArrayList = new ArrayList<>();

            Elements uls = document.select("ul.ds-artifact-list");
            Element list = uls.last();
            Elements lis = list.select("li");

            for (Element li : lis) {
                SearchHolder holder = new SearchHolder();

                Element imgEl = li.select("img").first();
                if(imgEl != null)  holder.imageUrl = "http://dspace.kuet.ac.bd" + imgEl.attr("src");
                else holder.imageUrl = "";

                Element div = li.select("div.artifact-description").first();

                if(div == null) continue;

                Element titleA = div.select("a").first();

                holder.title = titleA.ownText();
                holder.bookUrl = "http://dspace.kuet.ac.bd/bitstream" + titleA.attr("href") + "/Full%20Thesis.pdf?sequence=1&isAllowed=y";

                Element auth = div.select(".author").first();
                if (auth != null) {
                    holder.author = auth.text();
                }
                else holder.author = "Not Specified";

                Element pd = div.select(".publisher-date").first();
                if (pd != null) {
                    holder.publisher = pd.text();
                }
                else holder.publisher = "";

                Element abs = div.select(".abstract").first();
                if (abs != null) {
                    holder.callNo = abs.text();
                }
                else holder.callNo = "";

                searchHolderArrayList.add(holder);
            }


            JournalListAdapter adapter = new JournalListAdapter(context,searchHolderArrayList,searchList);

            searchList.setAdapter(adapter);
            searchList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(OnlineJournalActivity.this);
                    builder.setTitle("Note");
                    builder.setMessage("Do you want to download this as pdf ?");
                    builder.setNegativeButton("No", null);
                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String url = searchHolderArrayList.get(position).bookUrl;

                            DownloadManager downloadmanager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
                            Uri uri = Uri.parse(url);

                            DownloadManager.Request request = new DownloadManager.Request(uri);
                            request.setTitle("My File");
                            request.setDescription("Downloading");
                            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                            request.setVisibleInDownloadsUi(false);
                            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,
                                    "/" + searchHolderArrayList.get(position).title.replaceAll(" ","_") + ".pdf");

                            if (downloadmanager != null) {
                                downloadmanager.enqueue(request);
                            }
                            else Toast.makeText(OnlineJournalActivity.this, "Problem accessing download manager", Toast.LENGTH_SHORT).show();
                        }
                    });

                    builder.create().show();
                }
            });

            String string = "Your search returned "+numRes+ " results.";
            resNum.setText(string);
        }
    }

}
