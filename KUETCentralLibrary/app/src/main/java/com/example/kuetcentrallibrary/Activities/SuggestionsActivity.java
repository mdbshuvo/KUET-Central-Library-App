package com.example.kuetcentrallibrary.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.kuetcentrallibrary.Adapters.SuggestionAdapter;
import com.example.kuetcentrallibrary.R;

public class SuggestionsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggestions);

        SharedPreferences sharedPreferences_user_name = getSharedPreferences("loginInfo", Context.MODE_PRIVATE);
        String name = sharedPreferences_user_name.getString("username",null);

        if (name != null) {
            if(!name.substring(4,6).equals("07")){
                Toast.makeText(this, "Your Department base book list not provided yet!", Toast.LENGTH_SHORT).show();
                onBackPressed();
            }
        }

        String[] terms =  getResources().getStringArray(R.array.terms);
        SuggestionAdapter adapter = new SuggestionAdapter(this,terms);

        ListView listView = findViewById(R.id.suggestion_menu_list);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(SuggestionsActivity.this, "Please select a semeter", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
