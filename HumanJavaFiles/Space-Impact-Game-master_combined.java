package com.example.shefaliupadhyaya.project1;

import android.app.Application;
import android.test.ApplicationTestCase;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class ApplicationTest extends ApplicationTestCase<Application> {
    public ApplicationTest() {
        super(Application.class);
    }
}

package com.example.shefaliupadhyaya.project1;

import java.util.Random;

/**
 * Created by Shefali Upadhyaya on 13-07-2016.
 */
public class Star {
    private int x;
    private int y;
    private int speed;
    private int maxX;
    private int maxY;
    private int minX;
    private int minY;

    public Star(int screenX, int screenY) {
        maxX = screenX;
        maxY = screenY;
        minX = 0;
        minY = 0;
        Random generator = new Random();
        speed = generator.nextInt(10);

        //generating a random coordinate
        //but keeping the coordinate inside the screen size
        x = generator.nextInt(maxX);
        y = generator.nextInt(maxY);
    }

    public void update(int playerSpeed) {
        //animating the star horizontally left side
        //by decreasing x coordinate with player speed
        x -= playerSpeed;
        x -= speed;
        //if the star reached the left edge of the screen
        if (x < 0) {
            //again starting the star from right edge
            //this will give a infinite scrolling background effect
            x = maxX;
            Random generator = new Random();
            y = generator.nextInt(maxY);
            speed = generator.nextInt(15);
        }
    }

    public float getStarWidth() {
        //Making the star width random so that
        //it will give a real look
        float minX = 1.0f;
        float maxX = 4.0f;
        Random rand = new Random();
        float finalX = rand.nextFloat() * (maxX - minX) + minX;
        return finalX;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}


package com.example.shefaliupadhyaya.project1;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;

import java.util.Random;

/**
 * Created by Shefali Upadhyaya on 13-07-2016.
 */
public class Enemy {
    //bitmap for the enemy
    //we have already pasted the bitmap in the drawable folder
    private Bitmap bitmap;
    //x and y coordinates
    private int x;
    private int y;
    //enemy speed
    private int speed = 1;
    //min and max coordinates to keep the enemy inside the screen
    private int maxX;
    private int minX;
    private int maxY;
    private int minY;
    //creating a rect object
    public static int ecount=0;
    private Rect detectCollision;
    public Enemy(Context context, int screenX, int screenY) {
        //getting bitmap from drawable resource
        bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.enemy);

        //initializing min and max coordinates
        maxX = screenX;
        maxY = screenY;
        minX = 0;
        minY = 0;

        //generating a random coordinate to add enemy
        Random generator = new Random();
        speed = generator.nextInt(6) + 10;
        x = screenX;
        y = generator.nextInt(maxY) - bitmap.getHeight();
        //initializing rect object
        detectCollision = new Rect(x, y, bitmap.getWidth(), bitmap.getHeight());
    }

    public void update(int playerSpeed) {
        //decreasing x coordinate so that enemy will move right to left
        x -= playerSpeed;
        x -= speed;
        //if the enemy reaches the left edge
        if (x < minX - bitmap.getWidth()) {
            //adding the enemy again to the right edge
            ecount++;
            Random generator = new Random();
            speed = generator.nextInt(10) + 10;
            x = maxX;
            y = generator.nextInt(maxY) - bitmap.getHeight();
        }
        //Adding the top, left, bottom and right to the rect object
        detectCollision.left = x;
        detectCollision.top = y;
        detectCollision.right = x + bitmap.getWidth();
        detectCollision.bottom = y + bitmap.getHeight();
    }
    //adding a setter to x coordinate so that we can change it after collision
    public void setX(int x){
        this.x = x;
    }
    //public void setY(int y) { this.y = y; }
    public void setXY(int x, int y) { this.x=x; this.y=y; }
    //one more getter for getting the rect object
    public Rect getDetectCollision() {
        return detectCollision;
    }
    //getters
    public Bitmap getBitmap() {
        return bitmap;
    }
    public int getX() { return x; }
    public int getY() {
        return y;
    }
    public int getSpeed() {
        return speed;
    }
}


package com.example.shefaliupadhyaya.project1;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.AudioManager;
import android.media.SoundPool;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import java.util.ArrayList;
import java.util.Random;

/**
 * Created by Shefali Upadhyaya on 11-07-2016.
 */
public class GameView extends SurfaceView implements Runnable {
    //boolean variable to track if the game is playing or not
    volatile boolean playing;
     private int k=0;
    //the game thread
    private Thread gameThread = null;
    //adding the player to this class
    private Player player;
    //These objects will be used for drawing
    private Paint paint;
    private Canvas canvas;
    private SurfaceHolder surfaceHolder;
    //Class constructor
    //Adding an stars list
    private ArrayList<Star> stars = new
            ArrayList<Star>();
    //Adding enemies object array
    private Enemy[] enemies;
    //Adding 3 enemies you may increase the size
    private static int enemyCount = 3;
    //defining a boom object to display blast
    private Boom boom;
    private static int score=0;
    /*The SoundPool class manages and plays audio resources for applications.
    A SoundPool is a collection of samples that can be loaded into memory from a resource inside the APK
    or from a file in the file system.*/
    private SoundPool sound;
    private int whoop;
    public GameView(Context context, int screenX, int screenY) {
        super(context);
        //initializing player object
        //this time also passing screen size to player constructor
        player = new Player(context, screenX, screenY);

        //initializing drawing objects
        surfaceHolder = getHolder();
        paint = new Paint();

        //adding 100 stars you may increase the number
        int starNums = 100;
        for (int i = 0; i < starNums; i++) {
            Star s  = new Star(screenX, screenY);
            stars.add(s);
        }
        //initializing enemy object array
        enemies = new Enemy[enemyCount];
        for(int i=0; i<enemyCount; i++){
            enemies[i] = new Enemy(context, screenX, screenY);
        }
        //initializing boom object
        boom = new Boom(context);
        /*SoundPool (int maxStreams, int streamType, int srcQuality)
            maxStreams	int: the maximum number of simultaneous streams for this SoundPool object
             streamType	int: the audio stream type as described in AudioManager For example, game applications will normally use STREAM_MUSIC.
             srcQuality	int: the sample-rate converter quality. Currently has no effect. Use 0 for the default.
            int	STREAM_MUSIC-->The audio stream for music playback*/
        sound=new SoundPool(10, AudioManager.STREAM_MUSIC,0);
        //int	load(Context context, int resId, int priority (1 for no effect),Load the sound from the specified APK resource.
        whoop=sound.load(context,R.raw.whoosh,1);
    }
    @Override
    public void run() {
        while (playing) {
            //to update the frame
            update();
            //to draw the frame
            draw();
            //to control the frames drawn per second
            control();

        }
    }

    private void update() {
        Random generator=new Random();
        //updating player position
        player.update();
        //Updating the stars with player speed
        //setting boom outside the screen
        boom.setX(-250);
        boom.setY(-250);
        for (Star s : stars) {
            s.update(player.getSpeed());
        }
        //updating the enemy coordinate with respect to player speed
        for(int i=0; i<enemyCount; i++) {
            enemies[i].update(player.getSpeed());
            k=generator.nextInt(300);
            if (Rect.intersects(player.getDetectCollision(), enemies[i].getDetectCollision())) {
                //moving enemy outside the left edge
                //displaying boom at that location
                boom.setX(enemies[i].getX());
                boom.setY(enemies[i].getY());
                /*int play (int soundID,float leftVolume,float rightVolume,int priority,int loop,float rate)
                soundID	int: a soundID returned by the load() function
                leftVolume	float: left volume value (range = 0.0 to 1.0)
                rightVolume	float: right volume value (range = 0.0 to 1.0)
                priority	int: stream priority (0 = lowest priority)
                loop	int: loop mode (0 = no loop, -1 = loop forever)
                rate	float: playback rate (1.0 = normal playback, range 0.5 to 2.0)*/
                sound.play(whoop,1f,1f,0,0,1.5f);
                enemies[i].setXY(1000,k);
                score+=5;
            }
            else if(!Rect.intersects(player.getDetectCollision(), enemies[i].getDetectCollision()) & Enemy.ecount==5) {

                        Intent intent = new Intent().setClass(getContext(), Highscore.class);
                                ((Activity) getContext()).startActivityForResult(intent, 0);

            }
        }

    }

    public static int getScore() {
        return score;
    }

    private void draw() {
        if (surfaceHolder.getSurface().isValid()) {
            canvas = surfaceHolder.lockCanvas();
            canvas.drawColor(Color.BLACK);
            //setting the paint color to white to draw the stars
            paint.setColor(Color.WHITE);
            //drawing all stars
            for (Star s : stars) {
                paint.setStrokeWidth(s.getStarWidth());
                canvas.drawPoint(s.getX(), s.getY(), paint);
            }

            canvas.drawBitmap(
                    player.getBitmap(),
                    player.getX(),
                    player.getY(),
                    paint);
            //drawing the enemies
            for (int i = 0; i < enemyCount; i++) {
                canvas.drawBitmap(enemies[i].getBitmap(), enemies[i].getX(), enemies[i].getY(), paint);
            }
            //drawing boom image
            canvas.drawBitmap(
                    boom.getBitmap(),
                    boom.getX(),
                    boom.getY(),
                    paint
            );
            surfaceHolder.unlockCanvasAndPost(canvas);
        }
    }

    private void control() {
        try {
            gameThread.sleep(17);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void pause1() {
        //when the game is paused
        //setting the variable to false
        playing = false;
        try {
            //stopping the thread
            gameThread.join();
        } catch (InterruptedException e) {
        }
    }

    public void resume() {
        //when the game is resumed
        //starting the thread again
        playing = true;
        gameThread = new Thread(this);
        gameThread.start();
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_UP:
                //stopping the boosting when screen is released
                player.stopBoosting();
                break;
            case MotionEvent.ACTION_DOWN:
                //boosting the highscorewallpaper jet when screen is pressed
                player.setBoosting();
                break;
        }
        return true;
    }
}



package com.example.shefaliupadhyaya.project1;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.List;

public class MainHighScore extends Activity {
    ListView listView;
    ImageButton exit1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_high_score);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        listView=(ListView)findViewById(R.id.listView);
        exit1=(ImageButton)findViewById(R.id.imageButton2);
        DBHelper db = new DBHelper(getApplicationContext());
        List<Integer> list = db.getAllScores();
        if(list!=null) {
            ArrayAdapter<Integer> dataAdapter = new ArrayAdapter<>(this, R.layout.score_list, list);
            listView.setAdapter(dataAdapter);
        }
        else Toast.makeText(MainHighScore.this, "No scores enlisted.", Toast.LENGTH_SHORT).show();
        exit1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finishAffinity(); //works on Android 4.1 or higher
                System.exit(0);
                //android.os.Process.killProcess(android.os.Process.myPid());
            }
        });
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main_high_score, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}


package com.example.shefaliupadhyaya.project1;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    ImageButton playnow,exit,sound,info;
    MediaPlayer bckgrnd;
    TextView tv;
    private int flag1=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bckgrnd = MediaPlayer.create(MainActivity.this, R.raw.dualdragon);
        bckgrnd.setLooping(true);
        bckgrnd.start();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_main);
        tv = (TextView)findViewById(R.id.textView3);
        try {
            tv.setVisibility(View.GONE);
        }
        catch(Exception e){}
        info1();
        sound = (ImageButton)findViewById(R.id.sound);
        sound.setBackgroundResource(R.drawable.ic_volume_up_white_24dp);
        //final ImageButton finalSound = sound;
        sound.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (flag1 == 0) {
                    bckgrnd.pause();
                    sound.setBackgroundResource(R.drawable.ic_volume_off_white_24dp);
                    flag1 = 1;
                } else if (flag1 == 1) {
                    bckgrnd.start();
                    sound.setBackgroundResource(R.drawable.ic_volume_up_white_24dp);
                    flag1 = 0;
                }
            }
        });
        playnow=(ImageButton)findViewById(R.id.buttonPlay);
        exit=(ImageButton)findViewById(R.id.buttonScore);
        playnow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent("com.example.shefaliupadhyaya.project1.GameActivity");
                startActivity(intent);
            }
        });
        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finishAffinity(); //works on Android 4.1 or higher
                System.exit(0);
                //android.os.Process.killProcess(android.os.Process.myPid());
            }
        });

    }
    /*to make it invisible by changing its opacity
  msg.setalpha(0.0f);

  and to make it visible by changing its opacity
  msg.setalpha(1.0f);*/
    void info1(){
        info=(ImageButton)findViewById(R.id.info);
        info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tv.getVisibility() == TextView.GONE)
                    tv.setVisibility(View.VISIBLE);
                else
                    tv.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    @Override
    protected void onPause() {
        super.onPause();
        bckgrnd.release();
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}


package com.example.shefaliupadhyaya.project1;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;

/**
 * Created by Shefali Upadhyaya on 11-07-2016.
 */
public class Player {
    //Bitmap to get character from image
    private Bitmap bitmap;
    //coordinates
    private int x;
    private int y;
    //motion speed of the character
    private int speed = 0;
    //boolean variable to track the ship is boosting or not
    private boolean boosting;
    //Gravity Value to add gravity effect on the ship
    private final int GRAVITY = -10;
    //Controlling Y coordinate so that ship won't go outside the screen
    private int maxY;
    private int minY;
    //Limit the bounds of the ship's speed
    private final int MIN_SPEED = 1;
    private final int MAX_SPEED = 20;
    //constructor
    private Rect detectCollision;
    public Player(Context context, int screenX, int screenY) {
        x = 75;
        y = 50;
        speed = 1;
        bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.player);
        //calculating maxY
        maxY = screenY - bitmap.getHeight();
        //top edge's y point is 0 so min y will always be zero
        minY = 0;
        //setting the boosting value to false initially
        boosting = false;
        //initializing rect object
        detectCollision =  new Rect(x, y, bitmap.getWidth(), bitmap.getHeight());
    }
    //setting boosting true
    public void setBoosting() {
        boosting = true;
    }
    //setting boosting false
    public void stopBoosting() {
        boosting = false;
    }
    //Method to update coordinate of character
    public void update(){
        //updating x coordinate
        //x++;
        //if the ship is boosting
        if (boosting) {
            //speeding up the ship
            speed += 2;
        } else {
            //slowing down if not boosting
            speed -= 5;
        }
        //controlling the top speed
        if (speed > MAX_SPEED) {
            speed = MAX_SPEED;
        }
        //if the speed is less than min speed
        //controlling it so that it won't stop completely
        if (speed < MIN_SPEED) {
            speed = MIN_SPEED;
        }
        //moving the ship down
        y -= speed + GRAVITY;
        //but controlling it also so that it won't go off the screen
        if (y < minY) {
            y = minY;
        }
        if (y > maxY) {
            y = maxY;
        }
        //adding top, left, bottom and right to the rect object
        detectCollision.left = x;
        detectCollision.top = y;
        detectCollision.right = x + bitmap.getWidth();
        detectCollision.bottom = y + bitmap.getHeight();
    }
    //one more getter for getting the rect object
    public Rect getDetectCollision() {
        return detectCollision;
    }
    /*
    * These are getters you can generate it automatically
    * right click on editor -> generate -> getters
    * */
    public Bitmap getBitmap() {
        return bitmap;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getSpeed() {
        return speed;
    }
}


package com.example.shefaliupadhyaya.project1;

import android.content.pm.ActivityInfo;
import android.graphics.Point;
import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;


public class GameActivity extends AppCompatActivity {
    //declaring gameview
    public GameView gameView;
    private MediaPlayer bckgrndmusic;
    private int flag=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        bckgrndmusic=MediaPlayer.create(GameActivity.this,R.raw.skywanderer);
        bckgrndmusic.setLooping(true);
        bckgrndmusic.start();
            //Getting display object
            Display display = getWindowManager().getDefaultDisplay();
            //Getting the screen resolution into point object
            Point size = new Point();
            display.getSize(size);
            //Initializing game view object
            //this time we are also passing the screen size to the GameView constructor
        FrameLayout game = new FrameLayout(this);
        LinearLayout gameWidgets = new LinearLayout(this);
        final ImageButton pause= new ImageButton(this);
        pause.setBackgroundResource(R.drawable.ic_pause_white_24dp);
        gameView = new GameView(this, size.x, size.y);
        gameWidgets.addView(pause);
        game.addView(gameView);
        game.addView(gameWidgets);
        //adding it to content view
        setContentView(game);
        pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (flag == 0) {
                    gameView.pause1();
                    pause.setBackgroundResource(R.drawable.ic_play_arrow_white_24dp);
                    flag = 1;
                } else if (flag == 1) {
                    gameView.resume();
                    pause.setBackgroundResource(R.drawable.ic_pause_white_24dp);
                    flag = 0;
                }
            }
        });

    }
    //pausing the game when activity is paused
    @Override
    protected void onPause() {
        super.onPause();
        bckgrndmusic.release();
        gameView.pause1();
    }
    //running the game when activity is resumed
    @Override
    protected void onResume() {
        super.onResume();
        gameView.resume();
    }
}


package com.example.shefaliupadhyaya.project1;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * Created by Shefali Upadhyaya on 13-07-2016.
 */
public class Boom {
    //bitmap object
    private Bitmap bitmap;
    //coordinate variables
    private int x;
    private int y;
    //constructor
    public Boom(Context context) {
        //getting boom image from drawable resource
        bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.boom);

        //setting the coordinate outside the screen
        //so that it won't shown up in the screen
        //it will be only visible for a fraction of second
        //after collision
        x = -250;
        y = -250;
    }
    //setters for x and y to make it visible at the place of collision
    public void setX(int x) {
        this.x = x;
    }
    public void setY(int y) {
        this.y = y;
    }
    //getters
    public Bitmap getBitmap() {
        return bitmap;
    }
    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }
    public int getX() {
        return x;
    }
    public int getY() {
        return y;
    }
}


package com.example.shefaliupadhyaya.project1;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Point;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

public class Highscore extends AppCompatActivity //implements Runnable
{
    private TextView text;
    private ImageButton highsc;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_highscore);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        text = (TextView) findViewById(R.id.textView2);
        text.setText(String.valueOf(GameView.getScore()));
        highsc=(ImageButton)findViewById(R.id.imageButton);
        DBHelper db = new DBHelper(getApplicationContext());
        Integer value=GameView.getScore();
        if(value!=null) {
            db.insertScore(value); }
        highsc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent1 = new Intent("com.example.shefaliupadhyaya.project1.MainHighScore");
                startActivity(intent1);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_highscore, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}

