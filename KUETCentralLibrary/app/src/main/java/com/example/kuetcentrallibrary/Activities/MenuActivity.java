package com.example.kuetcentrallibrary.Activities;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.kuetcentrallibrary.Adapters.CustomAdpater;
import com.example.kuetcentrallibrary.Auxilaries.ImageSaveRetrieve;
import com.example.kuetcentrallibrary.Holders.CookieHolderExchange;
import com.example.kuetcentrallibrary.Holders.DetailsHolder;
import com.example.kuetcentrallibrary.R;
import com.google.gson.Gson;

import java.util.Arrays;
import java.util.HashMap;

public class MenuActivity extends AppCompatActivity {

    SharedPreferences sharedPreferences;
    //naming the Logout button if exists
    private final String LOGOUT = "Log Out";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);


        Intent intent = getIntent();
        CookieHolderExchange cookieHolderExchange = null;

        if(intent != null) {
            cookieHolderExchange = (CookieHolderExchange) intent.getSerializableExtra("cookieHolder");
        }
        sharedPreferences = getSharedPreferences("data", Context.MODE_PRIVATE);
        String json = sharedPreferences.getString("personal",null);

        if (json != null) {
            Gson gson = new Gson();
            DetailsHolder holder = gson.fromJson(json,DetailsHolder.class);

            TextView nameText = findViewById(R.id.name_text);
            ImageView patronImage = findViewById(R.id.patron_image);

            nameText.setText(holder.name);

            patronImage.setImageBitmap(ImageSaveRetrieve.retrieveImage(MenuActivity.this,"patron"));

        }else {
            SharedPreferences sharedPreferences_user_name = getSharedPreferences("loginInfo",Context.MODE_PRIVATE);
            String name = sharedPreferences_user_name.getString("name",null);

            if (name != null) {
                TextView nameText = findViewById(R.id.name_text);
                nameText.setText(name);
            }
        }


        ListView listView = findViewById(R.id.main_list_view);
        String[] options = getResources().getStringArray(R.array.options);

        //initializing
        Class<?>[] classes = {SummaryActivity.class,FinesActivity.class,PersonalDetailsActivity.class,ReadingHistoryActivity.class,SearchBooksActivity.class};
        HashMap<String,Class<?>> mapClass = new HashMap<>();
        for(int i =0 ; i<classes.length ; i++){
            mapClass.put(options[i],classes[i]);
        }


        //offline user handle
        if (cookieHolderExchange == null) {
            options = Arrays.copyOfRange(options,0,4);

            Toast.makeText(MenuActivity.this,"You are in offline mode",Toast.LENGTH_LONG).show();
        }


        int[] arr = {R.drawable.summary_icon,R.drawable.penalty,R.drawable.personal_details,R.drawable.reading_history,R.drawable.search_books,R.drawable.logout};
        CustomAdpater customAdpater = new CustomAdpater(MenuActivity.this,options,arr);

        listView.setAdapter(customAdpater);


        //To use in onClick
        final CookieHolderExchange finalCookieHolderExchange = cookieHolderExchange;
        final HashMap<String,Class<?>> FinalMapClass = mapClass;
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent1;
                String selected = (String) adapterView.getItemAtPosition(i);

                if(selected.equals(LOGOUT)){
                    //prompting log out
                    AlertDialog.Builder builder = new AlertDialog.Builder(MenuActivity.this);
                    builder.setTitle("Warning");
                    builder.setMessage("You are about to log out. Continue?");
                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //Logging the user out

                            SharedPreferences sharedPreferences = getSharedPreferences("loginInfo", Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();

                            editor.remove("cookieKey");
                            editor.remove("cookieValue");
                            editor.remove("password");
                            editor.remove("name");
                            editor.apply();

                            Intent intent = new Intent(MenuActivity.this,LoginActivity.class);

                            startActivity(intent);
                            finish();
                        }
                    });
                    builder.setNegativeButton("No",null);

                    builder.create().show();

                }else{
                    intent1 = new Intent(MenuActivity.this, FinalMapClass.get(selected));
                    intent1.putExtra("cookieHolder", finalCookieHolderExchange);

                    startActivity(intent1);
                }
            }

        });
        
//        textView.setText(document.text());
//        new GetHtml(cookieHolder).execute("http://library.kuet.ac.bd:8000/cgi-bin/koha/opac-memberentry.pl");
    }

    private boolean backPressed = false;
    @Override
    public void onBackPressed() {
        if(backPressed) {
            super.onBackPressed();
        }
        else {
            Toast.makeText(MenuActivity.this,"Press again back to exit",Toast.LENGTH_SHORT).show();
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    backPressed = true;

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        MenuActivity.super.onBackPressed();
                    }

                    backPressed = false;
                }
            });
            thread.start();
        }
    }
}
