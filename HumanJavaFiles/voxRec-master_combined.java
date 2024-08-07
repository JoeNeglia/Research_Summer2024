package com.example.vishwas.voxrec;

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
        assertEquals("com.example.vishwas.voxrec", appContext.getPackageName());
    }
}

package com.example.vishwas.voxrec;

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

package com.example.vishwas.voxrec;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public class TimeAgo {

    public String getTimeAgo(long duration) {
        Date now = new Date();

        long seconds = TimeUnit.MILLISECONDS.toSeconds(now.getTime() - duration);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(now.getTime() - duration);
        long hours = TimeUnit.MILLISECONDS.toHours(now.getTime() - duration);
        long days = TimeUnit.MILLISECONDS.toDays(now.getTime() - duration);

        if(seconds < 60){
            return "just now";
        } else if (minutes == 1) {
            return "a minute ago";
        } else if (minutes > 1 && minutes < 60) {
            return minutes + " minutes ago";
        } else if (hours == 1) {
            return "an hour ago";
        } else if (hours > 1 && hours < 24) {
            return hours + " hours ago";
        } else if (days == 1) {
            return "a day ago";
        } else {
            return days + " days ago";
        }

    }

}


package com.example.vishwas.voxrec;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}

package com.example.vishwas.voxrec;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

public class splashScreen extends AppCompatActivity {
    private static int SPLASH_SCREEN = 3000;
    ImageView splashimg;
    TextView splashtxt;
    Animation top,bottom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        //Setting Full Screen view for Splash Screen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

        splashimg = findViewById(R.id.splash_img);
        splashtxt = findViewById(R.id.splash_text);
        //Now load the animation files
        top = AnimationUtils.loadAnimation(this,R.anim.top);
        bottom = AnimationUtils.loadAnimation(this,R.anim.bottom);

        splashimg.setAnimation(top);
        splashtxt.setAnimation(bottom);
        //Redirection to another activity after splash screen gets over
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(splashScreen.this,MainActivity.class));
                finish();
            }
        },SPLASH_SCREEN);

    }
}

package com.example.vishwas.voxrec.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vishwas.voxrec.R;
import com.example.vishwas.voxrec.TimeAgo;

import java.io.File;

public class Rec_list_adapter extends RecyclerView.Adapter<Rec_list_adapter.Rec_ViewHolder>
{
    private File[] allFiles;
    private TimeAgo timeAgo;
    private onItemList_click onitemList_click;

    public Rec_list_adapter(File[] allFiles, onItemList_click onitemList_click)
    {
        this.allFiles = allFiles;
        this.onitemList_click = onitemList_click;
    }

    @NonNull
    @Override
    public Rec_ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.record_list_item,parent,false);
        timeAgo = new TimeAgo();
        return new Rec_ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Rec_ViewHolder holder, int position) {

        holder.list_title.setText(allFiles[position].getName());
        holder.list_date.setText(timeAgo.getTimeAgo(allFiles[position].lastModified()));

    }

    @Override
    public int getItemCount() {
        return allFiles.length;
    }

    public class Rec_ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ImageView list_image_view;
        private TextView list_title;
        private TextView list_date;

        public Rec_ViewHolder(@NonNull View itemView) {
            super(itemView);

            list_image_view = itemView.findViewById(R.id.list_image_view);
            list_title = itemView.findViewById(R.id.list_title);
            list_date = itemView.findViewById(R.id.list_date);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {

            onitemList_click.onClick_Listener(allFiles[getAdapterPosition()],getAdapterPosition());

        }
    }

    public interface onItemList_click
    {
        void onClick_Listener(File file,int position);
    }
}


package com.example.vishwas.voxrec.Fragment;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.vishwas.voxrec.R;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class RecordFragment extends Fragment implements View.OnClickListener {

    private static final int AUDIO_PERMISSION_CODE = 89 ;
    private NavController navController;
    private ImageButton list_btn;
    private ImageButton record_btn;
    private boolean is_recording = false;
    private String recording_permission = Manifest.permission.RECORD_AUDIO;
    private MediaRecorder mediaRecorder;
    private String record_file;
    private Chronometer chronoTimer;
    private TextView record_file_name;

    public RecordFragment() {
        // Required empty public constructor
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        navController = Navigation.findNavController(view);
        list_btn = view.findViewById(R.id.record_list_button);
        record_btn = view.findViewById(R.id.record_button);
        chronoTimer = view.findViewById(R.id.record_timer);
        record_file_name = view.findViewById(R.id.record_filename);

        list_btn.setOnClickListener(this);
        record_btn.setOnClickListener(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_record, container, false);
    }

    @Override
    public void onClick(View view)
    {
        switch (view.getId())
        {
            case R.id.record_list_button:
                if (is_recording)
                {
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext());
                    alertDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            navController.navigate(R.id.action_recordFragment_to_recordListFragment);
                        }
                    });

                    alertDialog.setNegativeButton("Cancel",null);

                    alertDialog.setTitle("Audio still recording");
                    alertDialog.setMessage("Are you sure, you want to stop recording?");
                    alertDialog.create().show();
                }
                else
                {
                    navController.navigate(R.id.action_recordFragment_to_recordListFragment);
                }

                break;
            case R.id.record_button:
                if (is_recording)
                {//Stop Recording

                    //Stop Recording Method
                    stop_recording();
                    record_btn.setImageDrawable(getResources().getDrawable(R.drawable.record_btn_stopped));
                    is_recording = false;
                }
                else
                {
                    //Start Recording
                    if(checkAudioPermission()) {
                        //Start Recording Method
                        start_recording();
                        record_btn.setImageDrawable(getResources().getDrawable(R.drawable.record_btn_recording));
                        is_recording = true;
                    }
                }
                break;
        }
    }

    private void start_recording()
    {
        chronoTimer.setBase(SystemClock.elapsedRealtime());
        chronoTimer.start();



        String rec_path = getActivity().getExternalFilesDir("/").getAbsolutePath();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd_MM_YYYY_hh_mm_ss", Locale.CANADA);
        Date date = new Date();

        record_file = "voxRec"+ simpleDateFormat.format(date) +".3gp";

        record_file_name.setText("Recording File Name: \n"+ record_file);

        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setOutputFile(rec_path + "/" + record_file);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);


        try {
            mediaRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }

        mediaRecorder.start();
    }


    private void stop_recording()
    {
        chronoTimer.stop();
        is_recording = false;
        record_file_name.setText("Recording Stopped, File Saved: \n"+ record_file);



        mediaRecorder.stop();
        mediaRecorder.release();
        mediaRecorder = null;
    }

    private boolean checkAudioPermission()
    {
        if(ActivityCompat.checkSelfPermission(getContext(),recording_permission )== PackageManager.PERMISSION_GRANTED)
        {
            return true;
        }
        else
        {
            ActivityCompat.requestPermissions(getActivity(), new String[]{recording_permission}, AUDIO_PERMISSION_CODE);
            return false;
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if(is_recording) {
            stop_recording();
        }
    }
}

package com.example.vishwas.voxrec.Fragment;

import android.media.MediaPlayer;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.vishwas.voxrec.Adapter.Rec_list_adapter;
import com.example.vishwas.voxrec.R;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.io.File;
import java.io.IOException;


public class RecordListFragment extends Fragment implements Rec_list_adapter.onItemList_click {

    private ConstraintLayout audio_playersheet;
    private BottomSheetBehavior bottomSheetBehavior;
    private RecyclerView record_list;
    private File[] allFiles;
    private Rec_list_adapter rec_list_adapter;

    private MediaPlayer mediaPlayer = null;
    private boolean isPlaying = false;

    private File file_toPlay;
    private ImageButton play_btn;
    private ImageButton play_prev_btn;
    private ImageButton play_forw_btn;
    private TextView player_file_name;
    private TextView player_title;
    private SeekBar seekBar;
    private Handler seekbarHandler;
    private Runnable updateseekbar;


    public RecordListFragment() {
        // Required empty public constructor
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_record_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        audio_playersheet = view.findViewById(R.id.playersheet);
        bottomSheetBehavior = BottomSheetBehavior.from(audio_playersheet);
        record_list = view.findViewById(R.id.record_recycler_list);

        play_btn = view.findViewById(R.id.play_btn);
        play_prev_btn = view.findViewById(R.id.play_back_btn);
        play_forw_btn = view.findViewById(R.id.play_forward_btn);
        player_file_name = view.findViewById(R.id.player_file_name);
        player_title = view.findViewById(R.id.player_title);
        seekBar = view.findViewById(R.id.seekbar_player);


        //Storing recording files into Files array

        String rec_path_files = getActivity().getExternalFilesDir("/").getAbsolutePath();
        File dir = new File(rec_path_files);
        allFiles = dir.listFiles();

        rec_list_adapter = new Rec_list_adapter(allFiles,this);

        record_list.setHasFixedSize(true);
        record_list.setLayoutManager(new LinearLayoutManager(getContext()));
        record_list.setAdapter(rec_list_adapter);


        bottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if(newState == BottomSheetBehavior.STATE_HIDDEN)
                {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });

        play_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isPlaying)
                {
                    pauseAudio();
                }
                else {
                    if(file_toPlay!= null)
                    {
                        resumeAudio();
                    }
                }
            }
        });

        play_prev_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pauseAudio();
                int curr_pos = mediaPlayer.getCurrentPosition();
                mediaPlayer.seekTo(curr_pos-1000);
                int seek_prog = seekBar.getProgress();
                seekBar.setProgress(seek_prog-10);
                updateRunnable();
                resumeAudio();
            }
        });

        play_forw_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pauseAudio();
                int curr_pos = mediaPlayer.getCurrentPosition();
                mediaPlayer.seekTo(curr_pos+1000);
                int seek_prog = seekBar.getProgress();
                seekBar.setProgress(seek_prog+10);
                updateRunnable();
                resumeAudio();

            }
        });


        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                pauseAudio();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int progress = seekBar.getProgress();
                mediaPlayer.seekTo(progress);
                resumeAudio();
            }
        });

    }

    @Override
    public void onClick_Listener(File file, int position)
    {
        file_toPlay = file;
        if(isPlaying)
        {
            stopAudio();
            playAudio(file_toPlay);
        }
        else
        {
            playAudio(file_toPlay);
        }
    }

    private void stopAudio()
    {
        play_btn.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.player_play_btn,null));
        player_title.setText("Stopped");
        isPlaying = false;
        mediaPlayer.stop();
    }

    private void playAudio(File file_toPlay)
    {


        mediaPlayer = new MediaPlayer();
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        try {
            mediaPlayer.setDataSource(file_toPlay.getAbsolutePath());
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        play_btn.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.player_pause_btn,null));
        player_file_name.setText(file_toPlay.getName());
        player_title.setText("Playing");
        isPlaying = true;

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                stopAudio();
                player_title.setText("Finsihed");
            }
        });

        seekBar.setMax(mediaPlayer.getDuration());
        seekbarHandler = new Handler();
        updateRunnable();
        seekbarHandler.postDelayed(updateseekbar,0);
    }

    private void updateRunnable() {
        updateseekbar = new Runnable() {
            @Override
            public void run() {
                seekBar.setProgress(mediaPlayer.getCurrentPosition());
                seekbarHandler.postDelayed(this,500);
            }
        };
    }

    private void pauseAudio()
    {
        mediaPlayer.pause();
        play_btn.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.player_play_btn,null));
        isPlaying = false;
        seekbarHandler.removeCallbacks(updateseekbar);
    }

    private void resumeAudio()
    {
        mediaPlayer.start();
        play_btn.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.player_pause_btn,null));
        isPlaying = true;

        updateRunnable();
        seekbarHandler.postDelayed(updateseekbar,0);
    }

    @Override
    public void onStop() {
        super.onStop();
        if(isPlaying)
        {
            stopAudio();
        }
    }
}

