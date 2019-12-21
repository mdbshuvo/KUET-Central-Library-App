package com.example.kuetcentrallibrary.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.example.kuetcentrallibrary.R;

public class BookListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_list);

        int id;

        Intent intent = getIntent();
        int selected = intent.getIntExtra("selected",0);

        switch (selected) {
            case 0 :
                id = R.array.term_11;
                break;
            case 1 :
                id = R.array.term_12;
                break;
//            case 2 :
//                id = R.array.term_21;
//                break;
//            case 3 :
//                id = R.array.term_22;
//                break;
//            case 4 :
//                id = R.array.term_31;
//                break;
//            case 5 :
//                id = R.array.term_32;
//                break;
//            case 6 :
//                id = R.array.term_41;
//                break;
//            case 7 :
//                id = R.array.term_42;
//                break;
            default:
                id = R.array.term_11;
        }

        final String[] books =  getResources().getStringArray(id);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,R.layout.suggestions_item_layout,books);

        ListView listView = findViewById(R.id.book_list);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(BookListActivity.this, "Please search for \""+ books[position] +"\" in \"Search Books\" option", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
