package com.example.sixteenpersonalties;

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
        assertEquals("com.example.sixteenpersonalties", appContext.getPackageName());
    }
}

package com.example.sixteenpersonalties;

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

package com.example.sixteenpersonalties;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class AdapterClass extends RecyclerView.Adapter<AdapterClass.ViewHolder> { // we have to make the adapter class a child of recycler view
    // THIS IS THE CONTROLLER OF MVC Architecture
    // In Andriod its called as an Adapter class

    ArrayList <PersonalityModel> data = new ArrayList<>(); // HERE WE CAN'T USE AN ARRAY AS IT STORES THE SAME TYPE OF DATA, SO WE HAVE TO USE AN ARRAYLIST WHICH CAN HAVE DIFFERENT TYPE OF DATA.

    public AdapterClass(ArrayList<PersonalityModel> data) {
        this.data = data;
    }

    @NonNull
    @Override
    public AdapterClass.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.personality_view, null));
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterClass.ViewHolder holder, int position) {
        holder.img.setImageResource(data.get(position).image);
        holder.desc.setText(data.get(position).desc);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView img;
        TextView desc;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.imgPersonality);
            desc = itemView.findViewById(R.id.personalityFact);
        }
    }
}


package com.example.sixteenpersonalties;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    RecyclerView mrecyclerView;
    ArrayList<PersonalityModel> data = new ArrayList<>();
    int[] catImages = {R.drawable.personality_1,
            R.drawable.personality_2,
            R.drawable.personality_3,
            R.drawable.personality_4,
            R.drawable.personality_5,
            R.drawable.personality_6,
            R.drawable.personality_7,
            R.drawable.personality_8,
            R.drawable.personality_9,
            R.drawable.personality_10,
            R.drawable.personality_11,
            R.drawable.personality_12,
            R.drawable.personality_13,
            R.drawable.personality_14,
            R.drawable.personality_15,
            R.drawable.personality_16
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mrecyclerView=findViewById(R.id.recyclerview);
        String [] catFacts = {getText(R.string.fact_1).toString(), getText(R.string.fact_2).toString(),
                getText(R.string.fact_3).toString(), getText(R.string.fact_4).toString(),
                getText(R.string.fact_5).toString(), getText(R.string.fact_6).toString(),
                getText(R.string.fact_7).toString(),getText(R.string.fact_8).toString(),
                getText(R.string.fact_9).toString(),getText(R.string.fact_10).toString(),
                getText(R.string.fact_11).toString(),getText(R.string.fact_12).toString(),
                getText(R.string.fact_13).toString(), getText(R.string.fact_14).toString(),
                getText(R.string.fact_15).toString(), getText(R.string.fact_16).toString()};
        for(int i=0; i<catImages.length; i++) {
            data.add(new PersonalityModel(catImages[i], catFacts[i]));
        }

        AdapterClass adapter = new AdapterClass(data);
        mrecyclerView.setAdapter(adapter);
        mrecyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
    }
}

package com.example.sixteenpersonalties;

public class PersonalityModel {
    // This is the data class of our app. It stores the basic details of a cat.
    // THIS ACTS AS A 'MODEL' of MVC architecture
    int image;
    String desc;

    // this is a model class / a data class
    public PersonalityModel(int image, String desc) {
        this.image = image;
        this.desc = desc;
    }
}


