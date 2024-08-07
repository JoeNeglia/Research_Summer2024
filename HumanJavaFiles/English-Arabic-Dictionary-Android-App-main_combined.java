package com.example.dictionary;

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
        assertEquals("com.example.dictionary", appContext.getPackageName());
    }
}

package com.example.dictionary;

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

package com.example.dictionary;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;

public class DictionaryListActivity extends AppCompatActivity {
    RecyclerView recyclerViewDictionary;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("TEST","5");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dictionary_list);
        //Adapter
        recyclerViewDictionary = findViewById(R.id.recycler_view_dictionary);
        ArrayList<Dictionary> dataSource = (ArrayList<Dictionary>) getIntent().getSerializableExtra("KEY_DICTIONARY_WORD_LIST");
        DictionaryAdapter adapter = new DictionaryAdapter(dataSource);
        recyclerViewDictionary.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewDictionary.setAdapter(adapter);
    }
}

package com.example.dictionary;

import java.io.Serializable;

public class Dictionary implements Serializable {
    private int englishWordID;
    private int arabicWordID;
    private int backgroundColorID;
    private int imageID;
    public Dictionary(int imageID,int englishWordID,int arabicWordID,int backgroundColorID)
    {
        this.arabicWordID=arabicWordID;
        this.englishWordID=englishWordID;
        this.imageID=imageID;
        this.backgroundColorID=backgroundColorID;
    }
    public int getEnglishWordID() {
        return englishWordID;
    }

    public void setEnglishWordID(int englishWordID) {
        this.englishWordID = englishWordID;
    }

    public int getArabicWordID() {
        return arabicWordID;
    }

    public void setArabicWordID(int arabicWordID) {
        this.arabicWordID = arabicWordID;
    }

    public int getBackgroundColorID() {
        return backgroundColorID;
    }

    public void setBackgroundColorID(int backgroundColorID) {
        this.backgroundColorID = backgroundColorID;
    }

    public int getImageID() {
        return imageID;
    }

    public void setImageID(int imageID) {
        this.imageID = imageID;
    }
}


package com.example.dictionary;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class DictionaryAdapter extends RecyclerView.Adapter<DictionaryAdapter.DictionaryViewHolder> {
    private ArrayList<Dictionary> dictionaryItems;
    public DictionaryAdapter(ArrayList<Dictionary> dictionaryItems)
    {

        this.dictionaryItems=dictionaryItems;
    }

    @NonNull
    @Override
    public DictionaryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.dictionary_element,parent,false);
        DictionaryViewHolder holder=new DictionaryViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull DictionaryViewHolder holder, int position) {
        Dictionary dictionaryElement=dictionaryItems.get(position);
        holder.englishWord.setText(dictionaryElement.getEnglishWordID());
        holder.arabicWord.setText(dictionaryElement.getArabicWordID());
        if(dictionaryElement.getImageID()!=0)
            holder.imageWord.setImageResource(dictionaryElement.getImageID());
        //holder.constraintLayoutElement.setBackgroundColor(dictionaryElement.getBackgroundColorID());
    }

    @Override
    public int getItemCount() {
        return dictionaryItems.size();
    }

    class DictionaryViewHolder extends RecyclerView.ViewHolder{
        TextView englishWord,arabicWord;
        ImageView imageWord;
        ConstraintLayout constraintLayoutElement;
        public DictionaryViewHolder(@NonNull View itemView) {
            super(itemView);
            englishWord=itemView.findViewById(R.id.text_view_dictionary_element_english);
            arabicWord=itemView.findViewById(R.id.text_view_dictionary_element_arabic);
            imageWord=itemView.findViewById(R.id.image_view_dictionary_element);
            if(dictionaryItems.get(0).getImageID() == 0)
                imageWord.setVisibility(View.GONE);
            else
                imageWord.setVisibility(View.VISIBLE);
            itemView.setBackgroundResource(dictionaryItems.get(0).getBackgroundColorID());
        }
    }
}


package com.example.dictionary;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Scanner;

public class HomeActivity extends AppCompatActivity {
    TextView textViewColors,textViewPhrases,textViewFamily,textViewNumbers;
    public final String KEY_DICTIONARY_WORD_LIST="KEY_DICTIONARY_WORD_LIST";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        //
        textViewColors=findViewById(R.id.text_view_colors);
        textViewFamily=findViewById(R.id.text_view_family_members);
        textViewNumbers=findViewById(R.id.text_view_numbers);
        textViewPhrases=findViewById(R.id.text_view_phrases);
        //datasources
        //1-colors
        ArrayList<Dictionary> colors=new ArrayList<>();
        colors.add(new Dictionary(R.drawable.color_black,R.string.color_black_english,
                R.string.color_black_arabic,R.color.colors_background));
        colors.add(new Dictionary(R.drawable.color_brown,R.string.color_brown_english,
                R.string.color_brown_arabic,R.color.colors_background));
        colors.add(new Dictionary(R.drawable.color_dusty_yellow,R.string.color_dusty_yellow_english,
                R.string.color_dusty_yellow_arabic,R.color.colors_background));
        colors.add(new Dictionary(R.drawable.color_mustard_yellow,R.string.color_mustard_yellow_english,
                R.string.color_mustard_yellow_arabic,R.color.colors_background));
        colors.add(new Dictionary(R.drawable.color_gray,R.string.color_grey_english,
                R.string.color_grey_arabic,R.color.colors_background));
        colors.add(new Dictionary(R.drawable.color_green,R.string.color_green_english,
                R.string.color_green_arabic,R.color.colors_background));
        colors.add(new Dictionary(R.drawable.color_red,R.string.color_red_english,
                R.string.color_red_arabic,R.color.colors_background));
        colors.add(new Dictionary(R.drawable.color_white,R.string.color_white_english,
                R.string.color_white_arabic,R.color.colors_background));
        //2-family
        ArrayList<Dictionary> familyMembers=new ArrayList<>();
        familyMembers.add(new Dictionary(R.drawable.family_daughter,R.string.family_daughter_english,
                R.string.family_daughter_arabic,R.color.family_members_background));
        familyMembers.add(new Dictionary(R.drawable.family_son,R.string.family_son_english,
                R.string.family_son_arabic,R.color.family_members_background));
        familyMembers.add(new Dictionary(R.drawable.family_father,
                R.string.family_father_english,R.string.family_father_arabic,R.color.family_members_background));
        familyMembers.add(new Dictionary(R.drawable.family_mother,R.string.family_mother_english,
                R.string.family_mother_arabic,R.color.family_members_background));
        familyMembers.add(new Dictionary(R.drawable.family_grandfather,R.string.family_grand_father_english,
                R.string.family_grand_father_arabic,R.color.family_members_background));
        familyMembers.add(new Dictionary(R.drawable.family_grandmother,R.string.family_grand_mother_english,
                R.string.family_grand_mother_arabic,R.color.family_members_background));
        familyMembers.add(new Dictionary(R.drawable.family_older_brother,R.string.family_older_brother_english,
                R.string.family_older_brother_arabic,R.color.family_members_background));
        familyMembers.add(new Dictionary(R.drawable.family_younger_brother,R.string.family_younger_brother_english,
                R.string.family_younger_brother_arabic,R.color.family_members_background));
        familyMembers.add(new Dictionary(R.drawable.family_older_sister,R.string.family_older_sister_english,
                R.string.family_older_sister_arabic,R.color.family_members_background));
        familyMembers.add(new Dictionary(R.drawable.family_younger_sister,R.string.family_younger_sister_english,
                R.string.family_younger_sister_arabic,R.color.family_members_background));
        //3-numbers
        ArrayList<Dictionary> numbers=new ArrayList<>();
        numbers.add(new Dictionary(R.drawable.number_one,R.string.number_one_english,
                R.string.number_one_arabic,R.color.numbers_background));
        numbers.add(new Dictionary(R.drawable.number_two,R.string.number_two_english,
                R.string.number_two_arabic,R.color.numbers_background));
        numbers.add(new Dictionary(R.drawable.number_three,R.string.number_three_english,
                R.string.number_three_arabic,R.color.numbers_background));
        numbers.add(new Dictionary(R.drawable.number_four,R.string.number_four_english,
                R.string.number_four_arabic,R.color.numbers_background));
        numbers.add(new Dictionary(R.drawable.number_five,R.string.number_five_english,
                R.string.number_five_arabic,R.color.numbers_background));
        numbers.add(new Dictionary(R.drawable.number_six,R.string.number_six_english,
                R.string.number_six_arabic,R.color.numbers_background));
        numbers.add(new Dictionary(R.drawable.number_seven,R.string.number_seven_english,
                R.string.number_seven_arabic,R.color.numbers_background));
        numbers.add(new Dictionary(R.drawable.number_eight,R.string.number_eight_english,
                R.string.number_eight_arabic,R.color.numbers_background));
        numbers.add(new Dictionary(R.drawable.number_nine,R.string.number_nine_english,
                R.string.number_nine_arabic,R.color.numbers_background));
        numbers.add(new Dictionary(R.drawable.number_ten,R.string.number_ten_english,
                R.string.number_ten_arabic,R.color.numbers_background));
        //4-phrases
        ArrayList<Dictionary> phrases = new ArrayList<>();
        phrases.add(new Dictionary(0,R.string.phrase_are_you_coming_english,R.string.phrase_are_you_coming_arabic
        ,R.color.phrases_background));
        phrases.add(new Dictionary(0,R.string.phrase_yes_iam_coming_english,R.string.phrase_yes_iam_coming_arabic
                ,R.color.phrases_background));
        phrases.add(new Dictionary(0,R.string.phrase_what_is_your_name_english,R.string.phrase_what_is_your_name_arabic
                ,R.color.phrases_background));
        phrases.add(new Dictionary(0,R.string.phrase_my_name_is_english,R.string.phrase_my_name_is_arabic
                ,R.color.phrases_background));
        phrases.add(new Dictionary(0,R.string.phrase_where_are_you_going_english,R.string.phrase_where_are_you_going_arabic
                ,R.color.phrases_background));
        phrases.add(new Dictionary(0,R.string.phrase_iam_going_to_english,R.string.phrase_iam_going_to_arabic
                ,R.color.phrases_background));
        phrases.add(new Dictionary(0,R.string.phrase_how_are_you_feeling_english,R.string.phrase_how_are_you_feeling_arabic
                ,R.color.phrases_background));
        phrases.add(new Dictionary(0,R.string.phrase_iam_feeling_good_english,R.string.phrase_iam_feeling_good_arabic
                ,R.color.phrases_background));
        //Explicit intent
        explicitIntent(textViewColors,colors);
        explicitIntent(textViewFamily,familyMembers);
        explicitIntent(textViewNumbers,numbers);
        explicitIntent(textViewPhrases,phrases);
    }
    public void explicitIntent(TextView view,ArrayList<Dictionary> datasource)
    {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this,DictionaryListActivity.class);
                intent.putExtra("KEY_DICTIONARY_WORD_LIST",(Serializable) datasource);
                Log.d("TEST","1");
                startActivity(intent);
            }
        });
    }
}

