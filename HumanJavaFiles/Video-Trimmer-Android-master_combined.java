package com.ahmedbadereldin.videotrimmer;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.MimeTypeMap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FileUtil {
    private static final int EOF = -1;
    private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;

    // By default, Android doesn't provide support for JSON
    public static final String MIME_TYPE_JSON = "application/json";

    private FileUtil() {

    }

    public static File from(Context context, Uri uri) throws IOException {
        InputStream inputStream = context.getContentResolver().openInputStream(uri);
        String fileName = getFileName(context, uri);
        String[] splitName = splitFileName(fileName);
        File tempFile = File.createTempFile(splitName[0], splitName[1]);
        tempFile = rename(tempFile, fileName);
        tempFile.deleteOnExit();
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(tempFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        if (inputStream != null) {
            copy(inputStream, out);
            inputStream.close();
        }

        if (out != null) {
            out.close();
        }
        return tempFile;
    }

    private static String[] splitFileName(String fileName) {
        String name = fileName;
        String extension = "";
        int i = fileName.lastIndexOf(".");
        if (i != -1) {
            name = fileName.substring(0, i);
            extension = fileName.substring(i);
        }

        return new String[]{name, extension};
    }

    private static String getFileName(Context context, Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf(File.separator);
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    private static File rename(File file, String newName) {
        File newFile = new File(file.getParent(), newName);
        if (!newFile.equals(file)) {
            if (newFile.exists() && newFile.delete()) {
                Log.d("FileUtil", "Delete old " + newName + " file");
            }
            if (file.renameTo(newFile)) {
                Log.d("FileUtil", "Rename file to " + newName);
            }
        }
        return newFile;
    }

    private static long copy(InputStream input, OutputStream output) throws IOException {
        long count = 0;
        int n;
        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        while (EOF != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
            count += n;
        }
        return count;
    }

    @Nullable
    public static String getMimeType(@NonNull Context context, @NonNull Uri uri) {

        String mimeType = null;
        if (uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
            ContentResolver cr = context.getContentResolver();
            mimeType = cr.getType(uri);
        } else {
            String fileExtension = getExtension(uri.toString());

            if(fileExtension == null){
                return null;
            }

            mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                    fileExtension.toLowerCase());

            if(mimeType == null){
                // Handle the misc file extensions
                return handleMiscFileExtensions(fileExtension);
            }
        }
        return mimeType;
    }

    @Nullable
    private static String getExtension(@Nullable String fileName){

        if(fileName == null || TextUtils.isEmpty(fileName)){
            return null;
        }

        char[] arrayOfFilename = fileName.toCharArray();
        for(int i = arrayOfFilename.length-1; i > 0; i--){
            if(arrayOfFilename[i] == '.'){
                return fileName.substring(i+1, fileName.length());
            }
        }
        return null;
    }

    @Nullable
    private static String handleMiscFileExtensions(@NonNull String extension){

        if(extension.equals("json")){
            return MIME_TYPE_JSON;
        }
        else{
            return null;
        }
    }

    public static String getMimeType(File file) {
        String mimeType = "";
        String extension = getExtention(file.getName());
        if (MimeTypeMap.getSingleton().hasExtension(extension)) {
            mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return mimeType;
    }

    private static String getExtention(String fileName){
        char[] arrayOfFilename = fileName.toCharArray();
        for(int i = arrayOfFilename.length-1; i > 0; i--){
            if(arrayOfFilename[i] == '.'){
                return fileName.substring(i+1, fileName.length());
            }
        }
        return "";
    }
}

package com.ahmedbadereldin.videotrimmer;

import android.app.Activity;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import androidx.annotation.NonNull;

import com.ahmedbadereldin.videotrimmer.customVideoViews.OnVideoTrimListener;
import com.coremedia.iso.boxes.Container;
import com.googlecode.mp4parser.FileDataSourceViaHeapImpl;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.googlecode.mp4parser.authoring.tracks.AppendTrack;
import com.googlecode.mp4parser.authoring.tracks.CroppedTrack;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class Utility {
    public static final String VIDEO_FORMAT = ".mp4";
    private static final String TAG = Utility.class.getSimpleName();

    public static void startTrim(Activity activity, @NonNull File src, @NonNull String dst, long startMs, long endMs,
                                 @NonNull OnVideoTrimListener callback) throws IOException {
        File file1 = create(activity, dst);
        if (file1 != null)
            generateVideo(src, file1, startMs, endMs, callback);
    }

    private static void generateVideo(@NonNull File src, @NonNull File dst, long startMs,
                                      long endMs, @NonNull OnVideoTrimListener callback) throws IOException {


        // NOTE: Switched to using FileDataSourceViaHeapImpl since it does not use memory mapping (VM).
        // Otherwise we get OOM with large movie files.
        Movie movie = MovieCreator.build(new FileDataSourceViaHeapImpl(src.getAbsolutePath()));

        List<Track> tracks = movie.getTracks();
        movie.setTracks(new LinkedList<Track>());
        // remove all tracks we will create new tracks from the old

        double startTime1 = startMs / 1000;
        double endTime1 = endMs / 1000;

        boolean timeCorrected = false;

        // Here we try to find a track that has sync samples. Since we can only start decoding
        // at such a sample we SHOULD make sure that the start of the new fragment is exactly
        // such a frame
        for (Track track : tracks) {
            if (track.getSyncSamples() != null && track.getSyncSamples().length > 0) {
                if (timeCorrected) {
                    // This exception here could be a false positive in case we have multiple tracks
                    // with sync samples at exactly the same positions. E.g. a single movie containing
                    // multiple qualities of the same video (Microsoft Smooth Streaming file)

                    throw new RuntimeException("The startTime has already been corrected by another track with SyncSample. Not Supported.");
                }
                startTime1 = correctTimeToSyncSample(track, startTime1, false);
                endTime1 = correctTimeToSyncSample(track, endTime1, true);
                timeCorrected = true;
            }
        }

        for (Track track : tracks) {
            long currentSample = 0;
            double currentTime = 0;
            double lastTime = -1;
            long startSample1 = -1;
            long endSample1 = -1;

            for (int i = 0; i < track.getSampleDurations().length; i++) {
                long delta = track.getSampleDurations()[i];


                if (currentTime > lastTime && currentTime <= startTime1) {
                    // current sample is still before the new starttime
                    startSample1 = currentSample;
                }
                if (currentTime > lastTime && currentTime <= endTime1) {
                    // current sample is after the new start time and still before the new endtime
                    endSample1 = currentSample;
                }
                lastTime = currentTime;
                currentTime += (double) delta / (double) track.getTrackMetaData().getTimescale();
                currentSample++;
            }
            movie.addTrack(new AppendTrack(new CroppedTrack(track, startSample1, endSample1)));
        }

        Container out = new DefaultMp4Builder().build(movie);

        try {
            FileOutputStream fos = new FileOutputStream(dst);
            FileChannel fc = fos.getChannel();
            out.writeContainer(fc);
            fc.close();
            fos.close();
            if (callback != null)
                callback.getResult(Uri.parse(dst.toString()));
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    private static double correctTimeToSyncSample(Track track, double cutHere, boolean next) {
        double[] timeOfSyncSamples = new double[track.getSyncSamples().length];
        long currentSample = 0;
        double currentTime = 0;
        for (int i = 0; i < track.getSampleDurations().length; i++) {
            long delta = track.getSampleDurations()[i];
            if (Arrays.binarySearch(track.getSyncSamples(), currentSample + 1) >= 0) {
                // samples always start with 1 but we start with zero therefore +1
                timeOfSyncSamples[Arrays.binarySearch(track.getSyncSamples(), currentSample + 1)] = currentTime;
            }
            currentTime += (double) delta / (double) track.getTrackMetaData().getTimescale();
            currentSample++;
        }
        double previous = 0;
        for (double timeOfSyncSample : timeOfSyncSamples) {
            if (timeOfSyncSample > cutHere) {
                if (next) {
                    return timeOfSyncSample;
                } else {
                    return previous;
                }
            }
            previous = timeOfSyncSample;
        }
        return timeOfSyncSamples[timeOfSyncSamples.length - 1];
    }


    private static File create(Activity activity, String dst) {
        File file = new File(dst);
        file.getParentFile().mkdirs();

//        File storageDir = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File storageDir = activity.getExternalFilesDir(file.getParentFile().getAbsolutePath());
        Log.d(TAG, "Generated file path " + file.getParentFile().getAbsolutePath() + " ----< 123123  file1 " + storageDir.exists());
        Log.d(TAG, "Generated file path " + Environment.DIRECTORY_PICTURES + " ----< 123123  file1 " + storageDir.exists());

        try {
            return File.createTempFile(
                    activity.getResources().getString(R.string.app_name) + new Date().getTime(), /* prefix */
                    ".mp4", /* suffix */
                    storageDir /* directory */
            );
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}


package com.ahmedbadereldin.videotrimmer.customVideoViews;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.annotation.NonNull;

import com.ahmedbadereldin.videotrimmer.R;

import java.util.List;
import java.util.Vector;


public class BarThumb {

    public static final int LEFT = 0;
    public static final int RIGHT = 1;

    private int mIndex;
    private float mVal;
    private float mPos;
    private Bitmap mBitmap;
    private int mWidthBitmap;
    private int mHeightBitmap;

    private float mLastTouchX;

    private BarThumb() {
        mVal = 0;
        mPos = 0;
    }

    public int getIndex() {
        return mIndex;
    }

    private void setIndex(int index) {
        mIndex = index;
    }

    public float getVal() {
        return mVal;
    }

    public void setVal(float val) {
        mVal = val;
    }

    public float getPos() {
        return mPos;
    }

    public void setPos(float pos) {
        mPos = pos;
    }

    public Bitmap getBitmap() {
        return mBitmap;
    }

    private void setBitmap(@NonNull Bitmap bitmap) {
        mBitmap = bitmap;
        mWidthBitmap = bitmap.getWidth();
        mHeightBitmap = bitmap.getHeight();
    }

    @NonNull
    public static List<BarThumb> initThumbs(Resources resources) {

        List<BarThumb> barThumbs = new Vector<>();

        for (int i = 0; i < 2; i++) {
            BarThumb th = new BarThumb();
            th.setIndex(i);
            if (i == 0) {
                int resImageLeft = R.drawable.time_line_a;
                th.setBitmap(BitmapFactory.decodeResource(resources, resImageLeft));
            } else {
                int resImageRight = R.drawable.time_line_a;
                th.setBitmap(BitmapFactory.decodeResource(resources, resImageRight));
            }

            barThumbs.add(th);
        }

        return barThumbs;
    }

    public static int getWidthBitmap(@NonNull List<BarThumb> barThumbs) {
        return barThumbs.get(0).getWidthBitmap();
    }

    public static int getHeightBitmap(@NonNull List<BarThumb> barThumbs) {
        return barThumbs.get(0).getHeightBitmap();
    }

    public float getLastTouchX() {
        return mLastTouchX;
    }

    public void setLastTouchX(float lastTouchX) {
        mLastTouchX = lastTouchX;
    }

    public int getWidthBitmap() {
        return mWidthBitmap;
    }

    private int getHeightBitmap() {
        return mHeightBitmap;
    }
}


package com.ahmedbadereldin.videotrimmer.customVideoViews;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;


import com.ahmedbadereldin.videotrimmer.R;

import java.util.ArrayList;
import java.util.List;


public class CustomRangeSeekBar extends View {

    private int mHeightTimeLine;
    private List<BarThumb> mBarThumbs;
    private List<OnRangeSeekBarChangeListener> mListeners;
    private float mMaxWidth;
    private float mThumbWidth;
    private float mThumbHeight;
    private int mViewWidth;
    private float mPixelRangeMin;
    private float mPixelRangeMax;
    private float mScaleRangeMax;
    private boolean mFirstRun;

    private final Paint mShadow = new Paint();
    private final Paint mLine = new Paint();

    public CustomRangeSeekBar(@NonNull Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomRangeSeekBar(@NonNull Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mBarThumbs = BarThumb.initThumbs(getResources());
        mThumbWidth = BarThumb.getWidthBitmap(mBarThumbs);
        mThumbHeight = BarThumb.getHeightBitmap(mBarThumbs);

        mScaleRangeMax = 100;
        mHeightTimeLine = getContext().getResources().getDimensionPixelOffset(R.dimen.frames_video_height);

        setFocusable(true);
        setFocusableInTouchMode(true);

        mFirstRun = true;

        int shadowColor = ContextCompat.getColor(getContext(), R.color.shadow_color);
        mShadow.setAntiAlias(true);
        mShadow.setColor(shadowColor);
        mShadow.setAlpha(177);

        int lineColor = ContextCompat.getColor(getContext(), R.color.line_color);
        mLine.setAntiAlias(true);
        mLine.setColor(lineColor);
        mLine.setAlpha(200);
    }

    public void initMaxWidth() {
        mMaxWidth = mBarThumbs.get(1).getPos() - mBarThumbs.get(0).getPos();

        onSeekStop(this, 0, mBarThumbs.get(0).getVal());
        onSeekStop(this, 1, mBarThumbs.get(1).getVal());
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int minW = getPaddingLeft() + getPaddingRight() + getSuggestedMinimumWidth();
        mViewWidth = resolveSizeAndState(minW, widthMeasureSpec, 1);

        int minH = getPaddingBottom() + getPaddingTop() + (int) mThumbHeight;
        int viewHeight = resolveSizeAndState(minH, heightMeasureSpec, 1);

        setMeasuredDimension(mViewWidth, viewHeight);

        mPixelRangeMin = 0;
        mPixelRangeMax = mViewWidth - mThumbWidth;

        if (mFirstRun) {
            for (int i = 0; i < mBarThumbs.size(); i++) {
                BarThumb th = mBarThumbs.get(i);
                th.setVal(mScaleRangeMax * i);
                th.setPos(mPixelRangeMax * i);
            }
            // Fire listener callback
            onCreate(this, currentThumb, getThumbValue(currentThumb));
            mFirstRun = false;
        }
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        drawShadow(canvas);
        drawThumbs(canvas);
    }

    private int currentThumb = 0;

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent ev) {
        final BarThumb mBarThumb;
        final BarThumb mBarThumb2;
        final float coordinate = ev.getX();
        final int action = ev.getAction();

        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                // Remember where we started
                currentThumb = getClosestThumb(coordinate);

                if (currentThumb == -1) {
                    return false;
                }

                mBarThumb = mBarThumbs.get(currentThumb);
                mBarThumb.setLastTouchX(coordinate);
                onSeekStart(this, currentThumb, mBarThumb.getVal());
                return true;
            }
            case MotionEvent.ACTION_UP: {

                if (currentThumb == -1) {
                    return false;
                }

                mBarThumb = mBarThumbs.get(currentThumb);
                onSeekStop(this, currentThumb, mBarThumb.getVal());
                return true;
            }

            case MotionEvent.ACTION_MOVE: {
                mBarThumb = mBarThumbs.get(currentThumb);
                mBarThumb2 = mBarThumbs.get(currentThumb == 0 ? 1 : 0);
                // Calculate the distance moved
                final float dx = coordinate - mBarThumb.getLastTouchX();
                final float newX = mBarThumb.getPos() + dx;

                if (currentThumb == 0) {

                    if ((newX + mBarThumb.getWidthBitmap()) >= mBarThumb2.getPos()) {
                        mBarThumb.setPos(mBarThumb2.getPos() - mBarThumb.getWidthBitmap());
                    } else if (newX <= mPixelRangeMin) {
                        mBarThumb.setPos(mPixelRangeMin);
                        if ((mBarThumb2.getPos() - (mBarThumb.getPos() + dx)) > mMaxWidth) {
                            mBarThumb2.setPos(mBarThumb.getPos() + dx + mMaxWidth);
                            setThumbPos(1, mBarThumb2.getPos());
                        }
                    } else {
                        //Check if thumb is not out of max width
//                        checkPositionThumb(mBarThumb, mBarThumb2, dx, true, coordinate);
                        if ((mBarThumb2.getPos() - (mBarThumb.getPos() + dx)) > mMaxWidth) {
                            mBarThumb2.setPos(mBarThumb.getPos() + dx + mMaxWidth);
                            setThumbPos(1, mBarThumb2.getPos());
                        }
                        // Move the object
                        mBarThumb.setPos(mBarThumb.getPos() + dx);

                        // Remember this touch position for the next move event
                        mBarThumb.setLastTouchX(coordinate);
                    }

                } else {
                    if (newX <= mBarThumb2.getPos() + mBarThumb2.getWidthBitmap()) {
                        mBarThumb.setPos(mBarThumb2.getPos() + mBarThumb.getWidthBitmap());
                    } else if (newX >= mPixelRangeMax) {
                        mBarThumb.setPos(mPixelRangeMax);
                        if (((mBarThumb.getPos() + dx) - mBarThumb2.getPos()) > mMaxWidth) {
                            mBarThumb2.setPos(mBarThumb.getPos() + dx - mMaxWidth);
                            setThumbPos(0, mBarThumb2.getPos());
                        }
                    } else {
                        //Check if thumb is not out of max width
//                        checkPositionThumb(mBarThumb2, mBarThumb, dx, false, coordinate);
                        if (((mBarThumb.getPos() + dx) - mBarThumb2.getPos()) > mMaxWidth) {
                            mBarThumb2.setPos(mBarThumb.getPos() + dx - mMaxWidth);
                            setThumbPos(0, mBarThumb2.getPos());
                        }
                        // Move the object
                        mBarThumb.setPos(mBarThumb.getPos() + dx);
                        // Remember this touch position for the next move event
                        mBarThumb.setLastTouchX(coordinate);
                    }
                }

                setThumbPos(currentThumb, mBarThumb.getPos());

                // Invalidate to request a redraw
                invalidate();
                return true;
            }
        }
        return false;
    }

    private void checkPositionThumb(@NonNull BarThumb mBarThumbLeft, @NonNull BarThumb mBarThumbRight, float dx, boolean isLeftMove, float coordinate) {

        if (isLeftMove && dx < 0) {
            if ((mBarThumbRight.getPos() - (mBarThumbLeft.getPos() + dx)) > mMaxWidth) {
                mBarThumbRight.setPos(mBarThumbLeft.getPos() + dx + mMaxWidth);
                setThumbPos(1, mBarThumbRight.getPos());
            }
        } else if (!isLeftMove && dx > 0) {
            if (((mBarThumbRight.getPos() + dx) - mBarThumbLeft.getPos()) > mMaxWidth) {
                mBarThumbLeft.setPos(mBarThumbRight.getPos() + dx - mMaxWidth);
                setThumbPos(0, mBarThumbLeft.getPos());
            }
        }

    }


    private float pixelToScale(int index, float pixelValue) {
        float scale = (pixelValue * 100) / mPixelRangeMax;
        if (index == 0) {
            float pxThumb = (scale * mThumbWidth) / 100;
            return scale + (pxThumb * 100) / mPixelRangeMax;
        } else {
            float pxThumb = ((100 - scale) * mThumbWidth) / 100;
            return scale - (pxThumb * 100) / mPixelRangeMax;
        }
    }

    private float scaleToPixel(int index, float scaleValue) {
        float px = (scaleValue * mPixelRangeMax) / 100;
        if (index == 0) {
            float pxThumb = (scaleValue * mThumbWidth) / 100;
            return px - pxThumb;
        } else {
            float pxThumb = ((100 - scaleValue) * mThumbWidth) / 100;
            return px + pxThumb;
        }
    }

    private void calculateThumbValue(int index) {
        if (index < mBarThumbs.size() && !mBarThumbs.isEmpty()) {
            BarThumb th = mBarThumbs.get(index);
            th.setVal(pixelToScale(index, th.getPos()));
            onSeek(this, index, th.getVal());
        }
    }

    private void calculateThumbPos(int index) {
        if (index < mBarThumbs.size() && !mBarThumbs.isEmpty()) {
            BarThumb th = mBarThumbs.get(index);
            th.setPos(scaleToPixel(index, th.getVal()));
        }
    }

    private float getThumbValue(int index) {
        return mBarThumbs.get(index).getVal();
    }

    public void setThumbValue(int index, float value) {
        mBarThumbs.get(index).setVal(value);
        calculateThumbPos(index);
        // Tell the view we want a complete redraw
        invalidate();
    }

    private void setThumbPos(int index, float pos) {
        mBarThumbs.get(index).setPos(pos);
        calculateThumbValue(index);
        // Tell the view we want a complete redraw
        invalidate();
    }

    private int getClosestThumb(float coordinate) {
        int closest = -1;
        if (!mBarThumbs.isEmpty()) {
            for (int i = 0; i < mBarThumbs.size(); i++) {
                // Find thumb closest to x coordinate
                final float tcoordinate = mBarThumbs.get(i).getPos() + mThumbWidth;
                if (coordinate >= mBarThumbs.get(i).getPos() && coordinate <= tcoordinate) {
                    closest = mBarThumbs.get(i).getIndex();
                }
            }
        }
        return closest;
    }

    private void drawShadow(@NonNull Canvas canvas) {
        if (!mBarThumbs.isEmpty()) {

            for (BarThumb th : mBarThumbs) {
                if (th.getIndex() == 0) {
                    final float x = th.getPos();
                    if (x > mPixelRangeMin) {
                        Rect mRect = new Rect(0, (int) (mThumbHeight - mHeightTimeLine) / 2,
                                (int) (x + (mThumbWidth / 2)), mHeightTimeLine + (int) (mThumbHeight - mHeightTimeLine) / 2);
                        canvas.drawRect(mRect, mShadow);
                    }
                } else {
                    final float x = th.getPos();
                    if (x < mPixelRangeMax) {
                        Rect mRect = new Rect((int) (x + (mThumbWidth / 2)), (int) (mThumbHeight - mHeightTimeLine) / 2,
                                (mViewWidth), mHeightTimeLine + (int) (mThumbHeight - mHeightTimeLine) / 2);
                        canvas.drawRect(mRect, mShadow);
                    }
                }
            }
        }
    }

    private void drawThumbs(@NonNull Canvas canvas) {

        if (!mBarThumbs.isEmpty()) {
            for (BarThumb th : mBarThumbs) {
                if (th.getIndex() == 0) {
                    canvas.drawBitmap(th.getBitmap(), th.getPos() + getPaddingLeft(), getPaddingTop(), null);
                } else {
                    canvas.drawBitmap(th.getBitmap(), th.getPos() - getPaddingRight(), getPaddingTop(), null);
                }
            }
        }
    }

    public void addOnRangeSeekBarListener(OnRangeSeekBarChangeListener listener) {

        if (mListeners == null) {
            mListeners = new ArrayList<>();
        }

        mListeners.add(listener);
    }

    private void onCreate(CustomRangeSeekBar CustomRangeSeekBar, int index, float value) {
        if (mListeners == null)
            return;

        for (OnRangeSeekBarChangeListener item : mListeners) {
            item.onCreate(CustomRangeSeekBar, index, value);
        }
    }

    private void onSeek(CustomRangeSeekBar CustomRangeSeekBar, int index, float value) {
        if (mListeners == null)
            return;

        for (OnRangeSeekBarChangeListener item : mListeners) {
            item.onSeek(CustomRangeSeekBar, index, value);
        }
    }

    private void onSeekStart(CustomRangeSeekBar CustomRangeSeekBar, int index, float value) {
        if (mListeners == null)
            return;

        for (OnRangeSeekBarChangeListener item : mListeners) {
            item.onSeekStart(CustomRangeSeekBar, index, value);
        }
    }

    private void onSeekStop(CustomRangeSeekBar CustomRangeSeekBar, int index, float value) {
        if (mListeners == null)
            return;

        for (OnRangeSeekBarChangeListener item : mListeners) {
            item.onSeekStop(CustomRangeSeekBar, index, value);
        }
    }

    public List<BarThumb> getThumbs() {
        return mBarThumbs;
    }
}


package com.ahmedbadereldin.videotrimmer.customVideoViews;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.LongSparseArray;
import android.view.View;

import androidx.annotation.NonNull;


import com.ahmedbadereldin.videotrimmer.R;

import java.util.HashMap;
import java.util.Map;

public class TileView extends View {

    private Uri mVideoUri;
    private int mHeightView;
    private LongSparseArray<Bitmap> mBitmapList = null;
    private int viewWidth = 0;
    private int viewHeight = 0;

    public TileView(@NonNull Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TileView(@NonNull Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mHeightView = getContext().getResources().getDimensionPixelOffset(R.dimen.frames_video_height);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int minW = getPaddingLeft() + getPaddingRight() + getSuggestedMinimumWidth();
        int w = resolveSizeAndState(minW, widthMeasureSpec, 1);

        final int minH = getPaddingBottom() + getPaddingTop() + mHeightView;
        int h = resolveSizeAndState(minH, heightMeasureSpec, 1);

        setMeasuredDimension(w, h);
    }

    @Override
    protected void onSizeChanged(final int w, int h, final int oldW, int oldH) {
        super.onSizeChanged(w, h, oldW, oldH);
        viewWidth = w;
        viewHeight = h;
        if (w != oldW) {
            if (mVideoUri != null)
                getBitmap();
        }
    }

    private void getBitmap() {
        BackgroundTask
                .execute(new BackgroundTask.Task("", 0L, "") {
                             @Override
                             public void execute() {
                                 try {
                                     LongSparseArray<Bitmap> thumbnailList = new LongSparseArray<>();

                                     MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
                                     mediaMetadataRetriever.setDataSource(getContext(), mVideoUri);

                                     // Retrieve media data
                                     long videoLengthInMs = Integer.parseInt(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)) * 1000;

                                     // Set thumbnail properties (Thumbs are squares)
                                     final int thumbWidth = mHeightView;
                                     final int thumbHeight = mHeightView;

                                     int numThumbs = (int) Math.ceil(((float) viewWidth) / thumbWidth);

                                     final long interval = videoLengthInMs / numThumbs;

                                     for (int i = 0; i < numThumbs; ++i) {
                                         Bitmap bitmap = mediaMetadataRetriever.getFrameAtTime(i * interval, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
                                         // TODO: bitmap might be null here, hence throwing NullPointerException. You were right
                                         try {
                                             bitmap = Bitmap.createScaledBitmap(bitmap, thumbWidth, thumbHeight, false);
                                         } catch (Exception e) {
                                             e.printStackTrace();
                                         }
                                         thumbnailList.put(i, bitmap);
                                     }

                                     mediaMetadataRetriever.release();
                                     returnBitmaps(thumbnailList);
                                 } catch (final Throwable e) {
                                     Thread.getDefaultUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), e);
                                 }
                             }
                         }
                );
    }

    private void returnBitmaps(final LongSparseArray<Bitmap> thumbnailList) {
        new MainThreadExecutor().runTask("", new Runnable() {
                    @Override
                    public void run() {
                        mBitmapList = thumbnailList;
                        invalidate();
                    }
                }
                , 0L);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        if (mBitmapList != null) {
            canvas.save();
            int x = 0;

            for (int i = 0; i < mBitmapList.size(); i++) {
                Bitmap bitmap = mBitmapList.get(i);

                if (bitmap != null) {
                    canvas.drawBitmap(bitmap, x, 0, null);
                    x = x + bitmap.getWidth();
                }
            }
        }
    }

    public void setVideo(@NonNull Uri data) {
        mVideoUri = data;
        getBitmap();
    }

    public final class MainThreadExecutor {

        private final Handler HANDLER = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                Runnable callback = msg.getCallback();
                if (callback != null) {
                    callback.run();
                    decrementToken((Token) msg.obj);
                } else {
                    super.handleMessage(msg);
                }
            }
        };

        private final Map<String, Token> TOKENS = new HashMap<>();

        private MainThreadExecutor() {
            // should not be instantiated
        }

        /**
         * Store a new task in the map for providing cancellation. This method is
         * used by AndroidAnnotations and not intended to be called by clients.
         *
         * @param id    the identifier of the task
         * @param task  the task itself
         * @param delay the delay or zero to run immediately
         */
        public void runTask(String id, Runnable task, long delay) {
            if ("".equals(id)) {
                HANDLER.postDelayed(task, delay);
                return;
            }
            long time = SystemClock.uptimeMillis() + delay;
            HANDLER.postAtTime(task, nextToken(id), time);
        }

        private Token nextToken(String id) {
            synchronized (TOKENS) {
                Token token = TOKENS.get(id);
                if (token == null) {
                    token = new MainThreadExecutor.Token(id);
                    TOKENS.put(id, token);
                }
                token.runnablesCount++;
                return token;
            }
        }

        private void decrementToken(Token token) {
            synchronized (TOKENS) {
                if (--token.runnablesCount == 0) {
                    String id = token.id;
                    Token old = TOKENS.remove(id);
                    if (old != token) {
                        // a runnable finished after cancelling, we just removed a
                        // wrong token, lets put it back
                        TOKENS.put(id, old);
                    }
                }
            }
        }

        /**
         * Cancel all tasks having the specified <code>id</code>.
         *
         * @param id the cancellation identifier
         */
        public void cancelAll(String id) {
            Token token;
            synchronized (TOKENS) {
                token = TOKENS.remove(id);
            }
            if (token == null) {
                // nothing to cancel
                return;
            }
            HANDLER.removeCallbacksAndMessages(token);
        }

        private final class Token {
            int runnablesCount = 0;
            final String id;

            private Token(String id) {
                this.id = id;
            }
        }

    }

}


package com.ahmedbadereldin.videotrimmer.customVideoViews;

import android.net.Uri;

public interface OnVideoTrimListener {

    void onTrimStarted();

    void getResult(final Uri uri);

    void cancelAction();

    void onError(final String message);
}


package com.ahmedbadereldin.videotrimmer.customVideoViews;
public interface OnRangeSeekBarChangeListener {
    void onCreate(CustomRangeSeekBar CustomRangeSeekBar, int index, float value);

    void onSeek(CustomRangeSeekBar CustomRangeSeekBar, int index, float value);

    void onSeekStart(CustomRangeSeekBar CustomRangeSeekBar, int index, float value);

    void onSeekStop(CustomRangeSeekBar CustomRangeSeekBar, int index, float value);
}


package com.ahmedbadereldin.videotrimmer.customVideoViews;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public final class BackgroundTask {

    private static final String TAG = "BackgroundTask";

    public static final Executor DEFAULT_EXECUTOR = Executors.newScheduledThreadPool(2 * Runtime.getRuntime().availableProcessors());
    private static Executor executor = DEFAULT_EXECUTOR;
    private static final List<Task> TASKS = new ArrayList<>();
    private static final ThreadLocal<String> CURRENT_SERIAL = new ThreadLocal<>();

    private BackgroundTask() {
    }

    /**
     * Execute a runnable after the given delay.
     *
     * @param runnable the task to execute
     * @param delay    the time from now to delay execution, in milliseconds
     *                 <p>
     *                 if <code>delay</code> is strictly positive and the current
     *                 executor does not support scheduling (if
     *                 Executor has been called with such an
     *                 executor)
     * @return Future associated to the running task
     * @throws IllegalArgumentException if the current executor set by Executor
     *                                  does not support scheduling
     */
    private static Future<?> directExecute(Runnable runnable, long delay) {
        Future<?> future = null;
        if (delay > 0) {
            /* no serial, but a delay: schedule the task */
            if (!(executor instanceof ScheduledExecutorService)) {
                throw new IllegalArgumentException("The executor set does not support scheduling");
            }
            ScheduledExecutorService scheduledExecutorService = (ScheduledExecutorService) executor;
            future = scheduledExecutorService.schedule(runnable, delay, TimeUnit.MILLISECONDS);
        } else {
            if (executor instanceof ExecutorService) {
                ExecutorService executorService = (ExecutorService) executor;
                future = executorService.submit(runnable);
            } else {
                /* non-cancellable task */
                executor.execute(runnable);
            }
        }
        return future;
    }

    /**
     * Execute a task after (at least) its delay <strong>and</strong> after all
     * tasks added with the same non-null <code>serial</code> (if any) have
     * completed execution.
     *
     * @param task the task to execute
     * @throws IllegalArgumentException if <code>task.delay</code> is strictly positive and the
     *                                  current executor does not support scheduling (if
     *                                  Executor has been called with such an
     *                                  executor)
     */
    public static synchronized void execute(Task task) {
        Future<?> future = null;
        if (task.serial == null || !hasRunning(task.serial)) {
            task.executionAsked = true;
            future = directExecute(task, task.remainingDelay);
        }
        if ((task.id != null || task.serial != null) && !task.managed.get()) {
            /* keep task */
            task.future = future;
            TASKS.add(task);
        }
    }

    /**
     * Indicates whether a task with the specified <code>serial</code> has been
     * submitted to the executor.
     *
     * @param serial the serial queue
     * @return <code>true</code> if such a task has been submitted,
     * <code>false</code> otherwise
     */
    private static boolean hasRunning(String serial) {
        for (Task task : TASKS) {
            if (task.executionAsked && serial.equals(task.serial)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Retrieve and remove the first task having the specified
     * <code>serial</code> (if any).
     *
     * @param serial the serial queue
     * @return task if found, <code>null</code> otherwise
     */
    private static Task take(String serial) {
        int len = TASKS.size();
        for (int i = 0; i < len; i++) {
            if (serial.equals(TASKS.get(i).serial)) {
                return TASKS.remove(i);
            }
        }
        return null;
    }

    /**
     * Cancel all tasks having the specified <code>id</code>.
     *
     * @param id                    the cancellation identifier
     * @param mayInterruptIfRunning <code>true</code> if the thread executing this task should be
     *                              interrupted; otherwise, in-progress tasks are allowed to
     *                              complete
     */
    public static synchronized void cancelAllTask(String id, boolean mayInterruptIfRunning) {
        for (int i = TASKS.size() - 1; i >= 0; i--) {
            Task task = TASKS.get(i);
            if (id.equals(task.id)) {
                if (task.future != null) {
                    task.future.cancel(mayInterruptIfRunning);
                    if (!task.managed.getAndSet(true)) {
                        /*
                         * the task has been submitted to the executor, but its
						 * execution has not started yet, so that its run()
						 * method will never call postExecute()
						 */
                        task.postExecute();
                    }
                } else if (task.executionAsked) {
                    Log.w(TAG, "A task with id " + task.id + " cannot be cancelled (the executor set does not support it)");
                } else {
                    /* this task has not been submitted to the executor */
                    TASKS.remove(i);
                }
            }
        }
    }

    public static abstract class Task implements Runnable {

        private String id;
        private long remainingDelay;
        private long targetTimeMillis; /* since epoch */
        private String serial;
        private boolean executionAsked;
        private Future<?> future;

        /*
         * A task can be cancelled after it has been submitted to the executor
         * but before its run() method is called. In that case, run() will never
         * be called, hence neither will postExecute(): the tasks with the same
         * serial identifier (if any) will never be submitted.
         *
         * Therefore, cancelAllTask() *must* call postExecute() if run() is not
         * started.
         *
         * This flag guarantees that either cancelAllTask() or run() manages this
         * task post execution, but not both.
         */
        private AtomicBoolean managed = new AtomicBoolean();

        protected Task(String id, long delay, String serial) {
            if (!"".equals(id)) {
                this.id = id;
            }
            if (delay > 0) {
                remainingDelay = delay;
                targetTimeMillis = System.currentTimeMillis() + delay;
            }
            if (!"".equals(serial)) {
                this.serial = serial;
            }
        }

        @Override
        public void run() {
            if (managed.getAndSet(true)) {
                /* cancelled and postExecute() already called */
                return;
            }

            try {
                CURRENT_SERIAL.set(serial);
                execute();
            } finally {
                /* handle next tasks */
                postExecute();
            }
        }

        public abstract void execute();

        private void postExecute() {
            if (id == null && serial == null) {
				/* nothing to do */
                return;
            }
            CURRENT_SERIAL.set(null);
            synchronized (BackgroundTask.class) {
				/* execution complete */
                TASKS.remove(this);

                if (serial != null) {
                    Task next = take(serial);
                    if (next != null) {
                        if (next.remainingDelay != 0) {
							/* the delay may not have elapsed yet */
                            next.remainingDelay = Math.max(0L, targetTimeMillis - System.currentTimeMillis());
                        }
						/* a task having the same serial was queued, execute it */
                        BackgroundTask.execute(next);
                    }
                }
            }
        }
    }


}



package com.ahmedbadereldin.videotrimmerapplication;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.kcode.permissionslib.main.OnRequestPermissionsCallBack;
import com.kcode.permissionslib.main.PermissionCompat;
import java.io.File;
import java.util.Objects;
import iam.thevoid.mediapicker.rxmediapicker.Purpose;
import iam.thevoid.mediapicker.rxmediapicker.RxMediaPicker;


public class NewPostActivity extends AppCompatActivity {

    private ImageView videoBtn;
    private FrameLayout postImgLY;
    private ImageView postImg;
    private ImageView cancelImgBtn;
    private Uri uriPostImg;
    private String pathPostImg;
    private static final int REQUEST_TAKE_GALLERY_VIDEO = 100;
    private static final int VIDEO_TRIM = 101;
    private String TAG = NewPostActivity.class.getSimpleName();

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_new_post);
        initViews();
        setSharedIntentData(getIntent());
        initClicks();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setSharedIntentData(intent);
    }

    private void initViews() {
        videoBtn = findViewById(R.id.videoBtn);
        postImgLY = findViewById(R.id.postImgLY);
        postImg = findViewById(R.id.postImg);
        cancelImgBtn = findViewById(R.id.cancelImgBtn);
    }

    private void setSharedIntentData(Intent sharedIntentData) {

        String receivedAction = sharedIntentData.getAction();
        String receivedType = sharedIntentData.getType();

        if (receivedAction != null && receivedAction.equals(Intent.ACTION_SEND)) {
            assert receivedType != null;
            if (receivedType.startsWith("image/")) {
                //handle sent image
                uriPostImg = sharedIntentData.getParcelableExtra(Intent.EXTRA_STREAM);
                if (uriPostImg != null) {
                    hideSoftKeyboard(this);

                    pathPostImg = null;
                    Glide.with(this)
                            .load(uriPostImg)
                            .into(postImg);
                    postImgLY.setVisibility(View.VISIBLE);

                }
            } else if (receivedType.startsWith("video/")) {
                //handle sent video
                uriPostImg = sharedIntentData.getParcelableExtra(Intent.EXTRA_STREAM);
                if (uriPostImg != null) {
                    //set the video
                    //RESAMPLE YOUR IMAGE DATA BEFORE DISPLAYING
                    Glide.with(this)
                            .load(uriPostImg)
                            .into(postImg);
                    postImgLY.setVisibility(View.VISIBLE);
                }
            }
        }

    }

    private void initClicks() {
        cancelImgBtn.setOnClickListener(view -> {
            uriPostImg = null;
            pathPostImg = null;
            postImgLY.setVisibility(View.GONE);
        });

        videoBtn.setOnClickListener(view -> {
            try {
                PermissionCompat.Builder builder = new PermissionCompat.Builder(this);
                builder.addPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE});
                builder.addPermissionRationale(getString(R.string.should_allow_permission));
                builder.addRequestPermissionsCallBack(new OnRequestPermissionsCallBack() {
                    @Override
                    public void onGrant() {
                        RxMediaPicker.builder(NewPostActivity.this)
                                .pick(Purpose.Pick.VIDEO)
                                .take(Purpose.Take.VIDEO)
                                .build()
                                .subscribe(uri -> loadImage(uri));
                    }

                    @Override
                    public void onDenied(String permission) {
                        Toast.makeText(NewPostActivity.this, getString(R.string.some_permission_denied), Toast.LENGTH_SHORT).show();
                    }
                });
                builder.build().request();

            } catch (Exception e) {
                e.printStackTrace();
            }


        });
    }

    private void loadImage(Uri filepath) {
        // MEDIA GALLERY
        String path = getPath(filepath);
        Uri uriFile = Uri.fromFile(new File(path));
        String fileExt = MimeTypeMap.getFileExtensionFromUrl(uriFile.toString());

        if (fileExt.equalsIgnoreCase("MP4")) {
            File file = new File(path);
            Log.d(TAG, "loadImageloadImageloadImageloadImage: " + path);
            Log.d(TAG, "loadImageloadImageloadImageloadImage: " + file.length());
            if (file.exists()) {
                startActivityForResult(new Intent(NewPostActivity.this, VideoTrimmerActivity.class).putExtra("EXTRA_PATH", path), VIDEO_TRIM);
                overridePendingTransition(0, 0);
            } else {
                Toast.makeText(NewPostActivity.this, "Please select proper video", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(this, getString(R.string.file_format) + " ," + fileExt, Toast.LENGTH_SHORT).show();
        }


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_TAKE_GALLERY_VIDEO) {
            if (resultCode == RESULT_OK) {
                Uri selectedImageUri = data.getData();
                // MEDIA GALLERY
                String path = getPath(selectedImageUri);
                Uri uriFile = Uri.fromFile(new File(path));
                String fileExtension = MimeTypeMap.getFileExtensionFromUrl(uriFile.toString());

                if (fileExtension.equalsIgnoreCase("MP4")) {
                    File file = new File(path);
                    if (file.exists()) {
                        startActivityForResult(new Intent(NewPostActivity.this, VideoTrimmerActivity.class).putExtra("EXTRA_PATH", path), VIDEO_TRIM);
                        overridePendingTransition(0, 0);
                    } else {
                        Toast.makeText(NewPostActivity.this, "Please select proper video", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(this, getString(R.string.file_format) + " ," + fileExtension, Toast.LENGTH_SHORT).show();
                }
            }
        } else if (requestCode == VIDEO_TRIM) {
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    String videoPath = data.getExtras().getString("INTENT_VIDEO_FILE");
                    File file = new File(videoPath);
                    Log.d(TAG, "onActivityResult: " + file.length());

                    pathPostImg = videoPath;

                    Glide.with(this)
                            .load(pathPostImg)
                            .into(postImg);
                    postImgLY.setVisibility(View.VISIBLE);

                }
            }
        }

    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private String getPath(Uri uri) {
        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(this, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(this, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{
                        split[1]
                };

                return getDataColumn(this, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(this, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return "";
    }

    private String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};
        try {
            int currentApiVersion = Build.VERSION.SDK_INT;
            //TODO changes to solve gallery video issue
            if (currentApiVersion > Build.VERSION_CODES.M && uri.toString().contains(getString(R.string.app_provider))) {
                cursor = context.getContentResolver().query(uri, null, null, null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    final int column_index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (cursor.getString(column_index) != null) {
                        String state = Environment.getExternalStorageState();
                        File file;
                        if (Environment.MEDIA_MOUNTED.equals(state)) {
                            file = new File(Environment.getExternalStorageDirectory() + "/DCIM/", cursor.getString(column_index));
                        } else {
                            file = new File(context.getFilesDir(), cursor.getString(column_index));
                        }
                        return file.getAbsolutePath();
                    }
                    return "";
                }
            } else {
                cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                        null);
                if (cursor != null && cursor.moveToFirst()) {
                    final int column_index = cursor.getColumnIndexOrThrow(column);
                    if (cursor.getString(column_index) != null) {
                        return cursor.getString(column_index);
                    }
                    return "";
                }
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return "";
    }

    private boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    private boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    private boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager =
                (InputMethodManager) activity.getSystemService(
                        Activity.INPUT_METHOD_SERVICE);
        assert inputMethodManager != null;
        inputMethodManager.hideSoftInputFromWindow(Objects.requireNonNull(activity.getCurrentFocus()).getWindowToken(), 0);
    }

}


package com.ahmedbadereldin.videotrimmerapplication;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.ahmedbadereldin.videotrimmer.Utility;
import com.ahmedbadereldin.videotrimmer.customVideoViews.BackgroundTask;
import com.ahmedbadereldin.videotrimmer.customVideoViews.BarThumb;
import com.ahmedbadereldin.videotrimmer.customVideoViews.CustomRangeSeekBar;
import com.ahmedbadereldin.videotrimmer.customVideoViews.OnRangeSeekBarChangeListener;
import com.ahmedbadereldin.videotrimmer.customVideoViews.OnVideoTrimListener;
import com.ahmedbadereldin.videotrimmer.customVideoViews.TileView;
import com.sangcomz.fishbun.FishBun;
import com.sangcomz.fishbun.adapter.image.impl.GlideAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;


public class VideoTrimmerActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView txtVideoCancel;
    private TextView txtVideoUpload;
    private TextView txtVideoEditTitle;
    private TextView txtVideoTrimSeconds;
    private RelativeLayout rlVideoView;
    private TileView tileView;
    private CustomRangeSeekBar mCustomRangeSeekBarNew;
    private VideoView mVideoView;
    private ImageView imgPlay;
    private SeekBar seekBarVideo;
    private TextView txtVideoLength;

    private int mDuration = 0;
    private int mDurationWithoutEdit = 0;
    private int mTimeVideo = 0;
    private int mStartPosition = 0;
    private int mEndPosition = 0;
    // set your max video trim seconds
    private int mMaxDuration = 120;
    private Handler mHandler = new Handler();

    private ProgressDialog mProgressDialog;
    String srcFile;
    String dstFile;

    OnVideoTrimListener mOnVideoTrimListener = new OnVideoTrimListener() {
        @Override
        public void onTrimStarted() {
            // Create an indeterminate progress dialog

            mProgressDialog = new ProgressDialog(VideoTrimmerActivity.this);
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mProgressDialog.setTitle(getString(R.string.save));
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.setCancelable(false);
            mProgressDialog.show();

        }

        @Override
        public void getResult(Uri uri) {
            Log.d("getResult", "getResult: " + uri);
            mProgressDialog.dismiss();
            Bundle conData = new Bundle();
            conData.putString("INTENT_VIDEO_FILE", uri.getPath());
            Intent intent = new Intent();
            intent.putExtras(conData);
            setResult(RESULT_OK, intent);
            finish();
        }

        @Override
        public void cancelAction() {
            mProgressDialog.dismiss();
        }

        @Override
        public void onError(String message) {
            mProgressDialog.dismiss();
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_trim);

        txtVideoCancel = findViewById(R.id.txtVideoCancel);
        txtVideoUpload = findViewById(R.id.txtVideoUpload);
//        txtVideoEditTitle = (TextView) findViewById(R.id.txtVideoEditTitle);
        txtVideoTrimSeconds = findViewById(R.id.txtVideoTrimSeconds);
        rlVideoView = findViewById(R.id.llVideoView);
        tileView = findViewById(R.id.timeLineView);
        mCustomRangeSeekBarNew = findViewById(R.id.timeLineBar);
        mVideoView = findViewById(R.id.videoView);
        imgPlay = findViewById(R.id.imgPlay);
        seekBarVideo = findViewById(R.id.seekBarVideo);
        txtVideoLength = findViewById(R.id.txtVideoLength);

        if (getIntent().getExtras() != null) {
            srcFile = getIntent().getExtras().getString("EXTRA_PATH");

        }
        dstFile = Environment.getExternalStorageDirectory() + "/" + getString(R.string.app_name) + new Date().getTime()
                + Utility.VIDEO_FORMAT;
//        dstFile = Environment.getExternalStorageDirectory() + "/" + getString(R.string.app_name) + new Date().getTime()
//                + Utility.VIDEO_FORMAT;

        Log.d("srcFile", "loadImageloadImageloadImageloadImage: " + dstFile);

        tileView.post(() -> {
            setBitmap(Uri.parse(srcFile));
            mVideoView.setVideoURI(Uri.parse(srcFile));
        });

        txtVideoCancel.setOnClickListener(this);
        txtVideoUpload.setOnClickListener(this);

        mVideoView.setOnPreparedListener(this::onVideoPrepared);

        mVideoView.setOnCompletionListener(mp -> onVideoCompleted());

        // handle your range seekbar changes
        mCustomRangeSeekBarNew.addOnRangeSeekBarListener(new OnRangeSeekBarChangeListener() {
            @Override
            public void onCreate(CustomRangeSeekBar customRangeSeekBarNew, int index, float value) {
                // Do nothing
            }

            @Override
            public void onSeek(CustomRangeSeekBar customRangeSeekBarNew, int index, float value) {
                onSeekThumbs(index, value);
            }

            @Override
            public void onSeekStart(CustomRangeSeekBar customRangeSeekBarNew, int index, float value) {
                if (mVideoView != null) {
                    mHandler.removeCallbacks(mUpdateTimeTask);
                    seekBarVideo.setProgress(0);
                    mVideoView.seekTo(mStartPosition * 1000);
                    mVideoView.pause();
                    imgPlay.setBackgroundResource(R.drawable.ic_white_play);
                }
            }

            @Override
            public void onSeekStop(CustomRangeSeekBar customRangeSeekBarNew, int index, float value) {
                onStopSeekThumbs();
            }
        });

        imgPlay.setOnClickListener(this);

        // handle changes on seekbar for video play
        seekBarVideo.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if (mVideoView != null) {
                    mHandler.removeCallbacks(mUpdateTimeTask);
                    seekBarVideo.setMax(mTimeVideo * 1000);
                    seekBarVideo.setProgress(0);
                    mVideoView.seekTo(mStartPosition * 1000);
                    mVideoView.pause();
                    imgPlay.setBackgroundResource(R.drawable.ic_white_play);
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Log.d("onStopTrackingTouch", "onStopTrackingTouch: 123123123   --->  " + (mStartPosition * 1000) + " <-----> " + seekBar.getProgress() + " <-----> " + seekBarVideo.getProgress());
                Log.d("onStopTrackingTouch", "onStopTrackingTouch: 123123123   --->  " + (mDuration * 1000) + " <-----> " + seekBarVideo.getProgress());
                Log.d("onStopTrackingTouch", "onStopTrackingTouch: 123123123   --->  " + ((mStartPosition * 1000) - seekBarVideo.getProgress()));
                Log.d("onStopTrackingTouch", "onStopTrackingTouch: 123123123   --->  " + ((mDuration * 1000) - seekBarVideo.getProgress()));
                Log.d("onStopTrackingTouch", "onStopTrackingTouch: 123123123   mVideoView--->  " + mVideoView.getDuration());
                Log.d("onStopTrackingTouch", "onStopTrackingTouch: 123123123   seekBar--->  " + seekBar.getProgress());
                Log.d("onStopTrackingTouch", "onStopTrackingTouch: 123123123   mStartPosition--->  " + (mStartPosition*1000));
                Log.d("onStopTrackingTouch", "onStopTrackingTouch: 123123123   mEndPosition--->  " + (mEndPosition*1000));

                // seek bar - 120 sec
                // start = 130
                // end = 255

//                if(mDuration)
                mHandler.removeCallbacks(mUpdateTimeTask);
//                mVideoView.seekTo((mDuration * 1000) - seekBarVideo.getProgress());
                mVideoView.seekTo((mStartPosition*1000) + seekBar.getProgress());
//                mVideoView.start();
            }
        });
    }

    @Override
    public void onClick(View view) {
        if (view == txtVideoCancel) {
            finish();
        } else if (view == txtVideoUpload) {
            int diff = mEndPosition - mStartPosition;
            if (diff < 3) {
                Toast.makeText(VideoTrimmerActivity.this, getString(R.string.video_length_validation),
                        Toast.LENGTH_LONG).show();
            } else {
                MediaMetadataRetriever
                        mediaMetadataRetriever = new MediaMetadataRetriever();
                mediaMetadataRetriever.setDataSource(VideoTrimmerActivity.this, Uri.parse(srcFile));
                final File file = new File(srcFile);
                Log.d(
                        "executeAAAA",
                        "execute: " + "Aaaa" + file.length() + " " + dstFile + " " + mStartPosition + " " + mEndPosition + " " + mOnVideoTrimListener
                );
                //notify that video trimming started
                if (mOnVideoTrimListener != null)
                    mOnVideoTrimListener.onTrimStarted();

                BackgroundTask.execute(new BackgroundTask.Task("", 0L, "") {
                                           @Override
                                           public void execute() {
                                               try {
                                                   Log.d(
                                                           "executeAAAA",
                                                           "execute: " + "Aaaa" + file.length() + " " + dstFile + " " + mStartPosition + " " + mEndPosition + " " + mOnVideoTrimListener
                                                   );
                                                   Utility.startTrim(VideoTrimmerActivity.this, file, dstFile, mStartPosition * 1000, mEndPosition * 1000, mOnVideoTrimListener);
                                               } catch (final Throwable e) {
                                                   Objects.requireNonNull(Thread.getDefaultUncaughtExceptionHandler()).uncaughtException(Thread.currentThread(), e);
                                               }
                                           }
                                       }
                );


            }

        } else if (view == imgPlay) {
            if (mVideoView.isPlaying()) {
                if (mVideoView != null) {
                    mVideoView.pause();
                    imgPlay.setBackgroundResource(R.drawable.ic_white_play);
                }
            } else {
                if (mVideoView != null) {
                    mVideoView.start();
                    imgPlay.setBackgroundResource(R.drawable.ic_white_pause);
                    if (seekBarVideo.getProgress() == 0) {
                        txtVideoLength.setText("00:00");
                        updateProgressBar();
                    }else{
                        txtVideoLength.setText(milliSecondsToTimer(seekBarVideo.getProgress()) + "");
                        updateProgressBar();
                    }
                }
            }
        }
    }

    private void setBitmap(Uri mVideoUri) {
        tileView.setVideo(mVideoUri);
    }

    //region todo onVideoPrepared
    private void onVideoPrepared(@NonNull MediaPlayer mp) {
        // Adjust the size of the video
        // so it fits on the screen
        //TODO manage proportion for video
        /*int videoWidth = mp.getVideoWidth();
        int videoHeight = mp.getVideoHeight();
        float videoProportion = (float) videoWidth / (float) videoHeight;
        int screenWidth = rlVideoView.getWidth();
        int screenHeight = rlVideoView.getHeight();
        float screenProportion = (float) screenWidth / (float) screenHeight;
        ViewGroup.LayoutParams lp = mVideoView.getLayoutParams();

        if (videoProportion > screenProportion) {
            lp.width = screenWidth;
            lp.height = (int) ((float) screenWidth / videoProportion);
        } else {
            lp.width = (int) (videoProportion * (float) screenHeight);
            lp.height = screenHeight;
        }
        mVideoView.setLayoutParams(lp);*/

        //mVideoView.getDuration() => get in msec we need it in sec
        mDuration = mVideoView.getDuration() / 1000;
        setSeekBarPosition();
    }
    //endregion

    public void updateProgressBar() {
        mHandler.postDelayed(mUpdateTimeTask, 100);
    }

    private Runnable mUpdateTimeTask = new Runnable() {
        public void run() {
            if (seekBarVideo.getProgress() >= seekBarVideo.getMax()) {
                seekBarVideo.setProgress((mVideoView.getCurrentPosition() - mStartPosition * 1000));
                txtVideoLength.setText(milliSecondsToTimer(seekBarVideo.getProgress()) + "");
                mVideoView.seekTo(mStartPosition * 1000);
                mVideoView.pause();
                seekBarVideo.setProgress(0);
                txtVideoLength.setText("00:00");
                imgPlay.setBackgroundResource(R.drawable.ic_white_play);
            } else {
                seekBarVideo.setProgress((mVideoView.getCurrentPosition() - mStartPosition * 1000));
                txtVideoLength.setText(milliSecondsToTimer(seekBarVideo.getProgress()) + "");
                mHandler.postDelayed(this, 100);
            }
        }
    };

    //region todo setSeekBarPosition
    private void setSeekBarPosition() {

        if (mDuration >= mMaxDuration) {
            mStartPosition = 0;
            mEndPosition = mMaxDuration;

            mCustomRangeSeekBarNew.setThumbValue(0, (mStartPosition * 100) / mDuration);
            mCustomRangeSeekBarNew.setThumbValue(1, (mEndPosition * 100) / mDuration);
            //////
            mDurationWithoutEdit = mDuration;
//            mDuration = mMaxDuration;
        } else {
            mStartPosition = 0;
            mEndPosition = mDuration;
            mDurationWithoutEdit = mDuration;
        }


        mTimeVideo = mDuration;
        mCustomRangeSeekBarNew.initMaxWidth();
//        seekBarVideo.setMax(mMaxDuration * 1000);
        seekBarVideo.setMax(mDurationWithoutEdit * 1000);
        mVideoView.seekTo(mStartPosition * 1000);

        String mStart = mStartPosition + "";
        if (mStartPosition < 10)
            mStart = "0" + mStartPosition;

        int startMin = Integer.parseInt(mStart) / 60;
        int startSec = Integer.parseInt(mStart) % 60;

        String mEnd = mEndPosition + "";
        if (mEndPosition < 10)
            mEnd = "0" + mEndPosition;

        int endMin = Integer.parseInt(mEnd) / 60;
        int endSec = Integer.parseInt(mEnd) % 60;

        txtVideoTrimSeconds.setText(String.format(Locale.US, "%02d:%02d - %02d:%02d", startMin, startSec, endMin, endSec));
    }
    //endregion

    /**
     * called when playing video completes
     */
    private void onVideoCompleted() {
        mHandler.removeCallbacks(mUpdateTimeTask);
        seekBarVideo.setProgress(0);
        mVideoView.seekTo(mStartPosition * 1000);
        mVideoView.pause();
        imgPlay.setBackgroundResource(R.drawable.ic_white_play);
    }

    /**
     * Handle changes of left and right thumb movements
     *
     * @param index index of thumb
     * @param value value
     */
    private void onSeekThumbs(int index, float value) {
        switch (index) {
            case BarThumb.LEFT: {
                mStartPosition = (int) ((mDuration * value) / 100L);
                mVideoView.seekTo(mStartPosition * 1000);
                break;
            }
            case BarThumb.RIGHT: {
                mEndPosition = (int) ((mDuration * value) / 100L);
                break;
            }
        }
        mTimeVideo = (mEndPosition - mStartPosition);
        seekBarVideo.setMax(mTimeVideo * 1000);
        seekBarVideo.setProgress(0);
        mVideoView.seekTo(mStartPosition * 1000);

        String mStart = mStartPosition + "";
        if (mStartPosition < 10)
            mStart = "0" + mStartPosition;

        int startMin = Integer.parseInt(mStart) / 60;
        int startSec = Integer.parseInt(mStart) % 60;

        String mEnd = mEndPosition + "";
        if (mEndPosition < 10)
            mEnd = "0" + mEndPosition;
        int endMin = Integer.parseInt(mEnd) / 60;
        int endSec = Integer.parseInt(mEnd) % 60;

        txtVideoTrimSeconds.setText(String.format(Locale.US, "%02d:%02d - %02d:%02d", startMin, startSec, endMin, endSec));

    }

    private void onStopSeekThumbs() {
//        mMessageHandler.removeMessages(SHOW_PROGRESS);
//        mVideoView.pause();
//        mPlayView.setVisibility(View.VISIBLE);
    }

    public String milliSecondsToTimer(long milliseconds) {
        String finalTimerString = "";
        String secondsString;
        String minutesString;


        int hours = (int) (milliseconds / (1000 * 60 * 60));
        int minutes = (int) (milliseconds % (1000 * 60 * 60)) / (1000 * 60);
        int seconds = (int) ((milliseconds % (1000 * 60 * 60)) % (1000 * 60) / 1000);
        // Add hours if there
        if (hours > 0) {
            finalTimerString = hours + ":";
        }

        // Prepending 0 to seconds if it is one digit
        if (seconds < 10) {
            secondsString = "0" + seconds;
        } else {
            secondsString = "" + seconds;
        }

        if (minutes < 10) {
            minutesString = "0" + minutes;
        } else {
            minutesString = "" + minutes;
        }

        finalTimerString = finalTimerString + minutesString + ":" + secondsString;

        // return timer string
        return finalTimerString;
    }


}


