package com.example.a360crowdsource;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.TrafficStats;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;


import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;

import android.media.MediaRecorder;
import android.widget.Toast;


public class MainActivity extends FragmentActivity {
    static final String serverURL = "http://10.17.76.28:80/crowdsource/index.php?";
    static final String YT_URL = "https://www.facebook.com/Breitling/videos/510602576175759/UzpfSTE2Nzk5OTE4Mzg4ODU5NTE6MjU1OTk3NTI3MDg4NzU5OQ/";
    static final String FB_URL = "https://www.facebook.com/AMD/videos/10154844546721473/";
    static final int GYRO_SAMPLING_PERIOD_US = 100000;
    static final int NETWORK_DATA_SAMPLING_PERIOD_MS = 1000;
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;

    //UI fragment setup
    /**
     * The number of pages (wizard steps) to show in this demo.
     */
    private static final int NUM_PAGES = 5;

    /**
     * The pager widget, which handles animation and allows swiping horizontally to access previous
     * and next wizard steps.
     */
    private ViewPager mPager;

    /**
     * The pager adapter, which provides the pages to the view pager widget.
     */


    
    static String TAG = "debug";
    long mStartRX = 0;
    long mStartTX = 0;
    Handler mHandler = new Handler();
    boolean stopRercordingFlag = false;
    //boolean thinkAloud = false;
    int READ_STORAGE_PERMISSION_REQUEST_CODE=0x3;
    int WRITE_STORAGE_PERMISSION_REQUEST_CODE=0x3;

    private static String fileNameFb = null;
    private static String fileNameYt = null;
    private EditText userID;
    private String userIDText;

    //private RecordButton recordButton = null;
    private MediaRecorder recorder = null;

    private SensorManager sensorManager;
    private Sensor sensor;
    GyroReader gyroReader;

    //setup csv files for logging data
    FileWriter trafficWriter;
    FileWriter gyroWriter;
    File root = Environment.getExternalStorageDirectory();
    File trafficDatafile = new File(root, "trafficData.csv");
    File gyroDatafile = new File(root, "gyroData.csv");

    //audio permissions
    boolean permissionToRecordAccepted = false;
    String [] permissions = {Manifest.permission.RECORD_AUDIO};

    public MainActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);

        //get storage permissions
        checkPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE, READ_STORAGE_PERMISSION_REQUEST_CODE);
        checkPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE, WRITE_STORAGE_PERMISSION_REQUEST_CODE);

        //setup audio recording
        fileNameFb = Environment.getExternalStorageDirectory().getAbsolutePath();
        fileNameFb += "/audiofb.3gp";
        fileNameYt = Environment.getExternalStorageDirectory().getAbsolutePath();
        fileNameYt += "/audioyt.3gp";
        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);

        //setup gyro
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        gyroReader = new GyroReader();

        //initiate file writers
        try {
            gyroWriter = new FileWriter(gyroDatafile);
            trafficWriter = new FileWriter(trafficDatafile);
        }
        catch (IOException e){
            e.printStackTrace();
        }

        //fb button
        final Button fbButton = findViewById(R.id.fb_button);
        fbButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

               //start recording audio and gyro
                if (!stopRercordingFlag) {
                    stopRercordingFlag = true;

                    //log rx and tx data
                    mStartRX = TrafficStats.getTotalRxBytes();
                    mStartTX = TrafficStats.getTotalTxBytes();

                    if (mStartRX == TrafficStats.UNSUPPORTED || mStartTX == TrafficStats.UNSUPPORTED) {
                        Log.d(TAG, "logging unsupported");
                    } else {
                        Log.d(TAG, "begin recording");
                        try {
                            trafficWriter.write("facebook\n");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        mHandler.postDelayed(mRunnable, NETWORK_DATA_SAMPLING_PERIOD_MS);
                    }

                    onRecord(true, fileNameFb);
                    try {
                        gyroWriter.write("facebook\n");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    gyroReader.start();
                    Log.d(TAG, "started recording");

                    //open fb 360
                    String url = FB_URL;
                    //String url = "https://www.pscp.tv/w/1dRJZXXgXEwKB";
                    //setContentView(R.layout.content_main);
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(url));
                    startActivity(i);
                }
                else{
                    Toast.makeText(v.getContext(), "Please follow the steps in the correct order", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //youtube button
        final Button youtubeButton = findViewById(R.id.youtube_button);
        youtubeButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                //stop previous recording instance
                if (stopRercordingFlag) {
                    stopRercordingFlag = false;
                    gyroReader.stop();
                    onRecord(false, fileNameFb);
                    Log.d(TAG, "stopped recording");
                    SystemClock.sleep(500);
                    //restart recording
                    stopRercordingFlag = true;
                    try {
                        gyroWriter.write("youtube\n");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    gyroReader.start();
                    onRecord(true, fileNameYt);
                    //log rx and tx data

                    //TODO - finalize recording data
                    mStartRX = TrafficStats.getTotalRxBytes();
                    mStartTX = TrafficStats.getTotalTxBytes();

                    if (mStartRX == TrafficStats.UNSUPPORTED || mStartTX == TrafficStats.UNSUPPORTED) {
                        Log.d(TAG, "logging unsupported");
                    } else {
                        try {
                            trafficWriter.write("youtube\n");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        mHandler.postDelayed(mRunnable, 1000);
                    }
                    //open youtube
                    String url = YT_URL;
                    //String url = "https://www.pscp.tv/w/1dRJZXXgXEwKB";
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(url));
                    startActivity(i);
                }
                else{
                    Toast.makeText(v.getContext(), "Please follow the steps in the correct order", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //submit data
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //stopRercordingFlag = true;
                if (stopRercordingFlag) {
                    stopRercordingFlag = false;
                    onRecord(false, fileNameYt);
                    gyroReader.stop();

                    try {
                        trafficWriter.flush();
                        trafficWriter.close();
                        gyroWriter.flush();
                        gyroWriter.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    //get userID
                    userID = findViewById(R.id.userID);
                    userIDText = userID.getText().toString();

                    //upload data
                    new UploadFileAsync().execute(Environment.getExternalStorageDirectory().getPath()+"/audiofb.3gp", "_audiofb.3gp");
                    new UploadFileAsync().execute(Environment.getExternalStorageDirectory().getPath()+"/audioyt.3gp", "_audioyt.3gp");
                    new UploadFileAsync().execute(Environment.getExternalStorageDirectory().getPath()+"/trafficData.csv", "_trafficData.csv");
                    new UploadFileAsync().execute(Environment.getExternalStorageDirectory().getPath()+"/gyroData.csv", "_gyroData.csv");
                    Snackbar.make(view, "Your contribution has been submitted. Thank you!", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
                else{
                    Toast.makeText(view.getContext(), "Please follow the steps in the correct order", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    //UI fragments
    public class ScreenSlidePageFragment extends Fragment {

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            ViewGroup rootView = (ViewGroup) inflater.inflate(
                    R.layout.fragment_screen_slide_page, container, false);

            return rootView;
        }
    }

    //menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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

    //runnable for logging network data
    private final Runnable mRunnable = new Runnable() {
        public void run() {
            if (stopRercordingFlag) {
                Log.d(TAG, "running");
                long rxBytes = TrafficStats.getTotalRxBytes() - mStartRX;
                Log.d(TAG, "RX " + Long.toString(rxBytes));
                long txBytes = TrafficStats.getTotalTxBytes() - mStartTX;
                Log.d(TAG, "TX " + Long.toString(txBytes));
                try{
                    writeTrafficStatsToCsv(rxBytes, txBytes, trafficWriter);
                }
                catch (IOException e){
                    e.printStackTrace();
                }
                mHandler.postDelayed(mRunnable, 10000);
            }
        }
    };

    //permission check for storage
    public void checkPermission(Activity activity, String permission, int permissionInt) {
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(activity,
                permission)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(activity,
                    new String[]{permission},
                    permissionInt);
        }
    }

    //permission for audio
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case REQUEST_RECORD_AUDIO_PERMISSION:
                permissionToRecordAccepted  = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
        }
        if (!permissionToRecordAccepted ) finish();
    }

    //csv logging functions
    private void writeGyroToCsv(SensorEvent event, Writer writer) throws IOException {
        Long time = System.nanoTime();
        String line = String.format("%d,%f,%f,%f,%f\n",time, event.values[0], event.values[1], event.values[2], event.values[3]);
        writer.write(line);
    }

    private void writeTrafficStatsToCsv(Long rx, Long tx, Writer writer) throws IOException {
        Long time = System.nanoTime();
        String line = String.format("%d,%d,%d\n",time, rx, tx);
        writer.write(line);
    }

    //read gyroscope data
    class GyroReader implements SensorEventListener{
        private Sensor rotationSensor;

        public GyroReader(){
            rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        }

        public void start(){
            //request 10ms updates
            sensorManager.registerListener(this, rotationSensor, GYRO_SAMPLING_PERIOD_US);
        }

        public void stop(){
            sensorManager.unregisterListener(this);
        }

        public void onSensorChanged(SensorEvent event) {
            // we received a sensor event. it is a good practice to check
            // that we received the proper event
            if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
                // convert the rotation-vector to a 4x4 matrix. the matrix
                // is interpreted by Open GL as the inverse of the
                // rotation-vector, which is what we want.
                Log.d(TAG, "Gyro readings "+ event.values[0] + " " + event.values[1]);
                try {
                    writeGyroToCsv(event, gyroWriter);
                }
                catch (IOException e){
                    e.printStackTrace();
                }
            }
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy){
        }
    }

    //upload files to server
    private class UploadFileAsync extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {

            try {
                String sourceFileUri = params[0];
                int serverResponseCode = 0;
                HttpURLConnection conn = null;
                DataOutputStream dos = null;
                String lineEnd = "\r\n";
                String twoHyphens = "--";
                String boundary = "*****";
                int bytesRead, bytesAvailable, bufferSize;
                byte[] buffer;
                int maxBufferSize = 1 * 1024 * 1024;
                File sourceFile = new File(sourceFileUri);
                Log.d(TAG, "begin send");
                if (sourceFile.isFile()) {

                    try {
                        String upLoadServerUri = serverURL;

                        Log.d(TAG, "user id "+userIDText);
                        // open a URL connection to the Servlet
                        FileInputStream fileInputStream = new FileInputStream(
                                sourceFile);
                        URL url = new URL(upLoadServerUri);
                        Log.d(TAG, "opened file");

                        // Open a HTTP connection to the URL
                        conn = (HttpURLConnection) url.openConnection();
                        conn.setDoInput(true); // Allow Inputs
                        conn.setDoOutput(true); // Allow Outputs
                        conn.setUseCaches(false); // Don't use a Cached Copy
                        conn.setRequestMethod("POST");
                        conn.setRequestProperty("Connection", "Keep-Alive");
                        conn.setRequestProperty("ENCTYPE",
                                "multipart/form-data");
                        conn.setRequestProperty("Content-Type",
                                "multipart/form-data;boundary=" + boundary);

                        Log.d(TAG, "source file uri "+sourceFileUri);
                        dos = new DataOutputStream(conn.getOutputStream());
                        dos.writeBytes(twoHyphens + boundary + lineEnd);
                        dos.writeBytes("Content-Disposition: form-data; name=\"abd\";filename=\""
                                + userIDText+ params[1] + "\"" + lineEnd);

                        dos.writeBytes(lineEnd);

                        // create a buffer of maximum size
                        bytesAvailable = fileInputStream.available();

                        bufferSize = Math.min(bytesAvailable, maxBufferSize);
                        buffer = new byte[bufferSize];

                        // read file and write it into form...
                        bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                        while (bytesRead > 0) {

                            dos.write(buffer, 0, bufferSize);
                            bytesAvailable = fileInputStream.available();
                            bufferSize = Math
                                    .min(bytesAvailable, maxBufferSize);
                            bytesRead = fileInputStream.read(buffer, 0,
                                    bufferSize);

                        }

                        // send multipart form data necesssary after file
                        // data...
                        dos.writeBytes(lineEnd);
                        dos.writeBytes(twoHyphens + boundary + twoHyphens
                                + lineEnd);

                        // Responses from the server (code and message)
                        serverResponseCode = conn.getResponseCode();
                        String serverResponseMessage = conn
                                .getResponseMessage();
                        Log.d(TAG, "sent file");

                        if (serverResponseCode == 200) {
                            Log.d(TAG, "send successful");

                        }

                        // close the streams //
                        fileInputStream.close();
                        dos.flush();
                        dos.close();

                    } catch (Exception e) {

                        // dialog.dismiss();
                        e.printStackTrace();

                    }
                    // dialog.dismiss();

                } // End else block


            } catch (Exception ex) {
                // dialog.dismiss();

                ex.printStackTrace();
            }
            return "Executed";
        }

        @Override
        protected void onPostExecute(String result) {
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }
    }

    //audio recording
    private void onRecord(boolean start, String filename) {
        if (start) {
            startRecording(filename);
        } else {
            stopRecording();
        }
    }

    private void startRecording(String fileName) {
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setOutputFile(fileName);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            recorder.prepare();
        } catch (IOException e) {
            Log.e(TAG, "prepare() failed");
        }

        recorder.start();
    }

    private void stopRecording() {
        recorder.stop();
        recorder.release();
        recorder = null;
    }
}


