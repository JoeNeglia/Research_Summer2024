package com.example.superheroes;

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
        assertEquals("com.example.superheroes", appContext.getPackageName());
    }
}

package com.example.superheroes;

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

package com.example.superheroes;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

public class MainActivity2 extends AppCompatActivity {
    ImageView image_card;
    TextView name_card;
    TextView gender_card;
    TextView height_card;
    TextView race_card;
    TextView weight_card;
    TextView hometown_card;
    TextView publisher_card;
    TextView intelligence_card;
    TextView speed_card;
    TextView power_card;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        image_card = findViewById(R.id.image_card);
        name_card = findViewById(R.id.name_card);
        gender_card = findViewById(R.id.gender_card);
        height_card = findViewById(R.id.height_card);
        weight_card = findViewById(R.id.weight_card);
        race_card = findViewById(R.id.race_card);
        hometown_card = findViewById(R.id.hometown_card);
        publisher_card = findViewById(R.id.publicher_card);
        intelligence_card = findViewById(R.id.intelligence_card);
        speed_card = findViewById(R.id.speed_card);
        power_card = findViewById(R.id.power_card);

        Character character=getIntent().getParcelableExtra("Characters");

        Picasso.get().load(character.getImage_url()).resize(400,350).centerCrop().into(image_card);
        name_card.setText(character.getName());
        gender_card.setText(character.getGender());
        height_card.setText(character.getHeight());
        weight_card.setText(character.getWeight());
        race_card.setText(character.getRace());
        hometown_card.setText(character.getHometown());
        publisher_card.setText(character.getPublisher());
        power_card.setText(character.getPower());
        speed_card.setText(character.getSpeed());
        intelligence_card.setText(character.getIntelligence());
    }
}

package com.example.superheroes;


import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.ViewHolder> {
    private Context context;
    private List<Character> characterList;
    public CustomAdapter(Context context, List<Character> characterList){
        this.context=context;
        this.characterList=characterList;
    }
    @NonNull
    @Override
    public CustomAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_custom, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CustomAdapter.ViewHolder holder, int position) {
        Character character=characterList.get(position);
        holder.name.setText(character.getName());
        holder.publisher.setText(character.getPublisher());
        Picasso.get().load(character.getImage_url()).resize(350,250).centerCrop().into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return characterList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public TextView name;
        public TextView publisher;
        public ImageView imageView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            name=itemView.findViewById(R.id.name);
            publisher=itemView.findViewById(R.id.publisher);
            imageView=itemView.findViewById(R.id.imageView);
        }

        @Override
        public void onClick(View view) {
            int position=getAdapterPosition();
            Character character=characterList.get(position);
            Intent intent=new Intent(context,MainActivity2.class);
            intent.putExtra("Characters",character);
            context.startActivity(intent);
        }
    }
}


package com.example.superheroes;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    private CustomAdapter customAdapter;
    private ArrayList<Character> characterArrayList;
    FloatingActionButton refresh_button;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        refresh_button = findViewById(R.id.refresh_button);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        characterArrayList = new ArrayList<>();
        fetchData();
        refresh_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                characterArrayList.clear();
                fetchData();
            }
        });
    }

    public void fetchData()
    {
        RequestQueue requestQueue= Volley.newRequestQueue(this);
        String url="https://superhero-search.p.rapidapi.com/api/heroes";
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET,
                url, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                try {
                    for(int i=0;i< response.length();i++)
                    {
                        JSONObject basics=response.getJSONObject(i);
                        JSONObject stats=basics.getJSONObject("powerstats");
                        JSONObject appearance=basics.getJSONObject("appearance");
                        JSONArray heightobj=appearance.getJSONArray("height");
                        JSONArray weightobj=appearance.getJSONArray("weight");
                        JSONObject bio=basics.getJSONObject("biography");
                        String name=basics.getString("name");
                        String gender=appearance.getString("gender");
                        String height= String.valueOf(heightobj.get(1));
                        String race=appearance.getString("race");
                        String weight= String.valueOf(weightobj.get(1));
                        String hometown=bio.getString("placeOfBirth");
                        String publisher=bio.getString("publisher");
                        String image_url=basics.getJSONObject("images").getString("lg");
                        String intelligence=stats.getString("intelligence");
                        String speed=stats.getString("speed");
                        String power=stats.getString("power");
                        Character character=new Character(name,gender,height,race,weight,hometown,publisher,image_url,intelligence,speed,power);
                        characterArrayList.add(character);
                    }
                    customAdapter = new CustomAdapter(MainActivity.this,characterArrayList);
                    recyclerView.setAdapter(customAdapter);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("kala", "Something went wrong");
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("X-RapidAPI-Host", "Url");
                params.put("X-RapidAPI-Key", "Rapid Api Key");
                return params;
            }
        };
        requestQueue.add(jsonArrayRequest);
    }
}


package com.example.superheroes;

import android.os.Parcel;
import android.os.Parcelable;

public class Character implements Parcelable {
    String name;
    String gender;
    String height;
    String race;
    String weight;
    String hometown;
    String publisher;
    String image_url;
    String intelligence;
    String speed;
    String power;

    public Character(String name, String gender, String height, String race, String weight, String hometown, String publisher, String image_url, String intelligence, String speed, String power) {
        this.name = name;
        this.gender = gender;
        this.height = height;
        this.race = race;
        this.weight = weight;
        this.hometown = hometown;
        this.publisher = publisher;
        this.image_url = image_url;
        this.intelligence = intelligence;
        this.speed = speed;
        this.power = power;
    }

    protected Character(Parcel in) {
        name = in.readString();
        gender = in.readString();
        height = in.readString();
        race = in.readString();
        weight = in.readString();
        hometown = in.readString();
        publisher = in.readString();
        image_url = in.readString();
        intelligence = in.readString();
        speed = in.readString();
        power = in.readString();
    }

    public static final Creator<Character> CREATOR = new Creator<Character>() {
        @Override
        public Character createFromParcel(Parcel in) {
            return new Character(in);
        }

        @Override
        public Character[] newArray(int size) {
            return new Character[size];
        }
    };

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getHeight() {
        return height;
    }

    public void setHeight(String height) {
        this.height = height;
    }

    public String getRace() {
        return race;
    }

    public void setRace(String race) {
        this.race = race;
    }

    public String getWeight() {
        return weight;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }

    public String getHometown() {
        return hometown;
    }

    public void setHometown(String hometown) {
        this.hometown = hometown;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public String getIntelligence() {
        return intelligence;
    }

    public void setIntelligence(String intelligence) {
        this.intelligence = intelligence;
    }

    public String getSpeed() {
        return speed;
    }

    public void setSpeed(String speed) {
        this.speed = speed;
    }

    public String getPower() {
        return power;
    }

    public void setPower(String power) {
        this.power = power;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(name);
        parcel.writeString(gender);
        parcel.writeString(height);
        parcel.writeString(race);
        parcel.writeString(weight);
        parcel.writeString(hometown);
        parcel.writeString(publisher);
        parcel.writeString(image_url);
        parcel.writeString(intelligence);
        parcel.writeString(speed);
        parcel.writeString(power);
    }
}

