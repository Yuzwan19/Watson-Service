package com.izx.watsontts;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.ibm.watson.developer_cloud.android.library.audio.StreamPlayer;
import com.ibm.watson.developer_cloud.text_to_speech.v1.TextToSpeech;
import com.ibm.watson.developer_cloud.text_to_speech.v1.model.Voice;
import com.squareup.picasso.Picasso;

public class MainActivity extends AppCompatActivity {

    TextView textView, txt_how, txt_ing;
    EditText editText;
    ImageView imgDetail;
    Button button, btn_ing, btn_how, btn_ing_trans, btn_how_trans;
    String tts_ing, tts_how, image, judul, how, ing;

    StreamPlayer streamPlayer;


    private TextToSpeech initTextToSpeechService(){
        TextToSpeech service = new TextToSpeech();
        String username = "ff033206-f592-4fd5-b04d-60a55c3e8d05";
        String password = "rEqFBPt1xgDw";
//        String password = "BA3g0uKmRimk"; // Akun lama
//        String username = "c9c2ec6b-89e2-41fb-898c-76d24e42fb3a";
        service.setUsernameAndPassword(username, password);
        return service;
    }

    private class WatsonTask_ing extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            tts_ing = txt_ing.getText().toString();
        }

        @Override
        protected String doInBackground(String... textToSpeak) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    textView.setText("running the Watson thread");
                }
            });

            TextToSpeech textToSpeech = initTextToSpeechService();
            streamPlayer = new StreamPlayer();
            streamPlayer.playStream(textToSpeech.synthesize(String.valueOf(tts_ing), Voice.EN_LISA).execute());

            return "text to speech done";
        }

        @Override
        protected void onPostExecute(String result) {
            textView.setText("TTS status: " + result);
        }

    }

    private class WatsonTask_how extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            tts_how = txt_how.getText().toString();
        }

        @Override
        protected String doInBackground(String... textToSpeak) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    textView.setText("running the Watson thread");
                }
            });

            TextToSpeech textToSpeech = initTextToSpeechService();
            streamPlayer = new StreamPlayer();
            streamPlayer.playStream(textToSpeech.synthesize(String.valueOf(tts_how), Voice.EN_LISA).execute());

            return "text to speech done";
        }

        @Override
        protected void onPostExecute(String result) {
            textView.setText("TTS status: " + result);
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupView();
        getdata();

        editText = (EditText) findViewById(R.id.editText);
        button = (Button) findViewById(R.id.button);
        textView = (TextView) findViewById(R.id.textView);

        btn_ing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("the text to speech: " + txt_ing.getText());

                WatsonTask_ing task = new WatsonTask_ing();
                task.execute();

            }
        });

        btn_how.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("the text to speech: " + txt_how.getText());

                WatsonTask_how task = new WatsonTask_how();
                task.execute();

            }
        });

        btn_ing_trans.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendIng();
            }
        });

        btn_how_trans.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendHow();
            }
        });

    }

    private void setupView(){
        txt_how = (TextView)findViewById(R.id.txt_how);
        txt_ing = (TextView)findViewById(R.id.txt_ing);
        btn_how = (Button)findViewById(R.id.btn_how);
        btn_ing = (Button)findViewById(R.id.btn_ing);
        btn_ing_trans = (Button)findViewById(R.id.btn_ing_trans);
        btn_how_trans = (Button)findViewById(R.id.btn_how_trans);
        imgDetail = (ImageView)findViewById(R.id.imgDetail);
    }

    private void getdata(){
        Intent i = getIntent();
        judul = i.getStringExtra(Constant.JUDUL);
        ing = i.getStringExtra(Constant.INGREDIENTS);
        how = i.getStringExtra(Constant.HOW);
        image = i.getStringExtra(Constant.IMAGE);
        getSupportActionBar().setTitle(judul);
        txt_how.setText(how);
        txt_ing.setText(ing);
        Picasso.with(MainActivity.this).load(image)
                .placeholder(R.drawable.placeholder).into(imgDetail);
    }

    private void sendIng(){
        Intent i = new Intent(MainActivity.this, TranslateActivity.class);
        i.putExtra(Constant.JUDUL, judul);
        i.putExtra(Constant.INGREDIENTS, ing);
        startActivity(i);
    }

    private void sendHow(){
        Intent i = new Intent(MainActivity.this, TranslateActivity.class);
        i.putExtra(Constant.JUDUL, judul);
        i.putExtra(Constant.HOW, how);
        startActivity(i);
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_translate) {
//            sendData();
//            return true;
//        }
//        return super.onOptionsItemSelected(item);
//    }

}