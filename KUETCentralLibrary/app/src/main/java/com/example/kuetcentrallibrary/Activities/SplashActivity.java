package com.example.kuetcentrallibrary.Activities;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.kuetcentrallibrary.R;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {
    private TextView tv1, tv2;
    private ImageView iv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        tv1 = (TextView) findViewById(R.id.textView);
        tv2 = (TextView) findViewById(R.id.textView2);
        iv = (ImageView) findViewById(R.id.imageView);

        Animation myanim = AnimationUtils.loadAnimation(SplashActivity.this, R.anim.mytransition);
        iv.startAnimation(myanim);
        tv1.startAnimation(myanim);
        tv2.startAnimation(myanim);

        new ShareLoader().execute();

    }

    private class ShareLoader extends AsyncTask<String,String,String> {

        @Override
        protected String doInBackground(String... strings) {

            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(SplashActivity.this,iv,"imageTransition");
            Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
            startActivity(intent,options.toBundle());
            finish();
        }
    }

}
