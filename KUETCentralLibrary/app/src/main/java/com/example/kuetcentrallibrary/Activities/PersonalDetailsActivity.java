package com.example.kuetcentrallibrary.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.MediaRouteButton;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.kuetcentrallibrary.Auxilaries.ImageSaveRetrieve;
import com.example.kuetcentrallibrary.Auxilaries.PDFSaver;
import com.example.kuetcentrallibrary.Holders.CookieHolderExchange;
import com.example.kuetcentrallibrary.Holders.DetailsHolder;
import com.example.kuetcentrallibrary.R;
import com.google.gson.Gson;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

public class PersonalDetailsActivity extends AppCompatActivity {

    SharedPreferences sharedPreferences;
    private LinearLayout progressLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_details);

        setTitle("Personal Details");

        progressLayout = findViewById(R.id.progress_bar);

        CookieHolderExchange cookieHolderExchange = null;
        Map<String, String> cookies = null;

        Intent intent = getIntent();

        if (intent != null) {
            cookieHolderExchange = (CookieHolderExchange) intent.getSerializableExtra("cookieHolder");
        }

        if (cookieHolderExchange != null) {
            cookies = cookieHolderExchange.cookieHolder;
        }

        //retrieving previous state
        sharedPreferences = getSharedPreferences("data", Context.MODE_PRIVATE);
        String json = sharedPreferences.getString("personal",null);

        if (json != null) {
            Gson gson = new Gson();
            DetailsHolder holder = gson.fromJson(json,DetailsHolder.class);

            TextView nameText = findViewById(R.id.name_text);
            TextView cardText = findViewById(R.id.card_text);
            TextView expiryText= findViewById(R.id.expiry_text);
            TextView catText = findViewById(R.id.category_text);
            TextView dobText = findViewById(R.id.dob_text);
            TextView genderText= findViewById(R.id.gender_text);
            TextView addressText = findViewById(R.id.address_text);
            TextView phoneText = findViewById(R.id.phone_text);
            TextView emailText = findViewById(R.id.email_text);
            ImageView patronImage = findViewById(R.id.patron_image);

            nameText.setText(holder.name);
            cardText.setText(holder.card);
            expiryText.setText(holder.expiry);
            catText.setText(holder.category);
            dobText.setText(holder.dob);
            genderText.setText(holder.gender);
            addressText.setText(holder.address);
            phoneText.setText(holder.phone);
            emailText.setText(holder.email);

            patronImage.setImageBitmap(ImageSaveRetrieve.retrieveImage(PersonalDetailsActivity.this,"patron"));

        }
        else progressVis();

        //updating current state
        if(cookies != null){
            new PersonalLoader(cookies).execute("http://library.kuet.ac.bd:8000/cgi-bin/koha/opac-memberentry.pl");
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


        return super.onOptionsItemSelected(item);
    }

    private static final int WRITE_REQUEST_CODE = 43;
    private void createFile() {
        Intent intent;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);

            // Filter to only show results that can be "opened", such as
            // a file (as opposed to a list of contacts or timezones).
            intent.addCategory(Intent.CATEGORY_OPENABLE);

            // Create a file with the requested MIME type.
            intent.setType("application/pdf");
            intent.putExtra(Intent.EXTRA_TITLE, "PersonalDetails");
            startActivityForResult(intent, WRITE_REQUEST_CODE);
        }
        else {
            Toast.makeText(PersonalDetailsActivity.this,"Sorry, Print as PDF is not supported on your device",Toast.LENGTH_LONG).show();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == WRITE_REQUEST_CODE) {
            View content = findViewById(R.id.activity_personal_details_id);

            if (data != null) {
                Uri fileUri = data.getData();
                PDFSaver.save(PersonalDetailsActivity.this,content, fileUri);
            }
        }
    }

    private class PersonalLoader extends AsyncTask<String ,String, Document>{

        private final Map<String, String> cookies;
        private Bitmap bitmap = null;

        public PersonalLoader(Map<String, String> cookies) {
            this.cookies = cookies;
        }

        @Override
        protected Document doInBackground(String... strings) {
            Document document = null;

            try {
                document = Jsoup.connect(strings[0])
                        .userAgent("Mozilla/5.0")
                        .cookies(cookies)
                        .post();

                Elements imgs = document.select("p.patronimage img");
                String imgLink = "http://library.kuet.ac.bd:8000" + imgs.first().attr("src");


//                CookieStore cookieStore;
//                CookieHandler.setDefault(new CookieManager(cookies, CookiePolicy.ACCEPT_ALL));

                HttpURLConnection urlConnection = (HttpURLConnection) new URL(imgLink).openConnection();
//                urlConnection.addRequestProperty("CGISESSID",cookies.get("CGISESSID"));

//                CookieManager msCookieManager = null;
//                msCookieManager.getCookieStore().getCookies();
//                HttpCookie httpCookie = new HttpCookie("CGISESSID",cookies.get("CGISESSID"));
//                List<HttpCookie> list = new ArrayList<>();
//                list.add(httpCookie);
                urlConnection.addRequestProperty("Cookie", "CGISESSID="+cookies.get("CGISESSID"));

                urlConnection.setDoOutput(true);
                urlConnection.setDoInput(true);
                urlConnection.setRequestMethod("POST");
                urlConnection.connect();

                // Download Image from URL
                InputStream input = urlConnection.getInputStream();
//                InputStream input = new java.net.URL(imgLink).openStream();
                // Decode Bitmap
                bitmap = BitmapFactory.decodeStream(input);

            } catch (IOException e) {
                e.printStackTrace();
            }

            return document;
        }

        @Override
        protected void onPostExecute(Document document) {
            super.onPostExecute(document);

            progressInvis();

            if(document == null || bitmap == null){
                Toast.makeText(PersonalDetailsActivity.this,"No internet or server down\nFailed to update",Toast.LENGTH_LONG).show();

                return;
            }

            Elements check = document.select("div#members");

            if( !check.first().text().contains("Welcome")){
                Toast.makeText(PersonalDetailsActivity.this,"Your session has been expired\nFailed to update",Toast.LENGTH_LONG).show();

                return;
            }

            DetailsHolder detailsHolder = new DetailsHolder();

            ImageView imageView = findViewById(R.id.patron_image);
            imageView.setImageBitmap(bitmap);

//            detailsHolder.image = bitmap;

//            Elements imgs = document.select("p.patronimage img");
//            String imgLink = "http://library.kuet.ac.bd:8000" + imgs.first().attr("src");
//            Picasso.get().load(imgLink).into(imageView);

            Elements names = document.select("#borrower_surname");
            TextView nameText = findViewById(R.id.name_text);
            nameText.setText(names.first().attr("value"));
            detailsHolder.name = names.first().attr("value");

            Elements memberentry_library = document.select("#memberentry_library");
            Elements lis = memberentry_library.first().select("li");

            ((TextView)findViewById(R.id.card_text)).setText(lis.get(0).ownText());
            ((TextView)findViewById(R.id.expiry_text)).setText(lis.get(1).ownText());
            ((TextView)findViewById(R.id.category_text)).setText(lis.get(3).ownText());
            detailsHolder.card = lis.get(0).ownText();
            detailsHolder.expiry = lis.get(1).ownText();
            detailsHolder.category = lis.get(3).ownText();

            Elements dateOfBirthField = document.select("#borrower_dateofbirth");
            ((TextView)findViewById(R.id.dob_text)).setText(dateOfBirthField.first().attr("value"));
            detailsHolder.dob = dateOfBirthField.first().attr("value");

            Elements femaleCheckedField = document.select("#sex-female");
            String femaleChecked = femaleCheckedField.first().attr("checked");
            Elements maleCheckedField = document.select("#sex-male");
            String maleChecked = maleCheckedField.first().attr("checked");

            if(femaleChecked.equals("checked")){
                ((TextView)findViewById(R.id.gender_text)).setText(getResources().getString(R.string.female));
                detailsHolder.gender = getResources().getString(R.string.female);
            }
            else if(maleChecked.equals("checked")) {
                ((TextView) findViewById(R.id.gender_text)).setText(getResources().getString(R.string.male));
                detailsHolder.gender = getResources().getString(R.string.male);
            }
            else{
                ((TextView)findViewById(R.id.gender_text)).setText(getResources().getString(R.string.non_specified));
                detailsHolder.gender = getResources().getString(R.string.non_specified);
            }



            Elements borrower_address = document.select("#borrower_address");
            ((TextView)findViewById(R.id.address_text)).setText(borrower_address.first().attr("value"));
            detailsHolder.address = borrower_address.first().attr("value");

            Elements borrower_mobile = document.select("#borrower_mobile");
            ((TextView)findViewById(R.id.phone_text)).setText(borrower_mobile.first().attr("value"));
            detailsHolder.phone = borrower_mobile.first().attr("value");

            Elements borrower_email = document.select("#borrower_email");
            ((TextView)findViewById(R.id.email_text)).setText(borrower_email.first().attr("value"));
            detailsHolder.email = borrower_email.first().attr("value");

            //Saving current state
            Gson gson = new Gson();
            String json = gson.toJson(detailsHolder);

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("personal",json);

            editor.apply();

            ImageSaveRetrieve.saveImage(PersonalDetailsActivity.this,"patron",bitmap);
        }


    }

}
