package fr.eilco.booksprojects;

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
        assertEquals("es.usj.booksprojects", appContext.getPackageName());
    }
}

package fr.eilco.booksprojects;

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

package fr.eilco.booksprojects;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.List;

import fr.eilco.booksprojects.model.Book;
import fr.eilco.booksprojects.serverOperations.GetRequest;
import fr.eilco.booksprojects.serverOperations.callback.BookGetRequestCallback;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28) // Ajustez cela selon la version de l'API Android que vous utilisez
public class GetRequestTest {

    @Test
    public void testRetrBooks() {
        GetRequest getRequest = new GetRequest();
        String searchName = "velo";

        // Utiliser un callback personnalisé pour traiter les résultats
        getRequest.retrBooks(searchName, new BookGetRequestCallback() {
            @Override
            public void onSuccess(List<Book> books) {
                // Vérifier que la liste de livres n'est pas vide
                assertNotNull(books);
                assertFalse(books.isEmpty());

                // Vérifier que la taille de la liste est égale à 100 (ou ajuster selon vos besoins)
                assertEquals(100, books.size());
            }

            @Override
            public void onFailure(Throwable t) {
                // En cas d'échec, émettre une assertion pour signaler une erreur dans le test
                assertFalse("La requête a échoué: " + t.getMessage(), true);
            }
        });
    }
}


package fr.eilco.booksprojects.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import fr.eilco.booksprojects.model.Book;

@Database(entities = {Book.class}, version = 4)
@TypeConverters(ClassConverter.class)
public abstract class AppDatabase extends RoomDatabase {
    public abstract BookDao bookDao();

    private static AppDatabase INSTANCE;

    public static synchronized AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "favoriteDB")
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return INSTANCE;
    }
}


package fr.eilco.booksprojects.database;

import androidx.room.TypeConverter;

import java.util.ArrayList;
import java.util.List;

public class ClassConverter {
    @TypeConverter
    public static List<String> fromString(String value) {
        // Convertit la chaîne de la base de données en liste de chaînes
        List<String> list = new ArrayList<>();
        if (value != null && !value.isEmpty()) {
            String[] items = value.split(",");
            for (String item : items) {
                list.add(item);
            }
        }
        return list;
    }

    @TypeConverter
    public static String toString(List<String> list) {
        // Convertit la liste de chaînes en une chaîne pour la base de données
        StringBuilder value = new StringBuilder();
        if (list != null) {
            for (String item : list) {
                value.append(item);
                value.append(",");
            }
        }
        return value.toString();
    }
}


package fr.eilco.booksprojects.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import fr.eilco.booksprojects.model.Book;

@Dao
public interface BookDao {
    @Insert
    void insert(Book book);

    @Update
    void update(Book book);

    @Query("SELECT * FROM Books")
    List<Book> getAllBooks();

    @Query("SELECT EXISTS (SELECT 1 FROM Books WHERE `key` = :key LIMIT 1)")
    boolean containsBook(String key);

    @Query("DELETE FROM Books WHERE `key` = :key")
    void deleteBookByKey(String key);

}


package fr.eilco.booksprojects.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import org.w3c.dom.Text;

import java.util.List;

import fr.eilco.booksprojects.R;
import fr.eilco.booksprojects.data.ImageData;
import fr.eilco.booksprojects.database.AppDatabase;
import fr.eilco.booksprojects.database.BookDao;
import fr.eilco.booksprojects.model.Book;

public class BookActivity extends AppCompatActivity {


    @SuppressLint("StaticFieldLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book);

        Intent intent = getIntent();
        if (intent != null) {
            String keyBook = intent.getStringExtra("BOOK_KEY");
            String bookListName = intent.getStringExtra("BookListName");

            AppDatabase db = AppDatabase.getInstance(getApplicationContext());
            // Obtenez les DAO KeyValueDao et BookDao
            BookDao bookDao = db.bookDao();
            ImageView starImage = findViewById(R.id.ivStarBook);

            if (keyBook != null) {
                Book book;
                if (bookListName.equals("NewBooks")){
                    book = BookListActivity.newList.getBookByKey(keyBook);
                }else if(bookListName.equals("FavoriteList")){
                    book = BookListActivity.favoriteList.getBookByKey(keyBook);
                }else if(bookListName.equals("RandomList")){
                    book = BookListActivity.randomList.getBookByKey(keyBook);
                } else {
                    book = SearchActivity.searchList.getBookByKey(keyBook);
                }


                boolean isStarred = book.isStar();
                if(isStarred){
                    starImage.setImageResource(android.R.drawable.btn_star_big_on);
                }else {
                    starImage.setImageResource(android.R.drawable.btn_star_big_off);
                }

                ImageView imageView = findViewById(R.id.ivBook);
                String bookCoverIsbn = book.getPrincipalIsbn();
                if(bookCoverIsbn != null){
                    imageView.setImageBitmap(ImageData.getInstance().getImage(bookCoverIsbn));
                }
                TextView tvTitleBook = findViewById(R.id.tvTitleBook);
                TextView tvAuthorName = findViewById(R.id.tvAuthorNameBook);
                TextView tvBookYear = findViewById(R.id.tvPublishYearBook);
                TextView tvPageNumber = findViewById(R.id.tvNumberPageBook);
                TextView tvLink = findViewById(R.id.tvLink);

                tvTitleBook.setText(book.getTitle());
                tvAuthorName.setText(book.getAuthorName());
                tvBookYear.setText(book.getFirstPublishYear());
                tvPageNumber.setText(book.getPageNumber());


                tvAuthorName.setOnClickListener(view -> {
                    String authorKey = book.getAuthorKey();
                    if (authorKey != null) {
                        Intent newIntent = new Intent(BookActivity.this, AuthorActivity.class);
                        newIntent.putExtra("AUTHOR_KEY", authorKey);
                        startActivity(newIntent);
                    } else {
                        // Afficher un message d'alerte si authorKey est nulle
                        AlertDialog.Builder builder = new AlertDialog.Builder(BookActivity.this);
                        builder.setMessage("Erreur de recuperation des données de l'auteur !");
                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        AlertDialog dialog = builder.create();
                        dialog.show();
                    }
                });


                starImage.setOnClickListener(view -> {
                    boolean isStared = !book.isStar();

                    // mise à jour de l'image Star
                    if (isStared) {
                        starImage.setImageResource(android.R.drawable.btn_star_big_on);
                        book.setStar(true);

                        // Utilisez AsyncTask pour insérer le livre dans la base de données Room
                        new AsyncTask<Void, Void, List<Book>>() {
                            @Override
                            protected List<Book> doInBackground(Void... voids) {
                                // Vérifiez d'abord si le livre existe dans la base de données
                                if (!bookDao.containsBook(book.getKey())) {
                                    // Aucun livre similaire trouvé, vous pouvez insérer le nouveau livre
                                    bookDao.insert(book);
                                    BookListActivity.favoriteList.getBooks().add(book);
                                }
                                return bookDao.getAllBooks();
                            }

                            @Override
                            protected void onPostExecute(List<Book> books) {
                            }
                        }.execute();
                    } else {
                        starImage.setImageResource(android.R.drawable.btn_star_big_off);
                        // mise à jour de l'état du livre
                        new AsyncTask<Void, Void, Void>() {
                            @Override
                            protected Void doInBackground(Void... voids) {
                                bookDao.deleteBookByKey(book.getKey());
                                BookListActivity.favoriteList.getBooks().remove(book);
                                return null;
                            }

                            @Override
                            protected void onPostExecute(Void aVoid) {
                                super.onPostExecute(aVoid);
                            }
                        }.execute();

                        book.setStar(false);
                    }
                });

                tvLink.setOnClickListener(view -> {
                    String bookLink = book.getLink();

                    if (bookLink != null && !bookLink.isEmpty()) {
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(bookLink));
                        //.setPackage("com.android.chrome");
                        startActivity(browserIntent);
                    } else {
                        Toast.makeText(BookActivity.this, "Aucun lien disponible pour ce livre", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }
}

package fr.eilco.booksprojects.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import fr.eilco.booksprojects.R;
import fr.eilco.booksprojects.adapters.BookListAdapter;
import fr.eilco.booksprojects.data.AuthorData;
import fr.eilco.booksprojects.data.BookData;
import fr.eilco.booksprojects.database.AppDatabase;
import fr.eilco.booksprojects.database.BookDao;
import fr.eilco.booksprojects.model.Author;
import fr.eilco.booksprojects.model.Book;
import fr.eilco.booksprojects.serverOperations.GetRequest;
import fr.eilco.booksprojects.serverOperations.callback.AuthorGetRequestCallback;
import fr.eilco.booksprojects.serverOperations.callback.BookGetRequestCallback;
import fr.eilco.booksprojects.serverOperations.callback.ImageBookGetRequestCallback;


public class BookListActivity extends AppCompatActivity {

    private RecyclerView rvRandomList;
    private RecyclerView rvNewBooks;
    private RecyclerView rvFavorite;
    private int cardViewId = R.layout.view_book_card;
    private BookListAdapter adapterRandomList;
    private BookListAdapter adapterNewList;
    private BookListAdapter adapterFavorite;
    public static BookData randomList;
    public static BookData newList;

    public static BookData favoriteList;


    @SuppressLint("StaticFieldLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_list);

        ImageView ivSearchImageView = findViewById(R.id.ivSearch);
        ivSearchImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BookListActivity.this, SearchActivity.class);
                startActivity(intent);
            }
        });

        GetRequest getRequest = new GetRequest();
        GetRequest getRequest2 = new GetRequest();

        rvRandomList = findViewById(R.id.rvRandomList);
        rvNewBooks = findViewById(R.id.rvNewBooks);
        rvFavorite = findViewById(R.id.rvFavorites);

        AppDatabase db = AppDatabase.getInstance(getApplicationContext());
        // Obtenez les DAO KeyValueDao et BookDao
        BookDao bookDao = db.bookDao();

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                List<Book> favoriteBooks= bookDao.getAllBooks();
                favoriteList = new BookData(favoriteBooks);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                adapterFavorite = new BookListAdapter(cardViewId, favoriteList.getBooks(),"FavoriteList");
                rvFavorite.setLayoutManager(new LinearLayoutManager(BookListActivity.this, LinearLayoutManager.HORIZONTAL, false));
                rvFavorite.setAdapter(adapterFavorite);
                super.onPostExecute(aVoid);
            }
        }.execute();


        String randomWord = UtilsWord.getRandomWord(this);
        TextView tvRandomWord = findViewById(R.id.tvRandomWord);
        if(!randomWord.isEmpty()){
            tvRandomWord.setText(randomWord);
        }
        Log.i("RANDOM",randomWord);
        getRequest.retrBooks(randomWord, 15, new BookGetRequestCallback() {
            @Override
            public void onSuccess(List<Book> books) {

                randomList = new BookData(books);
                adapterRandomList = new BookListAdapter(cardViewId, new ArrayList<>(books),"RandomList");

                rvRandomList.setLayoutManager(new LinearLayoutManager(BookListActivity.this, LinearLayoutManager.HORIZONTAL, false));
                rvRandomList.setAdapter(adapterRandomList);

                for (Book bookItem:randomList.getBooks()) {
                    GetRequest getRequestImage = new GetRequest();
                    getRequestImage.retrBookImage(bookItem, new ImageBookGetRequestCallback() {
                        @Override
                        public void onSuccess(Bitmap image) {
                            rvRandomList.setAdapter(adapterRandomList);
                        }

                        @Override
                        public void onFailure() {
                        }
                    });

                    String authorKey = bookItem.getAuthorKey();
                    if(!AuthorData.getInstance().haveAuthor(authorKey)){
                        GetRequest getRequestAuthor = new GetRequest();

                        getRequestAuthor.retrAuthor(authorKey, new AuthorGetRequestCallback() {
                            @Override
                            public void onSuccess(Author author) {
                                author.setKey(authorKey);
                                AuthorData.getInstance().addAuthor(author);
                            }

                            @Override
                            public void onFailure(Throwable t) {
                            }
                        });
                    }

                }
            }

            @Override
            public void onFailure(Throwable t) {
                Log.e("Request DEBUG", t.getMessage());
            }
        });


        getRequest2.retrBooks("The Lord of the Rings", 15,  new BookGetRequestCallback() {
            @Override
            public void onSuccess(List<Book> books) {

                newList = new BookData(books);

                adapterNewList = new BookListAdapter(cardViewId, new ArrayList<>(books),"NewBooks");
                rvNewBooks.setLayoutManager(new LinearLayoutManager(BookListActivity.this, LinearLayoutManager.HORIZONTAL, false));
                rvNewBooks.setAdapter(adapterNewList);

                for (Book bookItem:newList.getBooks()) {
                    GetRequest getRequestImage = new GetRequest();
                    getRequestImage.retrBookImage(bookItem, new ImageBookGetRequestCallback() {
                        @Override
                        public void onSuccess(Bitmap image) {
                            rvNewBooks.setAdapter(adapterNewList);
                        }

                        @Override
                        public void onFailure() {

                        }
                    });

                    String authorKey = bookItem.getAuthorKey();
                    if(!AuthorData.getInstance().haveAuthor(authorKey)){
                        GetRequest getRequestAuthor = new GetRequest();

                        getRequestAuthor.retrAuthor(authorKey, new AuthorGetRequestCallback() {
                            @Override
                            public void onSuccess(Author author) {
                                author.setKey(authorKey);
                                AuthorData.getInstance().addAuthor(author);
                            }

                            @Override
                            public void onFailure(Throwable t) {
                            }
                        });
                    }
                }
            }

            @Override
            public void onFailure(Throwable t) {
                ;
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        adapterFavorite = new BookListAdapter(cardViewId, favoriteList.getBooks(), "FavoriteList");
        rvFavorite.setLayoutManager(new LinearLayoutManager(BookListActivity.this, LinearLayoutManager.HORIZONTAL, false));
        rvFavorite.setAdapter(adapterFavorite);
    }

}


package fr.eilco.booksprojects.activity;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.Random;

import fr.eilco.booksprojects.R;

public final class UtilsWord {

    public static String getRandomWord(Context context) {
        InputStream inputStream = context.getResources().openRawResource(R.raw.liste_mots);
        ArrayList<String> mots = new ArrayList<>();

        // Lire le fichier
        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = br.readLine()) != null) {
                mots.add(line);
            }
        } catch (IOException e) {
            Log.e("Random", e.getMessage());
            e.printStackTrace();
        }

        // Générer un mot au hasard
        if (!mots.isEmpty()) {
            Random random = new Random();
            int index = random.nextInt(mots.size());
            return mots.get(index);
        } else {
            return null;
        }
    }


}


package fr.eilco.booksprojects.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import fr.eilco.booksprojects.R;
import fr.eilco.booksprojects.data.AuthorData;
import fr.eilco.booksprojects.model.Author;

public class AuthorActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_author);

        Intent intent = getIntent();
        String authorKey = intent.getStringExtra("AUTHOR_KEY");

        if(authorKey != null){
            Author author;
            author = AuthorData.getInstance().getAuthor(authorKey);

            if(author != null){
                TextView tvAuthorName = findViewById(R.id.tvAuthorName);
                TextView tvBioAuthor = findViewById(R.id.tvBioAuthor);
                TextView tvBirthAuthor = findViewById(R.id.tvBirthAuthor);
                TextView tvDeathAuthor = findViewById(R.id.tvDeathAuthor);
                TextView tvWikipedia = findViewById(R.id.tvWikipediaAuthor);
                TextView tvWebsite = findViewById(R.id.tvWebsiteAuthor);

                tvAuthorName.setText(author.getName());
                tvBioAuthor.setText(author.getBiographie());
                tvBirthAuthor.setText(author.getBirthDate());
                tvDeathAuthor.setText(author.getDeathDate());
                tvWikipedia.setText(author.getWikipedia());
                tvWebsite.setText(author.getWebsite());
            }
        }

    }
}

package fr.eilco.booksprojects.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import fr.eilco.booksprojects.R;
import fr.eilco.booksprojects.adapters.BookListAdapter;
import fr.eilco.booksprojects.data.AuthorData;
import fr.eilco.booksprojects.data.BookData;
import fr.eilco.booksprojects.model.Author;
import fr.eilco.booksprojects.model.Book;
import fr.eilco.booksprojects.serverOperations.GetRequest;
import fr.eilco.booksprojects.serverOperations.callback.AuthorGetRequestCallback;
import fr.eilco.booksprojects.serverOperations.callback.BookGetRequestCallback;

public class SearchActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private Button validerButton;
    //private SearchView searchView;
    private EditText searchEditText;
    private TextView searchTextView;
    private BookListAdapter adapter;
    private BookData searchResults;
    public static BookData searchList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        //searchView = findViewById(R.id.searchView);
        searchEditText = findViewById(R.id.searchEditText);
        validerButton = findViewById(R.id.validerButton);
        recyclerView = findViewById(R.id.recyclerView);
        searchTextView = findViewById(R.id.searchTextView);

        searchResults = new BookData();
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3)); //2 colonnes


        adapter = new BookListAdapter(R.layout.view_book_card, searchResults.getBooks(), "SearchResults");
        recyclerView.setAdapter(adapter);

        validerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                performSearch();
            }
        });
        recyclerView.setAdapter(adapter);

        TextView helloTextView = findViewById(R.id.searchTextView);
        helloTextView.setText("Recherche de Books");
    }

    private void performSearch() {
        String searchQuery = searchEditText.getText().toString();

        if (!TextUtils.isEmpty(searchQuery)) {
            GetRequest getRequest = new GetRequest();
            getRequest.retrBooks(searchQuery, 35, new BookGetRequestCallback() {
                @Override
                public void onSuccess(List<Book> books) {
                    searchList = new BookData(books);
                    searchResults.setBooks(books);
                    adapter.updateBooks(books);  // Use the new method to update data
                    searchTextView.setText(getString(R.string.search_results, searchQuery));

                    for (Book bookItem:searchList.getBooks()){
                        String authorKey = bookItem.getAuthorKey();
                        if(!AuthorData.getInstance().haveAuthor(authorKey)){
                            GetRequest getRequestAuthor = new GetRequest();

                            getRequestAuthor.retrAuthor(authorKey, new AuthorGetRequestCallback() {
                                @Override
                                public void onSuccess(Author author) {
                                    author.setKey(authorKey);
                                    AuthorData.getInstance().addAuthor(author);
                                }

                                @Override
                                public void onFailure(Throwable t) {
                                }
                            });
                        }
                    }

                }

                @Override
                public void onFailure(Throwable t) {
                    Toast.makeText(SearchActivity.this, "Error during search", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(this, "Please enter a valid search term", Toast.LENGTH_SHORT).show();
        }
    }
}

package fr.eilco.booksprojects.mapper;

import android.util.Log;

import fr.eilco.booksprojects.model.Author;
import fr.eilco.booksprojects.serverOperations.valueApi.AuthorValueApi;


public class AuthorMapper {
    public static Author toDomainAuthor(AuthorValueApi valueApi){
        Author author = new Author();
        author.setName(valueApi.getName());
        author.setBiographie(valueApi.getBio());
        author.setWikipedia(valueApi.getWikipedia());
        author.setWebsite(valueApi.getWebsite());
        author.setBirthDate(valueApi.getBirth_date());
        author.setDeathDate(valueApi.getDeath_date());
        return author;
    }
}


package fr.eilco.booksprojects.mapper;

import android.util.Log;

import fr.eilco.booksprojects.model.Book;
import fr.eilco.booksprojects.serverOperations.valueApi.BookValueApi;

public class BookMapper {

    public static Book toDomainBook(BookValueApi valueApi){
        Book book = new Book();
        book.setTitle(valueApi.getTitle());
        book.setIsbnList(valueApi.getIsbn());
        book.setAuthorName(valueApi.getAuthor_name() != null && !valueApi.getAuthor_name().isEmpty() ? valueApi.getAuthor_name().get(0) : "");
        book.setAuthorKey(valueApi.getAuthor_key() != null && !valueApi.getAuthor_key().isEmpty() ? valueApi.getAuthor_key().get(0) : "");
        book.setFirstPublishYear(valueApi.getFirst_publish_year());
        book.setPageNumber(valueApi.getNumber_of_pages_median());

        String link;
        if(valueApi.getSeed() != null){
            link = "https://openlibrary.org".concat(valueApi.getSeed().get(0));
        }else {
            link = "https://openlibrary.org";
        }

        book.setLink(link);

        String key = valueApi.getKey();
        int index = key.indexOf("/works/") + "/works/".length();
        String effectiveKey = key.substring(index);

        book.setKey(effectiveKey);
        return book;
    }
}


package fr.eilco.booksprojects.adapters;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import fr.eilco.booksprojects.R;
import fr.eilco.booksprojects.activity.BookActivity;
import fr.eilco.booksprojects.data.ImageData;
import fr.eilco.booksprojects.model.Book;

public class BookListAdapter extends RecyclerView.Adapter<BookListAdapter.BookViewHolder> {

    private final List<Book> localDataSet;
    //Context context;
    private int resourceId;
    //the Id of the layout we will repeat as many times we have items in the list

    private String listName;

    public BookListAdapter( int resourceId, List<Book> books, String listName){
        //this.context = context;
        this.localDataSet = books;
        this.resourceId = resourceId;
        this.listName = listName;
    }

    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(resourceId,parent, false);
        return new BookViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookViewHolder holder, int position) {

        if(localDataSet != null && position < localDataSet.size()){
            Book book = localDataSet.get(position);
            holder.tvBookTitle.setText(book.getTitle().toString());
            String isbn = book.getPrincipalIsbn();
            if(isbn != null){
                ImageView imageView =  holder.itemView.findViewById(R.id.imageView);
                Bitmap imageBook = ImageData.getInstance().getImage(isbn);
                if(imageBook!=null){
                    imageView.setImageBitmap(ImageData.getInstance().getImage(isbn));
                }
            }

            // Gestion du clic sur la carte
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Récupérer le livre associé à cette carte
                    int position = holder.getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        String keyBook = book.getKey();
                        if (keyBook != null) { // Vérifier si keyBook n'est pas nulle
                            // Ouvrir une nouvelle activité en transmettant les données du livre
                            Intent intent = new Intent(view.getContext(), BookActivity.class);
                            intent.putExtra("BOOK_KEY", keyBook);
                            intent.putExtra("BookListName", listName);
                            view.getContext().startActivity(intent);
                        } else {
                            // Afficher un message d'alerte si keyBook est nulle
                            AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                            builder.setMessage("Erreur sur les informartions du livre !");
                            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                            AlertDialog dialog = builder.create();
                            dialog.show();
                        }
                    }

                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return localDataSet.size();
    }


    public class BookViewHolder extends RecyclerView.ViewHolder{
        private TextView tvBookTitle;

        public BookViewHolder(View cardView){
            super(cardView);
            tvBookTitle = cardView.findViewById(R.id.tvBookTitle);
        }
    }

    public void updateBooks(List<Book> newBooks) {
        localDataSet.clear();
        localDataSet.addAll(newBooks);
        notifyDataSetChanged();
    }

}


package fr.eilco.booksprojects.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import fr.eilco.booksprojects.R;
import fr.eilco.booksprojects.data.ImageData;
import fr.eilco.booksprojects.model.Book;

public class SearchBookListAdapter extends RecyclerView.Adapter<SearchBookListAdapter.SearchBookViewHolder> {

    private final List<Book> searchDataSet;
    private int resourceId;
    private OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(Book book);
    }

    public SearchBookListAdapter(int resourceId, List<Book> searchDataSet) {
        this.resourceId = resourceId;
        this.searchDataSet = searchDataSet;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    @NonNull
    @Override
    public SearchBookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(resourceId, parent, false);
        return new SearchBookViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchBookViewHolder holder, int position) {
        if (searchDataSet != null && position < searchDataSet.size()) {
            Book book = searchDataSet.get(position);
            holder.tvBookTitle.setText(book.getTitle().toString());
            String isbn = book.getPrincipalIsbn();
            if (isbn != null) {
                ImageView imageView = holder.itemView.findViewById(R.id.imageView);
                imageView.setImageBitmap(ImageData.getInstance().getImage(isbn));
            }

            // Handle click on the item
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (onItemClickListener != null) {
                        onItemClickListener.onItemClick(book);
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return searchDataSet.size();
    }

    public static class SearchBookViewHolder extends RecyclerView.ViewHolder {
        private TextView tvBookTitle;

        public SearchBookViewHolder(View cardView) {
            super(cardView);
            tvBookTitle = cardView.findViewById(R.id.tvBookTitle);
        }
    }

    public void updateSearchResults(List<Book> newBooks) {
        searchDataSet.clear();
        searchDataSet.addAll(newBooks);
        notifyDataSetChanged();
    }
}

package fr.eilco.booksprojects.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "Author")
public class Author {

    @PrimaryKey
    private int id;

    private String key;

    private String name;

    private String biographie;

    private String wikipedia;

    private String website;

    private String birthDate;

    private String deathDate;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBiographie() {
        return biographie;
    }

    public void setBiographie(String biographie) {
        this.biographie = biographie;
    }

    public String getWikipedia() {
        return wikipedia;
    }

    public void setWikipedia(String wikipedia) {
        this.wikipedia = wikipedia;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public String getDeathDate() {
        return deathDate;
    }

    public void setDeathDate(String deathDate) {
        this.deathDate = deathDate;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }

    @Override
    public String toString() {
        return "Author{" +
                "id=" + id +
                ", key='" + key + '\'' +
                ", name='" + name + '\'' +
                ", biographie='" + biographie + '\'' +
                ", wikipedia='" + wikipedia + '\'' +
                ", website='" + website + '\'' +
                ", birthDate='" + birthDate + '\'' +
                ", deathDate='" + deathDate + '\'' +
                '}';
    }
}


package fr.eilco.booksprojects.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity(tableName = "Books")
public class Book {

        private int id;

        @PrimaryKey
        @NonNull
        private String key;

        private String title;

        private String principalIsbn;

        private List<String> isbnList = new ArrayList<>();

        private String firstPublishYear;

        private String link;
        private String pageNumber;

        private String authorName;

        private String authorKey;

        private boolean isStar;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getFirstPublishYear() {
        return firstPublishYear;
    }

    public boolean isStar() {
        return isStar;
    }

    public void setStar(boolean star) {
        isStar = star;
    }

    public void setFirstPublishYear(String firstPublishYear) {
        this.firstPublishYear = firstPublishYear;
    }

    public String getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(String pageNumber) {
        this.pageNumber = pageNumber;
    }

    public Book(int id, String title){
            this.id = id;
            this.title = title;
        }

    public Book() {
    }

        public int getId() {
                return id;
        }

    public List<String> getIsbnList() {
        return isbnList;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getAuthorKey() {
        return authorKey;
    }

    public void setAuthorKey(String authorKey) {
        this.authorKey = authorKey;
    }

    public String getTitle() {
                return title;
        }

    public String getPrincipalIsbn() {
        return principalIsbn;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public void setId(int id) {
                this.id = id;
        }

        public void setTitle(String title) {
                this.title = title;
        }

    public void setIsbnList(List<String> isbnList) {
        this.isbnList = isbnList;
    }


    public void setPrincipalIsbn(String principalIsbn) {
        this.principalIsbn = principalIsbn;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Book book = (Book) o;
        return Objects.equals(key, book.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, title, principalIsbn, isbnList, firstPublishYear, pageNumber, authorName, authorKey, isStar);
    }

    @Override
    public String toString() {
        return "Book{" +
                "id=" + id +
                ", key='" + key + '\'' +
                ", title='" + title + '\'' +
                ", principalIsbn='" + principalIsbn + '\'' +
                ", isbnList=" + isbnList +
                ", firstPublishYear='" + firstPublishYear + '\'' +
                ", pageNumber='" + pageNumber + '\'' +
                ", authorName='" + authorName + '\'' +
                ", authorKey='" + authorKey + '\'' +
                '}';
    }
}


package fr.eilco.booksprojects.serverOperations;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.util.List;

import fr.eilco.booksprojects.data.ImageData;
import fr.eilco.booksprojects.model.Book;
import fr.eilco.booksprojects.serverOperations.apiService.AuthorApiService;
import fr.eilco.booksprojects.serverOperations.apiService.BookApiService;
import fr.eilco.booksprojects.serverOperations.callback.AuthorGetRequestCallback;
import fr.eilco.booksprojects.serverOperations.callback.BookGetRequestCallback;
import fr.eilco.booksprojects.serverOperations.callback.ImageBookGetRequestCallback;
import fr.eilco.booksprojects.serverOperations.valueApi.AuthorValueApi;
import fr.eilco.booksprojects.serverOperations.valueApi.BooksApiResponse;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class GetRequest {

    private static final String BASE_URL = "https://openlibrary.org/";
    private static final String BASE_URL_COVER = "https://covers.openlibrary.org/";

    public void retrBooks(String searchName, int limit, BookGetRequestCallback callback) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        BookApiService apiService = retrofit.create(BookApiService.class);
        Call<BooksApiResponse> call = apiService.getBooks(searchName,limit);

        call.enqueue(callback);
    }

    public void retrBookImage(Book book, ImageBookGetRequestCallback callback) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL_COVER)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        BookApiService apiService = retrofit.create(BookApiService.class);

        if (book.getIsbnList() != null && !book.getIsbnList().isEmpty()) {
            retrieveImageForIsbn(apiService, book, callback, book.getIsbnList(), 0);
        } else {
            callback.onFailure();
        }
    }

    private void retrieveImageForIsbn(BookApiService apiService, Book book, ImageBookGetRequestCallback callback, List<String> isbnList, int index) {
        if (index >= isbnList.size()) {
            callback.onFailure();
            return;
        }

        String isbn = isbnList.get(index);
        Call<ResponseBody> call = apiService.getImageBook(isbn);
        ImageBookGetRequestCallback.ImageResponseHandler responseHandler =
                new ImageBookGetRequestCallback.ImageResponseHandler(callback, book, isbn) {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (response.isSuccessful()) {
                            String contentType = response.headers().get("content-type");
                            if (book.getPrincipalIsbn() == null) {
                                if (contentType != null && contentType.equals("image/jpeg")) {
                                    Bitmap image = GetRequest.convertResponseBodyToBitmap(response.body());
                                    ImageData.getInstance().addImage(isbn, image);
                                    book.setPrincipalIsbn(isbn);
                                    callback.onSuccess(image); // Passer les données au callback
                                    return;
                                }
                            }
                        }
                        retrieveImageForIsbn(apiService, book, callback, isbnList, index + 1);
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        retrieveImageForIsbn(apiService, book, callback, isbnList, index + 1);
                    }
                };
        call.enqueue(responseHandler);
    }


    public void retrAuthor(String authorKey, AuthorGetRequestCallback callback){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        AuthorApiService apiService = retrofit.create(AuthorApiService.class);
        Call<AuthorValueApi> call = apiService.getAuthor(authorKey);

        call.enqueue(callback);
    }

    public static Bitmap convertResponseBodyToBitmap(ResponseBody responseBody) {
        Bitmap bitmap = null;
        try {
            byte[] bytes = responseBody.bytes();
            bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }
}


package fr.eilco.booksprojects.serverOperations.valueApi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BookValueApi {

    @JsonProperty("title")
    private String title;

    @JsonProperty("key")
    private String key;

    @JsonProperty("author_name")
    private List<String> author_name;

    @JsonProperty("author_key")
    private List<String> author_key;

    @JsonProperty("number_of_pages_median")
    private String number_of_pages_median;

    @JsonProperty("isbn")
    private List<String> isbn;

    @JsonProperty("seed")
    private List<String> seed;

    @JsonProperty("first_publish_year")
    private String first_publish_year;

    public String getTitle() {
        return title;
    }

    public String getKey() {
        return key;
    }

    public List<String> getAuthor_name() {
        return author_name;
    }

    public List<String> getAuthor_key() {
        return author_key;
    }

    public String getNumber_of_pages_median() {
        return number_of_pages_median;
    }

    public List<String> getIsbn() {
        return isbn;
    }

    public List<String> getSeed() {
        return seed;
    }

    public String getFirst_publish_year() {
        return first_publish_year;
    }

    @Override
    public String toString() {
        return "BookValueApi{" +
                "title='" + title + '\'' +
                ", key='" + key + '\'' +
                ", author_name=" + author_name +
                ", author_key=" + author_key +
                ", number_of_pages_median='" + number_of_pages_median + '\'' +
                ", isbn=" + isbn +
                ", first_publish_year=" + first_publish_year +
                '}';
    }
}


package fr.eilco.booksprojects.serverOperations.valueApi;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class BooksApiResponse {

    @SerializedName("docs")
    private List<BookValueApi> books;

    public List<BookValueApi> getBooks() {
        return books;
    }
}


package fr.eilco.booksprojects.serverOperations.valueApi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AuthorValueApi {
    @JsonProperty("name")
    private String name;

    @JsonProperty("bio")
    private String bio;

    @JsonProperty("wikipedia")
    private String wikipedia;

    @JsonProperty("website")
    private String website;

    @JsonProperty("birth_date")
    private String birth_date;

    @JsonProperty("death_date")
    private String death_date;


    public String getName() {
        return name;
    }

    public String getBio() {
        return bio;
    }

    public String getWikipedia() {
        return wikipedia;
    }

    public String getWebsite() {
        return website;
    }

    public String getBirth_date() {
        return birth_date;
    }

    public String getDeath_date() {
        return death_date;
    }

    @Override
    public String toString() {
        return "AuthorValueApi{" +
                "name='" + name + '\'' +
                ", bio='" + bio + '\'' +
                ", wikipedia='" + wikipedia + '\'' +
                ", website='" + website + '\'' +
                ", birth_date='" + birth_date + '\'' +
                ", death_date='" + death_date + '\'' +
                '}';
    }
}


package fr.eilco.booksprojects.serverOperations.callback;

import android.graphics.Bitmap;

import fr.eilco.booksprojects.data.ImageData;
import fr.eilco.booksprojects.model.Book;
import fr.eilco.booksprojects.serverOperations.GetRequest;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public interface ImageBookGetRequestCallback {
    void onSuccess(Bitmap image);
    void onFailure();

    class ImageResponseHandler implements Callback<ResponseBody> {

        private final ImageBookGetRequestCallback callback;
        private final Book book;
        private final String isbn;

        public ImageResponseHandler(ImageBookGetRequestCallback callback, Book book, String isbn) {
            this.callback = callback;
            this.book = book;
            this.isbn = isbn;
        }

        @Override
        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
            if (response.isSuccessful()) {
                String contentType = response.headers().get("content-type");
                if (book.getPrincipalIsbn() == null) {
                    if (contentType != null && contentType.equals("image/jpeg")) {
                        Bitmap image = GetRequest.convertResponseBodyToBitmap(response.body());
                        ImageData.getInstance().addImage(isbn, image);
                        book.setPrincipalIsbn(isbn);
                        callback.onSuccess(image); // Passer les données au callback
                        return;
                    }
                }
            }
            callback.onFailure(); // Signaler l'échec au callback
        }

        @Override
        public void onFailure(Call<ResponseBody> call, Throwable t) {
            callback.onFailure(); // Signaler l'échec au callback
        }
    }
}


package fr.eilco.booksprojects.serverOperations.callback;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import fr.eilco.booksprojects.mapper.BookMapper;
import fr.eilco.booksprojects.model.Book;
import fr.eilco.booksprojects.serverOperations.valueApi.BookValueApi;
import fr.eilco.booksprojects.serverOperations.valueApi.BooksApiResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public interface BookGetRequestCallback extends Callback<BooksApiResponse> {

    void onSuccess(List<Book> books);
    void onFailure(Throwable t);

    @Override
    default void onResponse(Call<BooksApiResponse> call, Response<BooksApiResponse> response) {
        List<Book> booksList = new ArrayList<>();
        if (response.isSuccessful()) {
            BooksApiResponse booksResponse = response.body();
            if (booksResponse != null && booksResponse.getBooks() != null) {
                for (BookValueApi bookApi : booksResponse.getBooks()) {
                    Book book = BookMapper.toDomainBook(bookApi);
                    booksList.add(book);
                }
                onSuccess(booksList);
            }
        } else {
            onFailure(new Exception("Request failed: " + response.code()));
        }
    }

    @Override
    default void onFailure(@NonNull Call<BooksApiResponse> call, Throwable t) {
        onFailure(t);
    }
}


package fr.eilco.booksprojects.serverOperations.callback;

import androidx.annotation.NonNull;

import fr.eilco.booksprojects.mapper.AuthorMapper;
import fr.eilco.booksprojects.model.Author;
import fr.eilco.booksprojects.serverOperations.valueApi.AuthorValueApi;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public interface AuthorGetRequestCallback extends Callback<AuthorValueApi> {

    void onSuccess(Author author);
    void onFailure(Throwable t);

    @Override
    default void onResponse(Call<AuthorValueApi> call, Response<AuthorValueApi> response) {
        if(response.isSuccessful()){
            AuthorValueApi authorValueApi = response.body();
            if(authorValueApi != null){
                Author author = AuthorMapper.toDomainAuthor(authorValueApi);
                onSuccess(author);
            }
        } else {
            onFailure(new Exception("Request failed: " + response.code()));
        }
    }

    @Override
    default void onFailure(@NonNull Call<AuthorValueApi> call, Throwable t) {
        onFailure(t);
    }
}


package fr.eilco.booksprojects.serverOperations.apiService;


import fr.eilco.booksprojects.serverOperations.valueApi.AuthorValueApi;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface AuthorApiService {
    // https://openlibrary.org/authors/OL2622837A.json
    @GET("authors/{keySearch}.json")
    Call<AuthorValueApi> getAuthor(
            @Path("keySearch") String keySearch
    );

}

package fr.eilco.booksprojects.serverOperations.apiService;


import fr.eilco.booksprojects.serverOperations.valueApi.BooksApiResponse;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface BookApiService {
    @GET("search.json")
    Call<BooksApiResponse> getBooks(
            @Query("q") String searchName,
            @Query("limit") int limit);


    //https://openlibrary.org/search.json?q=your_search_query&limit=10

    // https://covers.openlibrary.org/b/isbn/9780130501042-S.jpg
    @GET("b/isbn/{isbn}-M.jpg")
    Call<ResponseBody> getImageBook(
            @Path("isbn") String isbn);


}


package fr.eilco.booksprojects.data;

import java.util.ArrayList;
import java.util.List;

import fr.eilco.booksprojects.model.Book;

public class BookData {
    private List<Book> books;

    public BookData(){
        books = new ArrayList<>();
    }

    public BookData(List<Book> books){
        this.books = new ArrayList<>();
        this.books = books;
    }

    public List<Book> getBooks() {
        return books;
    }

    public void setBooks(List<Book> books) {
        this.books = books;
    }

    public Book getBookByKey(String key){
        for (Book book: books) {
            if(book.getKey().equals(key)){
                return book;
            }
        }
        return null;
    }

}


package fr.eilco.booksprojects.data;

import java.util.HashMap;

import fr.eilco.booksprojects.model.Author;

public class AuthorData {

    private static AuthorData instance;

    private HashMap<String, Author> authorHashMap;

    private AuthorData(){
        authorHashMap = new HashMap<>();
    }

    public static AuthorData getInstance(){
        if (instance == null){
            instance = new AuthorData();
        }
        return instance;
    }

    public Author getAuthor(String key){
        return authorHashMap.get(key);
    }

    public void addAuthor(Author author){
        authorHashMap.put(author.getKey(),author);
    }

    public Boolean haveAuthor(String authorKey){
        return  authorHashMap.containsKey(authorKey);
    }

}


package fr.eilco.booksprojects.data;


import android.graphics.Bitmap;

import java.util.HashMap;

public final class ImageData {
    private static ImageData instance;

    private HashMap<String, Bitmap> imageMap;
    private ImageData() {imageMap = new HashMap<>();};

    public static ImageData getInstance() {
        if (instance == null) {
            instance = new ImageData();
        }
        return instance;
    }

    public Bitmap getImage(String isbn){
        return imageMap.get(isbn);
    }

    public void addImage(String isbn, Bitmap image){
        imageMap.put(isbn, image);
    }

    public void deleteImage(String isbn){
        imageMap.remove(isbn);
    }

    public void clearImages() {
        imageMap.clear();
    }
}


