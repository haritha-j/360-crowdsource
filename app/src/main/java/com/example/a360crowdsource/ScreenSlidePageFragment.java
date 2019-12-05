package com.example.a360crowdsource;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.TrafficStats;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import static com.example.a360crowdsource.MainActivity.TAG;

public class ScreenSlidePageFragment extends Fragment {
    int position;
    MainActivity main;
    public EditText response1;
    Button mDevice;
    //TextView mItemSelected;
    String[] listDevices;
    boolean[] checkedDevices;
    ArrayList<Integer> mUserDevices = new ArrayList<>();
    Button mApplication;
    //TextView mItemSelected;
    String[] listApps;
    boolean[] checkedApps;
    ArrayList<Integer> mUserApps = new ArrayList<>();


    public ScreenSlidePageFragment(int position, MainActivity main){
        this.position = position;

        this.main = main;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final ViewGroup rootView;

        //intro screen
        if (position ==0){
            rootView = (ViewGroup) inflater.inflate(
                    R.layout.fragment_screen_slide_intro, container, false);
            main.userID = rootView.findViewById(R.id.userID);


            //select device - multiple choice
            mDevice = (Button) rootView.findViewById(R.id.device_button);
            //mItemSelected = (TextView) findViewById(R.id.tvItemSelected);

            listDevices = getResources().getStringArray(R.array.devicetypelist);
            checkedDevices = new boolean[listDevices.length];

            mDevice.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder mBuilder = new AlertDialog.Builder(getContext());
                    mBuilder.setTitle("select devices");
                    mBuilder.setMultiChoiceItems(listDevices, checkedDevices, new DialogInterface.OnMultiChoiceClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int position, boolean isChecked) {
//                        if (isChecked) {
//                            if (!mUserItems.contains(position)) {
//                                mUserItems.add(position);
//                            }
//                        } else if (mUserItems.contains(position)) {
//                            mUserItems.remove(position);
//                        }
                            if(isChecked){
                                mUserDevices.add(position);
                            }else{
                                mUserDevices.remove((Integer.valueOf(position)));
                            }
                        }
                    });

                    mBuilder.setCancelable(false);
                    mBuilder.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int which) {
                            String item = "";
                            for (int i = 0; i < mUserDevices.size(); i++) {
                                item = item + listDevices[mUserDevices.get(i)];
                                if (i != mUserDevices.size() - 1) {
                                    item = item + ", ";
                                }
                            }
                            //mItemSelected.setText(item);
                        }
                    });


                    mBuilder.setNeutralButton("Clear", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int which) {
                            for (int i = 0; i < checkedDevices.length; i++) {
                                checkedDevices[i] = false;
                                mUserDevices.clear();
                                //mItemSelected.setText("");
                            }
                        }
                    });

                    AlertDialog mDialog = mBuilder.create();
                    mDialog.show();
                }
            });


            ////select application - multiple choice
            mApplication = (Button) rootView.findViewById(R.id.application_button);
            //mItemSelected = (TextView) findViewById(R.id.tvItemSelected);

            listApps = getResources().getStringArray(R.array.applicationtypelist);
            checkedApps = new boolean[listApps.length];

            mApplication.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder mBuilder = new AlertDialog.Builder(getContext());
                    mBuilder.setTitle("select devices");
                    mBuilder.setMultiChoiceItems(listApps, checkedApps, new DialogInterface.OnMultiChoiceClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int position, boolean isChecked) {
//                        if (isChecked) {
//                            if (!mUserItems.contains(position)) {
//                                mUserItems.add(position);
//                            }
//                        } else if (mUserItems.contains(position)) {
//                            mUserItems.remove(position);
//                        }
                            if(isChecked){
                                mUserApps.add(position);
                            }else{
                                mUserApps.remove((Integer.valueOf(position)));
                            }
                        }
                    });

                    mBuilder.setCancelable(false);
                    mBuilder.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int which) {
                            String item = "";
                            for (int i = 0; i < mUserApps.size(); i++) {
                                item = item + listApps[mUserApps.get(i)];
                                if (i != mUserApps.size() - 1) {
                                    item = item + ", ";
                                }
                            }
                            //mItemSelected.setText(item);
                        }
                    });


                    mBuilder.setNeutralButton("Clear", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int which) {
                            for (int i = 0; i < checkedApps.length; i++) {
                                checkedApps[i] = false;
                                mUserApps.clear();
                                //mItemSelected.setText("");
                            }
                        }
                    });

                    AlertDialog mDialog = mBuilder.create();
                    mDialog.show();
                }
            });

        }
        //facebook screen
        else if (position == 1) {
            rootView = (ViewGroup) inflater.inflate(
                    R.layout.fragment_screen_slide_fb, container, false);

            //fb button
            final Button fbButton = rootView.findViewById(R.id.fb_button);
            fbButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {

                    //start recording audio and gyro
                    if (!main.stopRercordingFlag) {
                        main.stopRercordingFlag = true;

                        //log rx and tx data
                        main.mStartRX = TrafficStats.getTotalRxBytes();
                        main.mStartTX = TrafficStats.getTotalTxBytes();

                        if (main.mStartRX == TrafficStats.UNSUPPORTED || main.mStartTX == TrafficStats.UNSUPPORTED) {
                            Log.d(TAG, "logging unsupported");
                        } else {
                            Log.d(TAG, "begin recording");
                            try {
                                main.trafficWriter.write("facebook\n");
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            main.mHandler.postDelayed(main.mRunnable, main.NETWORK_DATA_SAMPLING_PERIOD_MS);
                        }

                        main.onRecord(true, main.fileNameFb);
                        try {
                            main.gyroWriter.write("facebook\n");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        main.gyroReader.start();
                        Log.d(TAG, "started recording");

                        //open fb 360
                        String url = main.FB_URL;
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

        }

        //youtube screen
        else if (position ==2){
            rootView = (ViewGroup) inflater.inflate(
                    R.layout.fragment_screen_slide_yt, container, false);

            //youtube button
            final Button youtubeButton = rootView.findViewById(R.id.youtube_button);
            youtubeButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {

                    //stop previous recording instance
                    if (main.stopRercordingFlag) {
                        main.stopRercordingFlag = false;
                        main.gyroReader.stop();
                        main.onRecord(false, main.fileNameFb);
                        Log.d(TAG, "stopped recording");
                        SystemClock.sleep(500);
                        //restart recording
                        main.stopRercordingFlag = true;
                        try {
                            main.gyroWriter.write("youtube\n");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        main.gyroReader.start();
                        main.onRecord(true, main.fileNameYt);
                        //log rx and tx data

                        //TODO - finalize recording data
                        main.mStartRX = TrafficStats.getTotalRxBytes();
                        main.mStartTX = TrafficStats.getTotalTxBytes();

                        if (main.mStartRX == TrafficStats.UNSUPPORTED || main.mStartTX == TrafficStats.UNSUPPORTED) {
                            Log.d(TAG, "logging unsupported");
                        } else {
                            try {
                                main.trafficWriter.write("youtube\n");
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            main.mHandler.postDelayed(main.mRunnable, 1000);
                        }
                        //open youtube
                        String url = main.YT_URL;
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
        //final screen for submitting data
        else {
            rootView = (ViewGroup) inflater.inflate(
                    R.layout.fragment_screen_slide_submit, container, false);
            final Button fab = rootView.findViewById(R.id.submit_button);
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //stopRercordingFlag = true;
                    if (main.stopRercordingFlag) {
                        main.stopRercordingFlag = false;
                        main.onRecord(false, main.fileNameYt);
                        main.gyroReader.stop();

                        try {
                            main.trafficWriter.flush();
                            main.trafficWriter.close();
                            main.gyroWriter.flush();
                            main.gyroWriter.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }



                        //upload data
                        response1 = rootView.findViewById(R.id.response1);
                        String response1Text = response1.getText().toString();

                        main.userIDText = main.userID.getText().toString();
                        new UploadFileAsync().execute(Environment.getExternalStorageDirectory().getPath()+"/audiofb.3gp", "_audiofb.3gp");
                        new UploadFileAsync().execute(Environment.getExternalStorageDirectory().getPath()+"/audioyt.3gp", "_audioyt.3gp");
                        new UploadFileAsync().execute(Environment.getExternalStorageDirectory().getPath()+"/trafficData.csv", "_trafficData.csv");
                        new UploadFileAsync().execute(Environment.getExternalStorageDirectory().getPath()+"/gyroData.csv", "_gyroData.csv");
                        //new SendData().execute(main.serverURL, response1Text);
                        Snackbar.make(view, "Your contribution has been submitted. Thank you!", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    }
                    else{
                        Toast.makeText(view.getContext(), "Please follow the steps in the correct order", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        return rootView;
    }

    //upload files to server
    public class UploadFileAsync extends AsyncTask<String, Void, String> {

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
                        String upLoadServerUri = main.serverURL;

                        Log.d(TAG, "user id "+main.userIDText);
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
                                + main.userIDText+ params[1] + "\"" + lineEnd);

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
                        Log.d(TAG, "sent file " + serverResponseMessage);

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
/*
    //send additional user data
    public class SendData extends AsyncTask<String, String, String> {

        public SendData(){
            //set context variables if required
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }


        @Override
        protected String doInBackground(String... params) {
            String urlString = params[0]; // URL to call
            String data = params[1]; //data to post
            OutputStream out = null;

            try {
                URL url = new URL(urlString);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                out = new BufferedOutputStream(urlConnection.getOutputStream());

                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"));
                writer.write(data);
                writer.flush();
                writer.close();
                out.close();

                urlConnection.connect();
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }*/
}