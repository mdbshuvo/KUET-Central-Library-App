package com.example.kuetcentrallibrary.Activities;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.example.kuetcentrallibrary.Adapters.BookListAdapter;
import com.example.kuetcentrallibrary.R;

public class BookListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_list);

        int id;

        Intent intent = getIntent();
        int selected = intent.getIntExtra("term",11);

        switch (selected) {
            case 11 :
                id = R.array.term_11;
                break;
            case 12 :
                id = R.array.term_12;
                break;
                //cause bakigula lekhi nai
//            case 21 :
//                id = R.array.term_21;
//                break;
//            case 22 :
//                id = R.array.term_22;
//                break;
//            case 31 :
//                id = R.array.term_31;
//                break;
//            case 32 :
//                id = R.array.term_32;
//                break;
//            case 41 :
//                id = R.array.term_41;
//                break;
//            case 42 :
//                id = R.array.term_42;
//                break;
            default:
                id = R.array.term_11;
        }

        final String[] books =  getResources().getStringArray(id);
        BookListAdapter adapter = new BookListAdapter(this,books);

        ListView listView = findViewById(R.id.book_list);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                AlertDialog.Builder builder = new AlertDialog.Builder(BookListActivity.this);

                builder.setTitle("Note");
                builder.setMessage("Do you want to search the book in the library server?");
                builder.setNegativeButton("No",null);
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(BookListActivity.this, SearchBooksActivity.class);
                        intent.putExtra("query",books[position].split("by")[0]);

                        startActivity(intent);
                    }
                });

                builder.create().show();
            }
        });
    }
}
