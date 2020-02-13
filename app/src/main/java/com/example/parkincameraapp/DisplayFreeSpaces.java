package com.example.parkincameraapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.ImageView;

import java.io.BufferedReader;
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
import java.util.logging.Handler;

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
        //Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //toolbar.setTitle(name);
//        ImageView imageView = (ImageView) findViewById(R.id.imageview1);
//        imageView.setImageBitmap(i);
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
                URL githubEndpoint = new URL("http://192.168.101.206:9090/getImage/?id="+id[0]);
                HttpURLConnection myConnection =
                        (HttpURLConnection) githubEndpoint.openConnection();
                if (myConnection.getResponseCode() == 200) {
                    InputStream responseBody = myConnection.getInputStream();
                    Bitmap i = BitmapFactory.decodeStream(responseBody);
//                    InputStreamReader responseBodyReader =
//                            new InputStreamReader(responseBody, "UTF-8");
//                    JsonReader jsonReader = new JsonReader(responseBodyReader);
//                    System.out.println(responseBody);
//                    jsonReader.beginArray(); // Start processing the JSON object
//                    while (jsonReader.hasNext())
//                    {
//                        Methods method = new Methods();
//                        jsonReader.beginObject();
//                        while (jsonReader.hasNext()) {
//                            String key = jsonReader.nextName(); // Fetch the next key
//                            System.out.println(key);
//                            if (key.equals("name")) { // Check if desired key
//                                // Fetch the value as a String
//                                String value = jsonReader.nextString();
//                                //method.setName(value);
//                                //break; // Break out of the loop
//                            }
//                            else if(key.equals("number"))
//                            {
//                                String value = jsonReader.nextString();
//                                method.setNumber(Integer.valueOf(value));
//                            }
//                            else {
//                                jsonReader.skipValue(); // Skip values of other keys
//                            }
//                        }
//                        jsonReader.endObject();
//                        //methods.add(method);
//                    }
//                    jsonReader.endArray();
//                    jsonReader.close();
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
            ImageView imageView = (ImageView) findViewById(R.id.imageview1);
            imageView.setImageBitmap(result);
//            for (Methods method:result) {
//                TextView mTextView = (TextView) findViewById(R.id.textview1);
//                mTextView.setText(mTextView.getText()+method.getName()+":"+method.getNumber()+ "\n");
////                Button myButton = new Button(contextRef.get());
////                myButton.setText(camera.getName());
////                myButton.setTag(camera.getId());
////                LinearLayout ll = (LinearLayout) findViewById(R.id.buttonlayout);
////                Toolbar.LayoutParams lp = new Toolbar.LayoutParams(Toolbar.LayoutParams.MATCH_PARENT, Toolbar.LayoutParams.WRAP_CONTENT);
////                ll.addView(myButton, lp);
//            }
        }


    }
}
