package com.example.kuetcentrallibrary.Activities;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.example.kuetcentrallibrary.Adapters.BorrowListAdapter;
import com.example.kuetcentrallibrary.Holders.BorrowSampleHolder;
import com.example.kuetcentrallibrary.Holders.CookieHolderExchange;
import com.example.kuetcentrallibrary.R;
import com.example.kuetcentrallibrary.Receiver.NotifyReciever;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

public class SummaryActivity extends AppCompatActivity {

    private static Type holderArrayType;
    private static SharedPreferences sharedPreferences;
    private static SharedPreferences notSharedPreferences;
    private static LinearLayout progressLayout;
    private Map<String, String> cookies;
    private ListView listView;
    public static final String NOTIFICATION_CHANNEL_ID = "10001";
    public static final String default_notification_channel_id = "default";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);

        setTitle("Borrowed Items");

        progressLayout = findViewById(R.id.progress_bar);

        CookieHolderExchange cookieHolderExchange = null;
        cookies = null;

        Intent intent = getIntent();

        if(intent != null) {
            cookieHolderExchange = (CookieHolderExchange) intent.getSerializableExtra("cookieHolder");
        }

        if (cookieHolderExchange != null) {
            cookies = cookieHolderExchange.cookieHolder;
        }

        listView = findViewById(R.id.borrow_list);

        //saved data load
        holderArrayType = new TypeToken<ArrayList<BorrowSampleHolder> >(){}.getType();

        sharedPreferences = getSharedPreferences("data",Context.MODE_PRIVATE);
        notSharedPreferences = getSharedPreferences("not",Context.MODE_PRIVATE);

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.refresh_menu,menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.refresh_button) {
            if(cookies != null) {
                new GetHtml(listView,cookies,SummaryActivity.this,null, null)
                        .execute("http://library.kuet.ac.bd:8000/cgi-bin/koha/opac-user.pl");
            }
        }
        return true;
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

            isFirst = listView != null;
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

                long minDelay = 86400 * 30 * 2;     //max delay
                minDelay*=1000;
                long minTime = 0;

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

                    String temp = borrowSampleHolder.due.substring(10);
                    String[] date = temp.split("/");

                    Calendar calendar = Calendar.getInstance(); //1577294980692 1577294980692

                    calendar.set(Integer.parseInt(date[2]),Integer.parseInt(date[1]) - 1,Integer.parseInt(date[0]),9,0,0);

                    calendar.getTime();

                    long current =  Calendar.getInstance().getTimeInMillis();
                    long now = calendar.getTimeInMillis();

                    long delay = now - current;

                    if(delay < minDelay) {
                        minDelay = delay;
                        minTime = now;
                    }

                    //callNo
                    data = data.next();
                    borrowSampleHolder.callNo = data.first().text();

                    //renew
                    data =data.next();
                    Elements renAnchor = data.first().select("a");
                    borrowSampleHolder.renewUrl = "http://library.kuet.ac.bd:8000"+renAnchor.attr("href");
                    borrowSampleHolder.renewText = data.first().text();

                    //fines
                    Elements fineTd = data.next();
                    borrowSampleHolder.fine = fineTd.text();

                    borrowSampleHolderArrayList.add(borrowSampleHolder);
                }

//                minDelay -= ;
                scheduleNotification(getNotification(), (minDelay - 2 * 86400 * 1000));
                SharedPreferences.Editor editor = notSharedPreferences.edit();
                editor.putString("time",(minTime - 2 * 86400 * 1000) + "");
                editor.commit();

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

        private void scheduleNotification(Notification notification, long delay) {
//        if(delay == 5000){
//            Calendar calendar = Calendar.getInstance();
//
//            calendar.set(2019,11,25,22,45,0);
//
//            SharedPreferences.Editor editor = sharedPreferences.edit(); //1577287706632
//            editor.putString("time","2019/11/25/22/45/0");
//            editor.commit();
//
//            delay = (int) calendar.getTimeInMillis();
//        }

            Intent notificationIntent = new Intent( context, NotifyReciever.class ) ;
            notificationIntent.putExtra(NotifyReciever.NOTIFICATION_ID , 1 ) ;
            notificationIntent.putExtra(NotifyReciever.NOTIFICATION , notification) ;
            PendingIntent pendingIntent = PendingIntent. getBroadcast ( context, 0 , notificationIntent , PendingIntent. FLAG_UPDATE_CURRENT ) ;
            long futureInMillis = SystemClock.elapsedRealtime () + delay ;
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context. ALARM_SERVICE ) ;
            assert alarmManager != null;
            alarmManager.set(AlarmManager. ELAPSED_REALTIME_WAKEUP , futureInMillis , pendingIntent) ;
        }
        private Notification getNotification() {
            NotificationCompat.Builder builder = new NotificationCompat.Builder( context,
                    default_notification_channel_id ) ;
            builder.setContentTitle( "Scheduled Notification" ) ;
            builder.setContentText("Book renewal required ") ;
            builder.setSmallIcon(R.drawable.kuet_logo_ultra_small ) ;
            builder.setAutoCancel( true ) ;
            builder.setChannelId( NOTIFICATION_CHANNEL_ID ) ;
            return builder.build() ;
        }
    }
}
