package com.example.a360crowdsource;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
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

import android.os.Environment;
import android.os.Handler;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import android.media.MediaRecorder;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity {
    static String TAG = "debug";
    long mStartRX = 0;
    long mStartTX = 0;
    Handler mHandler = new Handler();
    boolean stopRercordingFlag = false;
    boolean thinkAloud = false;
    int READ_STORAGE_PERMISSION_REQUEST_CODE=0x3;
    int WRITE_STORAGE_PERMISSION_REQUEST_CODE=0x3;

    private static final String LOG_TAG = "AudioRecordTest";
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private static String fileNameFb = null;
    private static String fileNameYt = null;


    //private RecordButton recordButton = null;
    private MediaRecorder recorder = null;


    private final Runnable mRunnable = new Runnable() {
        public void run() {
            if (!stopRercordingFlag) {
                long rxBytes = TrafficStats.getTotalRxBytes() - mStartRX;
                Log.d(TAG, "RX " + Long.toString(rxBytes));
                long txBytes = TrafficStats.getTotalTxBytes() - mStartTX;
                Log.d(TAG, "TX " + Long.toString(txBytes));
                mHandler.postDelayed(mRunnable, 1000);
            }
        }
    };

    //permission check for storage
    public void checkPermission(Activity activity, String permission, int permissionInt) {
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(activity,
                permission)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted

            // No explanation needed; request the permission
            ActivityCompat.requestPermissions(activity,
                    new String[]{permission},
                    permissionInt);

            // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
            // app-defined int constant. The callback method gets the
            // result of the request.

        }
    }

    //get audio permission
    boolean permissionToRecordAccepted = false;
    String [] permissions = {Manifest.permission.RECORD_AUDIO};

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //get storage permissions
        checkPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE, READ_STORAGE_PERMISSION_REQUEST_CODE);
        checkPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE, WRITE_STORAGE_PERMISSION_REQUEST_CODE);

        //setup audio recording
        fileNameFb = Environment.getExternalStorageDirectory().getAbsolutePath();
        fileNameFb += "/audiofb.3gp";
        fileNameYt = Environment.getExternalStorageDirectory().getAbsolutePath();
        fileNameYt += "/audioyt.3gp";

        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);

        //submit data
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //stopRercordingFlag = true;
                if (stopRercordingFlag) {
                    stopRercordingFlag = false;
                    onRecord(false, fileNameYt);
                    //TODO - finalize recording data
                    //upload data
                    new UploadFileAsync().execute("");
                    Snackbar.make(view, "Your contribution has been submitted. Thank you!", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
                else{
                    Toast.makeText(view.getContext(), "Please follow the steps in the correct order", Toast.LENGTH_SHORT).show();
                }

            }
        });

        /*
        TextView link = (TextView) findViewById(R.id.text1);
        String linkText = "<a href='https://www.facebook.com/AMD/videos/10154844546721473/'>View facebook 360 video</a>";
        link.setText(Html.fromHtml(linkText));
        link.setMovementMethod(LinkMovementMethod.getInstance());
        */

        //fb button
        final Button fbButton = findViewById(R.id.fb_button);
        fbButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                //log rx and tx data
                mStartRX = TrafficStats.getTotalRxBytes();
                mStartTX = TrafficStats.getTotalTxBytes();

                if (mStartRX == TrafficStats.UNSUPPORTED || mStartTX == TrafficStats.UNSUPPORTED) {
                    Log.d(TAG, "logging unsupported");
                } else {
                    mHandler.postDelayed(mRunnable, 1000);
                }

               //start recording
                stopRercordingFlag = true;
                onRecord(true, fileNameFb);
                Log.d(TAG, "started recording");
                //open fb 360
                String url = "https://www.facebook.com/AMD/videos/10154844546721473/";
                //String url = "https://www.pscp.tv/w/1dRJZXXgXEwKB";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            }
        });

        //youtube button
        //fb button
        final Button youtubeButton = findViewById(R.id.youtube_button);
        youtubeButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //log rx and tx data

                //TODO - finalize recording data
                mStartRX = TrafficStats.getTotalRxBytes();
                mStartTX = TrafficStats.getTotalTxBytes();

                if (mStartRX == TrafficStats.UNSUPPORTED || mStartTX == TrafficStats.UNSUPPORTED) {
                    Log.d(TAG, "logging unsupported");
                } else {
                    mHandler.postDelayed(mRunnable, 1000);
                }

                //stop previous recording instance
                if (stopRercordingFlag) {
                    onRecord(false, fileNameFb);
                    Log.d(TAG, "started recording");
                    //restart recording
                    onRecord(true, fileNameYt);

                    //open fb 360
                    String url = "https://www.facebook.com/Breitling/videos/510602576175759/UzpfSTE2Nzk5OTE4Mzg4ODU5NTE6MjU1OTk3NTI3MDg4NzU5OQ/";
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
    }

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

    private class UploadFileAsync extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {

            try {
                String sourceFileUri = "/mnt/sdcard/abc.jpg";
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
                        String upLoadServerUri = "http://192.168.43.4:80/crowdsource/index.php?";

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
                        conn.setRequestProperty("bill", sourceFileUri);

                        dos = new DataOutputStream(conn.getOutputStream());

                        dos.writeBytes(twoHyphens + boundary + lineEnd);
                        dos.writeBytes("Content-Disposition: form-data; name=\"bill\";filename=\""
                                + sourceFileUri + "\"" + lineEnd);

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

                            // messageText.setText(msg);
                            //Toast.makeText(ctx, "File Upload Complete.",
                            //      Toast.LENGTH_SHORT).show();

                            // recursiveDelete(mDirectory1);

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

    //audio recording methods
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
            Log.e(LOG_TAG, "prepare() failed");
        }

        recorder.start();
    }

    private void stopRecording() {
        recorder.stop();
        recorder.release();
        recorder = null;
    }
}


