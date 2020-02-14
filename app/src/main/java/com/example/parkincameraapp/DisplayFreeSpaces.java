package com.example.parkincameraapp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DisplayFreeSpaces extends AppCompatActivity {

    ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_free_spaces);
        Bundle b = getIntent().getExtras();
        int value = -1; // or other values
        String name = "";
        if(b != null) {
            value = b.getInt("key");
            name = b.getString("key2");
        }
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        final int finalValue = value;
        final Context context = this;
        scheduler.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                myTask myTask = new myTask(context);
                myTask.execute(finalValue);
            }
        }, 0, 1, TimeUnit.SECONDS);
    }
    @Override
    public void onBackPressed() {
        scheduler.shutdown();
        Intent myIntent = new Intent(this, MainActivity.class);
        startActivity(myIntent);
    }
    private class myTask extends AsyncTask<Integer, Void, Bitmap> {
        private WeakReference<Context> contextRef;
        //initiate vars
        public myTask(Context context) {
            contextRef = new WeakReference<>(context);
        }

        protected Bitmap doInBackground(Integer... id) {
            List<Methods> methods = new ArrayList<>();
            BufferedReader reader = null;
            try {
                URL githubEndpoint = new URL("http://192.168.100.8:9090/getImage/?id="+id[0]);
                HttpURLConnection myConnection =
                        (HttpURLConnection) githubEndpoint.openConnection();
                if (myConnection.getResponseCode() == 200) {
                    InputStream responseBody = myConnection.getInputStream();
                    Bitmap i = BitmapFactory.decodeStream(responseBody);

                    myConnection.disconnect();
                    return i;
                } else {
                    // Error handling code goes here
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            ImageView imageView = findViewById(R.id.imageview1);
            imageView.setImageBitmap(result);
        }


    }
}
