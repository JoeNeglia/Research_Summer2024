package com.example.library;

import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertEquals("com.example.library", appContext.getPackageName());
    }
}

package com.example.library;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }
}

package com.example.library;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {
    RecyclerView mRecyclerview;
    ArrayList<Book> bookList=new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bookList=Book.getBooks("books.json",this);
        mRecyclerview=findViewById(R.id.recyclerView);
        BookAdapter adapter=new BookAdapter(bookList);
        mRecyclerview.setAdapter(adapter);
        //mRecyclerview.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        mRecyclerview.setLayoutManager(new LinearLayoutManager(this));


    }
}


package com.example.library;

import static java.nio.charset.StandardCharsets.UTF_8;

import android.content.Context;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;

public class Book {
    String author;
    String title;
    String ISBN;
    String synopsis;
    String cover;

    public Book(String author, String title, String ISBN, String synopsis, String cover) {
        this.author = author;
        this.title = title;
        this.ISBN = ISBN;
        this.synopsis = synopsis;
        this.cover = cover;
    }
     @NonNull
     static ArrayList<Book> getBooks(String filename, Context context){
        ArrayList<Book> booklist = new ArrayList<>();
        try {
            InputStream inputstream = context.getAssets().open(filename);
            byte[] buffer=new byte[inputstream.available()];
            inputstream.read(buffer);
            inputstream.close();

            //JSONObject json=new JSONObject(new String(buffer), StandardCharsets.UTF_8));
            JSONObject json=new JSONObject(new String(buffer,UTF_8));
            JSONArray books=json.getJSONArray("books");

            for(int i=0;i<books.length();i++){
                booklist.add(new Book(books.getJSONObject(i).getString("Author"),
                        books.getJSONObject(i).getString("Title"),
                        books.getJSONObject(i).getString("ISBN"),
                        books.getJSONObject(i).getString("Synopsis"),
                        books.getJSONObject(i).getString("Cover")
                ));
            }

        }
        catch (Exception e){
            e.printStackTrace();
        }
         return booklist;
     }
}




package com.example.library;

import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class BookAdapter extends RecyclerView.Adapter<BookAdapter.BookViewHolder> {
    ArrayList<Book> book;

    public BookAdapter(ArrayList<Book> book) {
        this.book = book;
    }

    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new BookViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.book_layout,parent,false));
    }


    @Override
    public void onBindViewHolder(@NonNull BookViewHolder holder, int position) {
        Book theBook=book.get(position);
        holder.txtAuthour.setText(book.get(position).author);
        holder.txtTitle.setText(book.get(position).title);
        holder.txtSynopsis.setText(book.get(position).synopsis);
        holder.txtISBN.setText(book.get(position).ISBN);


        Resources res=holder.itemView.getContext().getResources();
        int id=res.getIdentifier("@drawable/"+theBook.cover,"drawable", "com.example.library");
        holder.imgcover.setImageResource(id);


    }


    @Override
    public int getItemCount() {
        return book.size();
    }


    public class BookViewHolder extends RecyclerView.ViewHolder {

        TextView txtAuthour, txtISBN,txtSynopsis, txtTitle;
        ImageView imgcover;

        public BookViewHolder(@NonNull View itemView) {
            super(itemView);
            txtAuthour=itemView.findViewById(R.id.txtAuthor);
            txtISBN=itemView.findViewById(R.id.txtISBN);
            txtSynopsis=itemView.findViewById(R.id.txtSynopsis);
            txtTitle=itemView.findViewById(R.id.txtTitle);
            imgcover=itemView.findViewById(R.id.imgCover);

        }
    }
}

