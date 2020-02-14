package com.example.parkincameraapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.JsonReader;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.widget.Toolbar.LayoutParams;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final Context context = this;
        scheduler.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                myTask myTask = new myTask(context);
                myTask.execute();
            }
        }, 0, 5, TimeUnit.SECONDS);


    }
    @Override
    public void onBackPressed() {
        scheduler.shutdown();
        finishAffinity();
        finish();
        System.exit(0);
    }

    private class myTask extends AsyncTask<Void, Void, List<Cameras>> {
        private WeakReference<Context> contextRef;
        //initiate vars
        public myTask(Context context) {
            contextRef = new WeakReference<>(context);
        }

        @SuppressLint("WrongThread")
        protected List<Cameras> doInBackground(Void... params) {
            ProgressBar progressBar = findViewById(R.id.indeterminateBar);
            List<Cameras> cameras = new ArrayList<>();
            try {
                URL githubEndpoint = new URL("http://192.168.100.8:9090/getCameras");
                HttpURLConnection myConnection =
                        (HttpURLConnection) githubEndpoint.openConnection();
                if (myConnection.getResponseCode() == 200) {
                    InputStream responseBody = myConnection.getInputStream();
                    InputStreamReader responseBodyReader =
                            new InputStreamReader(responseBody, "UTF-8");
                    JsonReader jsonReader = new JsonReader(responseBodyReader);
                    jsonReader.beginArray(); // Start processing the JSON object
                    progressBar.setProgress(25);
                    while (jsonReader.hasNext())
                    {
                        Cameras camera = new Cameras();
                        jsonReader.beginObject();
                        while (jsonReader.hasNext()) {
                            String key = jsonReader.nextName(); // Fetch the next key
                            if (key.equals("name")) { // Check if desired key
                                // Fetch the value as a String
                                String value = jsonReader.nextString();
                                camera.setName(value);
                                //break; // Break out of the loop
                            }
                            else if(key.equals("id"))
                            {
                                String value = jsonReader.nextString();
                                camera.setId(value);
                            }
                            else {
                                jsonReader.skipValue(); // Skip values of other keys
                            }
                        }
                        jsonReader.endObject();
                        cameras.add(camera);
                    }
                    jsonReader.endArray();
                    jsonReader.close();
                    myConnection.disconnect();
                    progressBar.setProgress(75);
                    return cameras;
                } else {
                    progressBar.setProgress(0);
                    // Error handling code goes here
                }
            } catch (IOException e) {
                progressBar.setProgress(0);
                e.printStackTrace();
            }
            progressBar.setProgress(0);
            return cameras;
        }

        @Override
        protected void onPostExecute(List<Cameras> result) {
            ProgressBar progressBar = findViewById(R.id.indeterminateBar);
            int i = 1;
            String numbers = "";
            if (!result.isEmpty())
            {
                for (final Cameras camera : result) {
                    //////////////////
                    try {
                        URL githubEndpoint = new URL("http://192.168.100.8:9090/?id=" + i);
                        HttpURLConnection myConnection =
                                (HttpURLConnection) githubEndpoint.openConnection();
                        if (myConnection.getResponseCode() == 200) {
                            InputStream responseBody = myConnection.getInputStream();
                            InputStreamReader responseBodyReader =
                                    new InputStreamReader(responseBody, "UTF-8");
                            JsonReader jsonReader = new JsonReader(responseBodyReader);
                            jsonReader.beginObject();
                            while (jsonReader.hasNext()) {
                                String key = jsonReader.nextName(); // Fetch the next key
                                if (key.equals("average")) { // Check if desired key
                                    // Fetch the value as a String
                                    String value = jsonReader.nextString();
                                    numbers = "Wolne miejsca:" + value;
                                    //break; // Break out of the loop
                                } else if (key.equals("difference")) {
                                    String value = jsonReader.nextString();
                                    numbers = numbers + "+/-" + value;
                                } else {
                                    jsonReader.skipValue(); // Skip values of other keys
                                }
                            }
                            jsonReader.endObject();

                            jsonReader.close();
                            myConnection.disconnect();
                        } else {
                            // Error handling code goes here
                        }
                    } catch (IOException e) {
                        LinearLayout ll = findViewById(R.id.buttonlayout);
                        ll.removeAllViews();
                        e.printStackTrace();
                    }
                    /////////////////////
                    Button myButton = new Button(contextRef.get());
                    myButton.setText(camera.getName() + " " + numbers);
                    myButton.setTag(camera.getId());
                    myButton.setId(Integer.parseInt(camera.getId()));
                    myButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent myIntent = new Intent(contextRef.get(), DisplayFreeSpaces.class);
                            Bundle b = new Bundle();
                            b.putInt("key", Integer.parseInt(camera.getId()));
                            b.putString("key2", camera.getName());
                            scheduler.shutdown();
                            myIntent.putExtras(b);
                            startActivity(myIntent);
                        }
                    });
                    LinearLayout ll = findViewById(R.id.buttonlayout);
                    LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
                    if (findViewById(Integer.parseInt(camera.getId())) != null) {
                        ((Button) findViewById(Integer.parseInt(camera.getId()))).setText(camera.getName() + " " + numbers);
                    } else {
                        ll.addView(myButton, lp);
                    }
                    progressBar.setProgress(100);
                    i++;
                }
        }
            else {
                progressBar.setProgress(0);
                LinearLayout ll = findViewById(R.id.buttonlayout);
                ll.removeAllViews();
            }
        }


}
}
