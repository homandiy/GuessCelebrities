package com.example.homan.guesscelebrities;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    ArrayList<String> celebURLs = new ArrayList<String>();
    ArrayList<String> celebNames = new ArrayList<String>();

    ImageView imageView;
    TextView downloadView;
    Button button0;
    Button button1;
    Button button2;
    Button button3;
    int chosenCeleb = 0;
    int locationOfCorrectAnswer = 0;
    String[] answers = new String[4];

    public void celebChosen(View view) {

        Log.i("tms tag:", view.getTag().toString()+ " vs "+Integer.toString(locationOfCorrectAnswer));

        if (view.getTag().toString().equals(Integer.toString(locationOfCorrectAnswer))) {

            Toast.makeText(getApplicationContext(), "Correct!", Toast.LENGTH_LONG).show();

        } else {

            Toast.makeText(getApplicationContext(), "Wrong! It was " + celebNames.get(chosenCeleb), Toast.LENGTH_LONG).show();

        }

        createNewQuestion();

    }

    //access download task data
    public interface MyAccessResponse {
        void postResult(String asyncResult);
    }

    //download class
    public class DownloadTask extends AsyncTask<String, Void, String> {


        public MyAccessResponse delegate=null;
        public DownloadTask(MyAccessResponse asyncResponse) {
            delegate = asyncResponse;//Assigning call back interfacethrough constructor
        }


        @Override
        protected String doInBackground(String... urls) {

            URL url;
            HttpURLConnection urlConnection = null;

            try {
                //read 1st string
                url = new URL( urls[0] );
                urlConnection = (HttpURLConnection) url.openConnection();

                InputStream inpt = urlConnection.getInputStream();

                BufferedReader reader = new BufferedReader(new InputStreamReader(inpt));
                StringBuilder builder = new StringBuilder();
                String aux = "";

                while ((aux = reader.readLine()) != null) {
                    builder.append(aux);
                }
                return builder.toString();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }


        @Override
        protected void onPostExecute(String result) {

            Log.i("tms delegate", "access delegate");
            delegate.postResult(result);

            //super.onPostExecute(result);
            if(result!=null && delegate!=null)
            {

                delegate.postResult(result);
            } else {
                Log.i("MyAccess", "You have not assigned IApiAccessResponse delegate");
            }
        }
    } //end of Download Task

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = (ImageView) findViewById(R.id.imageView);

        button0 = (Button) findViewById(R.id.nameButton1);
        button1 = (Button) findViewById(R.id.nameButton2);
        button2 = (Button) findViewById(R.id.nameButton3);
        button3 = (Button) findViewById(R.id.nameButton4);

        button0.setText("Loading data");
        button1.setText("Loading data");
        button2.setText("Loading data");
        button3.setText("Loading data");


        DownloadTask task =  new DownloadTask(new MyAccessResponse() {

            @Override
            public void postResult(String asyncResult) {
                //html = asyncResult ;
                //Log.i("tms postResult", html);
                //process result--split point: "/imgs/hr_shadow.gif"
                String[] splitResult = asyncResult.split("/imgs/hr_shadow.gif");

                //example: <img src="/celebs/rihanna/thumbs/thumb1.jpg" alt="Rihanna" height="95" width="95" border="0" />
                Pattern p = Pattern.compile("src=\"(.*?)\"");
                Matcher m = p.matcher(splitResult[1]);
                while(m.find()) {
                    celebURLs.add( m.group(1));
                    Log.i("tms src:", m.group(1));
                }

                p = Pattern.compile("alt=\"(.*?)\"");
                m = p.matcher(splitResult[1]);
                while(m.find()) {
                    celebNames.add( m.group(1));
                    Log.i("tms name:", m.group(1));
                }

                createNewQuestion();
            }
        });

        //view-source:http://www.icelebz.com/topcelebs.html or
        //view-source:http://www.icelebz.com/topcelebs2.html
        task.execute("http://www.icelebz.com//topcelebs.html");
    }//end onCreate

    public void createNewQuestion() {

        Random random = new Random();
        chosenCeleb = random.nextInt(celebURLs.size());

        String webSite = "http://www.icelebz.com";

        Bitmap celebImage;

        try {

            //celebImage = imageTask.execute(webSite+celebURLs.get(chosenCeleb)).get();

            //imageView.setImageBitmap(celebImage);
            Picasso.with(MainActivity.this).load(webSite+celebURLs.get(chosenCeleb)).into(imageView);

            locationOfCorrectAnswer = random.nextInt(4);

            int incorrectAnswerLocation;

            for (int i=0; i<4; i++) {

                if (i == locationOfCorrectAnswer) {

                    answers[i] = celebNames.get(chosenCeleb);

                } else {

                    incorrectAnswerLocation = random.nextInt(celebURLs.size());

                    while (incorrectAnswerLocation == chosenCeleb) {

                        incorrectAnswerLocation = random.nextInt(celebURLs.size());

                    }

                    answers[i] = celebNames.get(incorrectAnswerLocation);


                }


            }
            Log.i("tms answer:", String.valueOf(locationOfCorrectAnswer));
            button0.setText(answers[0]);
            button1.setText(answers[1]);
            button2.setText(answers[2]);
            button3.setText(answers[3]);


        } catch (Exception e) {
            e.printStackTrace();
        }

    }//end create question
}

