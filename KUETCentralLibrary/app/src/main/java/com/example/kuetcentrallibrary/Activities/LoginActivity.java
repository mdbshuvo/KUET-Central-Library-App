package com.example.kuetcentrallibrary.Activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.kuetcentrallibrary.Holders.CookieHolderExchange;
import com.example.kuetcentrallibrary.R;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private Button loginButton;
    private Button scanButton;
    private TextView usernameText;
    private TextView passwordText;
    private LinearLayout progressBar;
    private TextView waitText;
    private SharedPreferences sharedPreferences;
    private CheckBox checkBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        setTitle("Log In to Continue");

        loginButton = findViewById(R.id.login_button);
        scanButton = findViewById(R.id.guest_button);
        usernameText = findViewById(R.id.username_text);
        passwordText = findViewById(R.id.password_text);
        checkBox = findViewById(R.id.checkBox);
        waitText = findViewById(R.id.wait_text);

        //verifying previous session existence
        sharedPreferences = getSharedPreferences("loginInfo",Context.MODE_PRIVATE);
        HashMap<String,String> cookies = new HashMap<>();
        String cookieKey = sharedPreferences.getString("cookieKey",null);
        String cookieValue = sharedPreferences.getString("cookieValue",null);
        String username = sharedPreferences.getString("username",null);
        String password = sharedPreferences.getString("password",null);

        if(username != null) usernameText.setText(username);

        if(username != null && password != null){
            progressVis();

            CheckBox checkBox = findViewById(R.id.checkBox);
            checkBox.setChecked(true);
            Authentication authentication = new Authentication(LoginActivity.this,null);
            authentication.execute(username,password);
            passwordText.setText(password);
        }
        else if(cookieKey != null && cookieValue != null) {      //continue the session
            cookies.put(cookieKey,cookieValue);

            String waitSt = "Verifying previous session...";
            waitText.setText(waitSt);
            progressVis();

            Authentication authentication = new Authentication(LoginActivity.this,cookies);
            authentication.execute();
        }

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //if not connected go offline
                boolean isOffline = false;
                ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                if(connectivityManager != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (connectivityManager.getActiveNetwork() == null) isOffline = true;
                    }else{
                        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
                        if(activeNetwork == null || !activeNetwork.isConnectedOrConnecting()) isOffline = true;
                    }
                }

                if(isOffline){
                    AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                    builder.setTitle("Warning");
                    builder.setMessage("You are offline.\nDo you want to log in offline??\n\nN.B: The offline mode can view the last logged in users data.");
                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(LoginActivity.this,MenuActivity.class);
                            startActivity(intent);
                        }
                    });
                    builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //Do nothing
                        }
                    });

                    builder.create().show();

                    return;
                }

                String username,password;

                username = usernameText.getText().toString();
                password = passwordText.getText().toString();

                if(username.equals("") || password.equals("")){
                    Toast.makeText(LoginActivity.this,"Please fill up",Toast.LENGTH_LONG).show();
                    return;
                }

                progressVis();

                Authentication authentication = new Authentication(LoginActivity.this, null);
                authentication.execute(username,password);

            }
        });

        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this,ScanActivity.class);
                startActivityForResult(intent,1);
            }
        });
    }

    private void progressVis(){
        progressBar = findViewById(R.id.progress_bar);
        progressBar.setVisibility(View.VISIBLE);
        loginButton.setEnabled(false);
        usernameText.setEnabled(false);
        passwordText.setEnabled(false);
        scanButton.setEnabled(false);
        checkBox.setEnabled(false);
    }

    private void progressInvis(){
        progressBar.setVisibility(View.INVISIBLE);
        loginButton.setEnabled(true);
        usernameText.setEnabled(true);
        passwordText.setEnabled(true);
        scanButton.setEnabled(true);
        checkBox.setEnabled(true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        String id = null;
        if (data != null) {
            id = data.getStringExtra("id");
        }
        String[] users = getResources().getStringArray(R.array.logins);

        if (id != null) {
            int index = Arrays.binarySearch(users,id);

            if(index<0) {
                Toast.makeText(LoginActivity.this,"Sorry your account doesnot support scan to log in. Please contact the developer.",Toast.LENGTH_LONG).show();
                return;
            }

            progressVis();
            Authentication authentication = new Authentication(LoginActivity.this,null);
            authentication.execute(users[0],users[1]);
        }
    }

    public class Authentication extends AsyncTask<String, String, Document> {

        private Context context;
        private Map<String ,String> cookies;
        private boolean isAutomatic;
        private String username;
        private String password;

        Authentication(Context context, Map<String, String> cookies) {
            this.context = context.getApplicationContext();
            this.cookies = cookies;
        }

        @Override
        protected Document doInBackground(String... strings) {
            Document document = null;
            isAutomatic = true;

            try {
                if(cookies == null){
                    publishProgress("Establishing a connection...");

                    username = strings[0];
                    password = strings[1];

                    Connection.Response loginCookiesHolder = Jsoup.connect("http://library.kuet.ac.bd:8000/cgi-bin/koha/opac-user.pl")
                        .userAgent("Mozilla/5.0")
                        .data("koha_login_context", "opac")
                        .data("userid", username)
                        .data("password", password)
                        .execute();

                    cookies = loginCookiesHolder.cookies();
                    isAutomatic = false;

                    publishProgress("Authenticating user...");
                }

                document = Jsoup.connect("http://library.kuet.ac.bd:8000/cgi-bin/koha/opac-user.pl")
                    .cookies(cookies)
                    .userAgent("Mozilla/5.0")
                    .post();


            } catch (IOException e) {
                e.printStackTrace();
            }

            return document;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);

            waitText.setText(values[0]);
        }

        @Override
        protected void onPostExecute(Document document) {
            super.onPostExecute(document);

            if(document == null){
                Toast.makeText(LoginActivity.this,"No internet or server down",Toast.LENGTH_LONG).show();
                progressInvis();
                return;
            }

            String name;
            Elements elements = document.select("div#members");
            if( elements.first().text().contains("Welcome")){
                name = elements.select("span.loggedinusername").first().text();

                Intent intent = new Intent(LoginActivity.this,MenuActivity.class);

                CookieHolderExchange cookieHolderExchange = new CookieHolderExchange();
                cookieHolderExchange.cookieHolder = cookies;

                intent.putExtra("cookieHolder",cookieHolderExchange);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                if(!isAutomatic){
                    //saving cookies in shared preferences
                    SharedPreferences.Editor editor = sharedPreferences.edit();

                    String key = "CGISESSID";
                    editor.putString("cookieKey",key);
                    editor.putString("cookieValue",cookies.get(key));

                    String prevUser = sharedPreferences.getString("username",null);
                        editor.putString("username",username);
                    editor.putString("name",name);
                    if(checkBox.isChecked())
                        editor.putString("password",password);
                    else
                        editor.remove("password");

                    editor.apply();

                    //checking if user switched
                    if(prevUser != null){
                        if(!prevUser.equals(username)){
                            sharedPreferences = getSharedPreferences("data",Context.MODE_PRIVATE);
                            editor = sharedPreferences.edit();

                            editor.clear();
                            editor.apply();
                        }
                    }
                }

//                Toast.makeText(context,"Welcome "+name,Toast.LENGTH_SHORT).show();

                context.startActivity(intent);
                finish();
            }
            else {
                if(isAutomatic)
                    Toast.makeText(LoginActivity.this,"Log in session expired",Toast.LENGTH_LONG).show();
                else
                    Toast.makeText(LoginActivity.this,"Well played",Toast.LENGTH_LONG).show();
            }

            progressInvis();

        }
    }
    @Override
    public void finishAfterTransition() {
        super.finish();
    }
}
